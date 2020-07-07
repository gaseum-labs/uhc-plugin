package com.codeland.uhc.command

import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.scoreboard.Scoreboard
import org.bukkit.scoreboard.Team

object TeamData {
    val teamColours = arrayOf<ChatColor>(
            ChatColor.BLUE,
            ChatColor.RED,
            ChatColor.GREEN,
            ChatColor.AQUA,
            ChatColor.LIGHT_PURPLE,
            ChatColor.YELLOW,
            ChatColor.DARK_RED,
            ChatColor.DARK_AQUA,
            ChatColor.DARK_PURPLE,
            ChatColor.GRAY,
            ChatColor.DARK_BLUE,
            ChatColor.DARK_GREEN,
            ChatColor.DARK_GRAY
    );

    val colorPrettyNames = arrayOf<String>(
            "Black",
            "Dark Blue",
            "Dark Green",
            "Dark Aqua",
            "Dark Red",
            "Dark Purple",
            "Gold",
            "Gray",
            "Dark Gray",
            "Blue",
            "Green",
            "Aqua",
            "Red",
            "Light Purple",
            "Yellow",
            "White",
            "Magic",
            "Bold",
            "Strike",
            "Underline",
            "Italic",
            "Reset"
    );

    /**
     * prevents non-color font modifiers and white, black, and gold
     */
    fun isValidColor(color: ChatColor): Boolean {
        return color.isColor && color != ChatColor.WHITE && color != ChatColor.BLACK && color != ChatColor.GOLD;
    }

    fun prettyTeamName(color: ChatColor): String {
        return "Team ${TeamData.colorPrettyNames[color.ordinal]}";
    }

    fun errorMessage(sender: CommandSender, text: String) {
        val message = TextComponent(text);
        message.color = ChatColor.RED.asBungee();
        message.isBold = true;

        sender.sendMessage(message);
    }

    /**
     * returns if the sender cannot use this command
     * you should return from original function if true
     */
    fun opGuard(sender: CommandSender): Boolean {
        if (!sender.isOp) {
            errorMessage(sender, "You must be a server operator to use this command!");

            return true;
        }

        return false;
    }

    fun addToTeam(scoreboard: Scoreboard, color: ChatColor, playerName: String) {
        /* remove player from old team if they are on one */
        val oldTeam = scoreboard.getEntryTeam(playerName);

        if (oldTeam != null)
            removeFromTeam(oldTeam, playerName);

        /* find if the new team exists */
        val teamName = color.name;
        var team = scoreboard.getTeam(teamName);

        /* create the team if it doesn't exist */
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
            team.color = color;
            team.displayName = prettyTeamName(color);
        }

        team.addEntry(playerName);
    }

    fun removeFromTeam(team: Team, playerName: String) {
        team.removeEntry(playerName);

        /* remove the team if no one is left on it */
        if (team.entries.size == 0)
            team.unregister();
    }
}