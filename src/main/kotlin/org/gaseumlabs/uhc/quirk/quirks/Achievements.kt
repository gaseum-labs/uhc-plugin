package org.gaseumlabs.uhc.quirk.quirks

import org.gaseumlabs.uhc.core.Game
import org.gaseumlabs.uhc.quirk.Quirk
import org.gaseumlabs.uhc.quirk.QuirkType

class Achievements(type: QuirkType, game: Game) : Quirk(type, game) {
    companion object {
        val achievementMap = mapOf(
            "husbandry/plant_seed" to 2,

            "story/follow_ender_eye" to 4,
            "nether/return_to_sender" to 4,
            "nether/explore_nether" to 4,
            "adventure/sleep_in_bed" to 4,
            "husbandry/breed_an_animal" to 4,
            "husbandry/fishy_business" to 4,
            "husbandry/tactical_fishing" to 4,
            "husbandry/tame_an_animal" to 4,
            "adventure/whos_the_pillager_now" to 4,
            "nether/obtain_crying_obsidian" to 4,
            "nether/obtain_ancient_debris" to 4,

            "adventure/voluntary_exile" to 10,
            "adventure/spyglass_at_parrot" to 10,
            "adventure/honey_block_slide" to 10,
            "adventure/spyglass_at_ghast" to 10,
            "adventure/throw_trident" to 10,
            "adventure/summon_iron_golem" to 10,
            "adventure/bullseye" to 10,
            "husbandry/ride_a_boat_with_a_goat" to 10,
            "husbandry/make_a_sign_glow" to 10,
            "husbandry/axolotl_in_a_bucket" to 10,
            "adventure/sniper_duel" to 10,
            "husbandry/wax_on" to 10,
            "husbandry/obtain_netherite_hoe" to 10,

            "nether/ride_strider" to 20,
            "nether/uneasy_alliance" to 20,
            "adventure/arbalistic" to 20,
        )
    }
}