package net.obmc.OBChestShop.Menus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Level;
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
import net.obmc.OBChestShop.ShopItem.ShopItem;

public class Settings {

	Logger log = Logger.getLogger("Minecraft");

    private Shop shop;
    private Inventory inv;
    private Player player;

    public Settings(Player player, String shopname) {

    	this.player = player;
    	this.shop = OBChestShop.getShopList().getShop(shopname);
    	//TODO: Ensure we have a valid shop? Must have been in a shop to get from the menu to here.. but what if deleted by someone in the mean time?
    	
    	inv = Bukkit.createInventory(null, 54, ChatColor.DARK_AQUA + "[SETTINGS]" + " " + ChatColor.DARK_GREEN + shopname);
    	inv.clear();

    	ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Back");
        back.setItemMeta(backMeta);
        inv.setItem(0, back);
    	
    	ItemStack name = new ItemStack(Material.NAME_TAG);
        ItemMeta nameMeta = name.getItemMeta();
        nameMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Change Name");
        nameMeta.setLore(Arrays.asList(ChatColor.YELLOW + "" + ChatColor.BOLD + "Left Click " + ChatColor.GRAY + "to change shop " + ChatColor.LIGHT_PURPLE + " Name"));
        name.setItemMeta(nameMeta);
        inv.setItem(2, name);

    	ItemStack desc = new ItemStack(Material.NAME_TAG);
        ItemMeta descMeta = desc.getItemMeta();
        descMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Change Description");
        descMeta.setLore(Arrays.asList(ChatColor.YELLOW + "" + ChatColor.BOLD + "Left Click " + ChatColor.GRAY + "to change shop " + ChatColor.LIGHT_PURPLE + " Description"));
        desc.setItemMeta(descMeta);
        inv.setItem(4, desc);

    	ItemStack limit = new ItemStack(Material.COMPASS);
        ItemMeta limitMeta = limit.getItemMeta();
        limitMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Set Stock Limit");
        limitMeta.setLore(Arrays.asList(ChatColor.YELLOW + "" + ChatColor.BOLD + "Left Click " + ChatColor.GRAY + "to change shop " + ChatColor.LIGHT_PURPLE + " stock limit"));
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
        
        ItemStack divider = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 9; i < 18; i++) {
        	inv.setItem(i, divider);
        }
        
        // load up shop items
        String itemname;
        ShopItem shopitem = null;
        ItemStack item = null;
        ItemMeta itemmeta = null;
        Iterator <String> isit = shop.getItems().keySet().iterator();
        int slot = 18;
        while (isit.hasNext()) {
        	itemname = isit.next();
        	shopitem = shop.getShopItem(itemname);
        	item = shopitem.getItem();
        	itemmeta = item.getItemMeta();
        	itemmeta.setLore(Arrays.asList(shopitem.getLoreSettings().split(",")));
        	item.setItemMeta(itemmeta);
        	inv.setItem(slot, item);
        	slot++;
        }

    }
    
    public void draw() {
        
    	player.openInventory(inv);
    }
}
