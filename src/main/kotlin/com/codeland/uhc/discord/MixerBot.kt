package com.codeland.uhc.discord

import com.destroystokyo.paper.utils.PaperPluginLogger
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.bukkit.scoreboard.Team
import java.io.*
import java.util.*
import java.util.logging.Level

class MixerBot(userDataPath: String, ip: String) : ListenerAdapter() {
	private val mixerBot: JDA
	//private val channelIDs = ArrayList<Long>()
	private val mcUserNames: ArrayList<String> = ArrayList()
	private val discordIDs: ArrayList<Long> = ArrayList()
	private val userDataPath: String = userDataPath

	fun createTeam(team : Team) {
		getTeamChannel(team)
	}

	fun destroyTeam(team : Team) {
		val vc: VoiceChannel = getTeamChannel(team)
		val members: List<Member> = vc.members
		val general: VoiceChannel = mixerBot.getVoiceChannelById(GENERAL_VOICE_CHANNEL_ID)!!
		for (m in members) {
			vc.guild.moveVoiceMember(m, general).complete()
		}
		vc.delete().complete()
	}

	fun addPlayerToTeam(team: Team, name: String) {
		val g: Guild = mixerBot.getGuildById(UHC_GUILD_ID)!!
		for (i in mcUserNames.indices) {
			if (mcUserNames[i] == name) {
				val m: Member? = g.getMemberById(discordIDs[i])
				if (m?.voiceState?.inVoiceChannel() == true) {
					g.moveVoiceMember(m, getTeamChannel(team)).complete()
				}
			}
		}
	}

	fun renameTeam(team: Team, newName: String) {
		val vc = getTeamChannel(team)
		vc.manager.setName(newName).complete()
	}

	fun moveToGeneral(playerUsername: String) {
		for (i in mcUserNames.indices) {
			if (mcUserNames[i] == playerUsername) {
				val targ: VoiceChannel = mixerBot.getVoiceChannelById(GENERAL_VOICE_CHANNEL_ID)!!
				val g: Guild = targ.guild
				val m: Member? = g.getMemberById(discordIDs[i])
				if (m?.voiceState?.inVoiceChannel() == true) {
					g.moveVoiceMember(m, targ).complete()
				}
				return
			}
		}
	}

	fun isLinked(user: String): Boolean {
		for (uname in mcUserNames) {
			if (uname == user) {
				return true
			}
		}
		return false
	}

	fun sendGameSummary(losers: Array<String>, winners: Array<String>, channelID: Long) {
		val sb = StringBuilder()
		if (winners.size > 1) {
			sb.append("UHC Completed! Winners are ")
		} else {
			sb.append("UHC Completed! The winner is ")
		}
		sb.append("**").append(winners[0]).append("**")
		for (i in 1 until winners.size - 1) {
			sb.append(", **").append(winners[i]).append("**")
		}
		if (winners.size > 1) {
			sb.append(" and **").append(winners[winners.size - 1]).append("**")
		}
		sb.append("\n\nThese are the other participants and their place\n")
		for (i in losers.indices) {
			sb.append(i + winners.size + 1).append(": ").append(losers[i]).append("\n")
		}
		val msg = sb.toString()
		val tc: TextChannel = mixerBot.getTextChannelById(channelID)!!
		tc.sendMessage(msg).complete()
	}//not used for now

	fun getTeamChannel(team : Team): VoiceChannel {
		val cat: Category = mixerBot.getCategoryById(VOICE_CHANNEL_CATEGORY_ID)!!
		val channels: List<VoiceChannel> = cat.voiceChannels
		for (c in channels) {
			if (c.name == team.displayName) {
				return c
			}
		}
		return cat.createVoiceChannel(team.displayName).complete()
	}

	fun clearTeamVCs() {
		val vcs: List<VoiceChannel> = mixerBot.getCategoryById(VOICE_CHANNEL_CATEGORY_ID)!!.voiceChannels
		for (vc in vcs) {
			if (vc.idLong != GENERAL_VOICE_CHANNEL_ID) {
				vc.delete().complete()
			}
		}
	}

