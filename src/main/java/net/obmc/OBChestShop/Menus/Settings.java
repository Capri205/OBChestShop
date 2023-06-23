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

public class Settings {

	Logger log = Logger.getLogger("Minecraft");

    private Shop shop;
    private Inventory inv;
    private Player player;

    public Settings(ShopItemTypes type, Player player, String shopname, int page) {

    	this.player = player;
    	this.shop = OBChestShop.getShopList().getShop(shopname);
    	
    	inv = Bukkit.createInventory(null, 54, ChatColor.DARK_AQUA + "[SETTINGS]" + " " + ChatColor.DARK_GREEN + shopname);
    	inv.clear();

    	ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Back");
        backMeta.setLocalizedName(type.toString() + "#" + String.valueOf(page));
        back.setItemMeta(backMeta);
        inv.setItem(0, back);

    	ItemStack name = new ItemStack(Material.NAME_TAG);
        ItemMeta nameMeta = name.getItemMeta();
        nameMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Change Name");
        nameMeta.setLore(Arrays.asList(
            	ChatColor.YELLOW + "" + ChatColor.BOLD + "Current Name: " + ChatColor.GRAY + "" + ChatColor.BOLD + shop.getName(),
            	" ",
            	ChatColor.YELLOW + "" + ChatColor.BOLD + "Left Click " + ChatColor.GRAY + "to change shop " + ChatColor.LIGHT_PURPLE + " Name"
        ));
        name.setItemMeta(nameMeta);
        inv.setItem(2, name);

    	ItemStack desc = new ItemStack(Material.NAME_TAG);
        ItemMeta descMeta = desc.getItemMeta();
        descMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Change Description");
        String description = shop.getDescription();
        if (shop.getDescription() == null || shop.getDescription().isEmpty()) {
        	description = ChatColor.ITALIC + "" + "no description set";
        }
        descMeta.setLore(Arrays.asList(
        	ChatColor.YELLOW + "" + ChatColor.BOLD + "Current Description: " + ChatColor.GRAY + "" + ChatColor.BOLD + description,
        	" ",
        	ChatColor.YELLOW + "" + ChatColor.BOLD + "Left Click " + ChatColor.GRAY + "to change shop " + ChatColor.LIGHT_PURPLE + " Description"
		));
        desc.setItemMeta(descMeta);
        inv.setItem(4, desc);

    	ItemStack limit = new ItemStack(Material.COMPASS);
        ItemMeta limitMeta = limit.getItemMeta();
        limitMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Set Stock Limit");
        limitMeta.setLore(Arrays.asList(ChatColor.YELLOW + "" + ChatColor.BOLD + "Current Limit: " + ChatColor.GRAY + "" + ChatColor.BOLD + shop.getStockLimit(),
        								" ",
        								ChatColor.YELLOW + "" + ChatColor.BOLD + "Left Click " + ChatColor.GRAY + "to change shop " + ChatColor.LIGHT_PURPLE + " stock limit")
        );
        limit.setItemMeta(limitMeta);
        inv.setItem(6, limit);

    	ItemStack open = new ItemStack(Material.LIME_WOOL);
    	ChatColor color = ChatColor.GREEN;
    	String msg = "Open";
    	if (!shop.isOpen()) {
    		open = new ItemStack(Material.RED_WOOL);
    		color = ChatColor.RED;
    		msg = "Closed";
    	}
        ItemMeta openMeta = desc.getItemMeta();
        openMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Toggle Shop " + ChatColor.DARK_GREEN + "Open" + ChatColor.GREEN + "/" + ChatColor.RED + "Closed");
        openMeta.setLore(Arrays.asList(
        	ChatColor.YELLOW + "" + ChatColor.BOLD + "Left Click " + ChatColor.GRAY + "to toggle shop " + ChatColor.LIGHT_PURPLE + " Open or Closed",
        	ChatColor.GRAY + "Shop is currently " + color + msg ));
        open.setItemMeta(openMeta);
        inv.setItem(8, open);
        
        // determine divider style and navigation buttons based on list and number of items in the list
        int pagesize = 36;
    	int numpages = 0;
    	ItemStack divider = null;
    	switch (type) {
    	case Sell:
    		numpages = (int) Math.ceil((double)shop.getSellItemList().size() / pagesize);
    		divider = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
    		break;
    	case Buy:
    		numpages = (int) Math.ceil((double)shop.getBuyItemList().size() / pagesize);
    		divider = new ItemStack(Material.CYAN_STAINED_GLASS_PANE);
    		break;
    	case Stock:
    		numpages = (int) Math.ceil((double)shop.getStockItemList().size() / pagesize);
    		divider = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
    		break;
    	}
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
        // load up shop items
        int slot;
        ShopItem shopitem = null;
        ItemStack item = null;
        ItemMeta itemmeta = null;
        Iterator<Integer> isit = shop.getShopItems(type).keySet().iterator();
        int startslot = 18 + ((page-1)*pagesize);
        int endslot = 53 + ((page-1)*pagesize);
        while (isit.hasNext()) {
        	slot = isit.next();
        	if (slot >= startslot && slot <= endslot) {
        		shopitem = shop.getShopItemBySlot(type, slot);
        		item = shopitem.getItem();
            	itemmeta = item.getItemMeta();
            	itemmeta.setLore(Arrays.asList(shopitem.getLoreSettings(shopname, type).split(",")));
            	item.setItemMeta(itemmeta);
            	inv.setItem(slot-((page-1)*pagesize), item);
        	}
        }
    }
    
    public void draw() {
    	player.openInventory(inv);
    }
}
