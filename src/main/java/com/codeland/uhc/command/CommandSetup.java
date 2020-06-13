package com.codeland.uhc.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import com.codeland.uhc.core.GameRunner;
import com.codeland.uhc.core.UHC;
import com.destroystokyo.paper.utils.PaperPluginLogger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.logging.Level;

@CommandAlias("uhc")
@Description("Setup the UHC round")
public class CommandSetup extends BaseCommand {

    @Dependency
    private GameRunner gameRunner;

    @CommandAlias("setup")
    public boolean onSetup(CommandSender sender, double startRadius, double endRadius, double shrinkTime, double graceTime) {

        var uhc = new UHC(startRadius, endRadius, shrinkTime, graceTime);

        gameRunner.setUhc(uhc);

        PaperPluginLogger.getGlobal().log(Level.INFO, "SETUP COMMAND");
        return true;
    }
}
 