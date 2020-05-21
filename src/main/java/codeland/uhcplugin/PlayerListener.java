package codeland.uhcplugin;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import static org.bukkit.Bukkit.getLogger;

public class PlayerListener implements Listener {
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent evt) {
		var player = evt.getPlayer(); // The player who joined
		
		player.sendMessage("HEWWWWWWWWWWWWWWWWWWWO");
		
		player.getScoreboard();
		getLogger().info("PLAYER JOINED");
		
		
	}
}
