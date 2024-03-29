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

public class Buying {

	Logger log = Logger.getLogger("Minecraft");

    private Shop shop;
    private Inventory inv;
    private Player player;

    public Buying(String shopname, Player player) {
        
    	this.shop = OBChestShop.getShopList().getShop(shopname);
    	this.player = player;
    	
    	Boolean isowner = false;
    	if (player.getUniqueId().toString().equals(shop.getOwner())) {
    		isowner = true;
    	}
        
        inv = Bukkit.createInventory(null, 54, ChatColor.DARK_AQUA + "[BUY]" + " " + ChatColor.DARK_GREEN + shopname);
        inv.clear();
        
    	ItemStack close = new ItemStack(Material.ARROW);
        ItemMeta backMeta = close.getItemMeta();
        backMeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Close");
        close.setItemMeta(backMeta);
        inv.setItem(0, close);
        
        ItemStack buymenu = new ItemStack(Material.LANTERN);
        ItemMeta buymenuMeta = buymenu.getItemMeta();
        buymenuMeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Sell menu (shop sell's to player)");
        buymenu.setItemMeta(buymenuMeta);
        inv.setItem(2, buymenu);
        
        if (isowner) {
        	ItemStack stock = new ItemStack(Material.CHEST);
        	ItemMeta stockMeta = stock.getItemMeta();
        	stockMeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Stock Room");
        	stockMeta.setLore(Arrays.asList(ChatColor.YELLOW + "" + ChatColor.BOLD + "Left Click " + ChatColor.GRAY + "to open your " + ChatColor.GREEN + "stock room"));
        	stock.setItemMeta(stockMeta);
        	inv.setItem(4, stock);
        }

        ItemStack options = new ItemStack(Material.ENDER_CHEST);
        ItemMeta optionsMeta = options.getItemMeta();
        String optionsmsg = ChatColor.GRAY + "See owner about any changes to the shop";
        if (isowner) {
        	optionsmsg = ChatColor.YELLOW + "" + ChatColor.BOLD + "Left Click " + ChatColor.GRAY + "to open " + ChatColor.LIGHT_PURPLE + "Shop Settings";
        }
        optionsMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + shopname);
        String description = ChatColor.GRAY + "" + shop.getDescription();
        if ((shop.getDescription() == null || shop.getDescription().isEmpty()) && player.getUniqueId().toString().equals(shop.getOwner())) {
        	description = ChatColor.GRAY + "" + ChatColor.ITALIC + "" + "no description set yet!";
        }
        optionsMeta.setLore(
        	Arrays.asList(
        		description,
        		" ",
        		ChatColor.YELLOW + "" + ChatColor.BOLD + "Owner: " + ChatColor.GREEN + "" + ChatColor.BOLD + shop.getOwnerName(),
        		ChatColor.YELLOW + "" + ChatColor.BOLD + "Stock Limit: " + ChatColor.GRAY + "" + ChatColor.BOLD + shop.getStockLimit(),
        		" ",
        		optionsmsg,
        		" "
        	)
        );
        options.setItemMeta(optionsMeta);
    	inv.setItem(8, options);
    	
    	ItemStack divider = new ItemStack(Material.CYAN_STAINED_GLASS_PANE);
        for (int i = 9; i < 18; i++) {
        	inv.setItem(i, divider);
        }
        
        // load up shop sell items into menu
        ShopItem shopitem = null;
        ItemStack item = null;
        ItemMeta itemmeta = null;
        Iterator <Integer> isit = shop.getBuyItems().keySet().iterator();
        int slot;
        while (isit.hasNext()) {
        	slot = isit.next();
        	shopitem = shop.getShopItemBySlot(ShopItemTypes.Buy, slot);
        	item = shopitem.getItem();
        	itemmeta = item.getItemMeta();
        	itemmeta.setLore(Arrays.asList(shopitem.getLore(shopname, ShopItemTypes.Buy).split(",")));
        	item.setItemMeta(itemmeta);
        	inv.setItem(slot, item);
        }
    }
    
    public void draw() {
    	player.openInventory(inv);
    }
}
