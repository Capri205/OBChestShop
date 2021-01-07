package net.obmc.OBChestShop.Menus;

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

public class Selling {

	Logger log = Logger.getLogger("Minecraft");

    private Shop shop;
    private Inventory inv;
    private Player player;

    public Selling(String shopname, Player player) {
        
    	this.shop = OBChestShop.getShopList().getShop(shopname);

    	this.player = player;
    	
    	Boolean isowner = false;
    	if (player.getUniqueId().toString().equals(shop.getOwner())) {
    		isowner = true;
    	}
        
        inv = Bukkit.createInventory(null, 54, ChatColor.DARK_AQUA + "[SELL]" + " " + ChatColor.DARK_GREEN + shopname);
        inv.clear();
        
    	ItemStack close = new ItemStack(Material.ARROW);
        ItemMeta backMeta = close.getItemMeta();
        backMeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Close");
        close.setItemMeta(backMeta);
        inv.setItem(0, close);
        
        // TODO: coming soon - remove for now
        //ItemStack cart = new ItemStack(Material.CHEST);
        //ItemMeta cartMeta = cart.getItemMeta();
        //cartMeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Checkout");
        //cartMeta.setLore(Arrays.asList(ChatColor.YELLOW + "" + ChatColor.BOLD + "Left Click " + ChatColor.GRAY + "to open your " + ChatColor.GREEN + "Cart"));
        //cart.setItemMeta(cartMeta);
        //inv.setItem(4, cart);

        ItemStack options = new ItemStack(Material.ENDER_CHEST);
        ItemMeta optionsMeta = options.getItemMeta();
        String optionsmsg = ChatColor.GRAY + "See owner about any changes to the shop";
        if (isowner) {
        	optionsmsg = ChatColor.YELLOW + "" + ChatColor.BOLD + "Left Click " + ChatColor.GRAY + "to open " + ChatColor.LIGHT_PURPLE + "Shop Settings";
        }
        optionsMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + shopname);
        optionsMeta.setLore(Arrays.asList(ChatColor.GRAY + "" + shop.getDescription(), " ",
        		ChatColor.YELLOW + "" + ChatColor.BOLD + "Owner: " + ChatColor.GREEN + "" + ChatColor.BOLD + shop.getOwnerName(),
        		" ",
        		optionsmsg,
        		" "));
        options.setItemMeta(optionsMeta);
    	inv.setItem(8, options);
    	
    	ItemStack divider = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
    	if (!shop.isOpen()) {
    		divider = new ItemStack(Material.RED_STAINED_GLASS_PANE);
    	}
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
        	itemmeta.setLore(Arrays.asList(shopitem.getLoreSell().split(",")));
        	item.setItemMeta(itemmeta);
        	inv.setItem(slot, item);
        	slot++;
        }
    }
    
    public void draw() {
    	player.openInventory(inv);
    }

}
