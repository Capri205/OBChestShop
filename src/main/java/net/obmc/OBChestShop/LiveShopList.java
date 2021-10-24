package net.obmc.OBChestShop;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Tag;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import net.obmc.OBChestShop.Shop.Shop;
import net.obmc.OBChestShop.ShopStates.ShopState;

public class LiveShopList {

	Logger log = Logger.getLogger("Minecraft");
	
    private HashMap<String, Shop> shoplist = new HashMap<String, Shop>();								// main active shop list while plugin running
    private HashMap<Location, List<Location>> locationlist = new HashMap<Location, List<Location>>(); // fast lookup of chest and sign locations
    
    // set up a new hash for managing our shops
    public LiveShopList() {
    	if (shoplist == null) {
    		shoplist = new HashMap<String, Shop>();
    	}
    }

    // add a shop to our working list
    public void addShop(String shopname, Shop shop) {
    	shoplist.put(shopname, shop);
    	if (!locationlist.containsKey(shop.getChestXYZ())) {
    		locationlist.put(shop.getChestXYZ(), new ArrayList<Location>());
    	}
    	locationlist.get(shop.getChestXYZ()).add(shop.getSignXYZ());
    }

    // remove a shop from our active lists
	public void removeShop(String shopname) {
		if (shoplist.containsKey(shopname)) {
			// remove the sign location for the chest if it exists
			if (locationlist.containsKey(shoplist.get(shopname).getChestXYZ())) {
				locationlist.get(shoplist.get(shopname).getChestXYZ()).removeIf(l -> l.equals(shoplist.get(shopname).getSignXYZ()));
			}
			// remove the chest location from the list if no more signs (shops) associated with it
			if (locationlist.get(shoplist.get(shopname).getChestXYZ()).isEmpty()) {
				locationlist.remove(shoplist.get(shopname).getChestXYZ());
			}
			shoplist.remove(shopname);
		}
	}

	// load shops from the shops directory
	public boolean loadShops() {
		Set<String> shopfiles = new HashSet<String>();
		File shopdir = new File(OBChestShop.getInstance().getDataFolder() + "/Shops");
		if (shopdir.exists()) {
			try {
				shopfiles = getShopFiles3(OBChestShop.getInstance().getDataFolder() + "/Shops");
			} catch (IOException e) {
				e.printStackTrace();
				log.log(Level.INFO, OBChestShop.getLogMsgPrefix() + "    Failed to get a list of shop files from " + shopdir.getAbsolutePath());
				return false;
			}
		} else {
			// no shops because there is no shop directory, so let's try to create it anyway
			try {
				shopdir.mkdir();
			} catch (Exception e) {
				log.log(Level.INFO, OBChestShop.getLogMsgPrefix() + "    Failed to create shop directory - " + shopdir.getAbsolutePath());
				e.printStackTrace();
			}
		}
		if (shopfiles.size() > 0) {
			// load each shop and put valid shops onto our active shop list
			int invalidcnt = 0;
			Iterator<String> it = shopfiles.iterator();
			while (it.hasNext()) {
				String filename = it.next();
				Shop shop = new Shop(filename);
				shoplist.put(shop.getName(), shop);
				if (shop.getState().compareTo(ShopState.ShopOK) < 0) {
					invalidcnt++;
				} else {
					// add valid shop chest and sign locations to our quick location lookup hash
					if (!locationlist.containsKey(shop.getChestXYZ())) {
						locationlist.put(shop.getChestXYZ(), new ArrayList<Location>());
					}
					locationlist.get(shop.getChestXYZ()).add(shop.getSignXYZ());
				}
				log.log(Level.INFO, OBChestShop.getLogMsgPrefix() + "    Loaded shop " + shop.getName());
			}
			log.log(Level.INFO, OBChestShop.getLogMsgPrefix() + "    Loaded " + shoplist.size() + " of " + shopfiles.size() + " shops. " + invalidcnt + " of which were invalid");
		} else {
			log.log(Level.INFO, OBChestShop.getLogMsgPrefix() + "    No shops found");
		}

		return true;
	}
	
