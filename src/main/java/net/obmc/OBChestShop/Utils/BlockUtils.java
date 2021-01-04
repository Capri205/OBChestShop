package net.obmc.OBChestShop.Utils;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Sign;
import org.bukkit.block.data.type.WallSign;

public class BlockUtils {

	// return the block a wall sign is attached to, or return the block below if on top of the block
	public static Block getSignAttachedBlock(Block b) {
		if (b.getBlockData() instanceof Directional) {
			Directional d = (Directional)b.getBlockData();
			return b.getRelative(d.getFacing().getOppositeFace());
		}
		return b.getRelative(BlockFace.DOWN);
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