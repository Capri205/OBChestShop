package net.obmc.OBChestShop.Menus;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.obmc.OBChestShop.Shop.ShopItemTypes;
import net.obmc.OBChestShop.ShopItem.ShopItem;

public class ItemConfig {

	Logger log = Logger.getLogger("Minecraft");
	
    private Player player;
    private Inventory inv;
    public ItemConfig(ShopItemTypes type, Player player, String shopname, ShopItem shopitem) {
		
		this.player = player;
		inv = Bukkit.createInventory(null, 54, ChatColor.DARK_AQUA + "[ITEM " + shopitem.getItem().getType().name() + "]" + " " + ChatColor.DARK_GREEN + shopname);
    	inv.clear();
    	
    	ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Back");
        backMeta.setLocalizedName(type.toString() + "#" + shopitem.getSlot());
        back.setItemMeta(backMeta);
        inv.setItem(0, back);
        
        ItemStack item = new ItemStack(Material.valueOf(shopitem.getItem().getType().name()), 1);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "" + ChatColor.BOLD + shopitem.getItem().getType().name() + ChatColor.AQUA + " (" + shopitem.getStockQuantity() + ")");
        item.setItemMeta(itemMeta);
        inv.setItem(4,  item);
        
        ItemStack delete = new ItemStack(Material.BARRIER, 1);
        ItemMeta deleteMeta = delete.getItemMeta();
        deleteMeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Remove item from shop");
        delete.setItemMeta(deleteMeta);
        inv.setItem(8,  delete);

        // PRICE CHANGE
        ItemStack price = new ItemStack(Material.TERRACOTTA, 1);
        ItemMeta priceMeta = price.getItemMeta();
        priceMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Change price of item" );
        priceMeta.setLore(Arrays.asList(ChatColor.YELLOW + "" + ChatColor.BOLD + "Left Click" + ChatColor.GRAY + " to change the" + ChatColor.LIGHT_PURPLE + " price" + ChatColor.GRAY + " of the item"));
        price.setItemMeta(priceMeta);
        inv.setItem(18,  price);
        // AMOUNT CHANGE
        ItemStack amount = new ItemStack(Material.LIGHT_GRAY_TERRACOTTA, 1);
        ItemMeta amountMeta = price.getItemMeta();
        amountMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Change amount sold" );
        amountMeta.setLore(Arrays.asList(ChatColor.YELLOW + "" + ChatColor.BOLD + "Left Click" + ChatColor.GRAY + " to change the" + ChatColor.LIGHT_PURPLE + " amount" + ChatColor.GRAY + " sold"));
        amount.setItemMeta(amountMeta);
        inv.setItem(19,  amount);
        // LORE CHANGE
        ItemStack lore = new ItemStack(Material.WHITE_TERRACOTTA, 1);
        ItemMeta loreMeta = price.getItemMeta();
        loreMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Change item description" );
        loreMeta.setLore(Arrays.asList(ChatColor.YELLOW + "" + ChatColor.BOLD + "Left Click" + ChatColor.GRAY + " to change the" + ChatColor.LIGHT_PURPLE + " description" + ChatColor.GRAY + " of the item"));
        lore.setItemMeta(loreMeta);
        inv.setItem(20,  lore);
        
