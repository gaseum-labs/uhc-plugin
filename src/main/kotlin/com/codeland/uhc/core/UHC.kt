package com.codeland.uhc.core

import com.codeland.uhc.customSpawning.CustomSpawning
import com.codeland.uhc.gui.Gui
import com.codeland.uhc.phase.Phase
import com.codeland.uhc.phase.PhaseType
import com.codeland.uhc.phase.PhaseVariant
import com.codeland.uhc.phase.phases.grace.GraceDefault
import com.codeland.uhc.phase.phases.waiting.AbstractLobby
import com.codeland.uhc.phase.phases.waiting.PvpData
import com.codeland.uhc.phase.phases.waiting.WaitingDefault
import com.codeland.uhc.quirk.Quirk
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.team.TeamData
import com.codeland.uhc.util.SchedulerUtil
import net.md_5.bungee.api.ChatColor
import net.minecraft.server.v1_16_R3.*
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.command.CommandSender
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class UHC(val defaultPreset: Preset, val defaultVariants: Array<PhaseVariant>) {
	var ledger = Ledger()

	var mobCapCoefficient = 1.0
	var killReward = KillReward.REGENERATION

	private var phaseVariants = Array(PhaseType.values().size) { index ->
		defaultVariants[index]
	}

	var quirks = Array(QuirkType.values().size) { index ->
		QuirkType.values()[index].createQuirk(this)
	}

	private var phaseTimes = arrayOf(
		0,
		defaultPreset.graceTime,
		defaultPreset.shrinkTime,
		0,
		0
	)

	var startRadius = defaultPreset.startRadius
	private set
	var endRadius = defaultPreset.endRadius
	private set
	var elapsedTime = 0

	/* null if it is a custom preset */
	var preset: Preset? = defaultPreset

	var currentPhase = null as Phase?

	var defaultWorldIndex = 0

	var naturalRegeneration = false

	var usingBot = GameRunner.bot != null
	private set

	var teleportGroups: Array<Array<UUID>>? = null
	var teleportLocations: ArrayList<Location>? = null

	var lobbyRadius = 60
	var lobbyPVPMin = -1
	var lobbyPVPMax = -1

	fun updateUsingBot(using: Boolean) {
		val bot = GameRunner.bot

		usingBot = if (bot == null) {
			false

		} else {
			bot.clearTeamVCs()
			using
		}
	}

	val gui = Gui(this)

	fun spectatorSpawnLocation(): Location {
		for ((uuid, playerData) in PlayerData.playerDataList) {
			if (playerData.alive && playerData.participating) {
				return GameRunner.getPlayerLocation(uuid)?.clone()?.add(0.0, 2.0, 0.0)
					?: Location(Bukkit.getWorlds()[0], 0.5, 100.0, 0.5)
			}
		}

		return Location(Bukkit.getWorlds()[0], 0.5, 100.0, 0.5)
	}

	/* state setters */

	fun updatePreset(preset: Preset) {
		updatePreset(preset, preset.startRadius, preset.endRadius, preset.graceTime, preset.shrinkTime)
	}

	fun updatePreset(startRadius: Int, endRadius: Int, graceTime: Int, shrinkTime: Int) {
		updatePreset(null, startRadius, endRadius, graceTime, shrinkTime)
	}

	private fun updatePreset(preset: Preset?, startRadius: Int, endRadius: Int, graceTime: Int, shrinkTime: Int) {
		this.preset = preset

		this.startRadius = startRadius
		this.endRadius = endRadius

		phaseTimes = arrayOf(
			0,
			graceTime,
			shrinkTime,
			0,
			0
		)
	}

	fun updateTime(phaseType: PhaseType, time: Int) {
		this.preset = null
		phaseTimes[phaseType.ordinal] = time
	}

	fun updateStartRadius(startRadius: Int) {
		this.preset = null
		this.startRadius = startRadius
	}

	fun updateEndRadius(endRadius: Int) {
		this.preset = null
		this.endRadius = endRadius
	}

	fun updateVariant(phaseVariant: PhaseVariant) {
		val index = phaseVariant.type.ordinal

		phaseVariants[index] = phaseVariant
	}

	fun updateQuirk(type: QuirkType, enabled: Boolean) {
		quirks[type.ordinal].enabled = enabled

		if (enabled) type.incompatibilities.forEach { other ->
			val otherQuirk = GameRunner.uhc.getQuirk(other)

			if (otherQuirk.enabled) {
				otherQuirk.enabled = false
				gui.quirkToggles[otherQuirk.type.ordinal].updateDisplay()
			}
		}
	}

	/**
	 * call after object is fully initialized
	 */
	fun updateDisplays() {
		quirks.forEach { quirk -> updateQuirk(quirk.type, quirk.enabled) }
	}

	/* state getters */

	fun getVariant(phaseType: PhaseType): PhaseVariant {
		return phaseVariants[phaseType.ordinal]
	}

	fun getTime(phaseType: PhaseType): Int {
		return phaseTimes[phaseType.ordinal]
	}

	fun getQuirk(quirkType: QuirkType): Quirk {
		return quirks[quirkType.ordinal]
	}

	fun isEnabled(quirkType: QuirkType): Boolean {
		return quirks[quirkType.ordinal].enabled
	}

	fun isPhase(compare: PhaseType): Boolean {
		return currentPhase?.phaseType == compare
	}

	fun isVariant(compare: PhaseVariant): Boolean {
		return currentPhase?.phaseVariant == compare
	}

	fun isGameGoing(): Boolean {
		return currentPhase?.phaseType?.gameGoing ?: false
	}

	fun getDefaultWorld(): World {
		return Bukkit.getWorlds()[defaultWorldIndex]
	}

	/* game flow modifiers */

	/**
	 * should be called when the world is loaded
	 *
	 * starts the waiting phase
	 */
	fun startWaiting() {
		startPhase(PhaseType.WAITING)

		/* begin global ticking task */
		/* holds a centralized list of all general continuous tasks throughout the game */
		var currentTick = 0

		SchedulerUtil.everyTick {
			if (isGameGoing() && !isPhase(PhaseType.ENDGAME)) CustomSpawning.spawnTick(currentTick)

			if (isGameGoing()) PlayerData.zombieBorderTick(currentTick)

			PvpData.onTick(currentTick)

			//ParkourCheckpoint.lobbyParkourTick() //DISABLED FOR NOW

			AbstractLobby.lobbyTipsTick(currentTick)

            TeamData.teams.forEach { team ->
                val teamPlayers = team.members.mapNotNull { Bukkit.getPlayer(it) }

                teamPlayers.forEachIndexed { i, player ->
                    teamPlayers.forEachIndexed { j, otherPlayer ->
                        if (i != j) {
                            val meta = DataWatcher((otherPlayer as CraftPlayer).handle)

	                        meta.register(DataWatcherObject(0, DataWatcherRegistry.a), 0x40)

                            (player as CraftPlayer).handle.playerConnection.sendPacket(
                                PacketPlayOutEntityMetadata(otherPlayer.entityId, meta, true)
                            )
                        }
                    }
                }
            }

            PlayerData.playerDataList.forEach { (uuid, playerData) ->
                val player = Bukkit.getPlayer(uuid)
                if (player != null) {
                    val entityPacket = PacketPlayOutEntityMetadata()
                    (player as CraftPlayer).handle.playerConnection.sendPacket(entityPacket)
                }
            }

			/* highly composite number */
			currentTick = (currentTick + 1) % 294053760
		}
	}

	/**
	 * starts the grace period and ends waiting phase
	 *
	 * @return a string if the game couldn't start
	 */
	fun startUHC(commandSender : CommandSender): String? {
		if (isGameGoing()) return "Game has already started!"

		val world = getDefaultWorld()
		val numTeams = TeamData.teams.size
		val individuals = ArrayList<UUID>()

		/* compile a list of all individuals that will play */
		PlayerData.playerDataList.forEach { (uuid, playerData) ->
			if (playerData.staged && !TeamData.isOnTeam(uuid)) individuals.add(uuid)
		}

		if (numTeams + individuals.size == 0) return "No one is playing!"

		/* get where players are teleporting */
		teleportLocations = GraceDefault.spreadPlayers(
			world,
			numTeams + individuals.size,
			startRadius - 5.0,
			if (world.environment == World.Environment.NETHER) GraceDefault.Companion::findYMid else GraceDefault.Companion::findYTop
		)

		/* if teleport locations are found! */
		return if (teleportLocations?.isNotEmpty() == true) {
			/* compile teams and individuals into who will teleport to which location */
			teleportGroups = Array(numTeams + individuals.size) { i ->
				if (i < numTeams) {
					val team = TeamData.teams[i]

					Array(team.members.size) { j ->
						team.members[j]
					}
				} else {
					arrayOf(individuals[i - numTeams])
				}
			}

			/* switch to grace in 4 seconds */
			val waiting = GameRunner.uhc.currentPhase as WaitingDefault
			waiting.updateLength(4)

			null

		} else {
			"Not enough valid spaces to teleport in this world!"
		}
	}

	/**
	 * called any time during the uhc to end it
	 *
	 * cleans up and summarizes the game
	 *
	 * sets the current phase to endgame
	 */
	fun endUHC(winners: ArrayList<UUID>) {
		/* if someone won */
		if (winners.isNotEmpty()) {
			val winningTeam = TeamData.playersTeam(winners[0])

			val topMessage: String
			val bottomMessage: String

			if (winningTeam == null) {
				val winningPlayer = Bukkit.getPlayer(winners[0])

				topMessage = "${ChatColor.GOLD}${ChatColor.BOLD}${winningPlayer?.name} Has Won!"
				bottomMessage = ""

				ledger.addEntry(winningPlayer?.name ?: "NULL", GameRunner.uhc.elapsedTime, "winning", true)

			} else {
				topMessage = winningTeam.colorPair.colorString("${winningTeam.displayName} Has Won!")

				var playerString = ""
				winners.forEach { winner ->
					val player = Bukkit.getPlayer(winner)

					playerString += "${player?.name} "
					ledger.addEntry(player?.name ?: "NULL", GameRunner.uhc.elapsedTime, "winning", true)
				}

				bottomMessage = winningTeam.colorPair.colorString(playerString.dropLast(1))
			}

			Bukkit.getServer().onlinePlayers.forEach { player -> player.sendTitle(topMessage, bottomMessage, 0, 200, 40) }

			ledger.createTextFile()

		/* no one won the game */
		} else {
			Bukkit.getServer().onlinePlayers.forEach { player -> player.sendTitle("${ChatColor.GOLD}${ChatColor.BOLD}No one wins?", "", 0, 200, 40) }
		}

		/* remove all teams */
		TeamData.removeAllTeams {}

		/* reset all player data states */
		PlayerData.playerDataList.forEach { (uuid, playerData) ->
			playerData.participating = false
			playerData.alive = false
		}

		/* stop all world borders */
		Bukkit.getWorlds().forEach { world ->
			world.worldBorder.size = world.worldBorder.size
		}

		/* go to postgame immediately */
		startPhase(PhaseType.POSTGAME)
	}

	/* starting phases */

	fun startNextPhase() {
		val oldPhase = currentPhase ?: return

		var nextIndex = (oldPhase.phaseType.ordinal + 1) % PhaseType.values().size

		startPhase(nextIndex)
	}

	fun startPhase(phaseType: PhaseType, onInject: (Phase) -> Unit = {}) {
		startPhase(phaseType.ordinal, onInject)
	}

	fun startPhase(phaseIndex: Int, onInject: (Phase) -> Unit = {}) {
		currentPhase?.onEnd()

		currentPhase = phaseVariants[phaseIndex].start(this, phaseTimes[phaseIndex], onInject)

		quirks.forEach { quirk ->
			if (quirk.enabled) quirk.onPhaseSwitch(phaseVariants[phaseIndex])
		}
	}

	fun containSpecs() {
		Bukkit.getOnlinePlayers().forEach { player ->
			if (player.gameMode == GameMode.SPECTATOR) {
				val locX = player.location.blockX.toDouble()
				val locZ = player.location.blockZ.toDouble()

				val x = when {
					locX > startRadius -> startRadius.toDouble()
					locX < -startRadius -> -startRadius.toDouble()
					else -> locX
				}

				val z = when {
					locZ > startRadius -> startRadius.toDouble()
					locZ < -startRadius -> -startRadius.toDouble()
					else -> locZ
				}

				if (x != locX || z != locZ) player.teleport(player.location.set(x + 0.5, player.location.y, z + 0.5))
			}
		}
	}

	fun updateMobCaps() {
		val world = getDefaultWorld()

		val borderRadius = world.worldBorder.size / 2

		var spawnModifier = borderRadius / 128.0
		if (spawnModifier > 1.0) spawnModifier = 1.0

		world.     monsterSpawnLimit = (70 * mobCapCoefficient * spawnModifier).roundToInt().coerceAtLeast(1)
		world.      animalSpawnLimit = (10 * mobCapCoefficient * spawnModifier).roundToInt().coerceAtLeast(1)
		world.     ambientSpawnLimit = (15 * mobCapCoefficient * spawnModifier).roundToInt().coerceAtLeast(1)
		world. waterAnimalSpawnLimit = ( 5 * mobCapCoefficient * spawnModifier).roundToInt().coerceAtLeast(1)
		world.waterAmbientSpawnLimit = (20 * mobCapCoefficient * spawnModifier).roundToInt().coerceAtLeast(1)
	}
}
