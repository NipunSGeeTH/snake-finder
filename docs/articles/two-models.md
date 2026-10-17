# The two models

Every photo goes through a snake-or-not check first. That model is an ImageNet MobileNet. It knows everyday objects and a set of snake classes. It does not name a species.

## Quick South Asia

This model is inside the app. It has 14 classes and needs no download. It is the better choice for the few species it was trained on, including several of the medically important snakes of South Asia. It still confuses look-alikes.

## Wide catalog

This is a separate download, about 171 MB, with 135 species. It is the SnakeSense network. The author reported about 66 percent training accuracy. It does not include the common krait. After the snake check passes, this model can suggest a name the small model does not have.

Use the wide catalog when you want a longer list of possible species. Use the quick model when you are offline or when the animal looks like one of the 14 built-in classes.
