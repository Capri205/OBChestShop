package net.obmc.OBChestShop.Listeners;

import java.io.File;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.InventoryView;

import net.obmc.OBChestShop.OBChestShop;
import net.obmc.OBChestShop.Shop.Shop;
import net.obmc.OBChestShop.Utils.BlockUtils;

public class ShopRemoval implements Listener {

	Logger log = Logger.getLogger("Minecraft");

	@EventHandler
    public void onBreak(BlockBreakEvent event) {
		
        Block block = event.getBlock();
        Player player = event.getPlayer();
        
    	if (OBChestShop.getShopList().shopRelatedBlock(block.getLocation())) {

        	if (OBChestShop.getShopList().getShopSignLocations().contains(block.getLocation()) || Tag.SIGNS.isTagged(block.getType())) {

        		// check ownership or OP
        		Block chestblock = BlockUtils.getSignAttachedBlock(block); 
        		if (player.getUniqueId().toString().equals(OBChestShop.getShopList().getShopOwnerByLocation(chestblock.getLocation())) || player.isOp()) {
        		
	        		// sign processing...
	        		int shopcount = OBChestShop.getShopList().getShopCountByLocation(chestblock.getLocation());
	
	   				String shopname = OBChestShop.getShopList().getShopnameBySignLocation(block.getLocation());
	   				Bukkit.getWorld(block.getWorld().getName()).getBlockAt(block.getLocation()).breakNaturally();
	   				// remove chest also if just one shop on it
	   				if (shopcount == 1) {
	   					Location chestloc = OBChestShop.getShopList().getShopChestLocation(shopname);
	   					Bukkit.getWorld(block.getWorld().getName()).getBlockAt(chestloc).breakNaturally();
	   				}

	   				// close out inventory for any player accessing the doomed shop
	   				Shop shop = OBChestShop.getShopList().getShop(shopname);
	   				for (Player onlineplayer : Bukkit.getOnlinePlayers()) {
	   					if (shop.isPlayerAccessing(onlineplayer)) {
	   						onlineplayer.closeInventory();
	   						onlineplayer.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.RED + "Shop " + shopname + " was removed!");
	   						log.log(Level.INFO, OBChestShop.getLogMsgPrefix() + "Force closed " + onlineplayer.getName() + "'s inventory view as shop " + shopname + " is being removed");
	   					}
	   				}

	   				// remove shop config file
	       			File shopfile = new File(OBChestShop.getInstance().getDataFolder() + "/Shops/" +
	       					OBChestShop.getShopList().getShopOwner(shopname) + "/" + shopname + ".yml");
	    			if (!shopfile.delete()) {
	    				log.log(Level.INFO, OBChestShop.getLogMsgPrefix() + "Failed to remove shop file for shop '" + shopname + "'");
	    			}
	   			
	    			// give player the shop inventory
	    			OBChestShop.getShopList().getShop(shopname).moveAllStockToInventory(player.getUniqueId().toString());
	    			
	    			// remove shop from our active list
	    			OBChestShop.getShopList().removeShop(shopname);
					player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.LIGHT_PURPLE + "'" + shopname + "' - " + ChatColor.GREEN + "Shop Removed!");
					log.log(Level.INFO, OBChestShop.getLogMsgPrefix() + player.getName() + " removed shop " + shopname);
    			} else {
    				player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.RED + "You cannot break another players shop!");
    				event.setCancelled(true);
    			}
        		
        	} else {

        		String owner = OBChestShop.getShopList().getShopOwnerByLocation(block.getLocation()); 
        		if (player.getUniqueId().toString().equals(owner) || player.isOp()) {

        			// chest processing...
	       			Set<String> shoplist = (Set<String>) OBChestShop.getShopList().getShopsByLocation(block.getLocation());

	   				// remove all shops associated with the chest
	   				String worldname = "";
	       			for (String shopname : shoplist) {
	   					worldname = OBChestShop.getShopList().getShopWorld(shopname);
	   					Location signloc = OBChestShop.getShopList().getShopSignLocation(shopname);
	   					Bukkit.getWorld(worldname).getBlockAt(signloc).breakNaturally();
	   				
	   					// remove shop config file
	   					File shopfile = new File(OBChestShop.getInstance().getDataFolder() + "/Shops/" +
	   							OBChestShop.getShopList().getShopOwner(shopname) + "/" + shopname + ".yml");
	   					if (!shopfile.delete()) {
	   						log.log(Level.INFO, OBChestShop.getLogMsgPrefix() + "Failed to remove shop '" + shopname +"'");
	   					}
	   					
		    			// give player the shop inventory
		    			OBChestShop.getShopList().getShop(shopname).moveAllStockToInventory(player.getUniqueId().toString());

	   					// remove shop from our active list
	   					OBChestShop.getShopList().removeShop(shopname);
   						player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.LIGHT_PURPLE + "'" + shopname + "' - " + ChatColor.GREEN + "Shop Removed!");
						log.log(Level.INFO, OBChestShop.getLogMsgPrefix() + player.getName() + " removed shop " + shopname);
	       			}
	
	       			// remove chest
					Bukkit.getWorld(worldname).getBlockAt(block.getLocation()).breakNaturally();
					
					// remove player directory if no further shops
					if (new File(OBChestShop.getInstance().getDataFolder() + "/Shops/" + owner).list().length == 0) {
						File ownerdir = new File(OBChestShop.getInstance().getDataFolder() + "/Shops/" + owner);
						ownerdir.delete();
					}

        		} else {
    				player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.RED + "You cannot break another players shop!");
    				event.setCancelled(true);
    			}
        	}
        }
	}
	

}
