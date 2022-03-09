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
import com.mojang.authlib.GameProfile
import net.kyori.adventure.text.format.TextColor
import net.minecraft.ChatFormatting.*
import net.minecraft.core.Registry
import net.minecraft.network.chat.TextComponent
import net.minecraft.network.protocol.game.*
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket.PlayerUpdate
import net.minecraft.network.syncher.*
import net.minecraft.world.Containers
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.scores.*
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.event.server.ServerListPingEvent
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
	fun metadataPacketDefaultState(player: Player): ClientboundSetEntityDataPacket {
		player as CraftPlayer

		return ClientboundSetEntityDataPacket(
			player.entityId,
			SynchedEntityData(player.handle),
			true
		)
	}

	fun initFakeTeam(
		teamPlayerUuid: UUID,
		teamPlayerRealName: String,
		teamPlayerIdName: String,
		sentPlayer: Player,
	) {
		fun createFakeTeam(name: String) {
			val sentTeam = PlayerTeam(Scoreboard(), name)
			sentTeam.players.add(name)

			(sentPlayer as CraftPlayer).handle.connection.send(
				ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(sentTeam, true)
			)
		}

		createFakeTeam(teamPlayerIdName)
		if (sentPlayer.uniqueId == teamPlayerUuid) createFakeTeam(teamPlayerRealName)
	}

	fun updateTeamColor(
		teamPlayer: Player,
		uhcTeam: AbstractTeam?,
		newName: String,
		sentPlayer: Player,
	) {
		sentPlayer as CraftPlayer
		val trueName = teamPlayer.name

		val arena = ArenaManager.playersArena(teamPlayer.uniqueId)

		val isOneSameTeam = when {
			teamPlayer.uniqueId == sentPlayer.uniqueId -> true
			arena == null -> uhcTeam?.members?.contains(sentPlayer.uniqueId) == true
			else -> arena is PvpArena && ArenaManager.playersTeam(arena, teamPlayer.uniqueId)
				?.contains(sentPlayer.uniqueId) == true
		}

		/* update fake scoreboard team */
		fun updateTeam(idName: String) {
			val playerTeam = PlayerTeam(Scoreboard(), idName)
			playerTeam.color = if (isOneSameTeam) AQUA else RED

			playerTeam.playerPrefix = if (uhcTeam != null) {
				Util.nmsGradientString(trueName, uhcTeam.colors[0], uhcTeam.colors[1])
			} else {
				TextComponent(trueName)
			}

			playerTeam.players.add(idName)

			sentPlayer.handle.connection.send(
				ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(playerTeam, true)
			)
		}

		updateTeam(newName)
		if (teamPlayer.uniqueId == sentPlayer.uniqueId) updateTeam(trueName)
	}

	fun init() {
		val protocolManager = ProtocolLibrary.getProtocolManager()

		protocolManager.addPacketListener(object :
			PacketAdapter(UHCPlugin.plugin, ListenerPriority.HIGH, PacketType.Play.Server.PLAYER_INFO) {
			override fun onPacketSending(event: PacketEvent) {
				/* this packet should not be modified */
				val oldPacket = event.packet.handle as ClientboundPlayerInfoPacket

				/* only look for add player packets */
				if (oldPacket.action != ClientboundPlayerInfoPacket.Action.ADD_PLAYER) return

				/* create a new packet to modify */
				val modifyPacket = ClientboundPlayerInfoPacket(
					oldPacket.action,
					oldPacket.entries.mapNotNull { oldEntry ->
						(Bukkit.getPlayer(oldEntry.profile.id) as? CraftPlayer)?.handle
					}
				)

				for (i in 0 until modifyPacket.entries.size) {
					val original = modifyPacket.entries[i]

					val idName = playersNewName(original.profile.id)

					modifyPacket.entries[i] = PlayerUpdate(
						GameProfile(original.profile.id, idName),
						original.latency,
						original.gameMode,
						original.displayName
					)

					initFakeTeam(
						original.profile.id,
						original.profile.name,
						idName,
						event.player
					)
				}

				/* the event now acts on the modify packet only for the sent player */
				event.packet = PacketContainer.fromPacket(modifyPacket)
			}
		})

		fun playerFromEntityId(id: Int): Player? {
			return Bukkit.getOnlinePlayers().find { player -> player.entityId == id }
		}

		protocolManager.addPacketListener(object :
			PacketAdapter(UHCPlugin.plugin, ListenerPriority.HIGH, PacketType.Play.Server.ENTITY_METADATA) {
			override fun onPacketSending(event: PacketEvent) {
				val originalPacket = event.packet.handle as ClientboundSetEntityDataPacket

				val dataList = originalPacket.unpackedData

				val dataPlayer = playerFromEntityId(originalPacket.id) ?: return

				val newData = SynchedEntityData((dataPlayer as CraftPlayer).handle)
				newData.assignValues(dataList)

				fun setGlowing() {
					val originalByte = newData.get(EntityDataAccessor(0, EntityDataSerializers.BYTE))
					newData.set(EntityDataAccessor(0, EntityDataSerializers.BYTE), originalByte.or(0x40))
				}

				val sentPlayer = event.player as CraftPlayer

				/* determine whether player should be glowing */

				/* only set when the game is going */
				val sentPlayerTeam = UHC.game?.teams?.playersTeam(sentPlayer.uniqueId)
				val metaPlayersArena = ArenaManager.playersArena(dataPlayer.uniqueId)

				/* glowing in games */
				if (metaPlayersArena is PvpArena) {
					/* if on same team as meta player (not same player), or if game is in glow phase */
					if (
						(
						dataPlayer.uniqueId != sentPlayer.uniqueId &&
						ArenaManager.playersTeam(metaPlayersArena, dataPlayer.uniqueId)
							?.contains(sentPlayer.uniqueId) == true
						) ||
						(metaPlayersArena.shouldGlow() && metaPlayersArena.teams.flatten()
							.contains(sentPlayer.uniqueId))
					) {
						setGlowing()
					}

					/* teammate glowing */
				} else if (
					UHC.game != null &&
					sentPlayer.uniqueId != dataPlayer.uniqueId &&
					sentPlayerTeam != null &&
					sentPlayerTeam.members.contains(dataPlayer.uniqueId)
				) {
					setGlowing()
				}

				event.packet = PacketContainer.fromPacket(ClientboundSetEntityDataPacket(
					originalPacket.id,
					newData,
					false
				))
			}
		})

		/* redirect hearts objective packets to target the fake player names */
		protocolManager.addPacketListener(object :
			PacketAdapter(UHCPlugin.plugin, ListenerPriority.HIGH, PacketType.Play.Server.SCOREBOARD_SCORE) {
			override fun onPacketSending(event: PacketEvent) {
				val packet = event.packet.handle as ClientboundSetScorePacket

				if (packet.objectiveName != UHC.heartsObjective.name) return
				val player = Bukkit.getPlayer(packet.owner) ?: return

				event.packet = PacketContainer.fromPacket(ClientboundSetScorePacket(
					packet.method,
					UHC.heartsObjective.name,
					playersNewName(player.uniqueId),
					packet.score
				))
			}
		})

		val enchantmentMenuId = 12//Registry.MENU.getId(MenuType.ENCHANTMENT) why doesn't this work?!

		/* set the name of every enchanting table gui */
		protocolManager.addPacketListener(object :
			PacketAdapter(UHCPlugin.plugin, ListenerPriority.HIGH, PacketType.Play.Server.OPEN_WINDOW) {
			override fun onPacketSending(event: PacketEvent) {
				val packet = event.packet.handle as ClientboundOpenScreenPacket

				/* enchantment table id: 12 */
				if (packet.containerId != enchantmentMenuId) return

				event.packet = PacketContainer.fromPacket(
					ClientboundOpenScreenPacket(
						packet.containerId,
						MenuType.ENCHANTMENT,
						Util.nmsGradientString("Replace Item to Cycle Enchants",
							TextColor.color(0xc40a0a),
							TextColor.color(0x820874))
					)
				)
			}
		})
	}
}
