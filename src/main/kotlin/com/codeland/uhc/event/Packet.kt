package com.codeland.uhc.event

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.UHC
import com.codeland.uhc.world.WorldManager
import com.codeland.uhc.lobbyPvp.PvpGameManager
import com.codeland.uhc.team.Team
import com.codeland.uhc.team.TeamData
import com.codeland.uhc.util.Util
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.EnumWrappers
import com.comphenix.protocol.wrappers.PlayerInfoData
import net.minecraft.server.v1_16_R3.*
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer
import org.bukkit.entity.Player
import java.util.*
import kotlin.experimental.or

object Packet {
	val playerNames = arrayListOf<UUID>()

	fun intToName(int: Int): String {
		var countDown = int

		val name = CharArray(16)

		for (i in 0..7) {
			name[i * 2    ] = ChatColor.COLOR_CHAR
			name[i * 2 + 1] = ChatColor.values()[countDown % 10].char
			countDown /= 10
		}

		return String(name)
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
		return intToName(playersIndex(uuid))
	}

	/**
	 *  creates a metadata packet for the specified player
	 *  that only contains the first byte field
	 */
	fun metadataPacketDefaultState(player: Player): PacketPlayOutEntityMetadata {
		player as CraftPlayer

		/* a new data watcher which only contains the first byte */
		val stateDataWatcher = DataWatcher(player.handle)
		stateDataWatcher.register(DataWatcherObject(0, DataWatcherRegistry.a), getDatawatcherByte(player.handle.dataWatcher))

		return PacketPlayOutEntityMetadata(player.entityId, stateDataWatcher, true)
	}

	val entriesField = DataWatcher::class.java.getDeclaredField("entries")
	init { entriesField.isAccessible = true }

	fun getDatawatcherByte(dataWatcher: DataWatcher): Byte {
		val entries = entriesField[dataWatcher] as Int2ObjectOpenHashMap<DataWatcher.Item<Any>>
		val item = entries[0] as DataWatcher.Item<Byte>
		return item.b()
	}

	fun updateTeamColor(teamPlayer: Player, uhcTeam: Team?, newName: String, sentPlayer: Player) {
		sentPlayer as CraftPlayer
		val oldName = teamPlayer.name

		val team = ScoreboardTeam(Scoreboard(), newName)
		team.color = if (uhcTeam != null && uhcTeam.members.contains(sentPlayer.uniqueId)) EnumChatFormat.AQUA else EnumChatFormat.RED
		team.prefix = if (uhcTeam != null) Util.nmsGradientString(oldName, uhcTeam.color1, uhcTeam.color2) else ChatComponentText(oldName).setChatModifier(ChatModifier.a.setColor(EnumChatFormat.WHITE))
		team.playerNameSet.add(newName)

		sentPlayer.handle.playerConnection.sendPacket(PacketPlayOutScoreboardTeam(team, 2))
	}

