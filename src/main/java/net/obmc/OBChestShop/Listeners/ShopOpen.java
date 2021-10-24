package net.obmc.OBChestShop.Listeners;

import java.util.logging.Logger;

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

    	if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            final Block block = event.getClickedBlock();
            final Player player = event.getPlayer();

            if (OBChestShop.getShopList().shopRelatedBlock(block.getLocation()) &&
            	Tag.SIGNS.isTagged(block.getType())) {

            	Shop shop = OBChestShop.getShopList().getShopBySignLocation(block.getLocation());

            	if (shop.getState().compareTo(ShopState.ShopOK) != 0) {
            		String errmsg = OBChestShop.getChatMsgPrefix() + ChatColor.RED;
            		if (shop.getOwner().equals(player.getUniqueId().toString())) {
            			errmsg += "Your shop is invalid. Please use /obs fix " + shop.getName() + " to fix it";
            		} else {
            			errmsg += shop.getOwnerName() + " needs to fix the shop before it can be used";
            		}
            		event.getPlayer().sendMessage(errmsg);
            	} else {
            		if (shop.isOpen() || shop.getOwner().equals(player.getUniqueId().toString())) {
            			Selling shopsellmenu = new Selling(shop.getName(), player);
            			shopsellmenu.draw();
            		}
            	}
            }
    	}
    }
}