	// save out the shop list 
	public boolean saveShops() {
		boolean success = true;
		Iterator<String> it = shoplist.keySet().iterator();
		while(it.hasNext()) {
			String shopname = it.next();
			if (!shoplist.get(shopname).saveShop()) {
				success = false;
			}
		}
		return success;
	}

	// retrieve a shop by name
	public Shop getShop(String shopname) {
		if (shoplist.containsKey(shopname)) {
			return shoplist.get(shopname);
		}
		return null;
	}
	
	// retrieve a collection of shop names by location - always the chest, even if a sign location passed in
	public Collection<String> getShopsByLocation(Location loc) {
		Collection<String> shops = new HashSet<String>();
		if (Tag.SIGNS.isTagged(Bukkit.getWorld(loc.getWorld().getName()).getBlockAt(loc).getType())) {
			if (getShopChestLocation(getShopnameBySignLocation(loc)) == null) {
				return shops;
			}
			loc = getShopChestLocation(getShopnameBySignLocation(loc));
		}
		Iterator<Location> lit = locationlist.keySet().iterator();
		Location sloc = null;
		while (lit.hasNext()){
			sloc = lit.next();
			if (locationEquals(sloc, loc)) {
				for (Location shoplocation : locationlist.get(sloc)) {
					shops.add(getShopnameBySignLocation(shoplocation));
				}
			}
		}
		return shops;
	}

	// get a shop count by location
	public int getShopCountByLocation(Location loc) {
		if (Tag.SIGNS.isTagged(Bukkit.getWorld(loc.getWorld().getName()).getBlockAt(loc).getType())) {
			if (getShopChestLocation(getShopnameBySignLocation(loc)) == null) {
				return 0;
			}
			loc = getShopChestLocation(getShopnameBySignLocation(loc));
		}
		Iterator<Location> lit = locationlist.keySet().iterator();
		Location sloc = null;
		while (lit.hasNext()){
			sloc = lit.next();
			if (locationEquals(sloc, loc)) {
				return locationlist.get(sloc).size();
			}
		}
		return 0;
	}
	
	// retrieve the world a shop is in
	public String getShopWorld(String shopname) {
		if (shoplist.containsKey(shopname)) {
			return shoplist.get(shopname).getWorld();
		}
		return null;
	}
	
	// retrieve a shop's chest location
	public Location getShopChestLocation(String shopname) {
		if (shoplist.containsKey(shopname)) {
			return shoplist.get(shopname).getChestXYZ();
		}
		return null;
	}

	// retrieve a shop's sign location
	public Location getShopSignLocation(String shopname) {
		if (shoplist.containsKey(shopname)) {
			return shoplist.get(shopname).getSignXYZ();
		}
		return null;
	}

	// retrieve a shop by chest location
	public List<String> getShopByChestLocation(Location loc) {
		List<String> shops = new ArrayList<String>();
		for (Shop shop : shoplist.values()) {
			if (locationEquals(shop.getChestXYZ(), loc)) {
				shops.add(shop.getName());
			}
		}
		return shops;
	}

	// retrieve a shop by sign location
	public String getShopnameBySignLocation(Location loc) {
		for (Shop shop : shoplist.values()) {
			if (locationEquals(shop.getSignXYZ(), loc)) {
				return shop.getName();
			}
		}
		return "";
	}
	public Shop getShopBySignLocation(Location loc) {
		for (Shop shop : shoplist.values()) {
			if (locationEquals(shop.getSignXYZ(), loc)) {
				return shop;
			}
		}
		return null;
	}

	// retrieve a shop owner
	public String getShopOwner(String shopname) {
		if (shoplist.containsKey(shopname)) {
			return shoplist.get(shopname).getOwner();
		}
		return null;
	}
	
	// get a list of shop owners from the shop list
	public Collection<Shop> getShops() {
		return shoplist.values();
	}
	
	// get the owner of a shop by location
	public String getShopOwnerByLocation(Location loc) {
		if (Tag.SIGNS.isTagged(Bukkit.getWorld(loc.getWorld().getName()).getBlockAt(loc).getType())) {
			if (getShopChestLocation(getShopnameBySignLocation(loc)) == null) {
				return "";
			}
			loc = getShopChestLocation(getShopnameBySignLocation(loc));
		}
		for (Shop shop : shoplist.values()) {
			if (shop.getState().compareTo(ShopState.ChestOK) >= 0) {
				if (locationEquals(shop.getChestXYZ(), loc)) {
					return shop.getOwner();
				}
			}
		}
		return "";
	}
	
