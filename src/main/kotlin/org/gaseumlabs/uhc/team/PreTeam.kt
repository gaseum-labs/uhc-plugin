package org.gaseumlabs.uhc.team

import org.bukkit.*
import org.bukkit.DyeColor.CYAN
import org.bukkit.Material.*
import org.bukkit.block.Banner
import org.bukkit.block.banner.Pattern
import org.bukkit.block.banner.PatternType.*
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BannerMeta
import org.bukkit.inventory.meta.BlockStateMeta
import org.gaseumlabs.uhc.event.TeamShield
import java.util.*

class PreTeam(
	color0: DyeColor,
	color1: DyeColor,
	members: ArrayList<UUID>,
) : AbstractTeam(
	arrayOf(color0, color1),
	members
) {
	var name: String? = null
	var bannerPattern: Banner? = null

	fun toTeam(): Team {
		var pattern = bannerPattern
		if (pattern == null || !TeamShield.checkBannerColors(pattern, colors[0], colors[1])) {
			pattern = TeamShield.randomBannerPattern(colors[0], colors[1])
		}

		return Team(
			name ?: randomName(),
			colors[0],
			colors[1],
			members,
			pattern
		)
	}

	override fun grabName(): String {
		return name ?: "[Name not chosen]"
	}

	override fun giveName(name: String) {
		this.name = name
	}

	companion object {
		fun randomName(): String {
			return nameParts.random()
		}

		private val nameParts = Material.values()
			.filter { !it.name.startsWith("LEGACY_") && it.name.length <= 20 }
			.map { material -> material.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() } }

	}
}