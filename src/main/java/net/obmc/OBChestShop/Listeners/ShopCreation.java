package net.obmc.OBChestShop.Listeners;

import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Lockable;
import org.bukkit.block.Sign;
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
        Block signblock = event.getBlock();
        Block chestblock = BlockUtils.getSignAttachedBlock(signblock);

        // check this is shop create sign
        if (event.getLine(0).equalsIgnoreCase("[obshop]")) {
        	
        	// check it's attached to a suitable type of block - must be an inventory type block and lockable or an ender_chest
        	String chesttype = chestblock.getType().toString();
        	if ((chestblock.getState() instanceof InventoryHolder && chestblock.getState() instanceof Lockable && (chesttype.equals("CHEST") ||
        			chesttype.equals("TRAPPED_CHEST") || chesttype.contains("SHULKER_BOX") || chesttype.equals("BARREL"))) ||
        			chesttype.equals("ENDER_CHEST")) {

        		// sort out ownership of the chest
        		//	empty chest => nobody owner, so set owner to player
        		//	someone owner => deny shop placement unless op and then owner is current chest owner
        		//  TODO: consider a config option to deny vertical placement - ie. chest directly on top of another
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

        		// create the shop
        		final String shopowner = owner;
        		if (goodtogo) {
        			
        			// get the name from the sign
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
        					.onClose(p -> {})
        					.onComplete((p, guireturnvalue) -> {
        						shopname = guireturnvalue;
        						createShop(player, chestblock, signblock, shopname, shopowner);
       							return AnvilGUI.Response.close();
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
        			
        		} else {
        			event.getPlayer().sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.RED + "Can't place a shop here!");
        			event.getPlayer().sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.RED + "This location is already taken by " + Bukkit.getPlayer(UUID.fromString(owner)).getName());
        			signblock.breakNaturally();
        		}
        	} else {
        		event.getPlayer().sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.RED + "Not a suitable chest type");
        	}
        }
	}

	private boolean createShop(Player player, Block chestblock, Block signblock, String shopname, String shopowner) {
		//TODO: validate name
		//			is text and <= 35 chars and name not already taken, replace "," for "_"
		if (OBChestShop.getShopList().shopExists(shopname)) {
			player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.RED + "Shop name already taken");
			signblock.breakNaturally();
			return false;
		}

		Shop shop = new Shop(shopowner, shopname, chestblock.getLocation(), signblock.getLocation());
		//TODO: change to check shop state
		if (!shop.CreateConfig(shopname)) {
			player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.RED + "Shop creation failed!");
  			signblock.breakNaturally();
  			return false;
		}
			
		// add shop to our live list
		OBChestShop.getShopList().addShop(shopname, shop);
		player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.GREEN + "Shop Created!");
			
		shop.setStatus(ShopState.ShopOK);

		// update the sign on the chest to reflect the shop
		Sign sign = (Sign) signblock.getState();
		sign.setLine(0, ChatColor.AQUA + "**************");
		sign.setLine(1, ChatColor.GREEN + "Shop");
		sign.setLine(2, ChatColor.RED + "Closed");
		sign.setLine(3, ChatColor.AQUA + "**************");
		for (Player players : Bukkit.getOnlinePlayers()) {
            players.sendSignChange(sign.getBlock().getLocation(), sign.getLines());
        }
		sign.update(true);

		return true;
	}

}
