package net.obmc.OBChestShop;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.file.YamlConfiguration;

public class PluginConfig {

	Logger log = Logger.getLogger("Minecraft");

	private File configFile = new File("plugins/" + OBChestShop.getPluginName() + "/config.yml");
	private YamlConfiguration config = new YamlConfiguration();

	//creates a new ConfigLoader, for handling default config.yml
	public PluginConfig() {
		if (configFile == null) {
			configFile = new File("plugins/" + OBChestShop.getPluginName() + "/config.yml");
		}
		if (config == null) {
			config = new YamlConfiguration();
		}
	}

	// returns the current in memory/loaded config
	public YamlConfiguration getConfig() {
		return config;
	}

	// save our config out
	public boolean save() {
		//TODO: switch to plugin saveConfig() ?
    	log.log(Level.INFO, OBChestShop.getLogMsgPrefix() + "    Saving configuration");
		try {
			config.save(configFile);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	// load config into memory from file
	public boolean load() {
		// Create target and copy res-content
		if (!this.configFile.exists()) {
			OBChestShop.getInstance().saveDefaultConfig();
			log.log(Level.INFO, OBChestShop.getLogMsgPrefix() + "    New config.yml has been created.");
		}
		OBChestShop.getInstance().getConfig();
		return true;
	}


}