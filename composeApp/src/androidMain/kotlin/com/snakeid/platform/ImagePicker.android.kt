package com.snakeid.platform

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File

@Composable
actual fun rememberImagePicker(onImagePicked: (ByteArray) -> Unit): ImagePickerLauncher {
    val context = LocalContext.current
    val onPicked by rememberUpdatedState(onImagePicked)
    var photoFile by remember { mutableStateOf<File?>(null) }

    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val file = photoFile
        if (!success || file == null || !file.exists() || file.length() == 0L) return@rememberLauncherForActivityResult
        try {
            val bytes = file.readBytes()
            val bitmap = decodeOrientedBitmap(bytes, maxSide = 1600)
            if (bitmap != null) {
                onPicked(bitmap.toJpeg())
                bitmap.recycle()
            } else {
                onPicked(bytes)
            }
        } catch (error: Exception) {
            Log.e(TAG, "Could not read the camera photo", error)
            Toast.makeText(context, "Could not read the camera photo", Toast.LENGTH_SHORT).show()
        }
    }

    val requestCamera = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            launchCameraCapture(context, takePicture) { photoFile = it }
        } else {
            Toast.makeText(context, "Camera permission is required to photograph a snake", Toast.LENGTH_LONG).show()
        }
    }

    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        try {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes == null || bytes.isEmpty()) {
                Toast.makeText(context, "That image could not be opened", Toast.LENGTH_SHORT).show()
                return@rememberLauncherForActivityResult
            }
            val bitmap = decodeOrientedBitmap(bytes, maxSide = 1600)
            if (bitmap != null) {
                onPicked(bitmap.toJpeg())
                bitmap.recycle()
            } else {
                onPicked(bytes)
            }
        } catch (error: Exception) {
            Log.e(TAG, "Could not read the gallery image", error)
            Toast.makeText(context, "Could not read that image", Toast.LENGTH_SHORT).show()
        }
    }

    return remember(takePicture, requestCamera, gallery) {
        object : ImagePickerLauncher {
            override fun launchCamera() {
                val granted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
                if (granted) {
                    launchCameraCapture(context, takePicture) { photoFile = it }
                } else {
                    requestCamera.launch(Manifest.permission.CAMERA)
                }
            }

            override fun launchGallery() {
                gallery.launch("image/*")
            }
        }
    }
}

private fun launchCameraCapture(
    context: android.content.Context,
    takePicture: androidx.activity.result.ActivityResultLauncher<android.net.Uri>,
    storeFile: (File) -> Unit
) {
    try {
        val file = File(context.cacheDir, "snake_${System.currentTimeMillis()}.jpg")
        file.parentFile?.mkdirs()
        if (!file.exists()) file.createNewFile()
        storeFile(file)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        takePicture.launch(uri)
    } catch (error: Exception) {
        Log.e(TAG, "Could not open the camera", error)
        Toast.makeText(context, "Could not open the camera", Toast.LENGTH_SHORT).show()
    }
}

private fun Bitmap.toJpeg(): ByteArray {
    val stream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, 92, stream)
    return stream.toByteArray()
}

private const val TAG = "SnakeID"
