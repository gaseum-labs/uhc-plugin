package codeland.uhcplugin.metadata;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import org.bukkit.Server;
import org.bukkit.metadata.Metadatable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Collection;

import static codeland.uhcplugin.Util.stackTrace;
import static codeland.uhcplugin.metadata.MetaData.setMetadata;

abstract public class Capability<M extends Metadatable> {
	private String name;
	private String filePath;
	
	private JsonElement cache;
	
	public Capability(String name, String filePath) {
		this.name = name;
		this.filePath = filePath;
	}
	
	public void attach(M object) {
		if (cache == null) {
			if (!object.hasMetadata(name)) {
				setMetadata(object, name, true);
				
				init(object);
			}
		} else {
		
		}
	}
	
	abstract public void init(M object);
	
	public void write(Collection<? extends M> objects) {
		try {
			var json = new JsonObject();
			
			for (var object : objects) {
				perTypeWrite(object, json);
			}
			
			var writer = new FileWriter(new File(filePath));
			
			writer.write(json.toString());
			
			writer.close();
			
		} catch (Exception ex) {
			stackTrace(ex);
		}
	}
	
	abstract public void perTypeWrite(M object, JsonObject json);
	
	public void read(Server server) {
		try {
			var file = new File(filePath);
			
			var fileReader = new FileReader(file);
			var jsonParser = new JsonParser();
			
			cache = jsonParser.parse(fileReader);
			
		} catch (Exception ex) {
			stackTrace(ex);
		}
	}
	
	abstract public void save(M object, JsonObject json);
	abstract public void load(M object, JsonElement json);
}
