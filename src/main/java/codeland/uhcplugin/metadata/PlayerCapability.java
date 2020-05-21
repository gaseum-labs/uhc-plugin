package codeland.uhcplugin.metadata;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.entity.Player;

import java.util.Collection;

abstract public class PlayerCapability extends Capability<Player> {
	public PlayerCapability(String name, String filePath) {
		super(name, filePath);
	}
	
	@Override
	public void customWrite(Collection<? extends Player> players, JSON) {
		var json = new JsonObject();
		
		for (var player : players) {
			var playerJson = new JsonObject();
			
			save(player, playerJson);
			
			json.add(player.getName(), playerJson);
		}
	}
}
