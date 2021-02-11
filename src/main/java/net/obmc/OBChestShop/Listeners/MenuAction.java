package net.obmc.OBChestShop.Listeners;

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
import net.obmc.OBChestShop.Menus.Buying;
import net.obmc.OBChestShop.Menus.ItemBuy;
import net.obmc.OBChestShop.Menus.ItemConfig;
import net.obmc.OBChestShop.Menus.ItemSell;
import net.obmc.OBChestShop.Menus.MenuTypes;
import net.obmc.OBChestShop.Menus.Settings;
import net.obmc.OBChestShop.Menus.StockRoom;
import net.obmc.OBChestShop.Shop.Shop;
import net.obmc.OBChestShop.Shop.ShopItemTypes;
import net.obmc.OBChestShop.ShopItem.ShopItem;

import net.wesjd.anvilgui.AnvilGUI;

public class MenuAction implements Listener {

	Logger log = Logger.getLogger("Minecraft");
	
	enum ClickType { LEFT, RIGHT, SHIFT_LEFT, SHIFT_RIGHT };

	@EventHandler
	public void clickEvent(InventoryClickEvent event) {

		AnvilGUI gui = null;

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
			final String shopname = shopview.substring(shopview.indexOf("] ")+2, shopview.length());

			// exit if the shop has since gone invalid (removed by owner whilst player has GUI open)
			if (OBChestShop.getShopList().shopExists(shopname)) {
				
				Shop shop = OBChestShop.getShopList().getShop(shopname);
				ItemStack itemclicked = event.getCurrentItem();
		    	
				//
				// SELL MENU
				//
				if (shopview.startsWith("[SELL] ")) {

					event.setCancelled(true);
					
					// clear menu navigation stack (players can exit menus at any point, so stack might contain data)
					if (OBChestShop.menunav.size() > 0) {
						OBChestShop.menunav.clear();
					}

					// MENU BACK
					if (event.getRawSlot() == 0 && itemclicked.getType().name().equals("ARROW") && clicktype == ClickType.LEFT) {
						player.closeInventory();
					}

					// BUY MENU
					if (event.getRawSlot() == 2 && itemclicked.getType().name().equals("SOUL_LANTERN") && clicktype == ClickType.LEFT) {
						OBChestShop.menunav.push(MenuTypes.Sell);
						Buying buymenu = new Buying(shopname, player);
						buymenu.draw();
					}
					//TODO: fix this illogical structure of conditionals...
					if (event.getRawSlot() < 18) {

						// store room menu
						if (event.getRawSlot() == 4 && itemclicked.getType().name().equals("CHEST") && clicktype == ClickType.LEFT) {
							OBChestShop.menunav.push(MenuTypes.Sell);
							StockRoom stockmenu = new StockRoom(shopname, player, 1);
							stockmenu.draw();
						}

						// go to settings menu
						if (itemclicked.getType().name().equals("ENDER_CHEST") && clicktype == ClickType.LEFT ) {
							if (OBChestShop.getShopList().shopExists(shopname)) {
								if (player.getUniqueId().toString().equals(OBChestShop.getShopList().getShop(shopname).getOwner())) {
									OBChestShop.menunav.push(MenuTypes.Sell);
									Settings settingsmenu = new Settings(ShopItemTypes.Sell, player, shopname, 1);
									settingsmenu.draw();
								}
							} else {
								player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.RED + "Oops. The shop appears to have been removed");
								log.log(Level.INFO, OBChestShop.getLogMsgPrefix() + "Shop " + shopname + " appears to have been removed");
								player.closeInventory();
							}
						}
					} else if (event.getRawSlot() > 17 && event.getRawSlot() < 54) {
						// lines 3 ~ 6 buy item
						ShopItem shopitem = OBChestShop.getShopList().getShop(shopname).getShopItem(ShopItemTypes.Sell, event.getRawSlot());
						OBChestShop.menunav.push(MenuTypes.Sell);
						ItemSell itemsell = new ItemSell(player, shopname, shopitem);
						itemsell.draw();
					}

				//
				// BUY MENU
				//
				} else if (shopview.startsWith("[BUY] ")) {

					event.setCancelled(true);

					// MENU BACK
					if (event.getRawSlot() == 0 && itemclicked.getType().name().equals("ARROW") && clicktype == ClickType.LEFT) {
						OBChestShop.menunav.pop();
						Selling sellmenu = new Selling(shopname, player);
						sellmenu.draw();
					}

					// SELL MENU
					if (event.getRawSlot() == 2 && itemclicked.getType().name().equals("LANTERN") && clicktype == ClickType.LEFT) {
						OBChestShop.menunav.clear();
						Selling sellmenu = new Selling(shopname, player);
						sellmenu.draw();
					}
					
					if (event.getRawSlot() < 18) {

						// store room menu
						if (event.getRawSlot() == 4 && itemclicked.getType().name().equals("CHEST") && clicktype == ClickType.LEFT) {
							OBChestShop.menunav.push(MenuTypes.Buy);
							StockRoom stockmenu = new StockRoom(shopname, player, 1);
							stockmenu.draw();
						}

						// go to settings menu
						if (itemclicked.getType().name().equals("ENDER_CHEST") && clicktype == ClickType.LEFT ) {
							if (OBChestShop.getShopList().shopExists(shopname)) {
								if (player.getUniqueId().toString().equals(OBChestShop.getShopList().getShop(shopname).getOwner())) {
									OBChestShop.menunav.push(MenuTypes.Buy);
									Settings settingsmenu = new Settings(ShopItemTypes.Buy, player, shopname, 1);
									settingsmenu.draw();
								}
							} else {
								player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.RED + "Oops. The shop appears to have been removed");
								log.log(Level.INFO, OBChestShop.getLogMsgPrefix() + "Shop " + shopname + " appears to have been removed");
								player.closeInventory();
							}
						}
					} else if (event.getRawSlot() > 17 && event.getRawSlot() < 54) {
						// lines 3 ~ 6 buy item
						OBChestShop.menunav.push(MenuTypes.Buy);
						ShopItem shopitem = OBChestShop.getShopList().getShop(shopname).getShopItem(ShopItemTypes.Buy, event.getRawSlot());
						ItemBuy itembuy = new ItemBuy(player, shopname, shopitem);
						itembuy.draw();
					}

				//
				// SELL ITEM MENU
				//
				} else if (shopview.startsWith("[SELL ")) {

					event.setCancelled(true);

					if (OBChestShop.getShopList().shopExists(shopname)) {

						// extract our hidden info - item slot
						int itemslot = Integer.parseInt(event.getView().getItem(0).getItemMeta().getLocalizedName().split("#")[1]);
						ShopItem shopitem = shop.getShopItem(ShopItemTypes.Sell, itemslot);
						ShopItem stockitem = shop.getShopItem(ShopItemTypes.Stock, shopitem.getItem().getType().name());

						// MENU BACK
						if (event.getRawSlot() == 0 && itemclicked.getType().name().equals("ARROW") && clicktype == ClickType.LEFT) {
							OBChestShop.menunav.pop();
							Selling sellmenu = new Selling(shopname, player);
							sellmenu.draw();

						} else if (event.getRawSlot() > 26 && event.getRawSlot() < 32
								&& (itemclicked.getType().name().startsWith("GREEN") || itemclicked.getType().name().startsWith("LIME"))) {
							// SELL ITEM
							Double debitamt = 0.0;
							int numitems = 0;
							if (event.getRawSlot() == 27 && itemclicked.getType().name().equals("LIME_WOOL") && clicktype == ClickType.LEFT) {
								// process purchase of one item
								debitamt = shopitem.getPrice();
								numitems = 1;
							}
							if (event.getRawSlot() == 28 && itemclicked.getType().name().equals("LIME_CONCRETE") && clicktype == ClickType.LEFT) {
								// process purchase of eight items
								debitamt = shopitem.getPrice() * 8;
								numitems = 8;
							}
							if (event.getRawSlot() == 29 && itemclicked.getType().name().equals("LIME_TERRACOTTA") && clicktype == ClickType.LEFT) {
								// process purchase of quarter stack (16 items)
								debitamt = shopitem.getPrice() * 16;
								numitems = 16;
							}
							if (event.getRawSlot() == 30 && itemclicked.getType().name().equals("GREEN_CONCRETE_POWDER") && clicktype == ClickType.LEFT) {
								// process purchase of half stack (32 items)
								debitamt = shopitem.getPrice() * 32;
								numitems = 32;
							}
							if (event.getRawSlot() == 31 && itemclicked.getType().name().equals("GREEN_CONCRETE") && clicktype == ClickType.LEFT) {
								// process purchase of one stack (64 items)
								debitamt = shopitem.getPrice() * 64;
								numitems = 64;
							}
							// deduct cost of items from player balance, give player their items, credit shop owner
							// TODO: add EconomyResponse ecr.type checking for not SUCCESS
							OBChestShop.getEconomy().withdrawPlayer(player, debitamt);
							stockitem.moveStockToInventory(player.getUniqueId().toString(), numitems);
							Player shopowner = (Player) Bukkit.getOfflinePlayer(UUID.fromString(shop.getOwner()));
							OBChestShop.getEconomy().depositPlayer(shopowner, debitamt);
							player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.GREEN + "Successfully purchased item. "
								+ ChatColor.GRAY + "$" + debitamt + ChatColor.GREEN + " removed from balance");
							ItemSell itemsell = new ItemSell(player, shopname, shopitem);
							itemsell.draw();
						}
					} else {
						player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.RED + "Oops. The shop appears to have been removed");
						log.log(Level.INFO, OBChestShop.getLogMsgPrefix() + "Shop " + shopname + " appears to have been removed");
						player.closeInventory();
					}

					//
					// BUY ITEM MENU
					//
					} else if (shopview.startsWith("[BUY ")) {

						event.setCancelled(true);

						if (OBChestShop.getShopList().shopExists(shopname)) {

							// extract our hidden info - item slot
							int itemslot = Integer.parseInt(event.getView().getItem(0).getItemMeta().getLocalizedName().split("#")[1]);
							ShopItem shopitem = shop.getShopItem(ShopItemTypes.Buy, itemslot);
							ShopItem stockitem = shop.getShopItem(ShopItemTypes.Stock, shopitem.getItemName());

							// MENU BACK
							if (event.getRawSlot() == 0 && itemclicked.getType().name().equals("ARROW") && clicktype == ClickType.LEFT) {
								// TODO: add to all menu back, or consider adding to after event cancel on each menu type?
								if (OBChestShop.menunav.size() >0) {
									OBChestShop.menunav.pop();
									Buying buymenu = new Buying(shopname, player);
									buymenu.draw();
								} else {
									player.closeInventory();
								}
							}  else if (event.getRawSlot() > 26 && event.getRawSlot() < 34
									&& (itemclicked.getType().name().startsWith("CYAN") || itemclicked.getType().name().startsWith("LIGHT_BLUE"))) {
								// BUY ITEM
								Double creditamt = 0.0;
								int numitems = 0;
								if (event.getRawSlot() == 27 && itemclicked.getType().name().equals("LIGHT_BLUE_CONCRETE_POWDER") && clicktype == ClickType.LEFT) {
									// process purchase of one item
									creditamt = shopitem.getPrice();
									numitems = 1;
								}
								if (event.getRawSlot() == 28 && itemclicked.getType().name().equals("LIGHT_BLUE_CONCRETE") && clicktype == ClickType.LEFT) {
									// process purchase of eight items
									creditamt = shopitem.getPrice() * 8;
									numitems = 8;
								}
								if (event.getRawSlot() == 29 && itemclicked.getType().name().equals("CYAN_WOOL") && clicktype == ClickType.LEFT) {
									// process purchase of quarter stack (16 items)
									creditamt = shopitem.getPrice() * 16;
									numitems = 16;
								}
								if (event.getRawSlot() == 30 && itemclicked.getType().name().equals("CYAN_CONCRETE") && clicktype == ClickType.LEFT) {
									// process purchase of half stack (32 items)
									creditamt = shopitem.getPrice() * 32;
									numitems = 32;
								}
								if (event.getRawSlot() == 31 && itemclicked.getType().name().equals("CYAN_TERRACOTTA") && clicktype == ClickType.LEFT) {
									// process purchase of one stack (64 items)
									creditamt = shopitem.getPrice() * 64;
									numitems = 64;
								}
								if (event.getRawSlot() == 33 && itemclicked.getType().name().equals("LIGHT_BLUE_TERRACOTTA") && clicktype == ClickType.LEFT) {
									// process purchase of all or as many items as we can from player - quantity is encoded in item meta
									numitems = Integer.parseInt(itemclicked.getItemMeta().getLocalizedName());
									creditamt = BigDecimal.valueOf(shopitem.getPrice() * numitems).setScale(2, RoundingMode.HALF_UP).doubleValue();
								}
								// credit player, take items from player and place into stock if we are selling this item
								// or place into storage and finally debit shop owner
								// TODO: add EconomyResponse ecr.type checking for not SUCCESS
								OBChestShop.getEconomy().depositPlayer(player, creditamt);
								// fill up sell item stock to limit first, then into stock item
								stockitem.moveInventoryToStock(player.getUniqueId().toString(), numitems);
								Player shopowner = (Player) Bukkit.getOfflinePlayer(UUID.fromString(shop.getOwner()));
								OBChestShop.getEconomy().withdrawPlayer(shopowner, creditamt);
								player.sendMessage(
										OBChestShop.getChatMsgPrefix() + ChatColor.GREEN + "Successfully sold item" + (numitems != 1 ? "s. " : ". ")
												+ ChatColor.GRAY + "$" + creditamt + ChatColor.GREEN + " added to balance");
								// TODO: add titlemanager overlay message

								ItemBuy itembuy = new ItemBuy(player, shopname, shopitem);
								itembuy.draw();
							}
							
							
						}
				//
				// SHOP STOREROOM MENU
				//
				} else if (shopview.startsWith("[STOCK] ")) {
					
					event.setCancelled(true);

					// extract our hidden info - page number
					Integer page = Integer.parseInt(event.getView().getItem(0).getItemMeta().getLocalizedName());

					// MENU BACK
					if (event.getRawSlot() == 0 && itemclicked.getType().name().equals("ARROW") && clicktype == ClickType.LEFT) {
						switch (OBChestShop.menunav.pop()) {
							case Sell:
								Selling sellmenu = new Selling(shopname, player);
								sellmenu.draw();
								break;
							case Buy:
								Buying buymenu = new Buying(shopname, player);
								buymenu.draw();
								break;
						}
					}

					// SELL MENU
					if (event.getRawSlot() == 2 && itemclicked.getType().name().equals("LANTERN") && clicktype == ClickType.LEFT) {
						OBChestShop.menunav.clear();
						Selling sellmenu = new Selling(shopname, player);
						sellmenu.draw();
					}
					// BUY MENU
					if (event.getRawSlot() == 3 && itemclicked.getType().name().equals("SOUL_LANTERN") && clicktype == ClickType.LEFT) {
						OBChestShop.menunav.clear();
						OBChestShop.menunav.push(MenuTypes.Sell);
						Buying buymenu = new Buying(shopname, player);
						buymenu.draw();
					}
					
					// go to settings menu
					if (event.getRawSlot() == 8 && itemclicked.getType().name().equals("ENDER_CHEST") && clicktype == ClickType.LEFT ) {
						if (OBChestShop.getShopList().shopExists(shopname)) {
							if (player.getUniqueId().toString().equals(OBChestShop.getShopList().getShop(shopname).getOwner())) {
								OBChestShop.menunav.push(MenuTypes.Stock);
								Settings settingsmenu = new Settings(ShopItemTypes.Stock, player, shopname, page);
								settingsmenu.draw();
							}
						} else {
							player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.RED + "Oops. The shop appears to have been removed");
							log.log(Level.INFO, OBChestShop.getLogMsgPrefix() + "Shop " + shopname + " appears to have been removed");
							player.closeInventory();
						}
					}
					
					// navigate to other stock pages
					if (event.getRawSlot() == 9 && itemclicked.getType().name().equals("GHAST_TEAR") && clicktype == ClickType.LEFT) {
						page = page.intValue() - 1;
						StockRoom prevmenu = new StockRoom(shopname, player, page);
						prevmenu.draw();
					}
					if (event.getRawSlot() == 17 && itemclicked.getType().name().equals("GOLD_NUGGET") && clicktype == ClickType.LEFT) {
						page = page.intValue() + 1;
						StockRoom nextmenu = new StockRoom(shopname, player, page);
						nextmenu.draw();
					}
					
					// process item clicked in stock 
					if (event.getRawSlot() > 17 && event.getRawSlot() < 54) {
						int configslot = event.getRawSlot() + ((page-1) * 36);
						ShopItem shopitem = OBChestShop.getShopList().getShop(shopname).getShopItem(ShopItemTypes.Stock, configslot);
						OBChestShop.menunav.push(MenuTypes.Stock);
						ItemConfig itemconfig = new ItemConfig(ShopItemTypes.Stock, player, shopname, shopitem, page);
						itemconfig.draw();
					}
					
					// ADD STOCK TO ITEM
					if (event.getRawSlot() > 53 && event.getRawSlot() < 90) {
						int stockaddamount = 1;
						boolean limitreached = false;

						if (shop.itemListContains(ShopItemTypes.Stock, itemclicked.getType().name())) {
							// get amount to add - default is one, or entire stack clicked, or entire inventory of the type
							if (clicktype == ClickType.SHIFT_LEFT) {
								stockaddamount = itemclicked.getAmount();
							} else if (clicktype == ClickType.RIGHT) {
								stockaddamount = getPlayerInventoryCount(player, itemclicked.getType().name());
							}

							ShopItem stockitem = shop.getShopItem(ShopItemTypes.Stock, itemclicked.getType().name());

							// check for stock limit breach and adjust down as necessary if there's space for more items
							if (stockitem.getStockQuantity() < shop.getStockLimit()) {
								if ((stockitem.getStockQuantity() + stockaddamount) > shop.getStockLimit()) {
									stockaddamount = shop.getStockLimit();
									limitreached = true;
								}
								
								// move inventory into stock
								if (clicktype == ClickType.RIGHT) {
									stockitem.moveInventoryToStock(player.getUniqueId().toString(), stockaddamount);
								} else {
									stockitem.addStock(stockaddamount);
									itemclicked.setAmount(itemclicked.getAmount() - stockaddamount);
								}
								shop.saveShop();

								// inform user operation completed
								player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.GREEN + "Added " + ChatColor.GRAY + itemclicked.getType().name().toLowerCase() + ChatColor.GREEN + " to stock");
								player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.GRAY + stockaddamount + ChatColor.GREEN + " items placed into stock");
								if (limitreached) {
									player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.RED + "Stock limit of " + ChatColor.GRAY + shop.getStockLimit() + ChatColor.RED + " reached for this item. ");
								}
								player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 400, 400);								
									
								StockRoom stockroom = new StockRoom(shopname, player, 1);
								stockroom.draw();
								
							} else {
								player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.RED + "Stock limit of " + ChatColor.GRAY + shop.getStockLimit() + ChatColor.RED + " reached for this item.");
							}
						}
					}
				
				//
				// SHOP SETTINGS MENU
				//
				} else if (shopview.startsWith("[SETTINGS] ")) {

					event.setCancelled(true);

					// extract our hidden info - type
					ShopItemTypes type = ShopItemTypes.valueOf(event.getView().getItem(0).getItemMeta().getLocalizedName().split("#")[0]);
					Integer page = Integer.parseInt(event.getView().getItem(0).getItemMeta().getLocalizedName().split("#")[1]);
					// MENU BACK
					if (event.getRawSlot() == 0 && itemclicked.getType().name().equals("ARROW") && clicktype == ClickType.LEFT) {
						switch (OBChestShop.menunav.pop()) {
							case Sell:
								Selling sellmenu = new Selling(shopname, player);
								sellmenu.draw();
								break;
							case Buy:
								Buying buymenu = new Buying(shopname, player);
								buymenu.draw();
								break;
							case Stock:
								StockRoom stockmenu = new StockRoom(shopname, player, page);
								stockmenu.draw();
								break;
						}
					}
					// CHANGE SHOP NAME
					if (event.getRawSlot() == 2 && itemclicked.getType().name().equals("NAME_TAG") && clicktype == ClickType.LEFT) {

						// get the new name of the shop using an anvil gui
						ItemStack guiitem = new ItemStack(Material.PAPER, 1);
						ItemMeta meta = guiitem.getItemMeta();
						meta.setDisplayName("OBChestShop - Shop name");
						meta.setLore(Arrays.asList("Enter name"));
						guiitem.setItemMeta(meta);

						gui = new AnvilGUI.Builder()
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

						gui = new AnvilGUI.Builder()
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
					if (event.getRawSlot() == 6 && itemclicked.getType().name().equals("COMPASS") && clicktype == ClickType.LEFT) {

						// get the description for the shop using an anvil gui
						ItemStack guiitem = new ItemStack(Material.COMPASS, 1);
						ItemMeta meta = guiitem.getItemMeta();
						meta.setDisplayName("OBChestShop - Shop stock limit");
						meta.setLore(Arrays.asList("Enter stock limit"));
						guiitem.setItemMeta(meta);
						String stocklimit = OBChestShop.getShopList().getShop(shopname).getStockLimit().toString();

						gui = new AnvilGUI.Builder()
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
						OBChestShop.menunav.clear();
						player.closeInventory();
					}

					// navigate to other stock pages
					if (event.getRawSlot() == 9 && itemclicked.getType().name().equals("GHAST_TEAR") && clicktype == ClickType.LEFT) {
						page = page.intValue() - 1;
						Settings prevmenu = new Settings(type, player, shopname, page);
						prevmenu.draw();
					}
					if (event.getRawSlot() == 17 && itemclicked.getType().name().equals("GOLD_NUGGET") && clicktype == ClickType.LEFT) {
						page = page.intValue() + 1;
						Settings nextmenu = new Settings(type, player, shopname, page);
						nextmenu.draw();
					}
					
					// CONFIGURE SHOP ITEM
					if (event.getRawSlot() > 17 && event.getRawSlot() < 54) {
						switch (type) {
						case Sell:
							OBChestShop.menunav.push(MenuTypes.SellSettings);
							break;
						case Buy:
							OBChestShop.menunav.push(MenuTypes.BuySettings);
							break;
						case Stock:
							OBChestShop.menunav.push(MenuTypes.StockSettings);
							break;
						}
						int configslot = event.getRawSlot() + ((page-1) * 36);
						ItemConfig itemconfig = new ItemConfig(type, player, shopname, shop.getShopItem(type, configslot), page);
						itemconfig.draw();
					}

					// ADD ITEM TO SHOP OR STOCK TO ITEM
					if (event.getRawSlot() > 53 && event.getRawSlot() < 90) {

						int stockaddamount = 1;
						boolean limitreached = false;

						boolean stockadd = false;
						if (type.equals(ShopItemTypes.Stock)) {
							if (shop.itemListContains(ShopItemTypes.Sell, itemclicked.getType().name()) ||
								shop.itemListContains(ShopItemTypes.Buy, itemclicked.getType().name())) {
								stockadd = true;
							}
						}
						// add item to buy or sell lists, or add stock to existing item
						if (type.equals(ShopItemTypes.Sell) || type.equals(ShopItemTypes.Buy) || stockadd) {
							if (!shop.itemListContains(type, itemclicked.getType().name())) {
								if (shop.hasSpace(type)) {

									// get amount to add - one, entire stack clicked, or entire inventory of the type
									if (clicktype == ClickType.SHIFT_LEFT && type.equals(ShopItemTypes.Sell)) {
										stockaddamount = itemclicked.getAmount();
									} else if (clicktype == ClickType.RIGHT && type.equals(ShopItemTypes.Sell)) {
										stockaddamount = getPlayerInventoryCount(player, itemclicked.getType().name());
									}

									// check for stock limit breach and adjust down as necessary
									if (stockaddamount > shop.getStockLimit()) {
										stockaddamount = shop.getStockLimit();
										limitreached = true;
									}
									
									// create a stock item if not already one there - we don't need to check for space
									// because if there's buy or sell space, then there's got to be stock space
									ShopItem stockitem = null;
									if (!shop.itemListContains(ShopItemTypes.Stock, itemclicked.getType().name())) {
										shop.addShopItem(ShopItemTypes.Stock, new ShopItem(shop.getNextOpenSlot(ShopItemTypes.Stock), itemclicked.getType().name(), 0));
									}
									stockitem = shop.getShopItem(ShopItemTypes.Stock, itemclicked.getType().name());

									// create an item in the inventory based off what was clicked
									ItemStack cloneitem = itemclicked.clone();
									cloneitem.setAmount(1);
									int openslot = shop.getNextOpenSlot(type);
									event.getInventory().setItem(openslot, cloneitem);

									// add item to appropriate sell or buy list
									shop.addShopItem(type, new ShopItem(openslot, itemclicked.getType().name(), 0));
									
									// move inventory for sell - don't need to move anything for buy item add
									if (type.equals(ShopItemTypes.Sell)) {
										if (clicktype == ClickType.RIGHT) {
											stockitem.moveInventoryToStock(player.getUniqueId().toString(), stockaddamount);
										} else {
											stockitem.addStock(stockaddamount);
											itemclicked.setAmount(itemclicked.getAmount() - stockaddamount);
										}
									}
									shop.saveShop();
									
									// inform user operation completed
									player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.GREEN + "Added " + ChatColor.GRAY + cloneitem.getType().name().toLowerCase() + ChatColor.GREEN + " to shop " + (type.equals(ShopItemTypes.Sell) ? "sell" : "buy")+ " list");
									if (type.equals(ShopItemTypes.Sell)) {
										player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.GRAY + stockaddamount + ChatColor.GREEN + " items placed into stock");
										if (limitreached) {
											player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.RED + "Stock limit of " + ChatColor.GRAY + shop.getStockLimit() + ChatColor.RED + " reached for this item. ");
										}
									}
									player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 400, 400);								

									Settings settingsmenu = new Settings(type, player, shopname, page);
									settingsmenu.draw();

								} else {
									player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.RED + "Unable to add item to shop's " + (type.equals(ShopItemTypes.Sell) ? "sell" : "buy") + " list as there's no space");								
								}
							} else {
								// add stock to existing item - sell and stock
								// get amount to add - one, entire stack clicked, or entire inventory of the type
								if (type.equals(ShopItemTypes.Sell) || type.equals(ShopItemTypes.Stock)) {
								
									// get quantity to add based on click type
									if (clicktype == ClickType.SHIFT_LEFT) {
										stockaddamount = itemclicked.getAmount();
									} else if (clicktype == ClickType.RIGHT) {
										stockaddamount = getPlayerInventoryCount(player, itemclicked.getType().name());
									}

									ShopItem stockitem = shop.getShopItem(ShopItemTypes.Stock, itemclicked.getType().name());

									if (stockitem.getStockQuantity() < shop.getStockLimit()) {

										// check for stock limit breach and adjust quantity down as necessary
										if ((stockitem.getStockQuantity() + stockaddamount) > shop.getStockLimit()) {
											stockaddamount = shop.getStockLimit() - stockitem.getStockQuantity();
											limitreached = true;
										}
										
										// move inventory to stock
										if (clicktype == ClickType.RIGHT) {
											stockitem.moveInventoryToStock(player.getUniqueId().toString(), stockaddamount);
										} else {
											stockitem.addStock(stockaddamount);
											itemclicked.setAmount(itemclicked.getAmount() - stockaddamount);
										}
										shop.saveShop();
										
										player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.GRAY + stockaddamount + ChatColor.GREEN + " items placed into stock");
										if (limitreached) {
											player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.RED + "Stock limit of " + ChatColor.GRAY + shop.getStockLimit() + ChatColor.RED + " reached for this item.");
										}
										player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 400, 400);
										
										Settings settingsmenu = new Settings(type, player, shopname, page);
										settingsmenu.draw();
										
									} else {
										player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.RED + "Stock limit of " + ChatColor.GRAY + shop.getStockLimit() + ChatColor.RED + " reached for this item.");
									}
								}
							}
						}
					}

				//
				// SHOP ITEM CONFIG
				//
				} else if (shopview.startsWith("[ITEM ")) {

					event.setCancelled(true);
					
					// extract our hidden info - type and item slot
					ShopItemTypes type = ShopItemTypes.valueOf(event.getView().getItem(0).getItemMeta().getLocalizedName().split("#")[0]);
					int itemslot = Integer.parseInt(event.getView().getItem(0).getItemMeta().getLocalizedName().split("#")[1]);
					int page = Integer.parseInt(event.getView().getItem(0).getItemMeta().getLocalizedName().split("#")[2]);
					
					// MENU BACK
					if (event.getRawSlot() == 0 && itemclicked.getType().name().equals("ARROW") && clicktype == ClickType.LEFT) {
						switch (OBChestShop.menunav.pop()) {
						case SellSettings:
							Settings sellsettings = new Settings(ShopItemTypes.Sell, player, shopname, 1);
							sellsettings.draw();
							break;
						case BuySettings:
							Settings buysettings = new Settings(ShopItemTypes.Buy, player, shopname, 1);
							buysettings.draw();
							break;
						case StockSettings:
							Settings stocksettings = new Settings(ShopItemTypes.Stock, player, shopname, page);
							stocksettings.draw();
							break;
						case Stock:
							StockRoom stockroom = new StockRoom(shopname, player, page);
							stockroom.draw();
							break;
						}
					}

					String itemname = (shopview.split(" ", 0)[1]).replace("]", "");
					ShopItem shopitem = OBChestShop.getShopList().getShop(shopname).getShopItem(type, itemslot);

					// REMOVE ITEM
					if (event.getRawSlot() == 8 && itemclicked.getType().name().equals("BARRIER") && clicktype == ClickType.LEFT) {

						if (type.equals(ShopItemTypes.Stock)) { 
							shopitem.moveStockToInventory(player.getUniqueId().toString(), shopitem.getStockQuantity());
							OBChestShop.getShopList().getShop(shopname).removeitem(ShopItemTypes.Stock, shopitem.getSlot());
							// remove any sell items for this stock item
							if (shop.itemListContains(ShopItemTypes.Sell, shopitem.getItemName())) {
								ShopItem sellitem = OBChestShop.getShopList().getShop(shopname).getShopItem(ShopItemTypes.Sell, shopitem.getItemName());
								OBChestShop.getShopList().getShop(shopname).removeitem(ShopItemTypes.Sell, sellitem.getSlot());
							}
							// remove any buy items for this stock item
							if (shop.itemListContains(ShopItemTypes.Buy, shopitem.getItemName())) {
								ShopItem buyitem = OBChestShop.getShopList().getShop(shopname).getShopItem(ShopItemTypes.Buy, shopitem.getItemName());
								OBChestShop.getShopList().getShop(shopname).removeitem(ShopItemTypes.Buy, buyitem.getSlot());
							}

						} else {
							ShopItem stockitem = shop.getShopItem(ShopItemTypes.Stock, shopitem.getItemName());
							// close out any player menus if they are accessing this item
							for (Player onlineplayer : Bukkit.getOnlinePlayers()) {
								if (shop.isPlayerAccessingItem(onlineplayer, type, itemname)) {
			   						onlineplayer.closeInventory();
			   						onlineplayer.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.RED + "Item " + itemname + " was removed from shop!");
								}
							}
							// determine whether to remove stock item
							if (type.equals(ShopItemTypes.Sell) && !shop.itemListContains(ShopItemTypes.Buy, shopitem.getItemName())) {
								stockitem.moveStockToInventory(player.getUniqueId().toString(), stockitem.getStockQuantity());
								OBChestShop.getShopList().getShop(shopname).removeitem(ShopItemTypes.Stock, stockitem.getSlot());
							}
							if (type.equals(ShopItemTypes.Buy) && !shop.itemListContains(ShopItemTypes.Sell, shopitem.getItemName())) {
								stockitem.moveStockToInventory(player.getUniqueId().toString(), stockitem.getStockQuantity());
								OBChestShop.getShopList().getShop(shopname).removeitem(ShopItemTypes.Stock, stockitem.getSlot());
							}
							// remove actual item
							OBChestShop.getShopList().getShop(shopname).removeitem(type, itemslot);
						}
						shop.saveShop();
						player.sendMessage(OBChestShop.getChatMsgPrefix() + ChatColor.GREEN + "Removed " + ChatColor.GRAY + itemname.toLowerCase() + ChatColor.GREEN + " from shop");
						OBChestShop.menunav.pop();
						Settings settingsmenu = new Settings(type, player, shopname, page);
						settingsmenu.draw();
					}

					// PRICE CHANGE
					if (event.getRawSlot() == 18 && itemclicked.getType().name().equals("TERRACOTTA") && clicktype == ClickType.LEFT) {
						// obtain new price for item using an anvil gui
						ItemStack guiitem = new ItemStack(Material.EMERALD, 1);

						gui = new AnvilGUI.Builder()
								.text(shopitem.getPriceFormatted())
								.title("Enter new price:")
								.itemLeft(guiitem)
								.onClose(p -> {})
								.onComplete((p, guireturnvalue) -> {
									shopitem.setPrice(SanitizePrice(shopitem.getPrice(), guireturnvalue));
									OBChestShop.getShopList().getShop(shopname).saveShop();
									Settings settingsmenu = new Settings(type, player, shopname, 1);
									settingsmenu.draw(); 
									return AnvilGUI.Response.close();
								})
								.plugin(OBChestShop.getInstance())
								.open(player);
					}

					// ITEM DESCRIPTION CHANGE
					if (event.getRawSlot() == 19 && itemclicked.getType().name().equals("WHITE_TERRACOTTA") && clicktype == ClickType.LEFT) {
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

						gui = new AnvilGUI.Builder()
								.text(description)
								.title("Enter new description:")
								.itemLeft(guiitem)
								.onClose(p -> {})
								.onComplete((p, guireturnvalue) -> {
									shopitem.setDescription(guireturnvalue.replace(",", ""));
									OBChestShop.getShopList().getShop(shopname).saveShop();
									Settings settingsmenu = new Settings(type, player, shopname, 1);
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
								// TODO: add boolean return on move inventory method
								// TODO: raises bigger issue of transaction integrity and rollback
								shopitem.moveInventoryToStock(player.getUniqueId().toString(), stockchangeamount);
								OBChestShop.getShopList().getShop(shopname).saveShop();
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

							// TODO: add boolean return on move method
							// TODO: raises bigger issue of transaction integrity and rollback
							shopitem.moveStockToInventory(player.getUniqueId().toString(), stockchangeamount);
							OBChestShop.getShopList().getShop(shopname).saveShop();

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
	}

	// Return a new sanitized double value from string, or return the current value
	private Double SanitizePrice(Double currentprice, String newprice) {
		String DOUBLE_PATTERN = "[0-9]*(\\.){0,1}\\d*";
		String INTEGER_PATTERN = "\\d+";

		if (Pattern.matches(DOUBLE_PATTERN, newprice) || Pattern.matches(INTEGER_PATTERN, newprice)) {
			try {
				BigDecimal bdc = null;
				try {
					bdc = new BigDecimal(newprice).setScale(2, RoundingMode.HALF_UP);
				} catch (Exception e) {
					log.log(Level.INFO, OBChestShop.getLogMsgPrefix() + "Failed to create new price from " + newprice);
					e.printStackTrace();
				} finally {
					Double d = bdc.doubleValue();
					if (d >= 0) {
						return d;
					} else {
						log.log(Level.INFO, OBChestShop.getLogMsgPrefix() + "New price of " + newprice + "invalid. Must be >= zero");	
					}
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
				return currentprice;
			}
		} else {
			log.log(Level.INFO, OBChestShop.getLogMsgPrefix() + "New price of " + newprice + " doesn't match our price specification");
		}
		return currentprice;
	}

	// return a new sanitized integer amount value or return the current amount if invalid
	private int SanitizeAmount(Integer currentamount, String newamount) {
		String INTEGER_PATTERN = "\\d+";
		
		if (Pattern.matches(INTEGER_PATTERN,  newamount)) {
			Integer i = null;
			try {
				i = Integer.parseInt(newamount);
			} catch (NumberFormatException e) {
				log.log(Level.INFO, OBChestShop.getLogMsgPrefix() + "Failed to create new amount of " + newamount);
				e.printStackTrace();
			} finally {
				if (i > 0 ) {
					return i;
				} else {
					log.log(Level.INFO, OBChestShop.getLogMsgPrefix() + "New amount of " + newamount + "invalid. Must be > zero");
				}
			}
		} else {
			log.log(Level.INFO, OBChestShop.getLogMsgPrefix() + "New amount of " + newamount + " doesn't match our integer specification");
		}
		return currentamount;
	}
	
	// return a new sanitized integer limit or the current limit if invalid
	private int SanitizeLimit(Integer currentlimit, Integer maxlimit, String newlimit) {
		String INTEGER_PATTERN = "\\d+";

		if (Pattern.matches(INTEGER_PATTERN,  newlimit)) {
			Integer i = null;
			try {
				i = Integer.parseInt(newlimit);
			} catch (NumberFormatException e) {
				log.log(Level.INFO, OBChestShop.getLogMsgPrefix() + "Failed to create new limit of " + newlimit);
				e.printStackTrace();
			} finally {
				if (i >= 0  && i <= maxlimit) {
					return i;
				} else {
					log.log(Level.INFO, OBChestShop.getLogMsgPrefix() + "New limit of " + newlimit + "invalid. Must be >= 0 and < " + maxlimit);
				}
			}
		} else {
			log.log(Level.INFO, OBChestShop.getLogMsgPrefix() + "New limit of " + newlimit + " doesn't match our integer specification");
		}
		return currentlimit;
	}
	
	// get player inventory count of item
	private int getPlayerInventoryCount(Player player, String itemname) {
        int onhand = 0;
        ItemStack checkitem = null;
		Inventory playerinv = player.getInventory();
		// get count of player inventory of item
		for (int i = 0; i < 40; i++) {
			checkitem = playerinv.getItem(i);
			if (checkitem != null && checkitem.getType().name().equals(itemname)) {
				onhand += checkitem.getAmount();
			}
		}
		return onhand;
	}
}