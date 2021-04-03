package com.codeland.uhc.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import com.codeland.uhc.blockfix.BlockFixType
import com.codeland.uhc.command.Commands.errorMessage
import com.codeland.uhc.customSpawning.CustomSpawning
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.phase.PhaseType
import com.codeland.uhc.phase.phases.waiting.PvpData
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.quirk.quirks.carePackages.CarePackages
import com.codeland.uhc.quirk.quirks.Deathswap
import com.codeland.uhc.quirk.quirks.LowGravity
import com.codeland.uhc.team.TeamData
import com.mojang.authlib.GameProfile
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityDestroy
import net.minecraft.server.v1_16_R3.PacketPlayOutNamedEntitySpawn
import org.bukkit.*
import org.bukkit.command.CommandSender
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.util.*

@CommandAlias("uhct")
class TestCommands : BaseCommand() {
	@Subcommand("next")
	@Description("Manually go to the next round")
	fun testNext(sender : CommandSender) {
		if (Commands.opGuard(sender)) return

		if (GameRunner.uhc.isPhase(PhaseType.WAITING))
			errorMessage(sender, "In waiting phase, use /start instead")
		else
			GameRunner.uhc.startNextPhase()
	}

	@Subcommand("fill")
	@Description("fill your inventory with random items")
	fun testFill(sender: CommandSender) {
		if (Commands.opGuard(sender)) return
		sender as Player

		val random = Random()

		for (i in 0 until 500) {
			sender.inventory.addItem(ItemStack(Material.values()[random.nextInt(Material.values().size)], random.nextInt(64) + 1))
		}
	}

	@Subcommand("gravity")
	@Description("change the gravity constant")
	fun testGravity(sender: CommandSender, gravity: Double) {
		LowGravity.gravity = gravity
	}

	@Subcommand("deathswap warning")
	@Description("change the length of pre-swap warnings")
	fun testDsWarnings(sender: CommandSender, warning: Int) {

	}

	@Subcommand("deathswap immunity")
	@Description("change the length of the post-swap immunity period")
	fun testDsImmunity(sender: CommandSender, immunity: Int) {

	}

	@Subcommand("deathswap swap")
	@Description("swap all players")
	fun testDsSwap(sender: CommandSender) {
		Deathswap.doSwaps()
	}

	@Subcommand("insomnia")
	@Description("get the insomnia of the sender")
	fun testExhaustion(sender: CommandSender) {
		if (Commands.opGuard(sender)) return

		sender as Player
		sender.sendMessage("${sender.name}'s insomnia: ${sender.getStatistic(Statistic.TIME_SINCE_REST)}")
	}

	@Subcommand("blockFix")
	@Description("gets when the next apple will drop for you")
	fun testBlockFix(sender: CommandSender, blockFixType: BlockFixType) {
		if (Commands.opGuard(sender)) return
		sender as Player

		blockFixType.blockFix.getInfoString(sender) { info ->
			GameRunner.sendGameMessage(sender, info)
		}
	}

	@Subcommand("elapsed")
	@Description("gets how long this UHC has been going for")
	fun testElapsed(sender: CommandSender) {
		if (Commands.opGuard(sender)) return

		sender as Player

		GameRunner.sendGameMessage(sender, "Elapsed time: ${GameRunner.uhc.elapsedTime}")
	}

	@Subcommand("teams")
	@Description("gives an overview of teams")
	fun testTeams(sender: CommandSender) {
		if (Commands.opGuard(sender)) return

		val teams = TeamData.teams

		teams.forEach { team ->
			GameRunner.sendGameMessage(sender, team.colorPair.colorString(team.displayName))
			team.members.forEach { uuid ->
				val player = Bukkit.getOfflinePlayer(uuid)
				GameRunner.sendGameMessage(sender, team.colorPair.colorString(player.name ?: "NULL"))
			}
		}

		PlayerData.playerDataList.forEach { (uuid, playerData) ->
			if ((playerData.participating || playerData.staged) && !TeamData.isOnTeam(uuid)) {
				val player = Bukkit.getOfflinePlayer(uuid)
				GameRunner.sendGameMessage(sender, player.name ?: "NULL")
			}
		}
	}

