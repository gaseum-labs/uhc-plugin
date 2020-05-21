package codeland.uhcplugin;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

public class UHCPlugin extends JavaPlugin {
	public static UHCPlugin UHC_PLUGIN;
	
	@Override
	public void onEnable() {
		getLogger().info("hewwo, world");
		
		UHC_PLUGIN = this;
		
		/* register event listeners */
		getServer().getPluginManager().registerEvents(new PlayerListener(), this);
	}
	
	@Override
	public void onDisable() {
	
	}
}
