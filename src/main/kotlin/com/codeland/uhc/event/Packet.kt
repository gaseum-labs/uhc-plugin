package com.codeland.uhc.event

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.core.UHC
import com.codeland.uhc.discord.storage.DiscordStorage
import com.codeland.uhc.lobbyPvp.ArenaManager
import com.codeland.uhc.lobbyPvp.arena.PvpArena
import com.codeland.uhc.team.AbstractTeam
import com.codeland.uhc.util.Util
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.*
import com.comphenix.protocol.wrappers.EnumWrappers
import com.comphenix.protocol.wrappers.PlayerInfoData
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.kyori.adventure.text.format.TextColor
import net.minecraft.EnumChatFormat
import net.minecraft.network.chat.ChatComponentText
import net.minecraft.network.chat.ChatModifier
import net.minecraft.network.protocol.game.*
import net.minecraft.network.protocol.status.PacketStatusOutServerInfo
import net.minecraft.network.protocol.status.ServerPing
import net.minecraft.network.syncher.*
import net.minecraft.network.syncher.DataWatcher.Item
import net.minecraft.world.inventory.Containers
import net.minecraft.world.scores.Scoreboard
import net.minecraft.world.scores.ScoreboardTeam
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import java.util.*
import kotlin.experimental.or

object Packet {
	val playerNames = arrayListOf<UUID>()

	fun intToName(int: Int, length: Int): String {
		var countDown = int

		val name = CharArray(length * 2)

		for (i in 0 until length) {
			name[i * 2] = ChatColor.COLOR_CHAR
			name[i * 2 + 1] = ChatColor.values()[countDown % 10].char
			countDown /= 10
		}

		return String(name)
	}

	fun nameToInt(name: String, length: Int): Int? {
		if (name.length < length * 2) return null

		var int = 0

		for (i in length - 1 downTo 0) {
			if (name[i * 2] != ChatColor.COLOR_CHAR) return null
			val digit = name[i * 2 + 1]

			if (digit < '0' || digit > '9') return null

			int *= 10
			int += (digit - '0')
		}

		return int
	}

	fun playersIndex(uuid: UUID): Int {
		var nameIndex = playerNames.indexOf(uuid)

		if (nameIndex == -1) {
			playerNames.add(uuid)
			nameIndex = playerNames.lastIndex
		}

		return nameIndex
	}

	fun playersNewName(uuid: UUID): String {
		return intToName(playersIndex(uuid), 8)
	}

	/**
	 *  creates a metadata packet for the specified player
	 *  that only contains the first byte field
	 */
	fun metadataPacketDefaultState(player: Player): PacketPlayOutEntityMetadata {
		player as CraftPlayer

		/* a new data watcher which only contains the first byte */
		val stateDataWatcher = DataWatcher(player.handle)
		stateDataWatcher.register(DataWatcherObject(0, DataWatcherRegistry.a),
			getDatawatcherByte(player.handle.dataWatcher))

		return PacketPlayOutEntityMetadata(player.entityId, stateDataWatcher, true)
	}

	val entriesField = DataWatcher::class.java.getDeclaredField("f")

	init {
		entriesField.isAccessible = true
	}

	fun getDatawatcherByte(dataWatcher: DataWatcher): Byte {
		val entries = entriesField[dataWatcher] as Int2ObjectOpenHashMap<Item<Any>>
		val item = entries[0] as DataWatcher.Item<Byte>
		return item.b()
	}

	fun updateTeamColor(
		teamPlayer: Player,
		uhcTeam: AbstractTeam?,
		newName: String,
		sentPlayer: Player,
	) {
		sentPlayer as CraftPlayer
		val oldName = teamPlayer.name

		val arena = ArenaManager.playersArena(teamPlayer.uniqueId)

		val isOnTeam = when {
			teamPlayer.uniqueId == sentPlayer.uniqueId -> true
			arena == null -> uhcTeam?.members?.contains(sentPlayer.uniqueId) == true
			else -> arena is PvpArena && ArenaManager.playersTeam(arena, teamPlayer.uniqueId)
				?.contains(sentPlayer.uniqueId) == true
		}

		/* update fake scoreboard team */
		fun updateTeam(name: String) {
			val scoreboardTeam = ScoreboardTeam(Scoreboard(), name)
			scoreboardTeam.color = if (isOnTeam) EnumChatFormat.l else EnumChatFormat.m
			scoreboardTeam.prefix = if (uhcTeam != null) {
				Util.nmsGradientString(oldName, uhcTeam.colors[0], uhcTeam.colors[1])
			} else {
				ChatComponentText(oldName).setChatModifier(ChatModifier.a.setColor(EnumChatFormat.p))
			}
			scoreboardTeam.playerNameSet.add(name)

			sentPlayer.handle.b.sendPacket(PacketPlayOutScoreboardTeam.a(scoreboardTeam, false))
		}

		updateTeam(newName)
		if (teamPlayer.uniqueId == sentPlayer.uniqueId) updateTeam(oldName)
	}

