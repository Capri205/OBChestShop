package net.obmc.OBChestShop.Tasks;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.scheduler.BukkitRunnable;

import net.obmc.OBChestShop.OBChestShop;
import net.obmc.OBChestShop.ShopStates.ShopState;
import net.obmc.OBChestShop.Shop.Shop;

public class ShopChecker extends BukkitRunnable {

	
	Logger log = Logger.getLogger("Minecraft");

	OBChestShop plugin;
	ArrayList<String> blacklist = new ArrayList<String>();
	
	public ShopChecker(OBChestShop plugin) {
		this.plugin = plugin;
		if (blacklist == null) {
			blacklist = new ArrayList<String>();
		}
	}
	
	@Override
	public void run() {
		
		// validate shop - make sure world, chests and signs are in place
		ShopState state = null;
		for (Shop shop : OBChestShop.getShopList().getShopsByOwner()) {

			state = shop.validateShop();

			if (shop.getState().compareTo(state) != 0) {
				if (blacklist.contains(shop.getName())) {
					shop.setStatus(state);
					if (state.compareTo(ShopState.ShopOK) == 0) {
						blacklist.remove(shop.getName());
					}
				} else {
					shop.setStatus(state);
					if (state.compareTo(ShopState.ShopOK) != 0) {
						blacklist.add(shop.getName());
					}
				}
			}
		}
	}

}
