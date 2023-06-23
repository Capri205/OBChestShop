package net.obmc.OBChestShop.Menus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.obmc.OBChestShop.OBChestShop;
import net.obmc.OBChestShop.Shop.Shop;
import net.obmc.OBChestShop.Shop.ShopItemTypes;
import net.obmc.OBChestShop.ShopItem.ShopItem;
import net.obmc.OBChestShop.Utils.Utils;

public class ItemBuy {

	Logger log = Logger.getLogger("Minecraft");
	
    private Player player;
    private Inventory inv;
    private Shop shop;

    public ItemBuy(Player player, String shopname, ShopItem shopitem) {
		
		this.player = player;
    	this.shop = OBChestShop.getShopList().getShop(shopname);
    	inv = Bukkit.createInventory(null, 54, ChatColor.DARK_AQUA + "[BUY " + shopitem.getItem().getType().name() + "]" + " " + ChatColor.DARK_GREEN + shopname);
    	inv.clear();
    	
    	ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Back");
        backMeta.setLocalizedName(ShopItemTypes.Sell.toString() + "#" + shopitem.getSlot());
        back.setItemMeta(backMeta);
        inv.setItem(0, back);
        
        ItemStack item = new ItemStack(Material.valueOf(shopitem.getItem().getType().name()), 1);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "" + ChatColor.BOLD + shopitem.getItem().getType().name() + ChatColor.GRAY + " - shop pays $" + shopitem.getPriceFormatted() + " for one item");
        item.setItemMeta(itemMeta);
        inv.setItem(4,  item);
        
    	ItemStack divider = new ItemStack(Material.CYAN_STAINED_GLASS_PANE);
        for (int i = 9; i < 18; i++) {
        	inv.setItem(i, divider);
        }
        
        OfflinePlayer shopowner = Bukkit.getOfflinePlayer(UUID.fromString(shop.getOwner()));
        Double ownerbalance = OBChestShop.getEconomy().getBalance(shopowner);
        int onhand = 0;
        ItemStack checkitem = null;
		Inventory playerinv = player.getInventory();
		// get count of player inventory of item
		for (int i = 0; i < 40; i++) {
			checkitem = playerinv.getItem(i);
			if ( checkitem == null ) continue;
			int itemhash = Utils.GenerateItemHash(checkitem); 
			if (checkitem != null && shopitem.getItemHash() == itemhash) {
				onhand += checkitem.getAmount();
			}
		}
        Boolean canbuyOne = ownerbalance >= shopitem.getPrice();
        Boolean playerhasOne = onhand >= 1;
        Boolean canbuyEight = ownerbalance >= (shopitem.getPrice() * 8);
        Boolean playerhasEight = onhand >= 8;
        Boolean canbuyQuarterStack = ownerbalance >= (shopitem.getPrice() * 16) ;
        Boolean playerhasQuarterStack = onhand >= 16;
        Boolean canbuyHalfStack = ownerbalance >= (shopitem.getPrice() * 32);
        Boolean playerhasHalfStack = onhand >= 32;
        Boolean canbuyStack = ownerbalance >= (shopitem.getPrice() * 64);
        Boolean playerhasStack = onhand >= 64;
        
        // buy all or as much as possible from player based on stock limits and available owner balance
        ShopItem stockitem = shop.getShopItemByHash(ShopItemTypes.Stock, shopitem.getItemHash());
        int canbuy = shop.getStockLimit() - stockitem.getStockQuantity();
        Boolean canbuyAll = false;
        if (canbuy < onhand) {
        	canbuyAll = ownerbalance >= canbuy * shopitem.getPrice();
        } else {
        	canbuyAll = ownerbalance >= onhand * shopitem.getPrice();
        }
        // dont have enough money to buy all or what can fit into stock, so reduce amount down based on price
        if (canbuyAll == false) {
    		canbuy = new BigDecimal(ownerbalance / shopitem.getPrice()).setScale(0, RoundingMode.FLOOR).intValue();
        	if (canbuy > 0) {
        		canbuyAll = true;
        	}
        }
        
