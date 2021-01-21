package net.obmc.OBChestShop;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;
import net.obmc.OBChestShop.Commands.OnCommand;
import net.obmc.OBChestShop.Listeners.MenuAction;
import net.obmc.OBChestShop.Listeners.ShopCreation;
import net.obmc.OBChestShop.Listeners.ShopOpen;
import net.obmc.OBChestShop.Listeners.ShopRemoval;
import net.obmc.OBChestShop.Tasks.ShopChecker;

public class OBChestShop extends JavaPlugin {

	static Logger log = Logger.getLogger("Minecraft");

	private static OBChestShop instance;
	private static String plugin = "OBChestShop";
	private static String pluginprefix = "[" + plugin + "]";
	private static String chatmsgprefix = ChatColor.AQUA + "" + ChatColor.BOLD + plugin + ChatColor.DARK_GRAY + ChatColor.BOLD + " » " + ChatColor.LIGHT_PURPLE + "";
	private static String logmsgprefix = pluginprefix + " » ";
	private static LiveShopList shoplist = new LiveShopList();
	private static PluginConfig config = new PluginConfig();
	private static Economy economy = null;

	public OBChestShop() {
		
		instance = this;
		
		if (config == null) {
			config = new PluginConfig();
		}
		
		// create our live shop list
		if (shoplist == null) {
			shoplist = new LiveShopList();
		}
	}
	
	@Override
    public void onEnable() {

		// check dependencies
		if (!setupEconomy()) {
			instance.setEnabled(false);
			return;
		}
		log.log(Level.INFO, getLogMsgPrefix() + "    Vault & Economy are available");
	
		
		// load config
		if (!config.load()) {
			instance.setEnabled(false);
			return;
		}

		// register listeners
		if (!Register()) {
			log.log(Level.INFO, getLogMsgPrefix() + "    Error enabling listeners");
			instance.setEnabled(false);
            return;
		}
		
		// load shops
		if (!shoplist.loadShops()) {
			log.log(Level.INFO, getLogMsgPrefix() + "    Error loading shops");
			instance.setEnabled(false);
			return;
		}

		// setup our shop checker task
		BukkitTask checkShops = new ShopChecker(instance).runTaskTimer(instance, instance.getConfig().getLong("shopcheckinterval"), instance.getConfig().getLong("shopcheckinterval"));
		log.log(Level.INFO, getLogMsgPrefix() + "    ShopChecker initialized (taskid " + checkShops.getTaskId() + ")");

	}
	
	// setup vault economy
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
			log.log(Level.INFO, getLogMsgPrefix() + "    Vault plugin not available!");
			log.log(Level.INFO, getLogMsgPrefix() + "    This is a required plugin for " + plugin + " to work");
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
			log.log(Level.INFO, getLogMsgPrefix() + "    Economy plugin not available!");
			log.log(Level.INFO, getLogMsgPrefix() + "    Vault requires an economy plugin, like Essentials, to function properly");
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

	@Override
    public void onDisable() {
		
		// save out shops - no need to do this as we save on every change
		// might come back to this later and perform saves only on specific changes and a final save
		if (!shoplist.saveShops()) {
			log.log(Level.INFO, getLogMsgPrefix() + "    Errors encountered saving shops");
		}
		
		// cancel any background tasks for this plugin
		Bukkit.getScheduler().cancelTasks(this);
		
		log.log(Level.INFO, getLogMsgPrefix() + "    Plugin successfully disabled");
	}
	
	// register listeners
	private boolean Register() {
		getServer().getPluginManager().registerEvents(new ShopCreation(), instance);
		getServer().getPluginManager().registerEvents(new ShopRemoval(), instance);
		getServer().getPluginManager().registerEvents(new ShopOpen(), instance);
		getServer().getPluginManager().registerEvents(new MenuAction(), instance);
        this.getCommand("obshop").setExecutor(new OnCommand());
		return true;
	}

	// return our plugin instance
	public static OBChestShop getInstance() {
        return instance;
    }
	
	// access to the shop list from other classes
	public static LiveShopList getShopList() {
		return shoplist;
	}

	// access to the config from other classes
	public static PluginConfig getPluginConfig() {
		return config;
	}
	
	// consistent messaging
	public static String getPluginName() {
		return plugin;
	}
	public static String getPluginPrefix() {
		return pluginprefix;
	}
	public static String getChatMsgPrefix() {
		return chatmsgprefix;
	}
	public static String getLogMsgPrefix() {
		return logmsgprefix;
	}

	// enable/disable auto fix
	public static void enableAutoFix() {
		if (!instance.getConfig().getBoolean("shopautofix")) {
			instance.getConfig().set("shopautofix", true);
			instance.saveConfig();
		}
	}
	public static void disableAutoFix() {
		if (instance.getConfig().getBoolean("shopautofix")) {
			instance.getConfig().set("shopautofix", false);
			instance.saveConfig();
		}
	}
	public static boolean getAutoFixState() {
		return instance.getConfig().getBoolean("shopautofix");
	}

	public static Plugin getPlugin() {
		return Bukkit.getPluginManager().getPlugin(plugin);
	}
	
    public static Economy getEconomy() {
    	return economy;
    }
}
