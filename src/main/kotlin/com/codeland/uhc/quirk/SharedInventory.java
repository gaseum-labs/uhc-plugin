package com.codeland.uhc.quirk;

import com.codeland.uhc.core.GameRunner;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SharedInventory extends Quirk {
    public SharedInventory(@NotNull QuirkType type) {
        super(type);
    }

    private ItemStack[] contents;
    private int taskId;

    @Override
    public void onEnable() {
        contents = Bukkit.getOnlinePlayers().iterator().next().getInventory().getContents();
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(GameRunner.plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                ItemStack[] playersContents = player.getInventory().getContents();
                if (!contentsSimilar(playersContents, contents)) {
                    contents = contentsCopy(playersContents);
                    for (Player other : Bukkit.getOnlinePlayers()) {
                        if (other != player) other.getInventory().setContents(contents);
                    }
                    break;
                }
            }
        }, 1, 1);
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTask(taskId);
    }

    private boolean contentsSimilar(ItemStack[] contents1, ItemStack[] contents2) {
        for (int i = 0; i < contents1.length; i++) {
            // required because java dumb
            if (contents1[i] == null && contents2[i] != null) return false;
            if (contents1[i] != null && contents2[i] == null) return false;
            if (contents1[i] == null && contents2[i] == null) continue;
            if (!contents1[i].equals(contents2[i])) return false;
        }
        return true;
    }

    private ItemStack[] contentsCopy(ItemStack[] contents) {
        ItemStack[] newContents = new ItemStack[contents.length];
        for (int i = 0; i < contents.length; i++) {
            newContents[i] = contents[i].clone();
        }
        return newContents;
    }
}
