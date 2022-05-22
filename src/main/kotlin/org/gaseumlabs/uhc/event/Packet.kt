package org.gaseumlabs.uhc.event

import org.gaseumlabs.uhc.UHCPlugin
import org.gaseumlabs.uhc.core.UHC
import org.gaseumlabs.uhc.lobbyPvp.ArenaManager
import org.gaseumlabs.uhc.lobbyPvp.arena.PvpArena
import org.gaseumlabs.uhc.team.AbstractTeam
import org.gaseumlabs.uhc.util.Util
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.*
import com.mojang.authlib.GameProfile
import net.minecraft.ChatFormatting.*
import net.minecraft.core.Registry
import net.minecraft.network.chat.*
import net.minecraft.network.protocol.game.*
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket.Action.UPDATE_DISPLAY_NAME
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket.PlayerUpdate
import net.minecraft.network.syncher.*
import net.minecraft.network.syncher.SynchedEntityData.DataItem
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.scores.*
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer
import org.bukkit.entity.Player
import org.gaseumlabs.uhc.util.reflect.UHCReflect
import java.util.*
import kotlin.collections.ArrayList
import kotlin.experimental.or

object Packet {
	private val playerNames = arrayListOf<UUID>()

	private fun intToName(int: Int, length: Int): String {
		var countDown = int

		val name = CharArray(length * 2)

		for (i in 0 until length) {
			name[i * 2] = ChatColor.COLOR_CHAR
			name[i * 2 + 1] = ChatColor.values()[countDown % 10].char
			countDown /= 10
		}

		return String(name)
	}

