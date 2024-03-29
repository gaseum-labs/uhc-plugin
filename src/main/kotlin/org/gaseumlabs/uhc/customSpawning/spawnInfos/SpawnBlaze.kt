package org.gaseumlabs.uhc.customSpawning.spawnInfos

import org.gaseumlabs.uhc.customSpawning.SpawnInfo
import org.gaseumlabs.uhc.customSpawning.SpawnUtil
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.BlockFace.DOWN
import org.bukkit.entity.Blaze
import org.bukkit.entity.Player
import org.bukkit.util.Vector

class SpawnBlaze : SpawnInfo<Blaze>(Blaze::class.java, Vector(0.5, 0.0, 0.5), true) {
	override fun allowSpawn(block: Block, spawnCycle: Int): Boolean {
		return SpawnUtil.spawnFloor(block.getRelative(DOWN)) &&
		SpawnUtil.spawnBox(block)
	}

	override fun onSpawn(block: Block, count: Int, player: Player?, entity: Blaze) {
		entity.removeWhenFarAway = false

		if (player == null) return

		val component = Component.text("Blaze Spawned!", TextColor.color(0xff6417))
		player.sendActionBar(component)
		player.sendMessage(component)

		player.playSound(player.location.add(
			entity.location
				.subtract(player.location)
				.toVector()
				.normalize()
				.multiply(3)
		), Sound.ENTITY_BLAZE_AMBIENT, 1.0f, 1.0f)
	}
}
