package net.obmc.OBChestShop.Shop;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.data.type.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.obmc.OBChestShop.OBChestShop;
import net.obmc.OBChestShop.ShopStates.ShopState;
import net.obmc.OBChestShop.ShopItem.ShopItem;

public class Shop {

	Logger log = Logger.getLogger("Minecraft");

	private YamlConfiguration shopconfig;
    private File shopfile;
    private String shopowner;
    private String shopname;
    private String description;
    private Block chestblock;
    private Map<String, Object> chestdata;
    private Location chestloc;
    private Block signblock;
    private Map<String, Object> signdata;
    private Location signloc;

    private String world;
    private Boolean isopen;
    private Integer stocklimit;
    private ShopState state;
    private Boolean maintenanceMode = false;
    
    private Map<Integer, ShopItem> sellitems = new HashMap<Integer, ShopItem>();
    private Map<Integer, ShopItem> buyitems = new HashMap<Integer, ShopItem>();

    private final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + String.valueOf('§') + "[0-9A-FK-OR]");

    // create shop stub
	public Shop(String owner, String name, Location chestlocation, Location signlocation) {
        shopowner = owner;
    	shopname = name;
    	chestloc = chestlocation;
    	description = null;
    	isopen = false;
    	stocklimit = OBChestShop.getInstance().getConfig().getInt("stocklimit");
		chestblock = chestloc.getBlock();
		world = chestloc.getWorld().getName();
		chestblock = Bukkit.getWorld(world).getBlockAt(chestloc);
		signloc = signlocation;
        signblock = Bukkit.getWorld(world).getBlockAt(signloc);;
        state = ShopState.NoShop;
	}

	// create a shop from a config file
	public Shop(String filename) {

		state = ShopState.NoShop;
		
		// shop config
		shopfile = new File(filename);
		shopconfig = YamlConfiguration.loadConfiguration(shopfile);
		if (shopconfig == null) {
			state = ShopState.NoConfig;
			return;
		}
		shopname = shopconfig.getString("Name");
		shopowner = shopconfig.getString("Owner");
		description = shopconfig.getString("Description");
		isopen = shopconfig.getBoolean("Open");
		stocklimit = shopconfig.getInt("StockLimit");
		if (stocklimit == -1 || stocklimit == null) {
			stocklimit = OBChestShop.getInstance().getConfig().getInt("stocklimit");
		}
		
		world = shopconfig.getString("World");
		state = ShopState.ConfigOK;
		
		// shop world
		state = ShopState.WorldNotExist;
		if (validateWorld().compareTo(ShopState.WorldOK) < 0) {
			logShopMessage();
			return;
		}
		state = ShopState.WorldOK;
		
		// shop chest config
		state = ShopState.NoChestConfig;
		chestdata = shopconfig.getConfigurationSection("Chest").getValues(false);
		if (chestdata == null) {
			logShopMessage();
			return;
		}
		chestloc = new Location(Bukkit.getWorld(world),
				Double.parseDouble((String)chestdata.get("X")),
				Double.parseDouble((String)chestdata.get("Y")),
				Double.parseDouble((String)chestdata.get("Z")));
		chestblock = chestloc.getBlock();
		state = ShopState.ChestConfigOK;

		// shop sign config
		state = ShopState.NoSignConfig;
		signdata = shopconfig.getConfigurationSection("Sign").getValues(false);
		if (signdata == null) {
			logShopMessage();
			return;
		}
		signloc = new Location(Bukkit.getWorld(world),
				Double.parseDouble((String)signdata.get("X")),
				Double.parseDouble((String)signdata.get("Y")),
				Double.parseDouble((String)signdata.get("Z")));
		signblock = signloc.getBlock();
		state = ShopState.SignConfigOK;
		
		// shop chest - actual world block checks
		state = ShopState.NotShopChest;
		if (validateChest().compareTo(ShopState.ChestOK) < 0) {
			// attempt to reinstate block as a shop chest
			if (reinstateChest().compareTo(ShopState.ChestOK) < 0) {
				state = ShopState.FixChestFail;
				logShopMessage();
				return;
			}
		}
		state = ShopState.ChestOK;

		// shop sign - actual world block checks
		state = ShopState.NotShopSign;
		if (validateSign().compareTo(ShopState.SignOK) < 0) {
			// attempt to reinstate block as a shop sign
			if (reinstateSign().compareTo(ShopState.SignOK) < 0) {
				state = ShopState.FixSignFail;
				logShopMessage();
				return;
			}
		}
		state = ShopState.SignOK;
		
		// initial load up of shop complete
		state = ShopState.ShopPreChecks;
		// validate the actual blocks in the world are good

		// load shop items
		sellitems.clear(); buyitems.clear();
		LoadShopItems(ShopItemTypes.Sell);
		LoadShopItems(ShopItemTypes.Buy);
		
		state = ShopState.ShopOK;
	}

	// load up shop items of a particular type
	private void LoadShopItems(ShopItemTypes type) {
		String typestr = type.toString();
		ShopItem shopitem = null;
		if (shopconfig.getConfigurationSection("Items").isConfigurationSection(typestr)) {
            for (String key : shopconfig.getConfigurationSection("Items").getConfigurationSection(typestr).getKeys(false)) {
            	int slot = Integer.parseInt(key);
           		shopitem = new ShopItem(slot,            				
           				shopconfig.getConfigurationSection("Items").getConfigurationSection(typestr).getConfigurationSection(key).getString("Item"),
           				shopconfig.getConfigurationSection("Items").getConfigurationSection(typestr).getConfigurationSection(key).getInt("Stock"));
           		shopitem.setPrice(shopconfig.getConfigurationSection("Items").getConfigurationSection(typestr).getConfigurationSection(key).getDouble("Price"));
           		shopitem.setAmount(shopconfig.getConfigurationSection("Items").getConfigurationSection(typestr).getConfigurationSection(key).getInt("Amount"));
           		shopitem.setStock(shopconfig.getConfigurationSection("Items").getConfigurationSection(typestr).getConfigurationSection(key).getInt("Stock"));
           		shopitem.setDescription(shopconfig.getConfigurationSection("Items").getConfigurationSection(typestr).getConfigurationSection(key).getString("Description"));
           		if (type.equals(ShopItemTypes.Sell)) {
           			sellitems.put(slot, shopitem);
           		} else {
           			buyitems.put(slot,  shopitem);
           		}
            }
		}
	}

	// create the player shop config file and populate
	public ShopState CreateConfig(String name) {

		File pluginfolder = OBChestShop.getInstance().getDataFolder();
		shopfile = new File(pluginfolder, "Shops/" + shopowner + "/" + shopname + ".yml");
		shopconfig = YamlConfiguration.loadConfiguration(shopfile);
		if (shopconfig == null) {
			state = ShopState.NoConfig;
			logShopMessage();
			return state;
		}

		HashMap<String,String> data = new HashMap<String, String>();
		shopconfig.set("Type", "OBChestShop");
		shopconfig.set("Name", shopname);
		shopconfig.set("Description", "No description");
		shopconfig.set("Owner", shopowner);
		shopconfig.set("Open", false);
		shopconfig.set("World", chestblock.getWorld().getName());
		state = ShopState.ConfigOK;
		
		data.put("Type",  chestblock.getType().toString());
		data.put("Facing", ((Directional)chestblock.getBlockData()).getFacing().toString());
		data.put("X", String.valueOf(chestblock.getX()));
		data.put("Y", String.valueOf(chestblock.getY()));
		data.put("Z", String.valueOf(chestblock.getZ()));
		shopconfig.createSection("Chest", data);
		chestdata = shopconfig.getConfigurationSection("Chest").getValues(false);
		data.clear();
		state = ShopState.ChestConfigOK;

		data.put("Type", signblock.getType().toString());
		if (signblock.getType().toString().contains("WALL_SIGN")) {
			org.bukkit.block.data.type.WallSign s = (WallSign) signblock.getBlockData();
			data.put("Facing", s.getFacing().toString());
		} else {
			org.bukkit.block.data.type.Sign s = (Sign) signblock.getBlockData();
			data.put("Facing", s.getRotation().toString());
		}
		data.put("X", String.valueOf(signblock.getX()));
		data.put("Y", String.valueOf(signblock.getY()));
		data.put("Z", String.valueOf(signblock.getZ()));
		shopconfig.createSection("Sign",  data);
		signdata = shopconfig.getConfigurationSection("Sign").getValues(false);
		data.clear();
		state = ShopState.SignConfigOK;

		shopconfig.createSection("Items");
		shopconfig.getConfigurationSection("Items").createSection("Sell");
		shopconfig.getConfigurationSection("Items").createSection("Buy");
		
		try {
			shopconfig.save(shopfile);
		} catch (IOException e) {
			e.printStackTrace();
			state = ShopState.ConfigSaveFail;
			logShopMessage();
		}

		state = ShopState.ShopPreChecks;
		return state;
	}

	// Output a shop message to the log
	public void logShopMessage() {
		log.log(Level.INFO, OBChestShop.getLogMsgPrefix() + "Message for shop '" + shopname +"'");
		switch (state) {
			case NoShop:
				break;
			case NoConfig: {
				log.log(Level.INFO, OBChestShop.getLogMsgPrefix() + "    Configuration file for shop doesn't appear to exist"); 
				log.log(Level.INFO, OBChestShop.getLogMsgPrefix() + "    Path: " + shopfile.getAbsolutePath());
				break;
			}
			case ConfigSaveFail: {
				log.log(Level.INFO, OBChestShop.getLogMsgPrefix() + "    Unable to save configuraton file for shop");
				log.log(Level.INFO, OBChestShop.getLogMsgPrefix() + "    Path: " + shopfile.getAbsolutePath());
				break;
			}
			case ConfigOK:
				break;
			case WorldNotExist: {
				log.log(Level.INFO, OBChestShop.getLogMsgPrefix() + "    World " + shopconfig.get("World") + " doesn't appear to exist any longer"); 
				break;
			}
			case WorldOK:
				break;
			case ShopPreChecks:
				break;
			case NoChestConfig: {
				log.log(Level.INFO, OBChestShop.getLogMsgPrefix() + "    Configuration file doesn't appear to have a valid 'Chest' section"); 
				break;
			}
			case ChestConfigOK:
				break;
			case NotShopChest: {
				log.log(Level.INFO, OBChestShop.getLogMsgPrefix() + "    Couldn't obtain a valid chest block from world");
				log.log(Level.INFO, OBChestShop.getLogMsgPrefix() + "    World: " + shopconfig.getString("World") + ", XYZ: " + chestloc.getX() + " " + chestloc.getY() + " " + chestloc.getZ()); 
				break;
			}
			case FixChestFail: {
				log.log(Level.INFO, OBChestShop.getLogMsgPrefix() + "    Unable to reinstate chest block. This needs to be checked");
				log.log(Level.INFO, OBChestShop.getLogMsgPrefix() + "    World: " + shopconfig.getString("World") + ", XYZ: " + chestloc.getX() + " " + chestloc.getY() + " " + chestloc.getZ()); 
				break;
			}
			case ChestOK:
				break;
			case NoSignConfig: {
				log.log(Level.INFO, OBChestShop.getLogMsgPrefix() + "    Configuration file doesn't appear to have a valid 'Sign' section"); 
				break;
			}
			case SignConfigOK:
				break;
			case NotShopSign: {
				log.log(Level.INFO, OBChestShop.getLogMsgPrefix() + "    Couldn't obtain a valid sign block from world");
				log.log(Level.INFO, OBChestShop.getLogMsgPrefix() + "    World: " + shopconfig.getString("World") + ", XYZ: " + signloc.getX() + " " + signloc.getY() + " " + signloc.getZ()); 
				break;
			}
			case FixSignFail: {
				log.log(Level.INFO, OBChestShop.getLogMsgPrefix() + "    Unable to reinstate sign block. This needs to be checked");
				log.log(Level.INFO, OBChestShop.getLogMsgPrefix() + "    World: " + shopconfig.getString("World") + ", XYZ: " + signloc.getX() + " " + signloc.getY() + " " + signloc.getZ()); 
				break;
			}
			case SignOK:
				break;
			case ShopOK:
				break;
			default:
				break;
		}
		log.log(Level.INFO, OBChestShop.getLogMsgPrefix() + "    Current state is : " + state.toString());
	}
	
	// save shop configuration to shop file, including shop items
	public boolean saveShop() {
		ShopItem shopitem;
		String slotstr;
		int slot;
        Iterator <Integer> isit = sellitems.keySet().iterator();
        while (isit.hasNext()) {
        	slot = isit.next(); slotstr = String.valueOf(slot);
        	shopitem = this.getShopItem(ShopItemTypes.Sell, slot);
        	if (!shopconfig.getConfigurationSection("Items").getConfigurationSection(ShopItemTypes.Sell.toString()).isConfigurationSection(slotstr)) {
        		shopconfig.getConfigurationSection("Items").getConfigurationSection(ShopItemTypes.Sell.toString()).createSection(slotstr);
        	}
    		shopconfig.getConfigurationSection("Items").getConfigurationSection(ShopItemTypes.Sell.toString()).getConfigurationSection(slotstr).set("Slot", shopitem.getSlot());
    		shopconfig.getConfigurationSection("Items").getConfigurationSection(ShopItemTypes.Sell.toString()).getConfigurationSection(slotstr).set("Item", shopitem.getItem().getType().name());
            shopconfig.getConfigurationSection("Items").getConfigurationSection(ShopItemTypes.Sell.toString()).getConfigurationSection(slotstr).set("Price", shopitem.getPrice());
            shopconfig.getConfigurationSection("Items").getConfigurationSection(ShopItemTypes.Sell.toString()).getConfigurationSection(slotstr).set("Amount", shopitem.getAmount());
            shopconfig.getConfigurationSection("Items").getConfigurationSection(ShopItemTypes.Sell.toString()).getConfigurationSection(slotstr).set("Stock", shopitem.getStockQuantity());
            shopconfig.getConfigurationSection("Items").getConfigurationSection(ShopItemTypes.Sell.toString()).getConfigurationSection(slotstr).set("Description", shopitem.getDescription());
		}
        isit = buyitems.keySet().iterator();
        while (isit.hasNext()) {
        	slot = isit.next(); slotstr = String.valueOf(slot);
        	shopitem = this.getShopItem(ShopItemTypes.Buy, slot);
    		shopconfig.getConfigurationSection("Items").getConfigurationSection(ShopItemTypes.Buy.toString()).createSection(slotstr);
    		shopconfig.getConfigurationSection("Items").getConfigurationSection(ShopItemTypes.Buy.toString()).getConfigurationSection(slotstr).set("Slot", shopitem.getSlot());
    		shopconfig.getConfigurationSection("Items").getConfigurationSection(ShopItemTypes.Buy.toString()).getConfigurationSection(slotstr).set("Item", shopitem.getItem().getType().name());
            shopconfig.getConfigurationSection("Items").getConfigurationSection(ShopItemTypes.Buy.toString()).getConfigurationSection(slotstr).set("Price", shopitem.getPrice());
            shopconfig.getConfigurationSection("Items").getConfigurationSection(ShopItemTypes.Buy.toString()).getConfigurationSection(slotstr).set("Amount", shopitem.getAmount());
            shopconfig.getConfigurationSection("Items").getConfigurationSection(ShopItemTypes.Buy.toString()).getConfigurationSection(slotstr).set("Stock", shopitem.getStockQuantity());
            shopconfig.getConfigurationSection("Items").getConfigurationSection(ShopItemTypes.Buy.toString()).getConfigurationSection(slotstr).set("Description", shopitem.getDescription());
		}

        try {
			shopconfig.save(shopfile);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public String getName() {
		return shopname;
	}
	public String setName(String newshopname) {
		if (!OBChestShop.getShopList().shopExists(newshopname)) {
			maintenanceMode = true;

			if (description.contains(shopname)) {
				description = description.replace(shopname, newshopname);
				shopconfig.set("Description", description);
			}

			shopconfig.set("Name", newshopname);
			try {
				shopconfig.save(shopfile);
			} catch (IOException e) {
				e.printStackTrace();
			}

			File newshopfile = new File(OBChestShop.getInstance().getDataFolder(), "Shops/" + shopowner + "/" + newshopname + ".yml");
			shopfile.renameTo(newshopfile);
			shopfile = newshopfile;
			
			shopconfig = YamlConfiguration.loadConfiguration(shopfile);
			
			OBChestShop.getShopList().removeShop(shopname);
			OBChestShop.getShopList().addShop(newshopname, this);
			
			shopname = newshopname;
			maintenanceMode = false;

		} else {
			return "ShopExists";
		}
		
		return "ok";
	}

	public String getOwner() {
		return shopowner;
	}
	
	public String getOwnerName() {
		return Bukkit.getOfflinePlayer(UUID.fromString(shopowner)).getName();
	}
	
	public String getWorld() {
		return shopconfig.getString("World");
	}

	// return the location of our chest and sign blocks
	public Location getChestXYZ() {
		return chestloc;
	}
	public Location getSignXYZ() {
		return signloc;
	}

	// validate a shop is still valid - world, chest and sign blocks
	public ShopState validateShop() {
		
		if (!maintenanceMode) {
			ShopState checkstate = ShopState.NoShop;

			// skip configuration file checks

			// validate shop world
			checkstate = validateWorld();
			if (checkstate.compareTo(ShopState.WorldOK) < 0) {
				return checkstate;
			}

			// validate shop chest block
			checkstate = validateChest();
			if (checkstate.compareTo(ShopState.ChestOK) < 0) {
				if (OBChestShop.getInstance().getConfig().getBoolean("shopautofix")) {
					// see if we can reinstate the chest
					checkstate = reinstateChest();
					if (checkstate.compareTo(ShopState.ChestOK) < 0) {
						return checkstate;
					}
					// add the chest location to the location list
					OBChestShop.getShopList().addChestLocation(chestloc);
				} else {
					return checkstate;
				}

			}

			// validate shop sign block
			checkstate = validateSign();
			if (checkstate.compareTo(ShopState.SignOK) < 0) {
				if (OBChestShop.getInstance().getConfig().getBoolean("shopautofix")) {
					// see if we can reinstate the sign
					state = reinstateSign();
					if (state.compareTo(ShopState.SignOK) < 0) {
						return checkstate;
					}
					// add the sign location to the location list
					OBChestShop.getShopList().addSignLocation(chestloc, signloc);
				} else {
					return checkstate;
				}
			}
		} else {
			return ShopState.ShopMAINT;
		}
		return ShopState.ShopOK;
	}
	
	// validate the world the shop exists in is still valid
	public ShopState validateWorld() {
		if (Bukkit.getWorld(shopconfig.getString("World")) == null) {
			return ShopState.WorldNotExist;
		}
		return ShopState.WorldOK;
	}
	
	// perform a series of checks to make sure the shop chest location, block and meta data are intact
	public ShopState validateChest() {
		if (chestdata == null) {
			return ShopState.NoChestConfig;
		}
		if (chestloc == null) {
			return ShopState.NotShopChest;
		}
		if (chestblock == null) {
			return ShopState.NotShopChest;
		}

		Block vcblock = null;
		try {
			vcblock = Bukkit.getWorld(world).getBlockAt(chestloc);
		} catch (NullPointerException e) {
			return ShopState.NotShopChest;
		}
		if (vcblock.getBlockData() instanceof Directional) {
			if (vcblock.getType().toString().equals(chestdata.get("Type")) && 
				((Directional)vcblock.getBlockData()).getFacing().toString().equals(chestdata.get("Facing"))) {
				return ShopState.ChestOK;
			}
		}
		return ShopState.NotShopChest;
	}


	// attempt a shop chest recreation based on state
	public ShopState reinstateChest() {
		if (chestdata == null) {
			try {
				chestdata = shopconfig.getConfigurationSection("Chest").getValues(false);
			} catch (Exception e) {
				return ShopState.NoChestConfig;
			}
		}
		if (chestloc == null) {
			try {
				chestloc = new Location(Bukkit.getWorld(world),
						Double.parseDouble((String)chestdata.get("X")),
						Double.parseDouble((String)chestdata.get("Y")),
						Double.parseDouble((String)chestdata.get("Z")));
			} catch (Exception e) {
				return ShopState.NotShopChest;
			}
		}
		try {
			chestblock = Bukkit.getWorld(world).getBlockAt(chestloc);
			chestblock.setType(Material.getMaterial(chestdata.get("Type").toString()));
			Directional chestbd = (Directional) chestblock.getBlockData();
			chestbd.setFacing(BlockFace.valueOf(chestdata.get("Facing").toString()));
			chestblock.setBlockData(chestbd);
		} catch (Exception e) {
			return ShopState.NotShopChest;
		}
		// re-check our reinstatement occurred successfully
		if (validateChest().compareTo(ShopState.ChestOK) != 0) {
			return ShopState.FixChestFail;
		}

		return ShopState.ChestOK;
	}


	// perform a series of checks to make sure the shop chest is intact
	public ShopState validateSign() {
		if (signdata == null) {
			return ShopState.NoSignConfig;
		}
		if (signloc == null) {
			return ShopState.NotShopSign;
		}
		if (signblock == null) {
			return ShopState.NotShopSign;
		}

		Block vsblock = null;
		try {
			vsblock = Bukkit.getWorld(world).getBlockAt(signloc);
		} catch (NullPointerException e) {
			return ShopState.NotShopSign;
		}
		if (Tag.SIGNS.isTagged(vsblock.getType())) {
			if (vsblock.getBlockData() instanceof Directional || vsblock.getBlockData() instanceof Rotatable) {
				// need to cater for wall and floor sign direction - wall sign is directional, floor sign rotatable 
				String signdirection = "";
				if (signdata.get("Type").toString().contains("WALL_SIGN")) {
					signdirection = ((Directional)vsblock.getBlockData()).getFacing().toString();
				} else {
					signdirection = ((Rotatable)vsblock.getBlockData()).getRotation().toString();
				}
				if (vsblock.getType().toString().equals(signdata.get("Type")) && 
						signdirection.equals(signdata.get("Facing"))) {
					org.bukkit.block.Sign blocksign = (org.bukkit.block.Sign) vsblock.getState();
					if (blocksign.getLine(1).equals("§aShop")) {
						return ShopState.SignOK;
					}
				}
			}
		}
		return ShopState.NotShopSign;
	}
	
	// recreate the chest at the location
	public ShopState reinstateSign() {
		if (signdata == null) {
			try {
				signdata = shopconfig.getConfigurationSection("Sign").getValues(false);
			} catch (Exception e) {
				return ShopState.NoSignConfig;
			}
		}
		if (signloc == null) {
			try {
				signloc = new Location(Bukkit.getWorld(world),
						Double.parseDouble((String)signdata.get("X")),
						Double.parseDouble((String)signdata.get("Y")),
						Double.parseDouble((String)signdata.get("Z")));
			} catch (Exception e) {
				return ShopState.NotShopSign;
			}
		}
		try {
			signblock = Bukkit.getWorld(world).getBlockAt(signloc);
		} catch (NullPointerException e) {
			return ShopState.FixSignFail;
		}
		signblock.setType(Material.getMaterial(signdata.get("Type").toString()));
		if (signdata.get("Type").toString().contains("WALL_SIGN")) {
			Directional signbd = (Directional)signblock.getBlockData();
			signbd.setFacing(BlockFace.valueOf(signdata.get("Facing").toString()));
			signblock.setBlockData(signbd);
		} else {
			Rotatable signbd = (Rotatable)signblock.getBlockData();
			signbd.setRotation(BlockFace.valueOf(signdata.get("Facing").toString()));
			signblock.setBlockData(signbd);
		}
		org.bukkit.block.Sign signtext = (org.bukkit.block.Sign) signblock.getState();
		signtext.setLine(0, ChatColor.AQUA + "**************");
		signtext.setLine(1, ChatColor.GREEN + "Shop");
		if (isopen) {
			signtext.setLine(2, ChatColor.GREEN + "Open");
		} else {
			signtext.setLine(2, ChatColor.RED + "Closed");
		}
		signtext.setLine(3, ChatColor.AQUA + "**************");
		signtext.update();
		// re-check our reinstatement occurred successfully
		if (validateSign().compareTo(ShopState.SignOK) != 0) {
			return ShopState.FixSignFail;
		}
		return ShopState.SignOK;
	}

	
	// get/set state of the shop
	public ShopState getState() {
		return state;
	}
	public void setState(ShopState state) {
		this.state = state;
	}

	public boolean isOpen() {
		return isopen;
	}
	public void toggleOpen() {
		isopen = !isopen;
		shopconfig.set("Open", isopen);
		try {
			shopconfig.save(shopfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		org.bukkit.block.Sign signtext = (org.bukkit.block.Sign) signblock.getState();
		if (isopen) {
			signtext.setLine(2, ChatColor.GREEN + "Open");
		} else {
			signtext.setLine(2, ChatColor.RED + "Closed");
		}
		signtext.update();
	}

	// get/set shop description
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
		shopconfig.set("Description", this.description);
		try {
			shopconfig.save(shopfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// return a list of items in the shop
	public Map<Integer, ShopItem> getShopItems(ShopItemTypes type) {
		if (type.equals(ShopItemTypes.Sell)) {
			return sellitems;
		} else {
			return buyitems;
		}
	}
	public int getItemSlotByName(ShopItemTypes type, String itemname) {
		Collection<ShopItem> items = null;
		if (type.equals(ShopItemTypes.Sell)) {
			items = sellitems.values();
		} else {
			items = buyitems.values();
		}
		for (ShopItem item : items) {
			if (item.getItemName().equals(itemname)) {
				return item.getSlot();
			}
		}
		return -1;
	}
	public Map<Integer, ShopItem> getSellItems() {
		return sellitems;
	}
	public Map<Integer, ShopItem> getBuyItems() {
		return buyitems;
	}
	public Set<Integer> getSellItemList() {
		return sellitems.keySet();
	}
	public Set<Integer> getBuyItemList() {
		return buyitems.keySet();
	}
	public Boolean itemListContains(ShopItemTypes type, String itemname) {
		Collection<ShopItem> items = null;
		if (type.equals(ShopItemTypes.Sell)) {
			items = sellitems.values();
		} else {
			items = buyitems.values();
		}
		for (ShopItem item : items) {
			if (item.getItemName().equals(itemname)) {
				return true;
			}
		}
		return false;
	}
	
	// add a new item to the shop
	public void addShopItem(ShopItemTypes type, ShopItem shopitem) {
		// put into the appropriate list
		if (type.equals(ShopItemTypes.Sell)) {
			sellitems.put(shopitem.getSlot(), shopitem);
		} else {
			buyitems.put(shopitem.getSlot(), shopitem);
		}
		String slot = String.valueOf(shopitem.getSlot());
   		shopconfig.getConfigurationSection("Items").getConfigurationSection(type.toString()).createSection(slot);
		shopconfig.getConfigurationSection("Items").getConfigurationSection(type.toString()).getConfigurationSection(slot).set("Slot", shopitem.getSlot());
		shopconfig.getConfigurationSection("Items").getConfigurationSection(type.toString()).getConfigurationSection(slot).set("Item", shopitem.getItem().getType().name());
        shopconfig.getConfigurationSection("Items").getConfigurationSection(type.toString()).getConfigurationSection(slot).set("Price", 5.0);
        shopconfig.getConfigurationSection("Items").getConfigurationSection(type.toString()).getConfigurationSection(slot).set("Amount", 1);
        shopconfig.getConfigurationSection("Items").getConfigurationSection(type.toString()).getConfigurationSection(slot).set("Stock", shopitem.getStockQuantity());
        shopconfig.getConfigurationSection("Items").getConfigurationSection(type.toString()).getConfigurationSection(slot).set("Description", "");
        ItemStack item = shopitem.getItem();
        ItemMeta itemmeta = item.getItemMeta();
    	itemmeta.setLore(Arrays.asList(shopitem.getLore().split(",")));
    	item.setItemMeta(itemmeta);
        try {
			shopconfig.save(shopfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public ShopItem getShopItem(ShopItemTypes type, int slot) {
		if (type.equals(ShopItemTypes.Sell)) {
			return sellitems.get(slot);
		}
		return buyitems.get(slot);
	}
	public void removeitem(ShopItemTypes type, int slot) {
		if (type.equals(ShopItemTypes.Sell)) {
			sellitems.remove(slot);
		} else {
			buyitems.remove(slot);
		}
		shopconfig.getConfigurationSection("Items").getConfigurationSection(type.toString()).set(String.valueOf(slot), null);
        try {
			shopconfig.save(shopfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void addItemStock(ShopItemTypes type, int slot, int stocktoadd) {
		if (type.equals(ShopItemTypes.Sell)) {
			sellitems.get(slot).addStock(stocktoadd);
	        shopconfig.getConfigurationSection("Items").getConfigurationSection(type.toString()).getConfigurationSection(String.valueOf(slot)).set("Stock", sellitems.get(slot).getStockQuantity());
		} else {
			buyitems.get(slot).addStock(stocktoadd);
	        shopconfig.getConfigurationSection("Items").getConfigurationSection(type.toString()).getConfigurationSection(String.valueOf(slot)).set("Stock", buyitems.get(slot).getStockQuantity());
		}
        try {
			shopconfig.save(shopfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void removeItemStock(ShopItemTypes type, int slot, int stocktoremove) {
		if (type.equals(ShopItemTypes.Sell)) {
			sellitems.get(slot).removeStock(stocktoremove);
			shopconfig.getConfigurationSection("Items").getConfigurationSection(type.toString()).getConfigurationSection(String.valueOf(slot)).set("Stock", sellitems.get(slot).getStockQuantity());
		} else {
			sellitems.get(slot).removeStock(stocktoremove);
			shopconfig.getConfigurationSection("Items").getConfigurationSection(type.toString()).getConfigurationSection(String.valueOf(slot)).set("Stock", buyitems.get(slot).getStockQuantity());
		}
        try {
			shopconfig.save(shopfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Boolean hasSpace(ShopItemTypes type) {
		if (type.equals(ShopItemTypes.Sell)) {
			if (sellitems.size() < 36) {
				return true;
			}
		} else {
			if (buyitems.size() < 36) {
				return true;
			}
		}
		return false;
	}
	public int getNextOpenSlot(ShopItemTypes type) {
		for (int slot = 18; slot < 54; slot++) {
			if (type.equals(ShopItemTypes.Sell)) {
				if (!sellitems.containsKey(slot)) {
					return slot;
				}
			} else {
				if (!buyitems.containsKey(slot)) {
					return slot;
				}
			}
		}
		return -1;
	}

	// give the player all stock sell items
	public void moveAllStockToInventory(String playeruuid) {
		for (int slot : sellitems.keySet()) {
			ShopItem shopitem = sellitems.get(slot);
			shopitem.moveStockToInventory(playeruuid, shopitem.getStockQuantity());
		}
	}

	// stock limit methods
	public Integer getStockLimit() {
		return stocklimit;
	}
	public void setStockLimit(Integer stocklimit) {
		// apply the new limit and save our shop config
		this.stocklimit = stocklimit;
		shopconfig.set("StockLimit", stocklimit);
		try {
			shopconfig.save(shopfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// check current items and warn on any breaches
		int itembreachcnt = itemBreachCheck();
		if (itembreachcnt > 0) {
			Bukkit.getPlayer(UUID.fromString(shopowner)).sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.RED + itembreachcnt + "sell item" + (itembreachcnt != 1 ? "s" : "") + " in the shop exceed" + (itembreachcnt == 1 ? "s" : "") + " new stock limit of " + ChatColor.GRAY + "" + stocklimit);
		}
	}

	// scan through shop items to see if any breach the shop stock limit and return count
	private int itemBreachCheck() {
		int breachcnt = 0;
		for (int slot : sellitems.keySet()) {
			if (sellitems.get(slot).getStockQuantity() > stocklimit) {
				breachcnt++;
			}
		}
		return breachcnt;
	}
	
	// check if player accessing this shop in some way
	public boolean isPlayerAccessing(Player player) {
		String playerview = stripcolor(player.getOpenInventory().getTitle());
		if (!playerview.isEmpty() && (playerview.startsWith("[SELL") || playerview.startsWith("[BUY"))) {
			String playerviewshop = playerview.substring(playerview.indexOf("] ")+2, playerview.length());
			if (playerviewshop.equals(shopname)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isPlayerAccessingItem(Player player, ShopItemTypes type, String itemname) {
		String playerview = stripcolor(player.getOpenInventory().getTitle());
		if (!playerview.isEmpty() && playerview.startsWith("[" + type.toString().toUpperCase() + " " + itemname + "] " + shopname)) {
			return true;
		}
		return false;
	}
	
	// strip out any text formatting
	public String stripcolor(String input) {
        return input == null?null:STRIP_COLOR_PATTERN.matcher(input).replaceAll("");
    }
}