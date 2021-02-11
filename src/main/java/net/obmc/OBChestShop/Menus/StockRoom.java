package net.obmc.OBChestShop.Menus;

import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.obmc.OBChestShop.OBChestShop;
import net.obmc.OBChestShop.Shop.Shop;
import net.obmc.OBChestShop.Shop.ShopItemTypes;
import net.obmc.OBChestShop.ShopItem.ShopItem;

public class StockRoom {

	Logger log = Logger.getLogger("Minecraft");

    private Shop shop;
    private Inventory inv;
    private Player player;

    public StockRoom(String shopname, Player player, int page) {
        
    	this.player = player;
    	this.shop = OBChestShop.getShopList().getShop(shopname);

        inv = Bukkit.createInventory(null, 54, ChatColor.DARK_AQUA + "[STOCK]" + " " + ChatColor.DARK_GREEN + shopname);
        inv.clear();
        
    	ItemStack close = new ItemStack(Material.ARROW);
        ItemMeta backMeta = close.getItemMeta();
        backMeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Close");
        backMeta.setLocalizedName(String.valueOf(page));
        close.setItemMeta(backMeta);
        inv.setItem(0, close);
        
        ItemStack sellmenu = new ItemStack(Material.LANTERN);
        ItemMeta sellmenuMeta = sellmenu.getItemMeta();
        sellmenuMeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Sell menu (shop sells to player)");
        sellmenu.setItemMeta(sellmenuMeta);
        inv.setItem(2, sellmenu);
        
        ItemStack buymenu = new ItemStack(Material.SOUL_LANTERN);
        ItemMeta buymenuMeta = buymenu.getItemMeta();
        buymenuMeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Buy menu (shop buy's from player)");
        buymenu.setItemMeta(buymenuMeta);
        inv.setItem(3, buymenu);
        
        ItemStack options = new ItemStack(Material.ENDER_CHEST);
        ItemMeta optionsMeta = options.getItemMeta();
        optionsMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + shopname);
        optionsMeta.setLore(Arrays.asList(ChatColor.GRAY + "" + shop.getDescription(), " ",
        		ChatColor.YELLOW + "" + ChatColor.BOLD + "Owner: " + ChatColor.GREEN + "" + ChatColor.BOLD + shop.getOwnerName(),
        		" ",
        		ChatColor.YELLOW + "" + ChatColor.BOLD + "Left Click " + ChatColor.GRAY + "to open " + ChatColor.LIGHT_PURPLE + "Shop Settings",
        		" "));
        options.setItemMeta(optionsMeta);
    	inv.setItem(8, options);

    	int pagesize = 36;
    	int numpages = (int) Math.ceil((double)shop.getStockItemList().size() / pagesize);
    	ItemStack divider = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);

    	ItemStack prevdivider = new ItemStack(Material.GHAST_TEAR);
    	ItemMeta prevdividerMeta = prevdivider.getItemMeta();
    	prevdividerMeta.setDisplayName("Previous Page");
    	
    	ItemStack nextdivider = new ItemStack(Material.GOLD_NUGGET);
    	ItemMeta nextdividerMeta = nextdivider.getItemMeta();
    	nextdividerMeta.setDisplayName("Next Page");

    	if (numpages <= 1) {
    		for (int i = 9; i < 18; i++) {
    			inv.setItem(i, divider);
    		}
    	} else {
    		if (page == numpages && numpages > 1) {
    			inv.setItem(9, prevdivider);
    			inv.setItem(17, divider);
    		}
    		if (page == 1 && numpages > 1) {
    			inv.setItem(9, divider);
    			inv.setItem(17, nextdivider);
    		}
    		if (page != 1 && page != numpages) {
    			inv.setItem(9, prevdivider);
    			inv.setItem(17, nextdivider);
    		}
    		for (int i = 10; i < 17; i++) {
    			inv.setItem(i, divider);
    		}
    	}
        
        // load up items into menu - determined by sell (direct) or buy (offset)
        ShopItem shopitem = null;
        ItemStack item = null;
        ItemMeta itemmeta = null;
        Iterator <Integer> isit = shop.getStockItems().keySet().iterator();
        int slot;
        int startslot = 18 + ((page-1)*pagesize);
        int endslot = 53 + ((page-1)*pagesize);
        while (isit.hasNext()) {
        	slot = isit.next();
        	if (slot >= startslot && slot <= endslot) {
        		shopitem = shop.getShopItem(ShopItemTypes.Stock, slot);
        		item = shopitem.getItem();
            	itemmeta = item.getItemMeta();
            	itemmeta.setLore(Arrays.asList(shopitem.getLore(shopname, ShopItemTypes.Stock).split(",")));
            	item.setItemMeta(itemmeta);
            	inv.setItem(slot-((page-1)*pagesize), item);
        	}
        }
    }
    
    public void draw() {
    	player.openInventory(inv);
    }
}