	private fun nameToInt(name: String, length: Int): Int? {
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

	private fun playersIndex(uuid: UUID): Int {
		var nameIndex = playerNames.indexOf(uuid)

		if (nameIndex == -1) {
			playerNames.add(uuid)
			nameIndex = playerNames.lastIndex
		}

		return nameIndex
	}

	private fun createNominalTeam(
		sendPlayer: CraftPlayer,
		teamPlayerName: String,
	) {
		val sentTeam = PlayerTeam(Scoreboard(), teamPlayerName)
		sentTeam.players.add(teamPlayerName)

		sendPlayer.handle.connection.send(
			ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(sentTeam, true)
		)
	}

	private fun coloredNameComponent(playerRealName: String, team: AbstractTeam?): Component {
		return if (team != null) {
			Util.nmsGradientString(
				playerRealName,
				team.colors[0].value(),
				team.colors[1].value(),
			)
		} else {
			TextComponent(playerRealName).setStyle(Style.EMPTY.withColor(0xffffff))
		}
	}

	/* INTERFACE */

	fun playersIdName(uuid: UUID): String {
		return intToName(playersIndex(uuid), 8)
	}

	fun playersMetadataPacket(player: Player): ClientboundSetEntityDataPacket {
		player as CraftPlayer

		return ClientboundSetEntityDataPacket(
			player.entityId,
			SynchedEntityData(player.handle),
			true
		)
	}

	fun updateNominalTeamColor(
		sendPlayer: CraftPlayer,
		teamPlayer: CraftPlayer,
		teamPlayerIdName: String,
		teamPlayerTeam: AbstractTeam?,
	) {
		val teamPlayerRealName = teamPlayer.name

		/* only update a self-player's display name */
		if (sendPlayer.uniqueId == teamPlayer.uniqueId) {
			val updateDisplayNamePacket = ClientboundPlayerInfoPacket(UPDATE_DISPLAY_NAME, sendPlayer.handle)

			val originalEntry = updateDisplayNamePacket.entries[0]
			updateDisplayNamePacket.entries[0] = PlayerUpdate(
				originalEntry.profile,
				originalEntry.latency,
				originalEntry.gameMode,
				coloredNameComponent(teamPlayerRealName, teamPlayerTeam),
			)

			sendPlayer.handle.connection.send(updateDisplayNamePacket)

			/* make player's self nominal team blue */
			val nominalTeam = PlayerTeam(Scoreboard(), teamPlayerRealName)
			nominalTeam.color = AQUA
			nominalTeam.players.add(teamPlayerRealName)

			sendPlayer.handle.connection.send(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(nominalTeam, true))

		} else {
			//TODO delegate this out to code somewhere else
			val arena = ArenaManager.playersArena(teamPlayer.uniqueId)
			val isOnSameTeam = if (arena == null) {
				teamPlayerTeam?.members?.contains(sendPlayer.uniqueId) == true
			} else {
				arena is PvpArena && ArenaManager.playersTeam(arena, teamPlayer.uniqueId)
					?.contains(sendPlayer.uniqueId) == true
			}

			val nominalTeam = PlayerTeam(Scoreboard(), teamPlayerIdName)

			nominalTeam.color = if (isOnSameTeam) AQUA else RED
			nominalTeam.playerPrefix = coloredNameComponent(teamPlayerRealName, teamPlayerTeam)
			nominalTeam.players.add(teamPlayerIdName)

			sendPlayer.handle.connection.send(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(nominalTeam, true))
		}
	}

	/* INIT */

	fun registerListeners() {
		val protocolManager = ProtocolLibrary.getProtocolManager()

		/* set players id names when first seen */
		protocolManager.addPacketListener(object :
			PacketAdapter(UHCPlugin.plugin, ListenerPriority.HIGH, PacketType.Play.Server.PLAYER_INFO) {
			override fun onPacketSending(event: PacketEvent) {
				val sendPlayer = event.player as CraftPlayer
				val oldPacket = event.packet.handle as ClientboundPlayerInfoPacket

				/* only look for add player packets */
				if (oldPacket.action != ClientboundPlayerInfoPacket.Action.ADD_PLAYER) return

				/* new packet with game profiles to be modified */
				/* have to create it first with real player data since that's the only constructor */
				val modifiedPacket = ClientboundPlayerInfoPacket(
					ClientboundPlayerInfoPacket.Action.ADD_PLAYER,
					oldPacket.entries.mapNotNull { oldEntry ->
						(Bukkit.getPlayer(oldEntry.profile.id) as? CraftPlayer)?.handle
					}
				)

				/* replace PlayerUpdates inside packet with new ones which have the id names */
				/* except that players will always know themselves by their real name */
				for (i in modifiedPacket.entries.indices) {
					val oldEntry = modifiedPacket.entries[i]

					if (oldEntry.profile.id == sendPlayer.uniqueId) {
						createNominalTeam(sendPlayer, oldEntry.profile.name)
					} else {
						val idName = playersIdName(oldEntry.profile.id)

						modifiedPacket.entries[i] = PlayerUpdate(
							GameProfile(oldEntry.profile.id, idName),
							oldEntry.latency,
							oldEntry.gameMode,
							oldEntry.displayName
						)

						createNominalTeam(sendPlayer, idName)
					}
				}

				event.packet = PacketContainer.fromPacket(modifiedPacket)
			}
		})

		fun playerFromEntityId(id: Int): Player? {
			return Bukkit.getOnlinePlayers().find { player -> player.entityId == id }
		}

		//TODO move this block somewhere else
		fun playerShouldGlow(sendPlayer: Player, dataPlayer: Player): Boolean {
			val metaPlayersArena = ArenaManager.playersArena(dataPlayer.uniqueId)

			if (sendPlayer.uniqueId == dataPlayer.uniqueId) return false

			/* arena glowing */
			return if (metaPlayersArena is PvpArena) {
				ArenaManager.playersTeam(
					metaPlayersArena,
					dataPlayer.uniqueId
				)?.contains(sendPlayer.uniqueId) == true || (
				metaPlayersArena.shouldGlow() &&
				metaPlayersArena.teams.flatten().contains(sendPlayer.uniqueId)
				)

			} else {
				/* teammate glowing */
				val sendPlayerTeam = UHC.game?.teams?.playersTeam(sendPlayer.uniqueId)

				sendPlayerTeam != null &&
				sendPlayerTeam.members.contains(dataPlayer.uniqueId)
			}
		}

		val packedItemsField = UHCReflect<ClientboundSetEntityDataPacket, List<DataItem<*>>>(
			ClientboundSetEntityDataPacket::class,
			"packedItems"
		)

		/* make players glow when they should */
		protocolManager.addPacketListener(object :
			PacketAdapter(UHCPlugin.plugin, ListenerPriority.HIGH, PacketType.Play.Server.ENTITY_METADATA) {
			override fun onPacketSending(event: PacketEvent) {
				val sendPlayer = event.player as CraftPlayer
				val oldPacket = event.packet.handle as ClientboundSetEntityDataPacket

				val oldDataList = oldPacket.unpackedData ?: return
				val dataPlayer = playerFromEntityId(oldPacket.id) ?: return

				val modifiedEntityData = SynchedEntityData((dataPlayer as CraftPlayer).handle)
				val modifiedPacket = ClientboundSetEntityDataPacket(
					oldPacket.id,
					modifiedEntityData,
					false
				)
				packedItemsField.set(modifiedPacket, ArrayList())

				for (oldDataEntry in oldDataList) {
					/* modify the byte flag */
					val value = if (oldDataEntry.accessor.id == 0 && playerShouldGlow(sendPlayer, dataPlayer)) {
						(oldDataEntry.value as Byte).or(0x40)
					} else {
						oldDataEntry.value
					}

					modifiedPacket.unpackedData?.add(DataItem(oldDataEntry.accessor as EntityDataAccessor<Any>, value))
				}

				event.packet = PacketContainer.fromPacket(modifiedPacket)
			}
		})

		/* redirect hearts objective packets to target the fake player names */
		protocolManager.addPacketListener(object :
			PacketAdapter(UHCPlugin.plugin, ListenerPriority.HIGH, PacketType.Play.Server.SCOREBOARD_SCORE) {
			override fun onPacketSending(event: PacketEvent) {
				val sendPlayer = event.player
				val packet = event.packet.handle as ClientboundSetScorePacket

				if (packet.objectiveName != UHC.heartsObjective.name) return
				val objectivePlayer = Bukkit.getPlayer(packet.owner) ?: return

				/* players know themselves on the scoreboard by their own names */
				if (sendPlayer.uniqueId == objectivePlayer.uniqueId) return

				event.packet = PacketContainer.fromPacket(ClientboundSetScorePacket(
					packet.method,
					UHC.heartsObjective.name,
					playersIdName(objectivePlayer.uniqueId),
					packet.score
				))
			}
		})

		//val enchantmentMenuId = Registry.MENU.getId(MenuType.ENCHANTMENT)

		/* set the name of every enchanting table gui */
		/* QUARANTIED */
		//protocolManager.addPacketListener(object :
		//	PacketAdapter(UHCPlugin.plugin, ListenerPriority.HIGH, PacketType.Play.Server.OPEN_WINDOW) {
		//	override fun onPacketSending(event: PacketEvent) {
		//		val packet = event.packet.handle as ClientboundOpenScreenPacket
//
		//		/* enchantment table id: 12 */
		//		if (packet.containerId != enchantmentMenuId) return
//
		//		event.packet = PacketContainer.fromPacket(
		//			ClientboundOpenScreenPacket(
		//				packet.containerId,
		//				MenuType.ENCHANTMENT,
		//				Util.nmsGradientString("Replace Item to Cycle Enchants", 0xc40a0a, 0x820874)
		//			)
		//		)
		//	}
		//})
	}
}
