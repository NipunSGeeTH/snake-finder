package com.snakeid.domain

object SnakeSpeciesDatabase {

    val speciesList: List<SnakeSpecies> = listOf(
        SnakeSpecies(
            id = "naja_naja",
            scientificName = "Naja naja",
            commonName = "Indian Spectacled Cobra",
            localNames = "Naya (Sinhala), Nalla Pambu (Tamil), Nag (Hindi)",
            venomStatus = VenomStatus.DEADLY,
            dangerLevel = 5,
            identifyingFeatures = listOf(
                "Distinctive spectacle (monocle or binocle) mark on dorsal hood",
                "Spreads a broad hood when threatened or alarmed",
                "Rounded snout with large nostrils",
                "Color ranges from yellow-brown to pitch black with light bands",
                "Round pupils with dark eyes"
            ),
            habitat = "Farmlands, paddy fields, open forest, near human dwellings looking for rodents.",
            behavior = "Raises front third of body, hisses loudly, and flattens hood when disturbed. Diurnal and crepuscular.",
            biteSymptoms = "Severe neurotoxicity: Drooping eyelids (ptosis), difficulty swallowing, slurred speech, respiratory paralysis within 30-120 minutes. Local swelling and necrosis.",
            firstAidAdvice = "URGENT MEDICAL EMERGENCY. Keep patient completely still. Immobilize bitten limb with a splint. Rush to hospital with Polyvalent Antivenom and ventilatory support.",
            lookAlikes = "Often confused with harmless Oriental Rat Snake (Ptyas mucosa), but rat snakes have larger eyes, black lip lines, and cannot spread a hood.",
        ),
        SnakeSpecies(
            id = "daboia_russelii",
            scientificName = "Daboia russelii",
            commonName = "Russell's Viper",
            localNames = "Thith Polonga (Sinhala), Kannadi Viriyan (Tamil), Daboia (Hindi)",
            venomStatus = VenomStatus.DEADLY,
            dangerLevel = 5,
            identifyingFeatures = listOf(
                "Triangular, flattened head distinct from slender neck",
                "Three longitudinal series of dark brown oval spots with black and white borders",
                "Heavily keeled, rough scales giving a dull appearance",
                "Vertical, slit-like pupils (cat eyes)",
                "Loud, piercing whistle/hiss like a pressure cooker when agitated"
            ),
            habitat = "Paddy fields, scrubland, grasslands, plantations, dry zones.",
            behavior = "Coils tightly and produces an extremely loud warning hiss before striking with explosive speed.",
            biteSymptoms = "Potent hemotoxin and myotoxin: Rapid severe local swelling, spontaneous bleeding from gums, nose, and bite punctures, acute kidney failure, blood clotting failure (DIC).",
            firstAidAdvice = "CRITICAL EMERGENCY. Restrict movement entirely. DO NOT apply tight tourniquet. Immediate transport to hospital equipped for dialysis and antivenom administration.",
            lookAlikes = "Often confused with juvenile Indian Rock Python (Python molurus) or Common Sand Boa, which are harmless.",
        ),
        SnakeSpecies(
            id = "bungarus_caeruleus",
            scientificName = "Bungarus caeruleus",
            commonName = "Common Krait",
            localNames = "Thel Karawala (Sinhala), Kattu Viriyan (Tamil), Karait (Hindi)",
            venomStatus = VenomStatus.DEADLY,
            dangerLevel = 5,
            identifyingFeatures = listOf(
                "Glossy blue-black or dark brownish-black scales",
                "Narrow white crossbars along the body, often paired",
                "Enlarged hexagonal vertebral scales running down the spine",
                "Small, indistinct head with small dark eyes",
                "Subcaudal scales undivided"
            ),
            habitat = "Termite mounds, rodent burrows, agricultural land, inside rural homes during night.",
            behavior = "Strictly nocturnal. Bites sleeping humans on floor mats, often painless with barely visible puncture marks.",
            biteSymptoms = "EXTREME NEUROTOXICITY: 'Silent killer'. Little to no local pain or swelling. Victim wakes with abdominal cramps, morning paralysis, ptosis, and respiratory arrest.",
            firstAidAdvice = "LIFE-THREATENING. Immediate hospital admission even if patient feels fine. Neostigmine and Polyvalent Antivenom needed with artificial ventilation.",
            lookAlikes = "Common Wolf Snake (Lycodon aulicus) closely mimics the Krait with white bands, but wolf snakes lack the enlarged vertebral scales and have distinct yellow collars.",
        ),
        SnakeSpecies(
            id = "echis_carinatus",
            scientificName = "Echis carinatus",
            commonName = "Saw-scaled Viper",
            localNames = "Weli Polonga (Sinhala), Suruttai Pambu (Tamil), Phoorsa (Hindi)",
            venomStatus = VenomStatus.DEADLY,
            dangerLevel = 5,
            identifyingFeatures = listOf(
                "Small size (under 60cm) with heavily keeled, serrated lateral scales",
                "Distinct white cross or bird-foot marking on top of head",
                "Wavy pale undulating line along flanks with dark patches",
                "Rubs serrated side scales together to make a loud sizzling rasping sound",
                "Vertical slit pupils"
            ),
            habitat = "Arid dry zones, sandy areas, rocky scrubland, under stones and thorny bushes.",
            behavior = "Extremely aggressive when cornered, strikes repeatedly in figure-8 loops.",
            biteSymptoms = "Powerful hemotoxin: Incoagulable blood, internal hemorrhaging, brain hemorrhage, severe pain and local necrosis.",
            firstAidAdvice = "HIGH EMERGENCY. Keep patient calm, immobilize bitten limb. Administer antivenom as soon as clotting test shows non-clotting blood.",
            lookAlikes = "May resemble harmless Carpet Vipers or young Sand Boas, but the head cross and scale rasping are diagnostic.",
        ),
        SnakeSpecies(
            id = "hypnale_hypnale",
            scientificName = "Hypnale hypnale",
            commonName = "Hump-nosed Pit Viper",
            localNames = "Kunakatuwa / Merem Polonga (Sinhala), Mukkan Viriyan (Tamil)",
            venomStatus = VenomStatus.VENOMOUS,
            dangerLevel = 4,
            identifyingFeatures = listOf(
                "Distinct upturned hump or wart-like snout at tip of nose",
                "Broad triangular head with heat-sensing pit between eye and nostril",
                "Greyish-brown body with paired dark triangular or trapezoidal blotches",
                "Short, stocky body with short tail",
                "Vertical pupils"
            ),
            habitat = "Rubber and tea plantations, leaf litter in wet and intermediate zones.",
            behavior = "Sluggish, relies on camouflage among dead leaves, strikes when stepped on.",
            biteSymptoms = "Responsible for the highest number of venomous snakebites in Sri Lanka & SW India. Local swelling, blistering, tissue necrosis, acute kidney injury (AKI), coagulopathy.",
            firstAidAdvice = "URGENT HOSPITAL ADMISSION. Standard polyvalent antivenom has limited efficacy; renal monitoring, kidney function tests, and supportive intensive care are essential.",
            lookAlikes = "Often mistaken for harmless vipers or small Russell's Viper juveniles.",
        ),
        SnakeSpecies(
            id = "trimeresurus_trigonocephalus",
            scientificName = "Trimeresurus trigonocephalus",
            commonName = "Green Pit Viper",
            localNames = "Pala Polonga (Sinhala), Pachai Viriyan (Tamil)",
            venomStatus = VenomStatus.VENOMOUS,
            dangerLevel = 4,
            identifyingFeatures = listOf(
                "Vibrant bright green body with black markings and yellow venter",
                "Broad heart-shaped or triangular head",
                "Deep heat-sensing loreal pit between eye and nostril",
                "Prehensile black-tipped tail adapted for climbing branches",
                "Vertical yellow or golden pupils"
            ),
            habitat = "Rainforests, coffee and tea bushes, rainforest canopy, low trees and shrubs.",
            behavior = "Arboreal, nocturnal ambush predator, remains motionless on branches.",
            biteSymptoms = "Severe localized pain, massive swelling, blistering, local tissue necrosis, mild coagulopathy. Rarely fatal if managed.",
            firstAidAdvice = "Hospital care required. Analgesia, wound care, monitor for systemic envenomation. Avoid incision.",
            lookAlikes = "Harmless Green Vine Snake (Ahaetulla nasuta), but vine snakes have very slender elongated heads and horizontal keyhole pupils.",
        ),
        SnakeSpecies(
            id = "ptyas_mucosa",
            scientificName = "Ptyas mucosa",
            commonName = "Oriental Rat Snake",
            localNames = "Garandiya (Sinhala), Sara Pambu (Tamil), Dhaman (Hindi)",
            venomStatus = VenomStatus.NON_VENOMOUS,
            dangerLevel = 1,
            identifyingFeatures = listOf(
                "Very long (up to 2-3 meters), slender and streamlined body",
                "Prominent large eyes with round pupils",
                "Black vertical bars/lines on yellow-white lips (labial scales)",
                "Olive-green, yellowish-brown to black dorsal color with dark cross-mesh towards tail",
                "Cannot spread a true hood (may puff neck vertically)"
            ),
            habitat = "Fields, gardens, roofs, agricultural land, human settlements eating rats.",
            behavior = "Incredibly fast and agile diurnal snake. Flees rapidly when approached, emits growl if trapped.",
            biteSymptoms = "NON-VENOMOUS. Bites can cause lacerations and mechanical puncture wounds. Wash thoroughly with soap and water.",
            firstAidAdvice = "Wash bite with antiseptic soap and water. Tetanus prophylaxis recommended. No antivenom needed.",
            lookAlikes = "Frequently killed due to confusion with the venomous Indian Cobra. Check lip lines (rat snakes have distinct black bars) and round large eyes.",
        ),
        SnakeSpecies(
            id = "python_molurus",
            scientificName = "Python molurus",
            commonName = "Indian Rock Python",
            localNames = "Pimburā (Sinhala), Malai Pambu (Tamil), Ajgar (Hindi)",
            venomStatus = VenomStatus.NON_VENOMOUS,
            dangerLevel = 2,
            identifyingFeatures = listOf(
                "Massive, thick-bodied snake reaching 3-6 meters in length",
                "Whitish or yellowish base color with irregular dark brown, tan-centered blotches",
                "Lance-shaped pinkish/brown arrowhead marking on head",
                "Vertical pupils with heat-sensing labial pits along lips",
                "Smooth, glossy scales"
            ),
            habitat = "Dense forests, rocky hills, wetlands, swamps, river valleys.",
            behavior = "Slow-moving, docile unless provoked. Constrictor that subdues prey by suffocation.",
            biteSymptoms = "NON-VENOMOUS. Large individuals have sharp recurved teeth that can cause deep puncture wounds and lacerations.",
            firstAidAdvice = "Clean and disinfect wound. Tetanus booster. Seek medical attention for deep tears.",
            lookAlikes = "Superficially confused with Russell's Viper, but python blotches are irregular and smooth-scaled, while viper spots are oval chains with rough keeled scales.",
        ),
        SnakeSpecies(
            id = "ahaetulla_nasuta",
            scientificName = "Ahaetulla nasuta",
            commonName = "Green Vine Snake / Long-nosed Whip Snake",
            localNames = "Ahasa Polonga / Ehella (Sinhala), Pachai Pambu (Tamil)",
            venomStatus = VenomStatus.MILDLY_VENOMOUS,
            dangerLevel = 2,
            identifyingFeatures = listOf(
                "Extremely slender, elongated whip-like body",
                "Bright fluorescent green color with yellow-white lateral stripe",
                "Elongated pointed snout with a dermal appendage at the tip",
                "Unique horizontal, keyhole-shaped pupils giving binocular vision",
                "Inter-scale skin shows black and white checkerboard pattern when puffed"
            ),
            habitat = "Bushes, shrubs, gardens, fruit trees, forest edges.",
            behavior = "Diurnal, sways head like a leaf in the breeze, opens mouth displaying dark lining when alarmed.",
            biteSymptoms = "Mild rear-fanged venom. Causes local swelling, itching, and mild throbbing pain lasting 24-48 hours. Not fatal.",
            firstAidAdvice = "Wash with soap and water. Do not cut the bite or apply ice. See a doctor if pain or swelling spreads. The story that it pecks eyes is a myth.",
            lookAlikes = "Distinct from Green Pit Viper by its ultra-slender body, pointed snout, and horizontal keyhole pupils.",
        ),
        SnakeSpecies(
            id = "lycodon_aulicus",
            scientificName = "Lycodon aulicus",
            commonName = "Common Wolf Snake",
            localNames = "Alu Radanakaya (Sinhala), Suvar Pambu (Tamil)",
            venomStatus = VenomStatus.NON_VENOMOUS,
            dangerLevel = 1,
            identifyingFeatures = listOf(
                "Small, slender snake (up to 50cm)",
                "Brownish or greyish body with 10-20 yellowish-white crossbars that expand on the flanks",
                "Distinct yellow or cream band/collar around the neck",
                "Flattened snout with enlarged front fang-like teeth (harmless to humans)",
                "Smooth, glossy scales with normal (non-enlarged) vertebral scales"
            ),
            habitat = "Houses, brick piles, cupboards, walls, rafters hunting geckos.",
            behavior = "Nocturnal house visitor, nervous and bites readily if handled.",
            biteSymptoms = "NON-VENOMOUS. Minor scratch or pinprick puncture. Harmless.",
            firstAidAdvice = "Wash with soap and water. Antiseptic cream.",
            lookAlikes = "DANGEROUS LOOK-ALIKE: Closely mimics the deadly Common Krait (Bungarus caeruleus). Check the spine: Krait has hexagonal enlarged scales on the midline; Wolf Snake does not. If in doubt, assume venomous!",
        ),
        SnakeSpecies(
            id = "boiga_trigonata",
            scientificName = "Boiga trigonata",
            commonName = "Common Cat Snake / Indian Gamma Snake",
            localNames = "Ran Dithpolonga (Sinhala), Poonai Pambu (Tamil)",
            venomStatus = VenomStatus.MILDLY_VENOMOUS,
            dangerLevel = 2,
            identifyingFeatures = listOf(
                "Broad, heart-shaped head distinctly wider than slender neck",
                "Large protruding eyes with vertical cat-like slit pupils",
                "Light brown or greyish body with dark Y-shaped or zigzag dorsal markings",
                "Yellowish underside with small dark dots",
                "Coils into tight S-loops and strikes aggressively"
            ),
            habitat = "Trees, bushes, thatched roofs, scrubland.",
            behavior = "Nocturnal. Mimics saw-scaled viper posture and strikes vigorously when threatened.",
            biteSymptoms = "Mild rear-fanged venom: local pain, minor swelling. Not dangerous to humans.",
            firstAidAdvice = "Clean the wound, reassure patient. No antivenom needed.",
            lookAlikes = "Mimics the deadly Saw-scaled Viper in defensive posture and head shape, but is much more slender and arboreal.",
        ),
        SnakeSpecies(
            id = "ophiophagus_hannah",
            scientificName = "Ophiophagus hannah",
            commonName = "King Cobra",
            localNames = "Raja Naya / Krishna Nag (Hindi)",
            venomStatus = VenomStatus.DEADLY,
            dangerLevel = 5,
            identifyingFeatures = listOf(
                "World's longest venomous snake (can reach 4-5.5 meters)",
                "Narrower, longer hood than Indian Cobra with chevron markings",
                "Pair of large occipital scales behind parietal shields on head",
                "Olive-green, tan, or black with pale chevron bands across body",
                "Deep, resonant growl-like hiss"
            ),
            habitat = "Primary rainforests, bamboo thickets, mangrove swamps, tea plantations.",
            behavior = "Can raise front third of body to human eye level. Diet consists almost entirely of other snakes.",
            biteSymptoms = "Very large venom yield. Rapid neurotoxicity, flaccid paralysis, and breathing failure can follow a bite.",
            firstAidAdvice = "Critical emergency. Keep the person still, immobilize the bitten limb, and get to a hospital immediately. Clinicians decide whether antivenom and breathing support are needed.",
            lookAlikes = "Indian Cobra (smaller, wider hood, spectacle mark) and Rat Snake (lacks occipital scales, non-venomous).",
        ),
        SnakeSpecies(
            id = "dendroaspis_polylepis",
            scientificName = "Dendroaspis polylepis",
            commonName = "Black Mamba",
            localNames = "Black Mamba",
            venomStatus = VenomStatus.DEADLY,
            dangerLevel = 5,
            identifyingFeatures = listOf(
                "Long, slender body reaching 2.5 to 4 meters",
                "Color is actually grey, olive, or dark brownish (not black)",
                "Inky pitch-black interior mouth lining shown in defensive display",
                "Coffin-shaped head with prominent brow ridges",
                "One of the fastest snakes on Earth (up to 20 km/h)"
            ),
            habitat = "Savannas, rocky hills, open woodland.",
            behavior = "Extremely nervous, raises up, spreads narrow neck flap, and opens ink-black mouth before striking rapidly.",
            biteSymptoms = "Highly potent neurotoxic venom. Collapse, heart rhythm problems, and breathing failure can develop quickly.",
            firstAidAdvice = "Emergency. Keep the person still and reach a hospital that can support breathing and give antivenom. Do not cut the bite or apply a tight tourniquet.",
            lookAlikes = "Green Mambas and harmless racers.",
        ),
        SnakeSpecies(
            id = "crotalus_adamanteus",
            scientificName = "Crotalus adamanteus",
            commonName = "Eastern Diamondback Rattlesnake",
            localNames = "Diamondback",
            venomStatus = VenomStatus.DEADLY,
            dangerLevel = 5,
            identifyingFeatures = listOf(
                "Heavy-bodied pit viper reaching 1.8 to 2.4 meters",
                "Prominent rattle on tail made of keratin segments",
                "Dorsal pattern of dark diamonds with brown centers and yellowish borders",
                "Diagonal dark stripe bordered by white running across each eye",
                "Large heat-sensing loreal pits"
            ),
            habitat = "Pine flatwoods, sandhills, coastal scrub.",
            behavior = "Coils and buzzes rattle loudly as warning; strikes up to two-thirds body length.",
            biteSymptoms = "Severe hemotoxin: Intolerable pain, massive swelling, tissue destruction, muscle fasciculations, bleeding.",
            firstAidAdvice = "Urgent. Keep the person still and the limb quiet. Do not cut, suck, or tightly tourniquet the bite. Antivenom is a hospital treatment.",
            lookAlikes = "Timber Rattlesnake, harmless Gopher Snake.",
        ),
        SnakeSpecies(
            id = "fowlea_piscator",
            scientificName = "Fowlea piscator",
            commonName = "Checkered Keelback",
            localNames = "Diya Polonga / Diya Bariya (Sinhala), Thanneer Pambu (Tamil)",
            venomStatus = VenomStatus.NON_VENOMOUS,
            dangerLevel = 1,
            identifyingFeatures = listOf(
                "Yellowish-brown or olive body with black checkered pattern like a chessboard",
                "Two distinct black streaks running backwards from each eye",
                "Keels on scales giving a rough texture",
                "Round pupils, eyes positioned somewhat dorsally for swimming",
                "Fierce defensive posture, flattens body"
            ),
            habitat = "Ponds, rivers, paddy fields, irrigation canals hunting fish and frogs.",
            behavior = "Semi-aquatic, fast swimmer, bites tenaciously when handled.",
            biteSymptoms = "NON-VENOMOUS. Superficial scratches. Clean thoroughly.",
            firstAidAdvice = "Soap and water wash. Tetanus shot.",
            lookAlikes = "Often misidentified as venomous vipers due to rough keeled scales.",
        ),
        SnakeSpecies(
            id = "naja_kaouthia",
            scientificName = "Naja kaouthia",
            commonName = "Monocled Cobra",
            localNames = "Padma Gokhra (Bengali), Monocled Cobra",
            venomStatus = VenomStatus.DEADLY,
            dangerLevel = 5,
            identifyingFeatures = listOf(
                "Distinctive single O-shaped monocle circular mark on dorsal hood",
                "Spreads high, broad hood when threatened",
                "Color ranges from yellow, brown, grey, to pitch black",
                "Round pupils with dark eyes"
            ),
            habitat = "Wetlands, agricultural areas, paddy fields, tropical forests across eastern South Asia and Southeast Asia.",
            behavior = "Very aggressive when cornered, raises hood, hisses and strikes forward rapidly.",
            biteSymptoms = "FATAL NEUROTOXIN AND CYTOTOXIN: Respiratory arrest, extensive tissue necrosis, ptosis, limb edema.",
            firstAidAdvice = "CRITICAL EMERGENCY. Complete immobilization of bitten limb. Rush immediately to emergency hospital with polyvalent antivenom.",
            lookAlikes = "Spectacled cobra has a spectacle mark rather than one circle. Oriental rat snakes are often killed because a raised neck looks like a hood; rat snakes have large eyes and black bars on the lips, and they cannot spread a true hood.",
        ),
        SnakeSpecies(
            id = "argyrogena_fasciolata",
            scientificName = "Argyrogena fasciolata",
            commonName = "Banded Racer",
            localNames = "Banded Racer",
            venomStatus = VenomStatus.NON_VENOMOUS,
            dangerLevel = 1,
            identifyingFeatures = listOf(
                "Slender, streamlined body built for extreme speed",
                "Greyish-brown with narrow dark crossbands on anterior body",
                "Large eyes with round pupils",
                "Smooth scales with glossy sheen"
            ),
            habitat = "Open dry plains, stony scrubland, agricultural hedges, gardens.",
            behavior = "Fast and agile hunter of lizards and rodents. Escapes rapidly into crevices.",
            biteSymptoms = "NON-VENOMOUS: Harmless minor scratch. No envenomation.",
            firstAidAdvice = "Wash bite with antiseptic soap and water. Update tetanus vaccine if needed.",
            lookAlikes = "Often mistaken for a juvenile krait because of the bands. Kraits have enlarged hexagonal scales along the spine and are nocturnal. The glossy-bellied racer (Platyceps ventromaculatus) is a different harmless snake.",
        ),
        SnakeSpecies(
            id = "eryx_conicus",
            scientificName = "Eryx conicus",
            commonName = "Common Sand Boa",
            localNames = "Rough-scaled Sand Boa, Kotuwa (Sinhala), Manal Pambu (Tamil)",
            venomStatus = VenomStatus.NON_VENOMOUS,
            dangerLevel = 1,
            identifyingFeatures = listOf(
                "Heavily built, thick cylindrical body with short, blunt tail",
                "Keeled scales giving a rough, pinecone-like texture",
                "Irregular dark brown blotches on yellow/tan background",
                "Small eyes with vertical slit pupils positioned atop head"
            ),
            habitat = "Sandy soils, loose dry earth, agricultural fields, semi-desert.",
            behavior = "Docile burrower. Coils into tight ball when disturbed. Constricts mice and lizards.",
            biteSymptoms = "NON-VENOMOUS: Completely harmless bite. No venom apparatus.",
            firstAidAdvice = "Wash with soap and water.",
            lookAlikes = "Frequently killed because its blotched pattern and rough scales mimic Russell's Viper.",
        ),
        SnakeSpecies(
            id = "coelognathus_helena",
            scientificName = "Coelognathus helena",
            commonName = "Common Trinket",
            localNames = "Trinket Snake, Kalu Bariya (Sinhala), Kattu Pambu (Tamil)",
            venomStatus = VenomStatus.NON_VENOMOUS,
            dangerLevel = 1,
            identifyingFeatures = listOf(
                "Tan or olive brown body with black and white crossbars",
                "Two distinct black longitudinal stripes on posterior body and tail",
                "Black eye-streak running diagonally downwards from eye",
                "Large round eyes with bold pupils"
            ),
            habitat = "Deciduous forests, scrubland, termite mounds, human residential gardens.",
            behavior = "Defensive display includes inflating neck to show black-and-white interstitial skin, coiling into an S-shape.",
            biteSymptoms = "NON-VENOMOUS: Non-toxic bite, purely superficial teeth marks.",
            firstAidAdvice = "Wash with antiseptic soap and water.",
            lookAlikes = "Often mistaken for venomous snakes when it assumes an aggressive S-shaped striking posture.",
        )
    )