	fun init() {
		val protocolManager = ProtocolLibrary.getProtocolManager()

		val playerInfoDataListField = PacketPlayOutPlayerInfo::class.java.getDeclaredField("b")
		playerInfoDataListField.isAccessible = true

		protocolManager.addPacketListener(object :
			PacketAdapter(UHCPlugin.plugin, ListenerPriority.HIGH, PacketType.Play.Server.PLAYER_INFO) {
			override fun onPacketSending(event: PacketEvent) {
				/* only change add player packets */
				val stalePacketWrapper = event.packet
				if (stalePacketWrapper.playerInfoAction.read(0) != EnumWrappers.PlayerInfoAction.ADD_PLAYER) return

				/* create a new packet that would be what the original was */
				val freshPacket = PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.a,
					(playerInfoDataListField[event.packet.handle] as List<PacketPlayOutPlayerInfo.PlayerInfoData>)
						.mapNotNull { (Bukkit.getPlayer(it.a().id) as CraftPlayer).handle }
				)

				/* give the new packet to the event to modify */
				val freshPacketWrapper = PacketContainer.fromPacket(freshPacket)
				event.packet = freshPacketWrapper

				val sentPlayer = event.player as CraftPlayer

				freshPacketWrapper.playerInfoDataLists.write(0,
					freshPacketWrapper.playerInfoDataLists.read(0).map { playerInfoData ->
						val newName = playersNewName(playerInfoData.profile.uuid)

						fun createTeam(name: String) {
							val sentTeam = ScoreboardTeam(Scoreboard(), name)
							sentTeam.playerNameSet.add(name)
							sentPlayer.handle.b.sendPacket(PacketPlayOutScoreboardTeam.a(sentTeam, true))
						}

						createTeam(newName)
						if (sentPlayer.uniqueId == playerInfoData.profile.uuid) createTeam(playerInfoData.profile.name)

						/* initialize the team that the sentplayer will know the playerInfoData player by */
						PlayerInfoData(
							playerInfoData.profile.withName(newName),
							playerInfoData.latency,
							playerInfoData.gameMode,
							playerInfoData.displayName
						)
					})
			}
		})

		val packetEntityIdField = PacketPlayOutEntityMetadata::class.java.getDeclaredField("a")
		packetEntityIdField.isAccessible = true

		val packetItemListField = PacketPlayOutEntityMetadata::class.java.getDeclaredField("b")
		packetItemListField.isAccessible = true

		val itemValueField = DataWatcher.Item::class.java.getDeclaredField("b")
		itemValueField.isAccessible = true