	@Subcommand("playerData")
	@Description("get this player's playerData")
	fun testPlayerData(sender: CommandSender, player: OfflinePlayer) {
		val playerData = PlayerData.getPlayerData(player.uniqueId)

		GameRunner.sendGameMessage(sender, "PlayerData for ${player.name}:")
		GameRunner.sendGameMessage(sender, "Staged: ${playerData.staged}")
		GameRunner.sendGameMessage(sender, "Participating: ${playerData.participating}")
		GameRunner.sendGameMessage(sender, "Alive: ${playerData.alive}")
		GameRunner.sendGameMessage(sender, "Opting Out: ${playerData.optingOut}")
		GameRunner.sendGameMessage(sender, "In Lobby PVP: ${playerData.lobbyPVP.inPvp}")
	}

	@Subcommand("zombie")
	@Description("creates an afk zombie for a player, even if they are online")
	fun testZombie(sender: CommandSender, player: OfflinePlayer) {
		if (Commands.opGuard(sender)) return

		val onlinePlayer = player.player ?: return errorMessage(sender, "${player.name} is offline!")

		val playerData = PlayerData.getPlayerData(player.uniqueId)
		playerData.createZombie(onlinePlayer)

		GameRunner.sendGameMessage(sender, "Created a zombie for ${player.name}")
	}

	@Subcommand("drop")
	@Description("drops the current care package immediately")
	fun testDrop(sender: CommandSender) {
		if (Commands.opGuard(sender)) return

		val carePackages = GameRunner.uhc.getQuirk(QuirkType.CARE_PACKAGES) as CarePackages

		if (!carePackages.enabled) return errorMessage(sender, "Care packages is not going!")

		val result = carePackages.forceDrop()

		if (!result) return errorMessage(sender, "All care packages have been dropped!")
	}

	@Subcommand("mobcaps")
	@Description("query the current spawn limit coefficient")
	fun getMobCaps(sender: CommandSender) {
		sender as Player

		GameRunner.sendGameMessage(sender, "Monster spawn limit: ${sender.world.monsterSpawnLimit}")
		GameRunner.sendGameMessage(sender, "Animal spawn limit: ${sender.world.animalSpawnLimit}")
		GameRunner.sendGameMessage(sender, "Ambient spawn limit: ${sender.world.ambientSpawnLimit}")
		GameRunner.sendGameMessage(sender, "Water animal spawn limit: ${sender.world.waterAnimalSpawnLimit}")
		GameRunner.sendGameMessage(sender, "Water ambient spawn limit: ${sender.world.waterAmbientSpawnLimit}")
	}

	@Subcommand("mobcap")
	@Description("test a player's individual mobcap")
	fun testMobCap(sender: CommandSender, player: Player) {
		val playerMobs = CustomSpawning.calcPlayerMobs(player)

		GameRunner.sendGameMessage(sender, "${player.name}'s mobcap: ${PlayerData.getPlayerData(player.uniqueId).mobcap} | filled with ${playerMobs.first} representing ${playerMobs.second} of the total")
	}

	@Subcommand("killstreak")
	@Description("test a player's individual mobcap")
	fun testKillstreak(sender: CommandSender) {
		if (Commands.opGuard(sender)) return

		sender as Player
		val pvpData = PlayerData.getLobbyPvp(sender.uniqueId)

		if (pvpData.inPvp) {
			PvpData.onKill(sender)
			GameRunner.sendGameMessage(sender, "Killstreak increased to ${pvpData.killstreak}")
		} else {
			errorMessage(sender, "You are not in PVP!")
		}
	}

	@CommandCompletion("@uhcplayer")
	@Subcommand("changeName")
	fun changeName(sender: CommandSender, player: OfflinePlayer) {
		val namePlayer = Bukkit.getPlayer(player.uniqueId) as CraftPlayer? ?: return

		val nameField = GameProfile::class.java.getDeclaredField("name")
		nameField.isAccessible = true

		val modifiers = Field::class.java.getDeclaredField("modifiers")
		modifiers.isAccessible = true
		modifiers.setInt(nameField, nameField.modifiers.and(Modifier.FINAL.inv()))

		val actualName = namePlayer.profile.name
		nameField.set(namePlayer.profile, "${ChatColor.RESET}A")

		Bukkit.getOnlinePlayers().filter { it != namePlayer }.forEach { viewPlayer ->
			viewPlayer as CraftPlayer

			val destroyPacket = PacketPlayOutEntityDestroy(namePlayer.entityId)
			viewPlayer.handle.playerConnection.sendPacket(destroyPacket)

			//val pack = PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, namePlayer.handle)
			//viewPlayer.handle.playerConnection.sendPacket(pack)

			val spawnPacket = PacketPlayOutNamedEntitySpawn(namePlayer.handle)
			viewPlayer.handle.playerConnection.sendPacket(spawnPacket)
		}

		nameField.set(namePlayer.profile, actualName)
	}
}
