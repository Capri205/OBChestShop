package net.obmc.OBChestShop.Listeners;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import net.md_5.bungee.api.ChatColor;
import net.obmc.OBChestShop.OBChestShop;
import net.obmc.OBChestShop.ShopStates.ShopState;
import net.obmc.OBChestShop.Menus.Selling;
import net.obmc.OBChestShop.Shop.Shop;

public class ShopOpen implements Listener {

	Logger log = Logger.getLogger("Minecraft");

    @EventHandler
    public void onInteract(final PlayerInteractEvent event) {

    	if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
    		return;
    	}
    	
        final Block block = event.getClickedBlock();
        if (!OBChestShop.getShopList().shopRelatedBlock(block.getLocation())) {
        	return;
        }
        
        if (!Tag.ALL_SIGNS.isTagged(block.getType())) {
        	return;
        }
        
        final Player player = event.getPlayer();
        Shop shop = OBChestShop.getShopList().getShopBySignLocation(block.getLocation());
        if (Bukkit.getOfflinePlayer(UUID.fromString(shop.getOwner())) == null) {
			event.getPlayer().sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.RED + "Shop owner invalid (" + shop.getOwnerName() +"). Removed from server player data.");
			event.getPlayer().sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.RED + "Raise this issue to the server administrators. Shop will be disabled for now.");
			shop.setState(ShopState.ShopLostOwner);
			event.setCancelled(true);
        }

       	if (shop.getState().compareTo(ShopState.ShopOK) != 0) {
            String errmsg = OBChestShop.getChatMsgPrefix() + ChatColor.RED;
            if (shop.getOwner().equals(player.getUniqueId().toString())) {
            	errmsg += "Your shop is invalid. Please use /obs fix " + shop.getName() + " to fix it";
           	} else {
           		errmsg += shop.getOwnerName() + " needs to fix the shop before it can be used";
           	}
           	event.getPlayer().sendMessage(errmsg);
           	event.setCancelled(true);
           	return;
       	}
       	
   		if (!shop.isOpen() && !shop.getOwner().equals(player.getUniqueId().toString())) {
         	event.getPlayer().sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.RED + "Shop needs to be opened by the owner (" + shop.getOwnerName() +")");
         	event.setCancelled(true);
         	return;
      	}
   		
        Selling shopsellmenu = new Selling(shop.getName(), player);
        event.setCancelled(true);
        shopsellmenu.draw();

    }
}
