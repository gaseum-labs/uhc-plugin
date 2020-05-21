package codeland.uhcplugin.metadata;

import codeland.uhcplugin.UHCPlugin;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.plugin.Plugin;

import java.util.List;

import static codeland.uhcplugin.UHCPlugin.UHC_PLUGIN;

public class MetaData {
	public static final String UHC_META_PREFIX = "CDLUHC";
	
	public static void setMetadata(Metadatable object, String key, Object value) {
		object.setMetadata(UHC_META_PREFIX + key, new FixedMetadataValue(UHC_PLUGIN, value));
	}
	
	private static MetadataValue getMetadata(Metadatable object, String key) {
		var values = object.getMetadata(key);
		
		if (values == null)
			return null;
		
		if (values.size() == 0)
			return null;
		
		return values.get(0);
	}
	
	public static int getMetaInt(Metadatable object, String key) {
		var value = getMetadata(object, key);
		
		return (value == null) ? 0 : value.asInt();
	}
	
	public static long getMetaLong(Metadatable object, String key) {
		var value = getMetadata(object, key);
		
		return (value == null) ? 0L : value.asLong();
	}
	
	public static Object getMetaObj(Metadatable object, String key) {
		var value = getMetadata(object, key);
		
		return (value == null) ? 0 : value.value();
	}
}
