package com.codeland.uhc;

import com.codeland.uhc.command.CommandSetup;
import org.bukkit.plugin.java.JavaPlugin;

public class UHCPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getCommand("setup").setExecutor(new CommandSetup());
    }

    @Override
    public void onDisable() {

    }
}
