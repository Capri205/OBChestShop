package net.obmc.OBChestShop.Listeners;

import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Lockable;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import net.obmc.OBChestShop.OBChestShop;
import net.obmc.OBChestShop.Utils.BlockUtils;
import net.obmc.OBChestShop.Shop.Shop;
import net.obmc.OBChestShop.ShopStates.ShopState;

import net.wesjd.anvilgui.AnvilGUI;

public class ShopCreation implements Listener {

	Logger log = Logger.getLogger("Minecraft");
	
	String shopname = "";
	Player p = null;
	
	public ShopCreation() {

		log.log(Level.INFO, OBChestShop.getLogMsgPrefix() + "    Shop creation listener registered");
	}
	
	@EventHandler
    public void onCreate(SignChangeEvent event) {
		
        final Player player = event.getPlayer();
        
        // exit if this isn't a shop create sign
        if (!event.getLine(0).equalsIgnoreCase("[obshop]")) {
        	return;
        }

        Block signblock = event.getBlock();
        Block chestblock = BlockUtils.getSignAttachedBlock(signblock);

        // exit if not the appropriate container
        String chesttype = chestblock.getType().toString();
        if ( !(chesttype.equals("CHEST") || chesttype.equals("TRAPPED_CHEST") || chesttype.contains("SHULKER_BOX") || chesttype.equals("BARREL") || chesttype.equals("ENDER_CHEST"))) {
        	event.getPlayer().sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.RED + "Not a suitable chest type");
        	event.getPlayer().sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.RED + "Place the sign on a Chest, EnderChest, Barrel, or Shulker Box.");
        	if (player.getGameMode().equals(GameMode.SURVIVAL)) signblock.breakNaturally();
        	return;
        }
            
   		// sort out ownership of the chest
   		// empty chest => nobody owner, so set owner to player
   		// someone owner => deny shop placement unless op and then owner is current chest owner
   		// TODO: consider a config option to deny vertical placement - ie. different players chest directly on top of another players chest
   		String owner = OBChestShop.getShopList().getShopOwnerByLocation(chestblock.getLocation());
   		Boolean goodtogo = false;
   		if (!owner.isEmpty()) {
   			if (player.getUniqueId().toString().equals(owner)) {
   				goodtogo = true;
   			} else {
   				if (player.isOp()) {
   					goodtogo = true;
   				}
   			}
   		} else {
  			owner = player.getUniqueId().toString();
   			goodtogo = true;
   		}
        final String shopowner = owner;
        if (!goodtogo) {
			event.getPlayer().sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.RED + "Can't place a shop here!");
			event.getPlayer().sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.RED + "This location is already taken by " + Bukkit.getPlayer(UUID.fromString(owner)).getName());
			if (player.getGameMode().equals(GameMode.SURVIVAL)) signblock.breakNaturally();
        	return;
        }

        // create the shop
		if (event.getLine(1).isEmpty()) {
        			
        	// get the name of the shop using an anvil gui and create the shop
        	ItemStack guiitem = new ItemStack(Material.PAPER, 1);
        	ItemMeta meta = guiitem.getItemMeta();
        	meta.setDisplayName("OBChestShop - Shop name");	// doesnt appear anywhere in anvil ui
        	meta.setLore(Arrays.asList("Enter/Confirm shop name"));	//lore of items in anvil ui
        	guiitem.setItemMeta(meta);
        	shopname = "?";
	        		
        	AnvilGUI gui = new AnvilGUI.Builder()
        		.text(shopname)				// text in entry box in anvil ui
        		.title("Enter shop name:")	// text above entry box in anvil ui
        		.itemLeft(guiitem)
        		.onClose(stateSnapshot -> {})
        		.onClick((slot, stateSnapshot) -> {
        			createShop(player, chestblock, signblock, stateSnapshot.getText(), shopowner);
        			return Arrays.asList(
    					AnvilGUI.ResponseAction.close()
    				);
        		})
        		.plugin(OBChestShop.getInstance()).open(player);
        } else {
        	// get the name of the shop from the sign and create the shop
        	new BukkitRunnable() {
        		public void run() {
        			createShop(player, chestblock, signblock, event.getLine(1), shopowner);
        		}
        	}.runTaskLater(OBChestShop.getInstance(), 20L);
        }
	}

	// create a new shop
	private boolean createShop(Player player, Block chestblock, Block signblock, String shopname, String shopowner) {

		// if shop doesn't already exist, create it, setup config and populate shop sign and finally validate it
		if (!OBChestShop.getShopList().shopExists(shopname)) {
		
			Shop shop = new Shop(shopowner, shopname, chestblock.getLocation(), signblock.getLocation());
			if (shop.CreateConfig(shopname).compareTo(ShopState.ShopPreChecks) == 0) {
			
				// add shop to our live list
				OBChestShop.getShopList().addShop(shopname, shop);
				player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.GREEN + "Shop Created!");
				
				// update the sign on the chest to reflect the shop
				Sign sign = (Sign) signblock.getState();
				sign.getSide(Side.FRONT).setLine(0, ChatColor.AQUA + "**************");
				sign.getSide(Side.FRONT).setLine(1, ChatColor.GREEN + "Shop");
				sign.getSide(Side.FRONT).setLine(2, ChatColor.RED + "Closed");
				sign.getSide(Side.FRONT).setLine(3, ChatColor.AQUA + "**************");
				if (shop.isSigndoublesided()) {
					sign.getSide(Side.BACK).setLine(0, ChatColor.AQUA + "**************");
					sign.getSide(Side.BACK).setLine(1, ChatColor.GREEN + "Shop");
					sign.getSide(Side.BACK).setLine(2, ChatColor.RED + "Closed");
					sign.getSide(Side.BACK).setLine(3, ChatColor.AQUA + "**************");
				}
				for (Player players : Bukkit.getOnlinePlayers()) {
					// reinstate once spigot bug fixed in 1.20.1
					//players.sendSignChange(sign.getBlock().getLocation(), sign.getSide(Side.FRONT).getLines());
				}
				sign.update(true);
				
				shop.setState(shop.validateShop());
				if (shop.getState().compareTo(ShopState.ShopOK) == 0) {
					return true;
				} else {
					player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.RED + "Shop validation failed! Shop state is " + shop.getState().toString());
				}
				
			} else {
				player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.RED + "Shop creation failed! Shop state is " + shop.getState().toString());
			}
			shop.logShopMessage();
			
		} else {
			player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.RED + "Shop name already taken");
		}
		signblock.breakNaturally();
		return false;
		
	}

}
