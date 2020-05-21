package codeland.uhcplugin.metadata;

import org.bukkit.metadata.Metadatable;

import static codeland.uhcplugin.metadata.MetaData.*;

public class MetaPart<Type> {
	String tag;
	
	public MetaPart(String tag) {
		this.tag = tag;
	}
	
	public Type get(Metadatable object) {
		return (Type)getMetaObj(object, tag);
	}
	
	public void set(Metadatable object, Type value) {
		setMetadata(object, tag, value);
	}
	
	public void save() {
	
	}
	
	public void read() {
	
	}
}
