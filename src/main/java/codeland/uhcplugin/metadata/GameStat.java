package codeland.uhcplugin.metadata;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.metadata.Metadatable;

import java.util.Vector;

import static codeland.uhcplugin.metadata.MetaData.setMetadata;

public class GameStat extends Capability<Player> {
	public static final MetaPart<Boolean> participating = new MetaPart<>("participating");
	public static final MetaPart<Boolean> alive = new MetaPart<>("alive");
	public static final MetaPart<Location> pausePosition = new MetaPart<>("pausePosition");
	
	public GameStat() {
		super("gamestat", "uhcdata/gamestats.json");
	}
	
	@Override
	public void init(Player player) {
		participating.set(player, true);
		alive.set(player, true);
		pausePosition.set(player, player.getLocation());
	}
}
