package com.codeland.uhc.event

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.team.TeamData
import com.codeland.uhc.util.Util
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.async.AsyncMarker
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.EnumWrappers
import com.comphenix.protocol.wrappers.PlayerInfoData
import net.minecraft.server.v1_16_R3.*
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer
import java.util.*
import kotlin.experimental.or
import kotlin.math.ceil

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

	fun splitName(name: String): Pair<String, String> {
		val mid: Int = ceil(name.length / 2.0).toInt()
		return Pair(name.substring(0, mid), name.substring(mid))
	}

	fun init() {
		val protocolManager = ProtocolLibrary.getProtocolManager()

		protocolManager.addPacketListener(object : PacketAdapter(UHCPlugin.plugin, ListenerPriority.HIGH, PacketType.Play.Server.PLAYER_INFO) {
			override fun onPacketSending(event: PacketEvent) {
				val packet = event.packet

				if (packet.playerInfoAction.read(0) != EnumWrappers.PlayerInfoAction.ADD_PLAYER) return

				val sentPlayer = event.player as CraftPlayer

				packet.playerInfoDataLists.write(0, packet.playerInfoDataLists.read(0).map { playerInfoData ->
					val uuid = playerInfoData.profile.uuid
					val uhcTeam = TeamData.playersTeam(uuid)

					if (uhcTeam == null) {
						/* do not change info about the player */
						playerInfoData

					} else {
						var nameIndex = playerNames.indexOf(uuid)

						if (nameIndex == -1) {
							playerNames.add(uuid)
							nameIndex = playerNames.size - 1
						}

						val oldName = playerInfoData.profile.name
						val (oldNamePrefix, oldNameSuffix) = splitName(oldName)
						val newName = intToName(nameIndex)

						/* send team packet */
						val team = ScoreboardTeam(Scoreboard(), newName)
						team.color = EnumChatFormat.values()[uhcTeam.colorPair.color0.ordinal]
						team.prefix = ChatMessage("${uhcTeam.colorPair.color0}${oldNamePrefix}")
						team.suffix = ChatMessage("${uhcTeam.colorPair.color1 ?: uhcTeam.colorPair.color0}${oldNameSuffix}")
						team.playerNameSet.add(newName)

						/* remove team then re-send */
						sentPlayer.handle.playerConnection.sendPacket(PacketPlayOutScoreboardTeam(team, 1))
						sentPlayer.handle.playerConnection.sendPacket(PacketPlayOutScoreboardTeam(team, 0))

						/* send another fake team targeting the self-player's entity */
						if (sentPlayer.uniqueId == uuid) {
							val selfTeam = ScoreboardTeam(Scoreboard(), oldName)
							selfTeam.color = EnumChatFormat.values()[uhcTeam.colorPair.color0.ordinal]
							selfTeam.playerNameSet.add(oldName)

							sentPlayer.handle.playerConnection.sendPacket(PacketPlayOutScoreboardTeam(selfTeam, 1))
							sentPlayer.handle.playerConnection.sendPacket(PacketPlayOutScoreboardTeam(selfTeam, 0))
						}
						/* ---------------- */

						val customProfile = playerInfoData.profile.withName(newName)

						PlayerInfoData(customProfile, playerInfoData.latency, playerInfoData.gameMode, playerInfoData.displayName)
					}
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
				event.packet = event.packet.deepClone()

				val sentPlayer = event.player as CraftPlayer
				val packet = event.packet.handle as PacketPlayOutEntityMetadata

				val metaPlayerID = packetEntityIdField.getInt(packet)
				val metaPlayer = Bukkit.getOnlinePlayers().find { it.entityId == metaPlayerID } as CraftPlayer? ?: return

				val sentPlayerTeam = TeamData.playersTeam(sentPlayer.uniqueId)

				if (sentPlayer.entityId != metaPlayerID && sentPlayerTeam != null && sentPlayerTeam.members.contains(metaPlayer.uniqueId)) {
					val itemList = packetItemListField.get(packet) as MutableList<DataWatcher.Item<Any>>
					val value = itemValueField.get(itemList[0]) as? Byte ?: return
					itemValueField.set(itemList[0], value.or(0x40))
				}
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
