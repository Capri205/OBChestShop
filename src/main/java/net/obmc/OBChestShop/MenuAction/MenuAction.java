package net.obmc.OBChestShop.MenuAction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.obmc.OBChestShop.OBChestShop;
import net.obmc.OBChestShop.Menus.Selling;
import net.obmc.OBChestShop.Menus.ItemConfig;
import net.obmc.OBChestShop.Menus.ItemSell;
import net.obmc.OBChestShop.Menus.Settings;
import net.obmc.OBChestShop.Shop.Shop;
import net.obmc.OBChestShop.ShopItem.ShopItem;

import net.wesjd.anvilgui.AnvilGUI;

public class MenuAction implements Listener {

	Logger log = Logger.getLogger("Minecraft");
	
	enum ClickType { LEFT, RIGHT, SHIFT_LEFT, SHIFT_RIGHT };

	@EventHandler
	public void clickEvent(InventoryClickEvent event) {
		
		if (event.getCurrentItem() != null) {

			Player player = (Player) event.getWhoClicked();

			ClickType clicktype = ClickType.LEFT;
			if (event.isRightClick() && !event.isShiftClick()) {
				clicktype = ClickType.RIGHT;
			} else if (event.isRightClick() && event.isShiftClick()) {
				clicktype = ClickType.SHIFT_RIGHT;
			} else if (event.isLeftClick() && event.isShiftClick()) {
				clicktype = ClickType.SHIFT_LEFT;
			}

			String shopview = ChatColor.stripColor(event.getView().getTitle());
			ItemStack itemclicked = event.getCurrentItem();

			//
			// SELL MENU
			//
			if (shopview.startsWith("[SELL] ")) {

				event.setCancelled(true);

				String action = (shopview.split(" ", 0)[0]).replace("[","").replace("]","");
				String shopname = shopview.split(" ", 0)[1];

				// MENU BACK
				if (event.getRawSlot() == 0 && itemclicked.getType().name().equals("ARROW") && clicktype == ClickType.LEFT) {
					player.closeInventory();
				}
				
				if (event.getRawSlot() < 18) {

					// first two lines - nav, status etc

					if (itemclicked.getType().name().equals("CHEST") && clicktype == ClickType.LEFT) {
					}

					if (itemclicked.getType().name().equals("ENDER_CHEST") && clicktype == ClickType.LEFT
							&& player.getUniqueId().toString().equals(OBChestShop.getShopList().getShop(shopname).getOwner())) {				
						Settings settingsmenu = new Settings(player, shopname);
						settingsmenu.draw();
					}
					
				} else if (event.getRawSlot() > 17 && event.getRawSlot() < 54) {
					// lines 3 ~ 6 buy item
					String itemname = event.getCurrentItem().getType().name();
					ShopItem shopitem = OBChestShop.getShopList().getShop(shopname).getShopItem(itemname);
					Shop shop = OBChestShop.getShopList().getShop(shopname);
					
					ItemSell itemsell = new ItemSell(player, shopname, shopitem);
					itemsell.draw();
				}

			//
			// SELL ITEM
			//
			} else if (shopview.startsWith("[SELL ")) {

				event.setCancelled(true);
				
				String action = (shopview.split(" ", 0)[0]).replace("[", "");
				String itemname = (shopview.split(" ", 0)[1]).replace("]", "");
				String shopname = shopview.split(" ", 0)[2];
				
				Shop shop = OBChestShop.getShopList().getShop(shopname);
				ShopItem shopitem = shop.getShopItem(itemname);
				
				// MENU BACK
				if (event.getRawSlot() == 0 && itemclicked.getType().name().equals("ARROW") && clicktype == ClickType.LEFT) {
					Selling sellmenu = new Selling(shopname, player);
            		sellmenu.draw();

				} else if (event.getRawSlot() > 26 && event.getRawSlot() < 32) {
					// SELL ITEM
					Double debitamt = 0.0;
					int numitems = 0;
					if (event.getRawSlot() == 27 && itemclicked.getType().name().equals("LIME_WOOL") && clicktype == ClickType.LEFT) {
						// process purchase of one item
						debitamt = shopitem.getPrice();
						numitems = shopitem.getAmount();
					}
					if (event.getRawSlot() == 28 && itemclicked.getType().name().equals("LIME_CONCRETE") && clicktype == ClickType.LEFT) {
						// process purchase of eight items
						debitamt = shopitem.getPrice() * 8;
						numitems = shopitem.getAmount() * 8;
					}
					if (event.getRawSlot() == 29 && itemclicked.getType().name().equals("LIME_TERRACOTTA") && clicktype == ClickType.LEFT) {
						// process purchase of quarter stack (16 items)
						debitamt = shopitem.getPrice() * 16;
						numitems = shopitem.getAmount() * 16;
					}
					if (event.getRawSlot() == 30 && itemclicked.getType().name().equals("GREEN_CONCRETE_POWDER") && clicktype == ClickType.LEFT) {
						// process purchase of half stack (32 items)
						debitamt = shopitem.getPrice() * 32;
						numitems = shopitem.getAmount() * 32;
					}
					if (event.getRawSlot() == 31 && itemclicked.getType().name().equals("GREEN_CONCRETE") && clicktype == ClickType.LEFT) {
						// process purchase of one stack (64 items)
						debitamt = shopitem.getPrice() * 64;
						numitems = shopitem.getAmount() * 64;
					}
					// deduct cost of items from player balance, give player their items, credit shop owner balance
					// TODO: add EconomyResponse ecr.type checking for not SUCCESS
					OBChestShop.getEconomy().withdrawPlayer(player, debitamt);
					shopitem.moveStockToInventory(player.getUniqueId().toString(), numitems);
					shopitem.removeStock(numitems);
					Player shopowner = (Player) Bukkit.getOfflinePlayer(UUID.fromString(shop.getOwner()));
					OBChestShop.getEconomy().depositPlayer(shopowner, debitamt);
					player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.GREEN + "Successfully purchased item. " + ChatColor.GRAY + "$" + debitamt + ChatColor.GREEN + " removed from balance");
					// TODO: add titlemanager overlay message
					
					ItemSell itemsell = new ItemSell(player, shopname, shopitem);
					itemsell.draw();
				}

			//
			// SETTINGS MENU
			//
			} else if (shopview.startsWith("[SETTINGS] ")) {

				event.setCancelled(true);

				String action = (shopview.split(" ", 0)[0]).replace("[","").replace("]","");
				String shopname = shopview.split(" ", 0)[1];
				Shop shop = OBChestShop.getShopList().getShop(shopname);

				// MENU BACK
				if (event.getRawSlot() == 0 && itemclicked.getType().name().equals("ARROW") && clicktype == ClickType.LEFT) {
					Selling buymenu = new Selling(shopname, player);
            		buymenu.draw();
				}
				// CHANGE SHOP NAME
				if (event.getRawSlot() == 2 && itemclicked.getType().name().equals("NAME_TAG") && clicktype == ClickType.LEFT) {
			
    				// get the new name of the shop using an anvil gui
    				ItemStack guiitem = new ItemStack(Material.PAPER, 1);
    				ItemMeta meta = guiitem.getItemMeta();
    				meta.setDisplayName("OBChestShop - Shop name");
    				meta.setLore(Arrays.asList("Enter name"));
    				guiitem.setItemMeta(meta);

    				AnvilGUI gui = new AnvilGUI.Builder()
    					.text(shopname)
    					.title("Enter name:")
    					.itemLeft(guiitem)
    					.onClose(p -> {})
    					.onComplete((p, guireturnvalue) -> {
    						switch (OBChestShop.getShopList().getShop(shopname).setName(guireturnvalue)) {
    						 case "ShopExists": player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.RED + "That shop name is already taken.");
    						 break;
    						 case "ok": player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.GREEN + "Shop has been renamed.");
    						 break;
    						}
   							return AnvilGUI.Response.close();
   						})
    					.plugin(OBChestShop.getInstance())
    					.open(player);
				}
				// CHANGE SHOP DESCRIPTION
				if (event.getRawSlot() == 4 &&  itemclicked.getType().name().equals("NAME_TAG") && clicktype == ClickType.LEFT) {
	
    				// get the description for the shop using an anvil gui
    				ItemStack guiitem = new ItemStack(Material.PAPER, 1);
    				ItemMeta meta = guiitem.getItemMeta();
    				meta.setDisplayName("OBChestShop - Shop description");
    				meta.setLore(Arrays.asList("Enter description"));
    				guiitem.setItemMeta(meta);
    				String description = OBChestShop.getShopList().getShop(shopname).getDescription();
    				if (description == null || description.equals("")) {
    					description = "?";
    				}

    				AnvilGUI gui = new AnvilGUI.Builder()
    					.text(description)
    					.title("Enter description:")
    					.itemLeft(guiitem)
    					.onClose(p -> {})
    					.onComplete((p, guireturnvalue) -> {
    						OBChestShop.getShopList().getShop(shopname).setDescription(guireturnvalue);
   							return AnvilGUI.Response.close();
   						})
    					.plugin(OBChestShop.getInstance())
    					.open(player);
				}
				// SET STOCK LIMIT
				// TODO: do we need to reduce items in stock to the new limit if less than before?
				if (event.getRawSlot() == 6 && itemclicked.getType().name().equals("COMPASS") && clicktype == ClickType.LEFT) {

    				// get the description for the shop using an anvil gui
    				ItemStack guiitem = new ItemStack(Material.COMPASS, 1);
    				ItemMeta meta = guiitem.getItemMeta();
    				meta.setDisplayName("OBChestShop - Shop stock limit");
    				meta.setLore(Arrays.asList("Enter stock limit"));
    				guiitem.setItemMeta(meta);
    				String stocklimit = OBChestShop.getShopList().getShop(shopname).getStockLimit().toString();

    				AnvilGUI gui = new AnvilGUI.Builder()
    					.text(stocklimit)
    					.title("Enter stock limit:")
    					.itemLeft(guiitem)
    					.onClose(p -> {})
    					.onComplete((p, guireturnvalue) -> {
    						shop.setStockLimit(SanitizeLimit(shop.getStockLimit(), OBChestShop.getInstance().getConfig().getInt("maxstocklimit"), guireturnvalue));
    						shop.saveShop();
    	    				return AnvilGUI.Response.close();
   						})
    					.plugin(OBChestShop.getInstance())
    					.open(player);
				}
				// TOGGLE SHOP OPEN/CLOSED
				if (event.getRawSlot() == 8 &&  Tag.WOOL.isTagged(itemclicked.getType()) && clicktype == ClickType.LEFT) {
					OBChestShop.getShopList().getShop(shopname).toggleOpen();
					player.closeInventory();
				}
				// CONFIGURE SHOP ITEM
				if (event.getRawSlot() > 17 && event.getRawSlot() < 54) {
					ItemConfig itemconfig = new ItemConfig(player, shopname, shop.getShopItem(itemclicked.getType().name()));
                	itemconfig.draw();
				}
				// ADD ITEM TO SHOP (from player inventory)
				if (event.getRawSlot() > 53 && event.getRawSlot() < 90) {
					// check if we have space for a new item in the shop
					// TODO: should be a method of shop - check size of shoplist instead of scanning inventory?
					int openslot = -1;
					if (shop.hasSpace()) {
						openslot = shop.getItemList().size() + 18;
					}
				
					// add new item to shop or replenish item from our inventory
					if (openslot > 17 && openslot < 54) {
						int stockchangeamount = 1;
						if (!shop.getItemList().contains(event.getCurrentItem().getType().toString())) {
							ItemStack cloneitem = itemclicked.clone();
							if ( clicktype == ClickType.SHIFT_LEFT ) {
								stockchangeamount = itemclicked.getAmount();
							}
							cloneitem.setAmount(1);
							shop.addShopItem(cloneitem.getType().toString(), new ShopItem(cloneitem, stockchangeamount));
							event.getInventory().setItem(openslot, cloneitem);
							itemclicked.setAmount(itemclicked.getAmount() - stockchangeamount);
							player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.GREEN + "Added " + stockchangeamount + " " + ChatColor.GRAY + cloneitem.getType().name().toLowerCase() + ChatColor.GREEN + " to shop");
                        	player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 400, 400);
						} else {
							if (clicktype == ClickType.LEFT || clicktype == ClickType.SHIFT_LEFT) {
								ShopItem shopitem = shop.getShopItem(itemclicked.getType().name());
								// add one or more of an item into stock for the shop item
								if (clicktype == ClickType.SHIFT_LEFT) {
									stockchangeamount = itemclicked.getAmount();
								}
								
								if (shopitem.getStockQuantity() < shop.getStockLimit()) {
									if ((shopitem.getStockQuantity() + stockchangeamount) > shop.getStockLimit()) {
										stockchangeamount = shop.getStockLimit() - shopitem.getStockQuantity();
										player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.RED + "Stock limit of " + ChatColor.GRAY + shop.getStockLimit() + ChatColor.RED + " reached. ");
									}
									shop.addItemStock(itemclicked.getType().name(), stockchangeamount);
									player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.GREEN + stockchangeamount + " " + ChatColor.GRAY + itemclicked.getType().name().toLowerCase() + ChatColor.GREEN + " added to stock");
									player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 400, 400);
									itemclicked.setAmount(itemclicked.getAmount() - stockchangeamount);	// do last as setting to zero removes the item from the inventory
								} else {
									player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.RED + "Stock quantity " + ChatColor.GRAY + shopitem.getStockQuantity() + ChatColor.RED + " already at or greater than limit.");
								}
							}
							Settings settingsmenu = new Settings(player, shopname);
							settingsmenu.draw();
						}
					} else {
						player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.RED + "No space left in this shop");
						player.closeInventory();
					}
				}
			} else if (shopview.startsWith("[ITEM ")) {
				
				event.setCancelled(true);

				String action = (shopview.split(" ", 0)[0]).replace("[", "");
				String itemname = (shopview.split(" ", 0)[1]).replace("]", "");
				String shopname = shopview.split(" ", 0)[2];
			
				ShopItem shopitem = OBChestShop.getShopList().getShop(shopname).getShopItem(itemname);
				Shop shop = OBChestShop.getShopList().getShop(shopname);
				
				// MENU BACK
				if (event.getRawSlot() == 0 && itemclicked.getType().name().equals("ARROW") && clicktype == ClickType.LEFT) {
					Settings settingsmenu = new Settings(player, shopname);
					settingsmenu.draw();
				}
				// REMOVE ITEM
				if (event.getRawSlot() == 8 && itemclicked.getType().name().equals("BARRIER") && clicktype == ClickType.LEFT) {
					shopitem.moveStockToInventory(player.getUniqueId().toString(), shopitem.getStockQuantity());
					OBChestShop.getShopList().getShop(shopname).removeitem(itemname);
					player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.GREEN + "Removed " + ChatColor.GRAY + itemname.toLowerCase() + ChatColor.GREEN + " from shop");
					//TODO: close out other players menus if they have it open, or double check item still exists in shop before purchase
    				Settings settingsmenu = new Settings(player, shopname);
					settingsmenu.draw();
				}
				// PRICE CHANGE
				if (event.getRawSlot() == 18 && itemclicked.getType().name().equals("TERRACOTTA") && clicktype == ClickType.LEFT) {
					// obtain new price for item using an anvil gui
					ItemStack guiitem = new ItemStack(Material.EMERALD, 1);

    				AnvilGUI gui = new AnvilGUI.Builder()
    					.text(shopitem.getPriceFormatted())
    					.title("Enter new price:")
    					.itemLeft(guiitem)
    					.onClose(p -> {})
    					.onComplete((p, guireturnvalue) -> {
    						shopitem.setPrice(SanitizePrice(shopitem.getPrice(), guireturnvalue));
    						OBChestShop.getShopList().getShop(shopname).saveShop();
    	    				Settings settingsmenu = new Settings(player, shopname);
    						settingsmenu.draw(); 
   							return AnvilGUI.Response.close();
   						})
    					.plugin(OBChestShop.getInstance())
    					.open(player);
				}
				// AMOUNT CHANGE
				if (event.getRawSlot() == 19 && itemclicked.getType().name().equals("LIGHT_GRAY_TERRACOTTA") && clicktype == ClickType.LEFT) {
					// obtain new price for item using an anvil gui
    				ItemStack guiitem = new ItemStack(Material.PRISMARINE_CRYSTALS, 1);
  				
    				AnvilGUI gui = new AnvilGUI.Builder()
    					.text(shopitem.getAmount().toString())
    					.title("Enter new amount:")
    					.itemLeft(guiitem)
    					.onClose(p -> {})
    					.onComplete((p, guireturnvalue) -> {
    						shopitem.setAmount(SanitizeAmount(shopitem.getAmount(), guireturnvalue));
    						OBChestShop.getShopList().getShop(shopname).saveShop();
    	    				Settings settingsmenu = new Settings(player, shopname);
    						settingsmenu.draw(); 
   							return AnvilGUI.Response.close();
   						})
    					.plugin(OBChestShop.getInstance())
    					.open(player);
				}
				// ITEM DESCRIPTION CHANGE
				if (event.getRawSlot() == 20 && itemclicked.getType().name().equals("WHITE_TERRACOTTA") && clicktype == ClickType.LEFT) {
					// set a new description for the item
    				ItemStack guiitem = new ItemStack(Material.PAPER, 1);
    				ItemMeta itemmeta = guiitem.getItemMeta();
    				String description = "?";
    				itemmeta.setDisplayName(description);
    				if (!shopitem.getDescription().isEmpty()) {
    					description = shopitem.getDescription();
        				itemmeta.setDisplayName(shopitem.getDescription());
    				}
    				guiitem.setItemMeta(itemmeta);

    				AnvilGUI gui = new AnvilGUI.Builder()
    					.text(description)
    					.title("Enter new description:")
    					.itemLeft(guiitem)
    					.onClose(p -> {})
    					.onComplete((p, guireturnvalue) -> {
    						shopitem.setDescription(guireturnvalue);
    						OBChestShop.getShopList().getShop(shopname).saveShop();
    	    				Settings settingsmenu = new Settings(player, shopname);
    						settingsmenu.draw(); 
   							return AnvilGUI.Response.close();
   						})
    					.plugin(OBChestShop.getInstance())
    					.open(player);
				}
				// INCREASE STOCK
				int stockchangeamount = 0;
				if (event.getRawSlot() == 24 || event.getRawSlot() == 25 || event.getRawSlot() == 26) {
					// get quantity in player inventory
					Inventory inv = player.getInventory();
					ItemStack checkitem = null;
					int onhand = 0;
					// get count of inventory on hand
					for (int i = 0; i < 40; i++) {
						checkitem = inv.getItem(i);
						if (checkitem != null && checkitem.getType().name().equals(itemname)) {
							onhand += checkitem.getAmount();
						}
					}
					if (onhand > 0) {
						if (itemclicked.getType().name().equals("PRISMARINE_BRICK_SLAB")
								&& clicktype == ClickType.LEFT) {
							stockchangeamount = 1;
						}
						if (itemclicked.getType().name().equals("PRISMARINE_BRICK_SLAB")
								&& clicktype == ClickType.SHIFT_LEFT) {
							stockchangeamount = 2;
						}
						if (itemclicked.getType().name().equals("PRISMARINE_BRICK_STAIRS")
								&& clicktype == ClickType.LEFT) {
							stockchangeamount = (int) (onhand * 0.25);
						}
						if (itemclicked.getType().name().equals("PRISMARINE_BRICK_STAIRS")
								&& clicktype == ClickType.SHIFT_LEFT) {
							stockchangeamount = (int) (onhand * 0.50);
						}
						if (itemclicked.getType().name().equals("PRISMARINE_BRICKS") && clicktype == ClickType.LEFT) {
							stockchangeamount = (int) (onhand * 0.75);
						}
						if (itemclicked.getType().name().equals("PRISMARINE_BRICKS")
								&& clicktype == ClickType.SHIFT_LEFT) {
							stockchangeamount = onhand;
						}
						// lock stock change amount to a minimum of 1 or what is on hand or
						// a quantity that brings us to the stock limit
						if (stockchangeamount == 0) {
							stockchangeamount = 1;
						}
						if (stockchangeamount > onhand) {
							stockchangeamount = onhand;
						}
						Boolean breach = false;
						if ((shopitem.getStockQuantity() + stockchangeamount) > shop.getStockLimit()) {
							stockchangeamount = shop.getStockLimit() - shopitem.getStockQuantity();
							breach = true;
						}
					
						// prevent possible negative amounts being added - like changing stock limit lower when stock is actually higher
						if (stockchangeamount > 0) {
							//TODO: add boolean return on move inventory method
							shopitem.moveInventoryToStock(player.getUniqueId().toString(), stockchangeamount);
							shopitem.addStock(stockchangeamount);
						}
						if (breach) {
							player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.RED + "Stock limit of " + ChatColor.GRAY + shop.getStockLimit() + ChatColor.RED + " reached. ");
						}
						// update visual inventory (in the item display name)
						ItemMeta itemmeta = event.getClickedInventory().getItem(4).getItemMeta();
						itemmeta.setDisplayName(ChatColor.WHITE + "" + ChatColor.BOLD + shopitem.getItem().getType().name() + ChatColor.AQUA + " (" + shopitem.getStockQuantity() + ")");
						event.getClickedInventory().getItem(4).setItemMeta(itemmeta);
					} else {
						player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.RED + "Not enough items in your inventory");
					}
				}

				// DECREASE STOCK
				if (event.getRawSlot() == 33 || event.getRawSlot() == 34 || event.getRawSlot() == 35) {
					stockchangeamount = shopitem.getStockQuantity();
					if (stockchangeamount > 0) {
						if (itemclicked.getType().name().equals("RED_SANDSTONE_SLAB") && clicktype == ClickType.LEFT) {
							stockchangeamount = 1;
						}
						if (itemclicked.getType().name().equals("RED_SANDSTONE_SLAB")
								&& clicktype == ClickType.SHIFT_LEFT) {
							stockchangeamount = 2;
						}
						if (itemclicked.getType().name().equals("RED_SANDSTONE_STAIRS")
								&& clicktype == ClickType.LEFT) {
							stockchangeamount = (int) (stockchangeamount * 0.25);
						}
						if (itemclicked.getType().name().equals("RED_SANDSTONE_STAIRS")
								&& clicktype == ClickType.SHIFT_LEFT) {
							stockchangeamount = (int) (stockchangeamount * 0.50);
						}
						if (itemclicked.getType().name().equals("RED_SANDSTONE") && clicktype == ClickType.LEFT) {
							stockchangeamount = (int) (stockchangeamount * 0.75);
						}
						if (itemclicked.getType().name().equals("RED_SANDSTONE") && clicktype == ClickType.SHIFT_LEFT) {
							stockchangeamount = (int) stockchangeamount;
						}
						// try to lock to a minimum of 1
						if (stockchangeamount == 0) {
							stockchangeamount = 1;
						} else if (stockchangeamount > shopitem.getStockQuantity()) {
							stockchangeamount = shopitem.getStockQuantity();
						}
						
						//TODO: add boolean return on move method
						shopitem.moveStockToInventory(player.getUniqueId().toString(), stockchangeamount);
						shopitem.removeStock(stockchangeamount);

						// update visual inventory (in the item display name)
						ItemMeta itemmeta = event.getClickedInventory().getItem(4).getItemMeta();
						itemmeta.setDisplayName(
								ChatColor.WHITE + "" + ChatColor.BOLD + shopitem.getItem().getType().name() +
								ChatColor.AQUA + " (" + shopitem.getStockQuantity() + ")");
						event.getClickedInventory().getItem(4).setItemMeta(itemmeta);
					} else {
						player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.RED + "Not enough items in stock");
					}
				}
			}
		}
	}

	// Return a new double value from string, or return the current value
	private Double SanitizePrice(Double currentprice, String newprice) {
		String DOUBLE_PATTERN = "[0-9]*(\\.){0,1}\\d*";
		String INTEGER_PATTERN = "\\d+";

		if (Pattern.matches(DOUBLE_PATTERN, newprice) || Pattern.matches(INTEGER_PATTERN, newprice)) {
			try {
				BigDecimal bdc = new BigDecimal(newprice).setScale(2, RoundingMode.HALF_UP);
				Double d = bdc.doubleValue();
				if (d >= 0) {
					return d;
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
				return currentprice;
			}
		}
		return currentprice;
	}

	// return a new integer amount or the current amount if invalid
	private int SanitizeAmount(Integer currentamount, String newamount) {
		String INTEGER_PATTERN = "\\d+";
		
		if (Pattern.matches(INTEGER_PATTERN,  newamount)) {
			Integer i = new Integer(newamount);
			if (i > 0 ) {
				return i;
			}
		}
		return currentamount;
	}
	
	// return a new integer amount or the current amount if invalid
	private int SanitizeLimit(Integer currentlimit, Integer maxlimit, String newlimit) {
		String INTEGER_PATTERN = "\\d+";
		// TODO: add some more checks in here
		if (Pattern.matches(INTEGER_PATTERN,  newlimit)) {
			Integer i = new Integer(newlimit);
			if (i >= 0  && i <= maxlimit) {
				return i;
			}
		}
		return currentlimit;
	}
	
}