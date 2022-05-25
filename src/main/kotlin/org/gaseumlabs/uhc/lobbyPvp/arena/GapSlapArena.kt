package org.gaseumlabs.uhc.lobbyPvp.arena

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.GameMode.ADVENTURE
import org.bukkit.GameMode.SPECTATOR
import org.bukkit.Material.AIR
import org.bukkit.Material.ENCHANTED_GOLDEN_APPLE
import org.bukkit.World
import org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftLivingEntity
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import org.gaseumlabs.uhc.component.ComponentAction.uhcTitle
import org.gaseumlabs.uhc.component.UHCColor
import org.gaseumlabs.uhc.component.UHCComponent
import org.gaseumlabs.uhc.core.PlayerData
import org.gaseumlabs.uhc.gui.ItemCreator
import org.gaseumlabs.uhc.lobbyPvp.*
import org.gaseumlabs.uhc.util.Action
import org.gaseumlabs.uhc.world.WorldManager
import java.util.*
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.random.Random.Default.nextFloat

class GapSlapArena(teams: ArrayList<ArrayList<UUID>>, val platform: Platform) : Arena(ArenaType.GAP_SLAP, teams) {
	class Platform(
		val builderName: String,
		val name: String,
		val width: Int,
		val height: Int,
		val upperLayer: Array<BlockData>,
		val platformLayer: Array<BlockData>,
		val lowerLayer: Array<BlockData>,
		val startPositions: ArrayList<Pair<Int, Int>>,
	)

	companion object {
		const val PLATFORM_Y_LEVEL = 110

		private const val PHASE_GAME = 0
		private const val PHASE_INTERMEDIATE = 1
		private const val PHASE_END = 2

		val PLAYING_TO = 3

		fun load(data: String, world: World): Arena? {
			return null
		}

		val submittedPlatforms = HashMap<UUID, Platform>()

		val slapApple = ItemCreator.regular(ENCHANTED_GOLDEN_APPLE).enchant(Enchantment.KNOCKBACK, 2)
			.name(Component.text("Slap Gap", NamedTextColor.AQUA))
	}

	val scores = teams.map { it.first() }.associateWith { 0 } as HashMap<UUID, Int>
	var roundNumber = 0
	var phase = 0
	var timer = 0

	val offX = (ArenaManager.ARENA_STRIDE - platform.width) / 2
	val offZ = (ArenaManager.ARENA_STRIDE - platform.height) / 2

	/**
	 * for event listeners
	 */
	fun playersShouldBeFrozen(): Boolean {
		return phase == PHASE_INTERMEDIATE
	}

	override fun customPerSecond(onlinePlayers: List<Player>): Boolean {
		return when (phase) {
			PHASE_GAME -> {
				/* eliminate players knocked off the platform */
				onlinePlayers.forEach { player ->
					if (
						player.gameMode === ADVENTURE && (
						player.location.y < PLATFORM_Y_LEVEL - 2 ||
						playerOutsideBorderBy(player) > 0
						)
					) {
						player.gameMode = SPECTATOR
						onlinePlayers.forEach { sendPlayer ->
							Action.sendGameMessage(sendPlayer, "${player.name} has been knocked off!")
						}
					}
				}

				val remaining = onlinePlayers.filter { it.gameMode === ADVENTURE }
				if (remaining.size <= 1) {
					startIntermediatePhase(remaining.firstOrNull(), onlinePlayers, false)
				}

				false
			}
			PHASE_INTERMEDIATE -> {
				if (--timer <= 0) {
					startPlayingPhase()
				} else {
					val titleComponent = UHCComponent.text(timer.toString(), UHCColor.U_RED)
					val subTitleComponent = UHCComponent.text("Round $roundNumber", UHCColor.SHRINK)

					online().forEach { player ->
						player.uhcTitle(titleComponent, subTitleComponent, 0, 21, 0)
					}
				}

				false
			}
			else -> {
				--timer <= 0
			}
		}
	}