        // STOCK CHANGE
        ItemStack addone = new ItemStack(Material.PRISMARINE_BRICK_SLAB, 1);
        ItemMeta addoneMeta = addone.getItemMeta();
        addoneMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Add one item from inventory to stock" );
        addoneMeta.setLore(Arrays.asList(ChatColor.YELLOW + "" + ChatColor.BOLD + "Left Click" + ChatColor.GRAY + " to add" + ChatColor.LIGHT_PURPLE + " one" + ChatColor.GRAY + " item to stock",
        		ChatColor.YELLOW + "" + ChatColor.BOLD + "Shift Left Click" + ChatColor.GRAY + " to add" + ChatColor.LIGHT_PURPLE + " two" + ChatColor.GRAY + " items to stock"));
        addone.setItemMeta(addoneMeta);
        inv.setItem(24, addone);
        ItemStack addhalf = new ItemStack(Material.PRISMARINE_BRICK_STAIRS, 1);
        ItemMeta addhalfMeta = addhalf.getItemMeta();
        addhalfMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Add 25/50% inventory to stock" );
        addhalfMeta.setLore(Arrays.asList(ChatColor.YELLOW + "" + ChatColor.BOLD + "Left Click" + ChatColor.GRAY + " to add" + ChatColor.LIGHT_PURPLE + " 25%" + ChatColor.GRAY + " to stock",
        		ChatColor.YELLOW + "" + ChatColor.BOLD + "Shift Left Click" + ChatColor.GRAY + " to add" + ChatColor.LIGHT_PURPLE + " 50%" + ChatColor.GRAY + " to stock"));
        addhalf.setItemMeta(addhalfMeta);
        inv.setItem(25, addhalf);
        ItemStack addall = new ItemStack(Material.PRISMARINE_BRICKS, 1);
        ItemMeta addallMeta = addall.getItemMeta();
        addallMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Add 75/100% inventory to stock" );
        addallMeta.setLore(Arrays.asList(ChatColor.YELLOW + "" + ChatColor.BOLD + "Left Click" + ChatColor.GRAY + " to add" + ChatColor.LIGHT_PURPLE + " 75%" + ChatColor.GRAY + " to stock",
        		ChatColor.YELLOW + "" + ChatColor.BOLD + "Shift Left Click" + ChatColor.GRAY + " to add" + ChatColor.LIGHT_PURPLE + " 100%" + ChatColor.GRAY + " to stock"));
        addall.setItemMeta(addallMeta);
        inv.setItem(26, addall);
        
        ItemStack removeone = new ItemStack(Material.RED_SANDSTONE_SLAB, 1);
        ItemMeta removeoneMeta = removeone.getItemMeta();
        removeoneMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Remove one item from stock to inventory" );
        removeoneMeta.setLore(Arrays.asList(ChatColor.YELLOW + "" + ChatColor.BOLD + "Left Click" + ChatColor.GRAY + " to add" + ChatColor.LIGHT_PURPLE + " one" + ChatColor.GRAY + " item to inventory",
        		ChatColor.YELLOW + "" + ChatColor.BOLD + "Shift Left Click" + ChatColor.GRAY + " to add" + ChatColor.LIGHT_PURPLE + " two" + ChatColor.GRAY + " items to inventory"));
        removeone.setItemMeta(removeoneMeta);
        inv.setItem(33, removeone);
        ItemStack removehalf = new ItemStack(Material.RED_SANDSTONE_STAIRS, 1);
        ItemMeta removehalfMeta = removehalf.getItemMeta();
        removehalfMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Remove 25/50% stock to inventory" );
        removehalfMeta.setLore(Arrays.asList(ChatColor.YELLOW + "" + ChatColor.BOLD + "Left Click" + ChatColor.GRAY + " to add" + ChatColor.LIGHT_PURPLE + " 25%" + ChatColor.GRAY + " to inventory",
        		ChatColor.YELLOW + "" + ChatColor.BOLD + "Shift Left Click" + ChatColor.GRAY + " to add" + ChatColor.LIGHT_PURPLE + " 50%" + ChatColor.GRAY + " to inventory"));
        removehalf.setItemMeta(removehalfMeta);
        inv.setItem(34, removehalf);
        ItemStack removeall = new ItemStack(Material.RED_SANDSTONE, 1);
        ItemMeta removeallMeta = removeall.getItemMeta();
        removeallMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Remove 75/100% stock to inventory" );
        removeallMeta.setLore(Arrays.asList(ChatColor.YELLOW + "" + ChatColor.BOLD + "Left Click" + ChatColor.GRAY + " to add" + ChatColor.LIGHT_PURPLE + " 75%" + ChatColor.GRAY + " to inventory",
        		ChatColor.YELLOW + "" + ChatColor.BOLD + "Shift Left Click" + ChatColor.GRAY + " to add" + ChatColor.LIGHT_PURPLE + " 100%" + ChatColor.GRAY + " to inventory"));
        removeall.setItemMeta(removeallMeta);
        inv.setItem(35, removeall);
        
	}
	
    public void draw() {
    	player.openInventory(inv);
    }
}