	fun updateRankings(players: ArrayList<ScoredPlayer>) {
		val msg = StringBuilder()
		msg.append("> ").append(1).append(". ").append(players[0].username).append("\n")
		msg.append("> score: ").append(players[0].score).append("\n")
		msg.append("> games played: ").append(players[0].gameCount)
		for (i in 1 until players.size) {
			msg.append("\n\n")
			msg.append("> ").append(i + 1).append(". ").append(players[i].username).append("\n")
			msg.append("> score: ").append(players[i].score).append("\n")
			msg.append("> games played: ").append(players[i].gameCount)
		}
		val pc: TextChannel? = mixerBot.getTextChannelById(704143873823342613L)
		if (pc == null) {
			println("channel ID is null")
		} else {
			pc.editMessageById(704144607876874340L, msg.toString()).complete()
		}
	}

	override fun onPrivateMessageReceived(event: PrivateMessageReceivedEvent) {
		val fromUserID: Long = event.author.idLong
		if (fromUserID == 244188985138741249L || fromUserID == 258485243038662657L) { //from varas or balduvian
			if (event.message.contentRaw == "die") {
				clearTeamVCs()
				close()
			} else if (event.message.contentRaw == "kill teams") {
				clearTeamVCs()
			}
		}
	}

	override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
		val message: String = event.message.contentRaw
		if (!message.startsWith("%")) {
			return
		}
		val userID: Long = event.author.idLong
		if (message.startsWith("%link ")) {
			val username = message.substring(1 + message.lastIndexOf(' '))
			for (i in discordIDs.indices) {
				if (discordIDs[i] == userID) {
					event.channel.sendMessage("changed minecraft username from \"" + mcUserNames[i] + "\" to \"" + username + "\"").complete()
					mcUserNames[i] = username
					reSaveUsernames()
					return
				}
			}
			mcUserNames.add(username)
			discordIDs.add(userID)
			event.channel.sendMessage("set minecraft username to \"$username\"").complete()
			try {
				val fr = FileWriter(File(userDataPath), true)
				fr.write("\n$username $userID")
				fr.close()
			} catch (e: IOException) {
				e.printStackTrace()
			}
		}
	}

	private fun close() {
		mixerBot.shutdownNow()
	}

	private fun getDiscordID(username: String): Long {
		for (i in mcUserNames.indices) {
			if (username == mcUserNames[i]) {
				return discordIDs[i]
			}
		}
		return -1
	}

	private fun reSaveUsernames() {
		try {
			val fr = FileWriter(File(userDataPath), false)
			fr.write(mixerBot.token)
			for (i in mcUserNames.indices) {
				fr.write("${mcUserNames[i]} ${discordIDs[i]}")
			}
			fr.close()
		} catch (e: IOException) {
			e.printStackTrace()
		}
	}

	private fun stripTeams(m: Member) {
		for (r in m.roles) {
			if (r.name.startsWith("Team ")) {
				m.guild.removeRoleFromMember(m, r).complete()
			}
		}
	}//not used for now

	companion object {
		const val UHC_GUILD_ID = 698352624063217725L
		const val VOICE_CHANNEL_CATEGORY_ID = 698352624604414024L
		const val GENERAL_VOICE_CHANNEL_ID = 698352624604414027L
	}

	init {
		val reader = BufferedReader(FileReader(userDataPath))
		val token = reader.readLine()
		mixerBot = JDABuilder().setToken(token).setActivity(Activity.playing("UHC at $ip")).addEventListeners(this).build()
		while (reader.ready()) {
			val line = reader.readLine()
			if (line.contains(" ")) {
				mcUserNames.add(line.substring(0, line.indexOf(" ")))
				discordIDs.add(line.substring(line.lastIndexOf(" ") + 1).toLong())
			}
		}
		reader.close()
		mixerBot.awaitReady()
	}
}