	private fun startIntermediatePhase(winner: Player?, onlinePlayers: List<Player>, initial: Boolean) {
		val newScore = if (winner == null) 0 else (scores[winner.uniqueId] ?: 0) + 1

		if (winner != null && newScore >= PLAYING_TO) {
			phase = PHASE_END
			timer = 10

			val titleComponent = UHCComponent.text("${winner.name} wins!", UHCColor.U_RED)

			onlinePlayers.forEach {
				it.uhcTitle(titleComponent, UHCComponent.text(), 0, 160, 40)
			}

		} else {
			++roundNumber
			phase = PHASE_INTERMEDIATE
			timer = 5

			val world = WorldManager.pvpWorld
			val startingPositions = platform.startPositions.shuffled().map { (sx, sz) ->
				world.getBlockAt(
					x * ArenaManager.ARENA_STRIDE + offX + sx,
					PLATFORM_Y_LEVEL,
					z * ArenaManager.ARENA_STRIDE + offZ + sz,
				)
			}

			onlinePlayers.forEachIndexed { i, onlinePlayer ->
				startPlayerInRound(onlinePlayer, startingPositions[i % startingPositions.size])
			}

			/* don't report anything for the initial phase */
			if (initial) return

			if (winner != null) {
				scores[winner.uniqueId] = newScore

				online().forEach { onlinePlayer ->
					onlinePlayer.uhcTitle(
						UHCComponent.text("Round Winner: ${winner.name}", UHCColor.U_GOLD),
						UHCComponent.text("Score: ${scores[winner.uniqueId]}", UHCColor.U_RED),
						0,
						40,
						10
					)
				}
			} else {
				online().forEach { onlinePlayer ->
					onlinePlayer.uhcTitle(
						UHCComponent.text("Round Tied", UHCColor.U_GOLD),
						UHCComponent.text(),
						0,
						40,
						10
					)
				}
			}
		}
	}

	private fun startPlayingPhase() {
		phase = PHASE_GAME
	}

	private fun startPlayerInRound(player: Player, block: Block) {
		player.gameMode = ADVENTURE

		/* reset effects */
		player.fireTicks = -1
		player.fallDistance = 0.0f
		player.inventory.clear()
		player.activePotionEffects.forEach { effect -> player.removePotionEffect(effect.type) }
		player.absorptionAmount = 0.0
		player.getAttribute(GENERIC_MAX_HEALTH)?.baseValue = 20.0
		player.health = 20.0

		/* give gap slap apple */
		player.inventory.setItem(0, slapApple.create())

		val (centerX, centerZ) = getCenter()
		val angle = atan2(centerZ.toFloat() - block.z.toFloat(), centerX.toFloat() - block.x.toFloat())

		player.teleport(block.location.add(0.5, 1.0, 0.5)
			.setDirection(Vector(1.0, 0.0, 0.0).rotateAroundY(angle.toDouble() + PI)))
	}

	override fun startingPositions(teams: ArrayList<ArrayList<UUID>>): List<List<Position>> {
		val (centerX, centerZ) = getCenter()

		return teams.map { list ->
			list.map {
				Position(
					centerX,
					centerZ,
					nextFloat() * PI.toFloat() * 2.0f,
					PLATFORM_Y_LEVEL + 2
				)
			}
		}
	}

	override fun customStartPlayer(player: Player, playerData: PlayerData) {
		player.gameMode = SPECTATOR
		Action.sendGameMessage(player, "Now entering ${platform.name} by ${platform.builderName}")
	}

	override fun prepareArena(world: World) {
		/* clear around the platform */
		for (y in PLATFORM_Y_LEVEL - 6..PLATFORM_Y_LEVEL + 6) {
			for (bz in z * ArenaManager.ARENA_STRIDE until (z + 1) * ArenaManager.ARENA_STRIDE) {
				for (bx in x * ArenaManager.ARENA_STRIDE until (x + 1) * ArenaManager.ARENA_STRIDE) {
					world.getBlockAt(bx, y, bz).setType(AIR, false)
				}
			}
		}

		/* place the platform */
		for (az in 0 until platform.height) {
			for (ax in 0 until platform.width) {
				world.getBlockAt(
					x * ArenaManager.ARENA_STRIDE + offX + ax,
					PLATFORM_Y_LEVEL + 1,
					z * ArenaManager.ARENA_STRIDE + offZ + az,
				).setBlockData(platform.upperLayer[az * platform.width + ax], false)

				world.getBlockAt(
					x * ArenaManager.ARENA_STRIDE + offX + ax,
					PLATFORM_Y_LEVEL,
					z * ArenaManager.ARENA_STRIDE + offZ + az,
				).setBlockData(platform.platformLayer[az * platform.width + ax], false)

				world.getBlockAt(
					x * ArenaManager.ARENA_STRIDE + offX + ax,
					PLATFORM_Y_LEVEL - 1,
					z * ArenaManager.ARENA_STRIDE + offZ + az,
				).setBlockData(platform.lowerLayer[az * platform.width + ax], false)
			}
		}
	}

	override fun arenaStart(onlinePlayers: List<Player>) {
		startIntermediatePhase(null, onlinePlayers, true)
	}

	override fun startText() = "Entering Gap Slap in"

	override fun shutdownOnLeave() = true

	override fun customSave(): String? = null
}
