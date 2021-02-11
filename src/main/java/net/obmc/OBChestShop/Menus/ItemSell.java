package net.obmc.OBChestShop.Menus;

import java.util.Arrays;
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
import net.obmc.OBChestShop.Shop.ShopItemTypes;
import net.obmc.OBChestShop.ShopItem.ShopItem;

public class ItemSell {

	Logger log = Logger.getLogger("Minecraft");
	
    private Player player;
    private Inventory inv;
    private Shop shop;
    
	public ItemSell(Player player, String shopname, ShopItem shopitem) {
		
		this.player = player;
    	this.shop = OBChestShop.getShopList().getShop(shopname);
		ShopItem stockitem = shop.getShopItem(ShopItemTypes.Stock, shopitem.getItemName());
    	inv = Bukkit.createInventory(null, 54, ChatColor.DARK_AQUA + "[SELL " + shopitem.getItem().getType().name() + "]" + " " + ChatColor.DARK_GREEN + shopname);
    	inv.clear();
    	
    	ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Back");
        backMeta.setLocalizedName(ShopItemTypes.Sell.toString() + "#" + shopitem.getSlot());
        back.setItemMeta(backMeta);
        inv.setItem(0, back);
        
        ItemStack item = new ItemStack(Material.valueOf(shopitem.getItem().getType().name()), 1);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "" + ChatColor.BOLD + shopitem.getItem().getType().name() + ChatColor.AQUA + " (" + stockitem.getStockQuantity() + ")");
        item.setItemMeta(itemMeta);
        inv.setItem(4,  item);
        
    	ItemStack divider = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
        for (int i = 9; i < 18; i++) {
        	inv.setItem(i, divider);
        }
        
        Boolean hasOne = stockitem.getStockQuantity() >= 1;
        Boolean hasEight = stockitem.getStockQuantity() > 8;
        Boolean hasQuarterStack = stockitem.getStockQuantity() > 16;
        Boolean hasHalfStack = stockitem.getStockQuantity() > 32;
        Boolean hasStack = stockitem.getStockQuantity() > 64;

        // BUY ONE
        ItemStack sell1 = new ItemStack(Material.LIME_WOOL, 1);
        ItemMeta sell1Meta = sell1.getItemMeta();
        if (OBChestShop.getEconomy().getBalance(Bukkit.getOfflinePlayer(player.getUniqueId())) < shopitem.getPrice()) {
        	sell1 = new ItemStack(Material.WHITE_WOOL, 1);
         	sell1Meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "You do not have enough money");
        } else if (hasOne) {
        	sell1Meta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Left Click" + ChatColor.GRAY + " to buy" + ChatColor.LIGHT_PURPLE + " one " + ChatColor.GRAY + "item");
        	sell1Meta.setLore(Arrays.asList(ChatColor.WHITE + "You pay $" + shopitem.getPriceFormatted()));
        } else {
        	sell1 = new ItemStack(Material.WHITE_WOOL, 1);
         	sell1Meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Not enough items in stock for one item");
        }
        sell1.setItemMeta(sell1Meta);
        inv.setItem(27, sell1);
        
        // BUY 8
        ItemStack sell8 = new ItemStack(Material.LIME_CONCRETE, 1);
        ItemMeta sell8Meta = sell8.getItemMeta();
        if (OBChestShop.getEconomy().getBalance(Bukkit.getOfflinePlayer(player.getUniqueId())) < (shopitem.getPrice()*8.0)) {
        	sell8 = new ItemStack(Material.WHITE_WOOL, 1);
         	sell8Meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "You do not have enough money");
        } else if (hasEight) {
        	sell8Meta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Left Click" + ChatColor.GRAY + " to buy" + ChatColor.LIGHT_PURPLE + " eight " + ChatColor.GRAY + "items");
        	sell8Meta.setLore(Arrays.asList(ChatColor.WHITE + "You pay $" + shopitem.getPriceFormatted(8)));
        } else {
        	sell8 = new ItemStack(Material.WHITE_WOOL, 1);
         	sell8Meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Not enough items in stock for eight items");
        }
        sell8.setItemMeta(sell8Meta);
        inv.setItem(28, sell8);

        // BUY 16
        ItemStack sell16 = new ItemStack(Material.LIME_TERRACOTTA, 1);
        ItemMeta sell16Meta = sell16.getItemMeta();
        if (OBChestShop.getEconomy().getBalance(Bukkit.getOfflinePlayer(player.getUniqueId())) < (shopitem.getPrice()*16.0)) {
        	sell16 = new ItemStack(Material.WHITE_WOOL, 1);
         	sell16Meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "You do not have enough money");
        } else if (hasQuarterStack) {
        	sell16Meta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Left Click" + ChatColor.GRAY + " to buy" + ChatColor.LIGHT_PURPLE + " quarter stack " + ChatColor.GRAY + "(16 items)");
        	sell16Meta.setLore(Arrays.asList(ChatColor.WHITE + "You pay $" + shopitem.getPriceFormatted(16)));

        } else {
        	sell16 = new ItemStack(Material.WHITE_WOOL, 1);
         	sell16Meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Not enough items in stock for quarter stack");
        }
        sell16.setItemMeta(sell16Meta);
        inv.setItem(29, sell16);        

        // BUY 32
        ItemStack sell32 = new ItemStack(Material.GREEN_CONCRETE_POWDER, 1);
        ItemMeta sell32Meta = sell32.getItemMeta();
        if (OBChestShop.getEconomy().getBalance(Bukkit.getOfflinePlayer(player.getUniqueId())) < (shopitem.getPrice()*32.0)) {
        	sell32 = new ItemStack(Material.WHITE_WOOL, 1);
         	sell32Meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "You do not have enough money");
        } else if (hasHalfStack) {
        	sell32Meta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Left Click" + ChatColor.GRAY + " to buy" + ChatColor.LIGHT_PURPLE + " half a stack " + ChatColor.GRAY + "(32 items)");        	
        	sell32Meta.setLore(Arrays.asList(ChatColor.WHITE + "You pay $" + shopitem.getPriceFormatted(32)));
        } else {
        	sell32 = new ItemStack(Material.WHITE_WOOL, 1);
         	sell32Meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Not enough items in stock for half a stack");
        }
        sell32.setItemMeta(sell32Meta);
        inv.setItem(30, sell32);
        
        // BUY 64
        ItemStack sell64 = new ItemStack(Material.GREEN_CONCRETE, 1);
        ItemMeta sell64Meta = sell64.getItemMeta();
        if (OBChestShop.getEconomy().getBalance(Bukkit.getOfflinePlayer(player.getUniqueId())) < (shopitem.getPrice()*64.0)) {
        	sell64 = new ItemStack(Material.WHITE_WOOL, 1);
         	sell64Meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "You do not have enough money");
        } else if (hasStack) {
        	sell64Meta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Left Click" + ChatColor.GRAY + " to buy" + ChatColor.LIGHT_PURPLE + " a stack " + ChatColor.GRAY + "(64 items)");        	
        	sell64Meta.setLore(Arrays.asList(ChatColor.WHITE + "You pay $" + shopitem.getPriceFormatted(64)));
        } else {
        	sell64 = new ItemStack(Material.WHITE_WOOL, 1);
         	sell64Meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Not enough items in stock for a full stack");
        }
        sell64.setItemMeta(sell64Meta);
        inv.setItem(31, sell64); 
	}

	 public void draw() {
		 player.openInventory(inv);
	 }
}