        // BUY OPTIONS
        ItemStack buy1 = new ItemStack(Material.LIGHT_BLUE_CONCRETE_POWDER, 1);
        ItemStack buy8 = new ItemStack(Material.LIGHT_BLUE_CONCRETE, 1);
        ItemStack buy16 = new ItemStack(Material.CYAN_WOOL, 1);
        ItemStack buy32 = new ItemStack(Material.CYAN_CONCRETE, 1);
        ItemStack buy64 = new ItemStack(Material.CYAN_TERRACOTTA, 1);
        ItemStack buyAll = new ItemStack(Material.LIGHT_BLUE_TERRACOTTA);
        
        int instock = stockitem.getStockQuantity();
    	
        // BUY 1
        ItemMeta buy1Meta = buy1.getItemMeta();
        if (playerhasOne) {
        	if ((shop.getStockLimit() - instock) >= 1) {
        		if (canbuyOne) {
        			buy1Meta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Left Click" + ChatColor.GRAY + " to sell" + ChatColor.LIGHT_PURPLE + " one" + ChatColor.GRAY + " item");
                	buy1Meta.setLore(Arrays.asList(ChatColor.WHITE + "Shop pays you $" + shopitem.getPriceFormatted()));

        		} else {
        			buy1 = new ItemStack(Material.WHITE_WOOL, 1);
        			buy1Meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Shop owner does not have enough money");
        		}
        	} else {
            	buy1 = new ItemStack(Material.WHITE_WOOL, 1);
            	buy1Meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Purchase would exceed stock limit");
        	}
        } else {
        	buy1 = new ItemStack(Material.WHITE_WOOL, 1);
        	buy1Meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "You do not have enough inventory (need at least one item)");
        }
        buy1.setItemMeta(buy1Meta);
        inv.setItem(27, buy1);

        // BUY 8
        ItemMeta buy8Meta = buy8.getItemMeta();
        if (playerhasEight) {
        	if ((shop.getStockLimit() - instock) >= 8) {
        		if (canbuyEight) {
        			buy8Meta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Left Click" + ChatColor.GRAY + " to sell" + ChatColor.LIGHT_PURPLE + " eight" + ChatColor.GRAY + " items");
                	buy8Meta.setLore(Arrays.asList(ChatColor.WHITE + "Shop pays you $" + shopitem.getPriceFormatted(8)));
        		} else {
        			buy8 = new ItemStack(Material.WHITE_WOOL, 1);
        			buy8Meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Shop owner does not have enough money");
        		}
        	} else {
            	buy8 = new ItemStack(Material.WHITE_WOOL, 1);
            	buy8Meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Exceeds the shop stock limit");
        	}
        } else {
        	buy8 = new ItemStack(Material.WHITE_WOOL, 1);
        	buy8Meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "You do not have enough inventory (need at least 8 items)");
        }
        buy8.setItemMeta(buy8Meta);
        inv.setItem(28, buy8);

        // BUY 16
        ItemMeta buy16Meta = buy16.getItemMeta();
        if (playerhasQuarterStack) {
        	if ((shop.getStockLimit() - instock) >= 16) {
        		if (canbuyQuarterStack) {
        			buy16Meta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Left Click" + ChatColor.GRAY + " to sell" + ChatColor.LIGHT_PURPLE + " quarter stack" + ChatColor.GRAY + " (16 items)");
        			buy16Meta.setLore(Arrays.asList(ChatColor.WHITE + "Shop pays you $" + shopitem.getPriceFormatted(16)));
        		} else {
        			buy16 = new ItemStack(Material.WHITE_WOOL, 1);
        			buy16Meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Shop owner does not have enough money");
        		}
        	} else {
            	buy16 = new ItemStack(Material.WHITE_WOOL, 1);
            	buy16Meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Exceeds the shop stock limit");
        	}
        } else {
        	buy16 = new ItemStack(Material.WHITE_WOOL, 1);
        	buy16Meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "You do not have enough inventory (need at least 16 items)");
        }
        buy16.setItemMeta(buy16Meta);
        inv.setItem(29, buy16);        

        // BUY 32
        ItemMeta buy32Meta = buy32.getItemMeta();
        if (playerhasHalfStack) {
        	if ((shop.getStockLimit() - instock) >= 32) {
        		if (canbuyHalfStack) {
        			buy32Meta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Left Click" + ChatColor.GRAY + " to sell" + ChatColor.LIGHT_PURPLE + " half stack" + ChatColor.GRAY + " (32 items)");
        			buy32Meta.setLore(Arrays.asList(ChatColor.WHITE + "Shop pays you $" + shopitem.getPriceFormatted(32)));
        		} else {
        			buy32 = new ItemStack(Material.WHITE_WOOL, 1);
        			buy32Meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Shop owner does not have enough money");
        		}
        	} else {
            	buy32 = new ItemStack(Material.WHITE_WOOL, 1);
            	buy32Meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Exceeds the shop stock limit");
        	}
        } else {
        	buy32 = new ItemStack(Material.WHITE_WOOL, 1);
        	buy32Meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "You do not have enough inventory (need at least 32 items)");
        }
        buy32.setItemMeta(buy32Meta);
        inv.setItem(30, buy32);
        
        // BUY 64
        ItemMeta buy64Meta = buy64.getItemMeta();
        if (playerhasStack) {
        	// TODO: this needs to factor in sell quantity and stock quantity to determine breach - do for all
        	if ((shop.getStockLimit() - instock) >= 64) {
        		if (canbuyStack) {
        			buy64Meta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Left Click" + ChatColor.GRAY + " to sell" + ChatColor.LIGHT_PURPLE + " a stack" + ChatColor.GRAY + " (64 items)");
        			buy64Meta.setLore(Arrays.asList(ChatColor.WHITE + "Shop pays you $" + shopitem.getPriceFormatted(64)));
        		} else {
        			buy64 = new ItemStack(Material.WHITE_WOOL, 1);
        			buy64Meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Shop owner does not have enough money");
        		}
        	} else {
            	buy64 = new ItemStack(Material.WHITE_WOOL, 1);
            	buy64Meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Exceeds the shop stock limit");
        	}
        } else {
        	buy64 = new ItemStack(Material.WHITE_WOOL, 1);
        	buy64Meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "You do not have enough inventory (need at least 64 items)");
        }
        buy64.setItemMeta(buy64Meta);
        inv.setItem(31, buy64);
        
        // BUY ALL (buy all player inventory of item type or as much as we can until stock limit on sell and stock items)
        ItemMeta buyAllMeta = buyAll.getItemMeta();
        if (onhand > 0) {
        	if (canbuy > 0) {
        		if (canbuyAll) {
        			buyAllMeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Left Click" + ChatColor.GRAY + " to sell " + ChatColor.LIGHT_PURPLE + (canbuy >= onhand ? "all" : canbuy) + ChatColor.GRAY + " of your items");
        			buyAllMeta.setLore(Arrays.asList(ChatColor.WHITE + "Shop pays you $" + (canbuy >= onhand ? (shopitem.getPriceFormatted(onhand)) : (shopitem.getPriceFormatted(canbuy)) )));
        			buyAllMeta.setLocalizedName((canbuy >= onhand ? String.valueOf(onhand) : String.valueOf(canbuy)));
        		} else {
        			buyAll = new ItemStack(Material.WHITE_WOOL, 1);
        			buyAllMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Shop owner does not have enough money");
        		}
        	} else {
        		buyAll = new ItemStack(Material.WHITE_WOOL, 1);
        		buyAllMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Exceeds the shop stock limit");       		
        	}
        } else {
    		buyAll = new ItemStack(Material.WHITE_WOOL, 1);
        	buyAllMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "You do not have enough inventory");
        }
        buyAll.setItemMeta(buyAllMeta);
        inv.setItem(33, buyAll);
	}

	 public void draw() {
		 player.openInventory(inv);
	 }
}
