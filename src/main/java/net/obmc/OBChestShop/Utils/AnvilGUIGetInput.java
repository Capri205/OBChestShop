package net.obmc.OBChestShop.Utils;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import net.obmc.OBChestShop.OBChestShop;

import net.wesjd.anvilgui.AnvilGUI;

public class AnvilGUIGetInput {

	private static String returnvalue;
	
	public static String getString(Player player, String prompt) {
		
		new BukkitRunnable() {
			@Override
			public void run() {

				ItemStack it = new ItemStack(Material.PAPER, 1);
				ItemMeta meta = it.getItemMeta();
				meta.setDisplayName("A");
				meta.setLore(Arrays.asList("B"));
				it.setItemMeta(meta);

				AnvilGUI gui = new AnvilGUI.Builder()
					.preventClose()
					.text("D")
					.title("E").item(it)
					.onClose(p -> {})
					.onComplete((p, guireturnvalue) -> {
						while(guireturnvalue.isEmpty()) {
							
						}
						returnvalue = guireturnvalue;
						return AnvilGUI.Response.close();
					})
					.plugin(OBChestShop.getInstance()).open(player);
			}
		}.runTask(OBChestShop.getInstance());
		return returnvalue;
	}
}
