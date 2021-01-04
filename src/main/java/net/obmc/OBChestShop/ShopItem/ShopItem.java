package net.obmc.OBChestShop.ShopItem;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;
import net.obmc.OBChestShop.OBChestShop;

public class ShopItem {

	static Logger log = Logger.getLogger("Minecraft");

    private ItemStack item;
    private String lore;
    private double price = 0.00;
    private int amount = 0;
    private int stock = 0;
    private String description = "";
    private DecimalFormat priceformatted = new DecimalFormat("#.00#");
    
    //TODO: change to pass in itemname and create a new item stack in the constructor for this.item
    public ShopItem(ItemStack item, int stocktoadd) {
    	this.item = item;
    	this.price = 5.00;
    	this.amount = 1;
    	this.stock = stocktoadd;
    	this.description = "";
    	this.priceformatted.setRoundingMode(RoundingMode.HALF_UP);
    	this.priceformatted.setGroupingUsed(false);
    	this.priceformatted.setMaximumFractionDigits(2);
    	setLore();
    }
    
    public ItemStack getItem() {
    	return item;
    }

	public Double getPrice() {
		return price;
	}
	public String getPriceFormatted() {
		return priceformatted.format(price);
	}
	public void setPrice(Double price) {
		this.price = price;
		setLore();
	}
	
	public Integer getAmount() {
		return amount;
	}
	public void setAmount(int amount) {
		this.amount = amount;
		setLore();
	}
	
	public void addStock(int quantity) {
		this.stock += quantity;
		setLore();
	}
	public void removeStock(int quantity) {
		if (this.stock >= quantity) {
			this.stock -= quantity;
		}
		setLore();
	}
	public int getStockQuantity() {
		return this.stock;
	}
	public void setStock(int quantity) {
		this.stock = quantity;
		setLore();
	}
	
	public String getLore() {
		return lore;
	}
	public void setLore(String lore) {
		this.lore = lore;
	}
	
	private void setLore() {
		this.lore = "";
		if (!description.isEmpty()) {
			this.lore = ChatColor.AQUA + description + ",";
		}
    	this.lore += ChatColor.YELLOW + "Price: " + priceformatted.format(price) + "," +
				ChatColor.YELLOW + "Amount: " + amount + "," +
    			ChatColor.YELLOW + "Stock: " + stock +",";
	}
	public String getLoreSell() {
		String lore = getLore();
		lore += ChatColor.YELLOW + "" + ChatColor.BOLD + "Left Click" + ChatColor.GRAY + " to buy" + "," +
				ChatColor.YELLOW + "" + ChatColor.BOLD + "Shift Click" + ChatColor.GRAY + " to add to " + ChatColor.GREEN + "cart";
		return lore;
	}
	public String getLoreSettings() {
		String lore = getLore();
		lore += ChatColor.YELLOW + "" + ChatColor.BOLD + "Left Click" + ChatColor.GRAY + " to configure item" + ",";
		return lore;
	}

	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
		setLore();
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
			//ItemStack moveitem = item.clone();
			ItemStack moveitem = new ItemStack(Material.valueOf(item.getType().name()));
			moveitem.setAmount(0);
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

		// split quantity into a count of full and partial stacks and process
		int fullstacks = quantitytomove / item.getMaxStackSize();
		int partial = quantitytomove - (fullstacks * item.getMaxStackSize());

		// iterate over inventory slots pulling whole or partial stacks into stock
		int slot = 0;
		int stockaddedtotal = 0;
		while (quantitytomove > 0 && slot < 40) {
			if (inv.getItem(slot) != null && inv.getItem(slot).getType().name().equals(item.getType().name())) {
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
		player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.GREEN + stockaddedtotal + " " +
				ChatColor.GRAY + item.getType().name().toLowerCase() + ChatColor.GREEN + " placed into stock");
	}
}