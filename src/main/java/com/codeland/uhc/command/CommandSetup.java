package com.codeland.uhc.command;

import com.destroystokyo.paper.utils.PaperPluginLogger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.logging.Level;

public class CommandSetup implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        PaperPluginLogger.getGlobal().log(Level.INFO, "SETUP COMMAND");
        return true;
    }
}