	fun init() {
		val protocolManager = ProtocolLibrary.getProtocolManager()

		val playerInfoDataListField = PacketPlayOutPlayerInfo::class.java.getDeclaredField("b")
		playerInfoDataListField.isAccessible = true

		protocolManager.addPacketListener(object : PacketAdapter(UHCPlugin.plugin, ListenerPriority.HIGH, PacketType.Play.Server.PLAYER_INFO) {
			override fun onPacketSending(event: PacketEvent) {
				/* only change add player packets */
				val stalePacketWrapper = event.packet
				if (stalePacketWrapper.playerInfoAction.read(0) != EnumWrappers.PlayerInfoAction.ADD_PLAYER) return

				/* create a new packet that would be what the original was */
				val freshPacket = PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER,
					(playerInfoDataListField[event.packet.handle] as List<PacketPlayOutPlayerInfo.PlayerInfoData>)
						.mapNotNull { (Bukkit.getPlayer(it.a().id) as CraftPlayer).handle }
				)

				/* give the new packet to the event to modify */
				val freshPacketWrapper = PacketContainer.fromPacket(freshPacket)
				event.packet = freshPacketWrapper

				val sentPlayer = event.player as CraftPlayer

				freshPacketWrapper.playerInfoDataLists.write(0, freshPacketWrapper.playerInfoDataLists.read(0).map { playerInfoData ->
					val newName = playersNewName(playerInfoData.profile.uuid)

					/* initialize the team that the sentplayer will know the playerInfoData player by */
					val sentTeam = ScoreboardTeam(Scoreboard(), newName)
					sentTeam.playerNameSet.add(newName)
					sentPlayer.handle.playerConnection.sendPacket(PacketPlayOutScoreboardTeam(sentTeam, 0))

					//self team would go here if needed

					PlayerInfoData(
						playerInfoData.profile.withName(playersNewName(playerInfoData.profile.uuid)),
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

		val entriesField = DataWatcher::class.java.getDeclaredField("entries")
		entriesField.isAccessible = true

		protocolManager.addPacketListener(object : PacketAdapter(UHCPlugin.plugin, ListenerPriority.HIGH, PacketType.Play.Server.ENTITY_METADATA) {
			override fun onPacketSending(event: PacketEvent) {
				/* make sure this packet is sending the first byte of datawatcher info */
				val staleItemList = packetItemListField[event.packet.handle] as MutableList<DataWatcher.Item<Any>>
				val byteValue = itemValueField[staleItemList[0]] as? Byte ?: return

				/* find out who this packet deals with */
				val sentPlayer = event.player as CraftPlayer

				val metaPlayerID = packetEntityIdField.getInt(event.packet.handle)
				val metaPlayer = Bukkit.getOnlinePlayers().find { it.entityId == metaPlayerID } as CraftPlayer? ?: return
				val sentPlayerTeam = TeamData.playersTeam(sentPlayer.uniqueId)
				val metaPlayersGame = PvpGameManager.playersGame(metaPlayer.uniqueId)

				/* begin modifying packet */
				event.packet = event.packet.deepClone()

				val freshItemList = packetItemListField[event.packet.handle] as MutableList<DataWatcher.Item<Any>>

				/* glowing in games */
				if (metaPlayersGame != null) {
					if (metaPlayersGame.shouldGlow() && metaPlayersGame.players.contains(sentPlayer.uniqueId)) {
						itemValueField[freshItemList[0]] = byteValue.or(0x40)
					}

				/* teammate glowing */
				} else if (
					UHC.isGameGoing() &&
					sentPlayer.entityId != metaPlayerID &&
					sentPlayerTeam != null &&
					sentPlayerTeam.members.contains(metaPlayer.uniqueId)
				) {
					itemValueField[freshItemList[0]] = byteValue.or(0x40)
				}
			}
		})

		val borderActionField = PacketPlayOutWorldBorder::class.java.getDeclaredField("a")
		borderActionField.isAccessible = true

		val borderDiameterField = PacketPlayOutWorldBorder::class.java.getDeclaredField("e")
		borderDiameterField.isAccessible = true

		val borderCenterXField = PacketPlayOutWorldBorder::class.java.getDeclaredField("c")
		borderCenterXField.isAccessible = true

		val borderCenterZField = PacketPlayOutWorldBorder::class.java.getDeclaredField("d")
		borderCenterZField.isAccessible = true

		protocolManager.addPacketListener(object : PacketAdapter(UHCPlugin.plugin, ListenerPriority.HIGH, PacketType.Play.Server.WORLD_BORDER) {
			override fun onPacketSending(event: PacketEvent) {
				val sentPlayer = event.player
				val pvpGame = PvpGameManager.playersGame(sentPlayer.uniqueId)

				if (pvpGame != null && sentPlayer.world.name == WorldManager.PVP_WORLD_NAME) {
					event.packet = event.packet.deepClone()
					val packet = event.packet.handle as PacketPlayOutWorldBorder

					if (borderActionField[packet] == PacketPlayOutWorldBorder.EnumWorldBorderAction.INITIALIZE) {
						val (centerX, centerZ) = pvpGame.centerLocation()

						borderDiameterField[packet] = pvpGame.borderSize.toDouble()
						borderCenterXField[packet] = centerX.toDouble()
						borderCenterZField[packet] = centerZ.toDouble()
					}
				}
			}
		})

		val scoreboardObjectiveField = PacketPlayOutScoreboardScore::class.java.getDeclaredField("b")
		scoreboardObjectiveField.isAccessible = true

		val scoreboardPlayerField = PacketPlayOutScoreboardScore::class.java.getDeclaredField("a")
		scoreboardPlayerField.isAccessible = true

		protocolManager.addPacketListener(object : PacketAdapter(UHCPlugin.plugin, ListenerPriority.HIGH, PacketType.Play.Server.SCOREBOARD_SCORE) {
			override fun onPacketSending(event: PacketEvent) {
				val objectiveName = scoreboardObjectiveField[event.packet.handle] as String
				if (objectiveName != GameRunner.heartsObjective.name) return

				val originalPlayerName = scoreboardPlayerField[event.packet.handle] as String
				val player = Bukkit.getPlayer(originalPlayerName) ?: return

				val mappedPlayerName = playersNewName(player.uniqueId)
				scoreboardPlayerField.set(event.packet.handle, mappedPlayerName)
			}
		})

		/*
				val sentPlayer = event.player as CraftPlayer
				val packet = event.packet.handle as PacketPlayOutEntityMetadata

				val metaPlayerID = packetEntityIdField.getInt(packet)
				val metaPlayer = Bukkit.getOnlinePlayers().find { it.entityId == metaPlayerID } as CraftPlayer? ?: return

				val itemList = packetItemListField.get(packet) as MutableList<DataWatcher.Item<Any>>
				if (itemList.isEmpty()) return
				val packetFirst = itemValueField.get(itemList[0]) as? Byte ?: return

				val realWatcher = metaPlayer.handle.dataWatcher
				val realEntries = entriesField.get(realWatcher) as Int2ObjectOpenHashMap<DataWatcher.Item<Any>>
				if (realEntries.isEmpty()) return
				val realValue = itemValueField.get(realEntries.get(0)) as? Byte ?: return

				val sentPlayerTeam = TeamData.playersTeam(sentPlayer.uniqueId)
				val value = if (sentPlayer.entityId != metaPlayerID && sentPlayerTeam != null && sentPlayerTeam.members.contains(metaPlayer.uniqueId)) {
					realValue.or(0x40)
				} else {
					realValue
				}

				itemList.removeAt(0)
				itemList.add(0, DataWatcher.Item(DataWatcherObject(0, DataWatcherRegistry.a), value) as DataWatcher.Item<Any>)
		 */
/*
		val sentPlayer = event.player as CraftPlayer

				val dataWatcher = sentPlayer.handle.dataWatcher

				val metaPlayerID = event.packet.integers.read(0)
				val metaPlayer = Bukkit.getOnlinePlayers().find { it.entityId == metaPlayerID } ?: return

				val sentPlayerTeam = TeamData.playersTeam(sentPlayer.uniqueId)

				if (sentPlayer.entityId != metaPlayerID && sentPlayerTeam != null && sentPlayerTeam.members.contains(metaPlayer.uniqueId)) {
					val watchables = event.packet.watchableCollectionModifier.optionRead(0).orElseGet(null) ?: return
					if (watchables.isEmpty()) return
					val value = watchables[0]?.value as? Byte ?: return

					val clonedItem = DataWatcher.Item(DataWatcherObject(0, DataWatcherRegistry.a), value.or(0x40))
					watchables.add(0, WrappedWatchableObject(0, clonedItem))

					event.packet.watchableCollectionModifier.write(0, watchables)
				}

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
*/
	}
}
