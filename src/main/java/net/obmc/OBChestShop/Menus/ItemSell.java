package net.obmc.OBChestShop.Menus;

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

public class ItemSell {

	Logger log = Logger.getLogger("Minecraft");
	
    private Player player;
    private Inventory inv;
    private ShopItem shopitem;
    private Shop shop;
    
    //TODO: change to pass playername and shopitemname
	public ItemSell(Player player, String shopname, ShopItem shopitem) {
		
		this.player = player;
		this.shopitem = shopitem;
    	this.shop = OBChestShop.getShopList().getShop(shopname);
    	//TODO: Ensure we have a valid shop? Must have been in a shop to get from the menu to here.. but what if deleted by someone in the mean time?

    	inv = Bukkit.createInventory(null, 54, ChatColor.DARK_AQUA + "[SELL " + shopitem.getItem().getType().name() + "]" + " " + ChatColor.DARK_GREEN + shopname);
    	inv.clear();
    	
    	ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Back");
        back.setItemMeta(backMeta);
        inv.setItem(0, back);
        
        ItemStack item = new ItemStack(Material.valueOf(shopitem.getItem().getType().name()), 1);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "" + ChatColor.BOLD + shopitem.getItem().getType().name() + ChatColor.AQUA + " (" + shopitem.getStockQuantity() + ")");
        item.setItemMeta(itemMeta);
        inv.setItem(4,  item);
        
    	ItemStack divider = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
    	if (!shop.isOpen()) {
    		divider = new ItemStack(Material.RED_STAINED_GLASS_PANE);
    	}
        for (int i = 9; i < 18; i++) {
        	inv.setItem(i, divider);
        }
        
        // BUY SELECTORS
        Double playerbal = OBChestShop.getEconomy().getBalance(Bukkit.getOfflinePlayer(player.getUniqueId()));
        Boolean hasOne = shopitem.getStockQuantity() >= shopitem.getAmount();
        Boolean hasEight = shopitem.getStockQuantity() > shopitem.getAmount() * 8;
        Boolean hasQuarterStack = shopitem.getStockQuantity() > shopitem.getAmount() * 16;
        Boolean hasHalfStack = shopitem.getStockQuantity() > shopitem.getAmount() * 32;
        Boolean hasStack = shopitem.getStockQuantity() > shopitem.getAmount() * 64;

        // BUY ONE
        ItemStack buy1 = new ItemStack(Material.LIME_WOOL, 1);
        ItemMeta buy1Meta = buy1.getItemMeta();
        if (OBChestShop.getEconomy().getBalance(Bukkit.getOfflinePlayer(player.getUniqueId())) < shopitem.getPrice()) {
        	buy1 = new ItemStack(Material.WHITE_WOOL, 1);
         	buy1Meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "You do not have enough money");
        } else if (hasOne) {
        	buy1Meta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Left Click" + ChatColor.GRAY + " to buy" + ChatColor.LIGHT_PURPLE + " one" + ChatColor.GRAY + " item");
        } else {
        	buy1 = new ItemStack(Material.WHITE_WOOL, 1);
         	buy1Meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Not enough items in stock for one item");
        }
        buy1.setItemMeta(buy1Meta);
        inv.setItem(27, buy1);
        
        // BUY 8
        ItemStack buy8 = new ItemStack(Material.LIME_CONCRETE, 1);
        ItemMeta buy8Meta = buy8.getItemMeta();
        if (OBChestShop.getEconomy().getBalance(Bukkit.getOfflinePlayer(player.getUniqueId())) < (shopitem.getPrice()*8.0)) {
        	buy8 = new ItemStack(Material.WHITE_WOOL, 1);
         	buy8Meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "You do not have enough money");
        } else if (hasEight) {
        	buy8Meta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Left Click" + ChatColor.GRAY + " to buy" + ChatColor.LIGHT_PURPLE + " eight" + ChatColor.GRAY + " items");        	
        } else {
        	buy8 = new ItemStack(Material.WHITE_WOOL, 1);
         	buy8Meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Not enough items in stock for eight items");
        }
        buy8.setItemMeta(buy8Meta);
        inv.setItem(28, buy8);

        // BUY 16
        ItemStack buy16 = new ItemStack(Material.LIME_TERRACOTTA, 1);
        ItemMeta buy16Meta = buy16.getItemMeta();
        if (OBChestShop.getEconomy().getBalance(Bukkit.getOfflinePlayer(player.getUniqueId())) < (shopitem.getPrice()*16.0)) {
        	buy16 = new ItemStack(Material.WHITE_WOOL, 1);
         	buy16Meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "You do not have enough money");
        } else if (hasQuarterStack) {
        	buy16Meta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Left Click" + ChatColor.GRAY + " to buy" + ChatColor.LIGHT_PURPLE + " sixteen" + ChatColor.GRAY + " items");        	
        } else {
        	buy16 = new ItemStack(Material.WHITE_WOOL, 1);
         	buy16Meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Not enough items in stock for quarter stack");
        }
        buy16.setItemMeta(buy16Meta);
        inv.setItem(29, buy16);        

        // BUY 32
        ItemStack buy32 = new ItemStack(Material.GREEN_CONCRETE_POWDER, 1);
        ItemMeta buy32Meta = buy32.getItemMeta();
        if (OBChestShop.getEconomy().getBalance(Bukkit.getOfflinePlayer(player.getUniqueId())) < (shopitem.getPrice()*32.0)) {
        	buy32 = new ItemStack(Material.WHITE_WOOL, 1);
         	buy32Meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "You do not have enough money");
        } else if (hasHalfStack) {
        	buy32Meta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Left Click" + ChatColor.GRAY + " to buy" + ChatColor.LIGHT_PURPLE + " half a stack (32)" + ChatColor.GRAY + " of items");        	
        } else {
        	buy32 = new ItemStack(Material.WHITE_WOOL, 1);
         	buy32Meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Not enough items in stock for half a stack");
        }
        buy32.setItemMeta(buy32Meta);
        inv.setItem(30, buy32);
        
        // BUY 64
        ItemStack buy64 = new ItemStack(Material.GREEN_CONCRETE, 1);
        ItemMeta buy64Meta = buy64.getItemMeta();
        if (OBChestShop.getEconomy().getBalance(Bukkit.getOfflinePlayer(player.getUniqueId())) < (shopitem.getPrice()*64.0)) {
        	buy64 = new ItemStack(Material.WHITE_WOOL, 1);
         	buy64Meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "You do not have enough money");
        } else if (hasStack) {
        	buy64Meta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Left Click" + ChatColor.GRAY + " to buy" + ChatColor.LIGHT_PURPLE + " a stack (64) of" + ChatColor.GRAY + " items");        	
        } else {
        	buy64 = new ItemStack(Material.WHITE_WOOL, 1);
         	buy64Meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Not enough items in stock for a full stack");
        }
        buy64.setItemMeta(buy64Meta);
        inv.setItem(31, buy64); 
	}

	 public void draw() {
		 player.openInventory(inv);
	 }
}