    fun findSpecies(query: String): SnakeSpecies? {
        val normalized = ModelLabels.normalize(query)
        val dehyphenated = normalized.replace("-", " ")
        val id = ModelLabels.speciesIdFor(query)
        if (id != null) {
            speciesList.find { it.id == id }?.let { return it }
        }
        return speciesList.find { species ->
            species.id.equals(query.trim(), ignoreCase = true) ||
                species.id == normalized.replace(' ', '_') ||
                ModelLabels.normalize(species.scientificName) == normalized ||
                ModelLabels.normalize(species.scientificName) == dehyphenated ||
                ModelLabels.normalize(species.commonName) == normalized ||
                ModelLabels.normalize(species.commonName) == dehyphenated
        }
    }

    fun filterByStatus(status: VenomStatus?): List<SnakeSpecies> {
        if (status == null) return speciesList
        return speciesList.filter { it.venomStatus == status }
    }

    fun search(term: String): List<SnakeSpecies> {
        if (term.isBlank()) return speciesList
        val t = term.trim().lowercase()
        return speciesList.filter { s ->
            s.commonName.lowercase().contains(t) ||
            s.scientificName.lowercase().contains(t) ||
            s.localNames.lowercase().contains(t) ||
            s.habitat.lowercase().contains(t) ||
            s.identifyingFeatures.any { it.lowercase().contains(t) }
        }
    }
}
