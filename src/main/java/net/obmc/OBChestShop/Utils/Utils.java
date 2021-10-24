package net.obmc.OBChestShop.Utils;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Utils {

	static Logger log = Logger.getLogger("Minecraft");

	// turn itemstack into a json string and back for saving to and loading from the shop config
	public static String ItemStackAsString(ItemStack item) {
		return JsonItemStack.toJson(item);
    }
	public static ItemStack ItemStackFromString(String itemstring) {
		return JsonItemStack.fromJson(itemstring);
    }

	// create a hash value for an itemstack for uniqueness of shop item
	public static int GenerateItemHash(ItemStack item) {
		ItemStack localitem = item.clone();
		StripShopLore(localitem);
		return Objects.hash(ItemStackAsString(localitem));
	}

	// strip out shop related lore from an item
	public static void StripShopLore(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		if (meta != null) {
			List<String> lore = meta.getLore(); 
			if (lore != null && !lore.isEmpty()) {
				Iterator<String> it = lore.iterator();
				while (it.hasNext()) {
					String s = it.next();
					if ( s.contains("Price: ") || s.contains("Stock: ") || s.contains("Left Click") || s.contains("Right Click")) {
						it.remove();
					}
				}
				meta.setLore(lore);
				item.setItemMeta(meta);
			}
		}
	}
}