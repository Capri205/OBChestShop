package net.obmc.OBChestShop.Commands;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.obmc.OBChestShop.OBChestShop;
import net.obmc.OBChestShop.ShopStates.ShopState;
import net.obmc.OBChestShop.Shop.Shop;


public class OnCommand implements CommandExecutor {

	Logger log = Logger.getLogger("Minecraft");

	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		// usage if no arguments passed
		if (args.length == 0) {
			Usage(sender);
			return true;
		}
		
        Player player = (Player) sender;

        //TODO: do we need to make this player only command or ok to run from console?
        
		// process the command and any arguments
		if (command.getName().equalsIgnoreCase("obshop") || command.getName().equalsIgnoreCase("obs")) {

			String arg = "";

			switch (args[0].toLowerCase()) {

				// list shops 
				case "list":
					if (args.length > 1 && args[1].equals("all")) {
						OBChestShop.getShopList().listShops(player, "all");
					} else {
						OBChestShop.getShopList().listShops(player, "");
					}
					break;
			    // status of shops 
				case "status":
					if (args.length > 1 && args[1].equals("all")) {
						arg = "all";
					}
					OBChestShop.getShopList().listShopStatus(player, arg);
					break;
				// location of shops
				case "location":
					if (args.length > 1 && args[1].equals("all")) {
						arg = "all";
					}
					OBChestShop.getShopList().listShopLocations(player, arg);
					break;
                // remove a shop
				case "remove":
					//TODO: if console command then we need to use log
					if (args.length > 1) {
						// build shop name from all arguments - caters for shop names with spaces in them
						String shopname = args[1];
						for (int i = 2; i < args.length; i++) {
                            shopname = shopname + " " + args[i];
                        }
						
						//TODO: idea! see if we can trigger a shop removal event instead of calling removeShop - combine code from listener and command
						//TODO: see if we can also use an index number for removal. Would mean tracking unique ID along with name in the shop object
						
						if (OBChestShop.getShopList().shopExists(shopname)) {
							
			        		// check ownership or OP
			        		if (player.getUniqueId().toString().equals(OBChestShop.getShopList().getShopOwner(shopname)) || player.isOp()) {
			        			
								// see if world shop exists in is accessible
								if (Bukkit.getWorld(OBChestShop.getShopList().getShopWorld(shopname)) != null) {
							
									// remove shop config file and directory if no more shop files for player
									String owner = OBChestShop.getShopList().getShopOwner(shopname);
									File shopfile = new File(OBChestShop.getInstance().getDataFolder() + "/Shops/" + owner + "/" + shopname + ".yml");
									if (shopfile.exists()) {
										if (!shopfile.delete()) {
											player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.RED + "Failed to remove shop configuration file. Check with the server operator why");
											log.log(Level.INFO, OBChestShop.getLogMsgPrefix() + "Failed to remove shop configuration file:");
											log.log(Level.INFO, OBChestShop.getLogMsgPrefix() + "    " + shopfile.getAbsolutePath() + shopname + ".yml");
											break;
										}
										// remove player directory if no further shops
										if (new File(OBChestShop.getInstance().getDataFolder() + "/Shops/" + OBChestShop.getShopList().getShopOwner(shopname)).list().length == 0) {
											File ownerdir = new File(OBChestShop.getInstance().getDataFolder() + "/Shops/" + owner);
											ownerdir.delete();
										}
									} else {
											log.log(Level.INFO, OBChestShop.getLogMsgPrefix() + "Strange.. No config file to remove for shop " + shopname + ". Lets's proceed anyway");
											log.log(Level.INFO, OBChestShop.getLogMsgPrefix() + "    " + shopfile.getAbsolutePath() + shopname + ".yml");
									}
								
									// get count of shops on the chest
									int shopcount = OBChestShop.getShopList().getShopCountByLocation(OBChestShop.getShopList().getShopChestLocation(shopname));
									if (shopcount > 1) {
										// remove sign only
										Location signloc = OBChestShop.getShopList().getShopSignLocation(shopname);
										Bukkit.getWorld(OBChestShop.getShopList().getShopWorld(shopname)).getBlockAt(signloc).breakNaturally();
									} else {
										// remove sign and chest
										Location signloc = OBChestShop.getShopList().getShopSignLocation(shopname);
										Bukkit.getWorld(OBChestShop.getShopList().getShopWorld(shopname)).getBlockAt(signloc).breakNaturally();
										Location chestloc = OBChestShop.getShopList().getShopChestLocation(shopname);
										Bukkit.getWorld(OBChestShop.getShopList().getShopWorld(shopname)).getBlockAt(chestloc).breakNaturally();
									}
									
									if (OBChestShop.getShopList().removeShop(shopname)) {
										player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.GREEN + "Shop Removed!");
									}
								} else {
									player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.RED + "The world the shop exists in not presently available!");
									player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.RED + "Check with the server operator and reinstate the world,");
									player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.RED + "    or remove the shop file and reload the plugin or restart the server.");
								}
			        		} else {
			        			player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.RED + "You cannot remove another players shop!");
			        		}
						} else {
							player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.RED + "That shop doesn't exist");
						}
					} else {
						Usage(sender);
					}
					break;
				// status of shops 
				case "autofix":
					if (args.length > 1 && args[1].equals("enable")) {
						OBChestShop.enableAutoFix();
						player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.GREEN + "Shop autofix enabled");
					}
					if (args.length > 1 && args[1].equals("disable")) {
						OBChestShop.disableAutoFix();
						player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.GREEN + "Shop autofix disabled");
					}
					break;
				// fix a shop
				case "fix":
					if (args.length > 1) {
						String shopname = args[1];
						if (OBChestShop.getShopList().shopExists(shopname)) {
							Shop shop = OBChestShop.getShopList().getShop(shopname);
							if (shop.validateWorld().compareTo(ShopState.WorldOK) < 0) {
								player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.RED + "The world the shop exists in not presently available!");
								player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.RED + "Check with the server operator and reinstate the world,");
								player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.RED + "    or remove the shop file and reload the plugin or restart the server.");
								return true;
							}
							player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.GREEN + "World validated");
							if (shop.validateChest().compareTo(ShopState.ChestOK) < 0) {
								shop.reinstateChest();
								if (shop.validateChest().compareTo(ShopState.ChestOK) < 0) {
									player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.RED + "Failed. Chest for shop '" + shopname + "' could not be reinstated");
									return true;
								} else {
									player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.GREEN + "Chest was invalid, but successfully reinstated");
								}
							} else {
								player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.GREEN + "Chest ok");
							}
							if (shop.validateSign().compareTo(ShopState.SignOK) < 0) {
								shop.reinstateSign();
								if (shop.validateSign().compareTo(ShopState.SignOK) < 0) {
									player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.RED + "Failed. Sign for shop '" + shopname + "' could not be reinstated");
									return true;
								} else {
									player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.GREEN + "Sign was invalid, but successfully reinstated");
								}
							} else {
								player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.GREEN + "Sign ok");
							}
							player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.GREEN + "Checks completed for shop '" + shopname + "'");
						} else {
							player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.RED + "No shop exists called '" + shopname + "'");
						}
					}
					
					break;
				// any other command
				default:
					Usage(sender);
					break;
			}
			return true;
		} else {
			Usage(sender);
			return true;
		}
	}

	void Usage(CommandSender sender) {
    	sender.sendMessage(ChatColor.LIGHT_PURPLE + "/obshop" + ChatColor.GOLD + " - Display this menu");
    	sender.sendMessage(ChatColor.LIGHT_PURPLE + "/obshop remove <shopname " + ChatColor.ITALIC + "or " + ChatColor.RESET + "" + ChatColor.LIGHT_PURPLE + "all>" + ChatColor.GOLD + " - remove a shop of yours");
    	sender.sendMessage(ChatColor.LIGHT_PURPLE + "/obshop list [all]" + ChatColor.GOLD + " - List your shops or all shops");
    	sender.sendMessage(ChatColor.LIGHT_PURPLE + "/obshop status [all]" + ChatColor.GOLD + " - Show status of your shops or all shops");
    	sender.sendMessage(ChatColor.LIGHT_PURPLE + "/obshop fix <shopname>" + ChatColor.GOLD + " - Attempt to fix an invalid shop");
    	sender.sendMessage(ChatColor.LIGHT_PURPLE + "/obshop autofix [enable|disable]" + ChatColor.GOLD + " - Enable/disable shop auto fix");
	}
	
}