	// list shops in list - on server or for player
	public void listShops(Player player, String listtype) {
		if (listtype.isEmpty()) {
			if (shoplist.size() > 0) {
				player.sendMessage(ChatColor.LIGHT_PURPLE + "Your shops on this server:");
				for (String key : shoplist.keySet()) {
					if (shoplist.get(key).getOwner().equals(player.getUniqueId().toString())) {
						player.sendMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "  " + key + " : " + ChatColor.DARK_PURPLE + "" + shoplist.get(key).getWorld());
					}
				}
			} else {
				player.sendMessage(ChatColor.LIGHT_PURPLE + "You have no shops on this server");
			}
		} else {
			if (shoplist.size() > 0) {
				player.sendMessage(ChatColor.GOLD + "Shops on this server:");
				for (Shop shop : getShopsByOwner()) {
					OfflinePlayer ofp = Bukkit.getOfflinePlayer(UUID.fromString(shop.getOwner()));
					if (ofp == null) {
						player.sendMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "  " + shop.getName() + " : " + ChatColor.DARK_PURPLE + "" + shop.getWorld() + ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + " (unknown?)");
					}
					player.sendMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "  " + shop.getName() + " : " + ChatColor.DARK_PURPLE + "" + shop.getWorld() + ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + " (" + ofp.getName() + ")");
				}
			} else {
				player.sendMessage(ChatColor.LIGHT_PURPLE + "No shops on the server");
			}
		}
	}

	// list shop locations
	public void listShopLocations(Player player, String listtype) {
		if (listtype.isEmpty()) {
			if (shoplist.size() > 0) {
				player.sendMessage(ChatColor.LIGHT_PURPLE + "Your shops on this server:");
				Location signloc = null;
				for (String key : shoplist.keySet()) {
					if (shoplist.get(key).getOwner().equals(player.getUniqueId().toString())) {
						signloc = shoplist.get(key).getSignXYZ();
						player.sendMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "  " + key + " : " +
								ChatColor.DARK_PURPLE + "" + signloc.getWorld().getName() + ChatColor.LIGHT_PURPLE + " at " +
								ChatColor.DARK_PURPLE + signloc.getX() + " " + signloc.getY() + " " + signloc.getZ() +
								ChatColor.LIGHT_PURPLE + "]");
					}
				}
			} else {
				player.sendMessage(ChatColor.LIGHT_PURPLE + "You have no shops on this server");
			}
		} else {
			if (shoplist.size() > 0) {
				player.sendMessage(ChatColor.GOLD + "Shops on this server:");
				Location signloc = null;
				String playername = "";
				for (Shop shop : getShopsByOwner()) {
					playername = "unknown";
					signloc = shop.getSignXYZ();
					OfflinePlayer ofp = Bukkit.getOfflinePlayer(UUID.fromString(shop.getOwner()));
					if (ofp != null) {
						playername = ofp.getName();
					}
					player.sendMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "  " + shop.getName() + " : " +
							ChatColor.DARK_PURPLE + "" + shop.getWorld() + ChatColor.LIGHT_PURPLE + "" +
							ChatColor.BOLD + " (" + ChatColor.DARK_PURPLE + playername + ChatColor.LIGHT_PURPLE + ") at " +
							ChatColor.DARK_PURPLE + signloc.getX() + " " + signloc.getY() + " " + signloc.getZ());
				}
			} else {
				player.sendMessage(ChatColor.LIGHT_PURPLE + "No shops on the server");
			}
		}
		
	}
	// list status of shops on the server
	public void listShopStatus(Player player, String listtype) {
		String noshopmsg = ChatColor.LIGHT_PURPLE + "You have no shops on the server";
		String titlemsg = ChatColor.LIGHT_PURPLE + "Status of your shops on this server:";
		if (listtype == "all") {
			noshopmsg = ChatColor.LIGHT_PURPLE + "No shops on the server";
			titlemsg = ChatColor.GOLD + "Status of shops on this server:";
		}
		if (shoplist.size() > 0) {
			player.sendMessage(titlemsg);
			// get a list of shops sorted by owner and iterate over them
			for (Shop shop : getShopsByOwner()) {
				String shopowner = "unknown?";
				String statusmsg = ChatColor.LIGHT_PURPLE + " - ";
				if (shop.getState().equals(ShopState.ShopOK)) {
					statusmsg = statusmsg + ChatColor.GREEN + "OK";
				} else {
					statusmsg = statusmsg + ChatColor.RED + "Invalid (" + shop.getState().toString() + ")";
				}
				String shopmsg = ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "  " + shop.getName() + " : " + ChatColor.DARK_PURPLE + "" + shop.getWorld();
				
				if (listtype.isEmpty()) {
					if (shop.getOwner().equals(player.getUniqueId().toString())) {
						player.sendMessage(shopmsg + statusmsg);
					}
				} else {
					OfflinePlayer ofp = Bukkit.getOfflinePlayer(UUID.fromString(shop.getOwner()));
					if (ofp != null) {
						shopowner = ofp.getName();
					}
					player.sendMessage(shopmsg + ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + " (" + shopowner + ")" + statusmsg);
				}
			}
		} else {
			player.sendMessage(noshopmsg);
		}
	}

	// see if a block location is shop related
	public boolean shopRelatedBlock(Location loc) {
		
		// Can't use contains as the world isn't the same following world recreation - mv delete/mv create even for the same name
		for (Location l : getLocations()) {
			if (locationEquals(loc, l)) {
				return true;
			}
		}
		return false;
	}
	
	// return a consolidated list of shop chest and sign locations
	public List<Location> getLocations() {
		List<Location> alllocations = new ArrayList<Location>(locationlist.keySet());
		for (Location loc : locationlist.keySet()) {
			alllocations.addAll(locationlist.get(loc));
		}
		return alllocations;
	}
	// return a list of shop chest locations
	public List<Location> getShopChestLocations() {
		List<Location> chestlocations = new ArrayList<Location>();
		for (Location loc : locationlist.keySet()) {
			chestlocations.add(loc);
		}
		return chestlocations;
	}
	// get a list of shop sign locations
	public List<Location> getShopSignLocations() {
		List<Location> signlocations = new ArrayList<Location>();
		for (Location loc : locationlist.keySet()) {
			signlocations.addAll(locationlist.get(loc));
		}
		return signlocations;
	}

	// check if a shop exists
	public boolean shopExists(String shopname) {
		if (shoplist.containsKey(shopname)) {
			return true;
		}
		return false;
	}

	// get a list of shops sorted by owner
	public List<Shop> getShopsByOwner() {
		List<Shop> byowner = new ArrayList<>(shoplist.values());
		Collections.sort(byowner, new ShopComparatorOwner());
		return byowner;
	}
	
	// used for getting sorted list of shops by owner
	static class ShopComparatorOwner implements Comparator<Shop> {
		@Override
		public int compare(Shop s1, Shop s2) {
			return s1.getOwner().compareTo(s2.getOwner());
		}
	}
	
	// get a list of .yml files from a particular directory
	public Set<String> getShopFiles3(String dir) throws IOException {
        try (Stream<Path> stream = Files.walk(Paths.get(dir), 3)) {
        	return stream.map(path -> path.toString())
        		.filter(f -> f.endsWith(".yml"))
        		.collect(Collectors.toSet());
        }
    }
	
	// add a chest location
	public boolean addChestLocation(Location loc) {
		if (!locationlist.containsKey(loc)) {
			locationlist.put(loc, new ArrayList<Location>());
		}
		return true;
	}
	
	// add a sign location
	public boolean addSignLocation(Location chestloc, Location signloc) {
		if (!locationlist.get(chestloc).contains(signloc)) {
			locationlist.get(chestloc).add(signloc);
		}
		return true;
	}

	// compare attributes of two locations for equality
	public boolean locationEquals(Location loc1, Location loc2) {
		if (loc1.getWorld().getName().equals(loc2.getWorld().getName()) &&
			loc1.getBlockX() == loc2.getBlockX() &&
			loc1.getBlockY() == loc2.getBlockY() &&
			loc1.getBlockZ() == loc2.getBlockZ()) {
			return true;
		}
		return false;
	}
}