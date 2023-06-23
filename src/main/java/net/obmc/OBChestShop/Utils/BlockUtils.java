package net.obmc.OBChestShop.Utils;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;

public class BlockUtils {

	static Logger log = Logger.getLogger("Minecraft");

	// return the block below by default, except for a wall sign
	public static Block getSignAttachedBlock(Block b) {
		
		// assume sign is above the container or on the container for certain types
		Block attachedtoblock = b.getRelative(BlockFace.DOWN);
		if (Tag.WALL_SIGNS.isTagged(b.getType())) {
			Directional d = (Directional)b.getBlockData();
			attachedtoblock = b.getRelative(d.getFacing().getOppositeFace());
		}
		return attachedtoblock;
	}
	
	// return the block face the sign is on - return self if not a wall sign
	public static BlockFace getSignAttachedFace(Block b) {
		if (b.getBlockData() instanceof Directional) {
			Directional d = (Directional)b.getBlockData();
			return d.getFacing();
		}
		return BlockFace.SELF;
	}
}