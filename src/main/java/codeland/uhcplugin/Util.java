package codeland.uhcplugin;

import java.util.logging.Level;

import static org.bukkit.Bukkit.getLogger;

public class Util {
	public static void stackTrace(Exception ex) {
		getLogger().log(Level.INFO, ex.getMessage(), ex);
	}
}
