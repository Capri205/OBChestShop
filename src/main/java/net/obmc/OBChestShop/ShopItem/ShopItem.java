package net.obmc.OBChestShop.ShopItem;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import net.md_5.bungee.api.ChatColor;
import net.obmc.OBChestShop.OBChestShop;
import net.obmc.OBChestShop.Shop.ShopItemTypes;
import net.obmc.OBChestShop.Utils.Utils;

public class ShopItem {

	static Logger log = Logger.getLogger("Minecraft");

	private int slot;
    private ItemStack item;
    private int itemhash;
    private double price = 0.00;
    private int stock = 0;
    private String description = "";
    private DecimalFormat priceformatted = new DecimalFormat("#0.00#");
    
    public ShopItem(int slot, ItemStack item, int stocktoadd) {
    	this.slot = slot;
    	this.item = item;
    	this.itemhash = Utils.GenerateItemHash(item);
    	this.price = 5.00;
    	this.stock = stocktoadd;
    	this.description = "";
    	this.priceformatted.setRoundingMode(RoundingMode.HALF_UP);
    	this.priceformatted.setGroupingUsed(false);
    	this.priceformatted.setMaximumFractionDigits(2);
    }
    
    public int getSlot() {
    	return slot;
    }
    public void setSlot(int slot) {
    	this.slot = slot;
    }
    
    public ItemStack getItem() {
    	return item;
    }
    public String getItemName() {
    	return item.getType().name();
    }
    public int getItemHash() {
    	return itemhash;
    }
    
	public Double getPrice() {
		return price;
	}
	public String getPriceFormatted() {
		return priceformatted.format(price);
	}
	public String getPriceFormatted(int multiplier) {
		return priceformatted.format(price * multiplier);
	}
	public void setPrice(Double price) {
		this.price = price;
	}
	
	public void addStock(int quantity) {
		this.stock += quantity;
	}
	public void removeStock(int quantity) {
		if (this.stock >= quantity) {
			this.stock -= quantity;
		}
	}
	public int getStockQuantity() {
		return this.stock;
	}
	public void setStock(int quantity) {
		this.stock = quantity;
	}

	public String getLore(String shopname, ShopItemTypes type) {
		String lore = "";
		if (!description.isEmpty() && type.equals(ShopItemTypes.Sell)) {
			lore += ChatColor.AQUA + description + ",";
		}
		if (!type.equals(ShopItemTypes.Stock)) {
			lore += ChatColor.YELLOW + "Price: " + priceformatted.format(price) + ",";
		}
		if (type.equals(ShopItemTypes.Sell) || type.equals(ShopItemTypes.Stock)) {
			if (type.equals(ShopItemTypes.Sell)) {
				lore += ChatColor.YELLOW + "Stock: " + OBChestShop.getShopList().getShop(shopname).getShopItemByHash(ShopItemTypes.Stock, this.itemhash).getStockQuantity() + ",";
			} else {
				lore += ChatColor.YELLOW + "Stock: " + stock + ",";
			}
		}
		if (!type.equals(ShopItemTypes.Stock)) {
			lore += ChatColor.YELLOW + "" + ChatColor.BOLD + "Left Click" + ChatColor.GRAY + " to " +
					(type.equals(ShopItemTypes.Sell) ? "buy" : "sell" ) + ",";
		} else {
			lore += ChatColor.YELLOW + "" + ChatColor.BOLD + "Left Click" + ChatColor.GRAY + " to manage";
		}
		return lore;
	}
	public String getLoreSettings(String shopname, ShopItemTypes type) {
		String lore = "";
		if (!description.isEmpty() && type.equals(ShopItemTypes.Sell)) {
			lore += ChatColor.AQUA + description + ",";
		}
		if (!type.equals(ShopItemTypes.Stock)) {
			lore += ChatColor.YELLOW + "Price: " + priceformatted.format(price) + ",";
		}
		if (type.equals(ShopItemTypes.Sell) || type.equals(ShopItemTypes.Stock)) {
			lore += ChatColor.YELLOW + "Stock: " + OBChestShop.getShopList().getShop(shopname).getShopItemByHash(ShopItemTypes.Stock, this.itemhash).getStockQuantity() + ",";
		}
		lore += ChatColor.YELLOW + "" + ChatColor.BOLD + "Left Click" + ChatColor.GRAY + " to configure item" + ",";
		return lore;
	}

	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	// move stock to a player inventory - fill up inventory and drop excess on the ground
	public void moveStockToInventory(String playeruuid, int quantitytomove) {
		Player player = Bukkit.getPlayer(UUID.fromString(playeruuid));
		Inventory inv = player.getInventory();
		HashMap<Integer, ItemStack> remainder = null;

		// split quantity into a count of full and partial stacks and process
		int fullstacks = quantitytomove / item.getMaxStackSize();
		int partial = quantitytomove - (fullstacks * item.getMaxStackSize());
		int invaddedtotal = 0;
		int droppedtotal = 0;
		int invaddqty = 0;
		while (quantitytomove > 0) {
			// set stack quantity
			ItemStack moveitem = item.clone();
			Utils.StripShopLore(moveitem);
			if (partial > 0) {
				moveitem.setAmount(partial);
				partial = 0;
			} else if (fullstacks > 0) {
				moveitem.setAmount(item.getMaxStackSize());
				fullstacks--;
			}
			// attempt to add to inventory
			invaddqty = moveitem.getAmount();
			remainder = inv.addItem(moveitem);
			if (remainder.isEmpty()) {
				// all items moved
				quantitytomove -= invaddqty;
				invaddedtotal += invaddqty;
			} else {
				// check for partial add of the quantity
				if (invaddqty != moveitem.getAmount()) {
					quantitytomove -= (invaddqty - moveitem.getAmount());
					invaddedtotal += (invaddqty - moveitem.getAmount());
				}
				// remaining must be what we drop on the floor and track that total
				player.getWorld().dropItemNaturally(player.getLocation(), remainder.get(0));
				quantitytomove -= moveitem.getAmount();
				droppedtotal += moveitem.getAmount();
			}
		}
		this.removeStock(invaddedtotal + droppedtotal);
		// report quantity moved
		String movemsg = OBChestShop.getChatMsgPrefix() + ChatColor.GREEN + invaddedtotal + " " + ChatColor.GRAY
				+ item.getType().name().toLowerCase() + ChatColor.GREEN + " placed into inventory";
		if (droppedtotal > 0) {
			movemsg += ". " + ChatColor.RED + "(" + droppedtotal + " dropped on ground)";
		}
		player.sendMessage(movemsg);
	}
	
	// add player inventory to stock - fill up inventory to allowable limit
	public void moveInventoryToStock(String playeruuid, int quantitytomove) {
		
		Player player = Bukkit.getPlayer(UUID.fromString(playeruuid));
		Inventory inv = player.getInventory();

		// iterate over inventory slots pulling whole or partial stacks into stock
		int slot = 0;
		int stockaddedtotal = 0;
		while (quantitytomove > 0 && slot < 40) {
			if (inv.getItem(slot) != null && itemhash == Utils.GenerateItemHash(inv.getItem(slot))) {
				int itemqty = inv.getItem(slot).getAmount();
				if (quantitytomove >= itemqty) {
					stockaddedtotal += itemqty;
					quantitytomove -= itemqty;
					inv.getItem(slot).setAmount(0);
				} else {
					stockaddedtotal += quantitytomove;
					inv.getItem(slot).setAmount(itemqty-quantitytomove);
					quantitytomove = 0;
				}
			}
			slot++;
		}
		this.addStock(stockaddedtotal);
	}
}