		protocolManager.addPacketListener(object :
			PacketAdapter(UHCPlugin.plugin, ListenerPriority.HIGH, PacketType.Play.Server.ENTITY_METADATA) {
			override fun onPacketSending(event: PacketEvent) {
				/* make sure this packet is sending the first byte of datawatcher info */
				val staleItemList = packetItemListField[event.packet.handle] as MutableList<DataWatcher.Item<Any>>
				val byteValue = itemValueField[staleItemList[0]] as? Byte ?: return

				/* find out who this packet deals with */
				val sentPlayer = event.player as CraftPlayer

				val metaPlayerID = packetEntityIdField.getInt(event.packet.handle)
				val metaPlayer =
					Bukkit.getOnlinePlayers().find { it.entityId == metaPlayerID } as CraftPlayer? ?: return
				/* only set when the game is going */
				val sentPlayerTeam = UHC.game?.teams?.playersTeam(sentPlayer.uniqueId)
				val metaPlayersArena = ArenaManager.playersArena(metaPlayer.uniqueId)

				/* begin modifying packet */
				event.packet = event.packet.deepClone()

				val freshItemList = packetItemListField[event.packet.handle] as MutableList<DataWatcher.Item<Any>>

				/* glowing in games */
				if (metaPlayersArena is PvpArena) {
					/* if on same team as meta player (not same player), or if game is in glow phase */
					if (
						(
						metaPlayer.uniqueId != sentPlayer.uniqueId &&
						ArenaManager.playersTeam(metaPlayersArena, metaPlayer.uniqueId)
							?.contains(sentPlayer.uniqueId) == true
						) ||
						(metaPlayersArena.shouldGlow() && metaPlayersArena.teams.flatten()
							.contains(sentPlayer.uniqueId))
					) {
						itemValueField[freshItemList[0]] = byteValue.or(0x40)
					}

					/* teammate glowing */
				} else if (
					UHC.game != null &&
					sentPlayer.entityId != metaPlayerID &&
					sentPlayerTeam != null &&
					sentPlayerTeam.members.contains(metaPlayer.uniqueId)
				) {
					itemValueField[freshItemList[0]] = byteValue.or(0x40)
				}
			}
		})

		val scoreboardObjectiveField = PacketPlayOutScoreboardScore::class.java.getDeclaredField("b")
		scoreboardObjectiveField.isAccessible = true

		val scoreboardPlayerField = PacketPlayOutScoreboardScore::class.java.getDeclaredField("a")
		scoreboardPlayerField.isAccessible = true

		protocolManager.addPacketListener(object :
			PacketAdapter(UHCPlugin.plugin, ListenerPriority.HIGH, PacketType.Play.Server.SCOREBOARD_SCORE) {
			override fun onPacketSending(event: PacketEvent) {
				val objectiveName = scoreboardObjectiveField[event.packet.handle] as String
				if (objectiveName != UHC.heartsObjective.name) return

				val originalPlayerName = scoreboardPlayerField[event.packet.handle] as String
				val player = Bukkit.getPlayer(originalPlayerName) ?: return

				val mappedPlayerName = playersNewName(player.uniqueId)
				scoreboardPlayerField.set(event.packet.handle, mappedPlayerName)
			}
		})

		protocolManager.addPacketListener(object :
			PacketAdapter(UHCPlugin.plugin, ListenerPriority.HIGH, PacketType.Status.Server.SERVER_INFO) {
			override fun onPacketSending(event: PacketEvent) {
				/* do not attempt to modify MOTD if discordstorage is not set */
				val splashText = DiscordStorage.splashText ?: return
				val color0 = TextColor.color(DiscordStorage.color0 ?: return)
				val color1 = TextColor.color(DiscordStorage.color1 ?: return)

				val oldPing = (event.packet.handle as PacketStatusOutServerInfo).b()

				val newPing = ServerPing()
				newPing.setServerInfo(ServerPing.ServerData("UHC ${Bukkit.getMinecraftVersion()}",
					oldPing.serverData.protocolVersion))
				newPing.setPlayerSample(oldPing.b())

				val length = 48
				val strip =
					String(CharArray(length + 1) { i -> if (i == length) '\n' else splashText[i % splashText.length] })

				newPing.setMOTD(Util.nmsGradientString(strip, color0, color1)
					.addSibling(Util.nmsGradientString(strip, color0, color1)))
				newPing.setFavicon(UHC.bot?.serverIcon ?: oldPing.d())

				event.packet = PacketContainer.fromPacket(PacketStatusOutServerInfo(newPing))
			}
		})

		/* set the name of every enchanting table gui */
		protocolManager.addPacketListener(object :
			PacketAdapter(UHCPlugin.plugin, ListenerPriority.HIGH, PacketType.Play.Server.OPEN_WINDOW) {
			override fun onPacketSending(event: PacketEvent) {
				val packet = event.packet.handle as PacketPlayOutOpenWindow

				if (packet.c() === Containers.m) {
					event.packet = PacketContainer.fromPacket(
						PacketPlayOutOpenWindow(
							packet.b(),
							Containers.m,
							Util.nmsGradientString("Replace Item to Cycle Enchants",
								TextColor.color(0xc40a0a),
								TextColor.color(0x820874))
						)
					)
				}
			}
		})
	}
}
