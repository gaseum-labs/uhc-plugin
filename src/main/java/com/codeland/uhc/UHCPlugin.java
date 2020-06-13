package com.codeland.uhc;

import co.aikar.commands.PaperCommandManager;
import com.codeland.uhc.command.CommandSetup;
import com.codeland.uhc.core.GameRunner;
import org.bukkit.plugin.java.JavaPlugin;

public class UHCPlugin extends JavaPlugin {

    PaperCommandManager commandManager;

    GameRunner gameRunner = new GameRunner();

    @Override
    public void onEnable() {
        commandManager = new PaperCommandManager(this);

        commandManager.registerDependency(GameRunner.class, gameRunner);

        commandManager.registerCommand(new CommandSetup());
    }

    @Override
    public void onDisable() {

    }
}
