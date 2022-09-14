package com.vergilprime.iaconnectables.extras;

import dev.lone.itemsadder.api.CustomFurniture;
import org.bukkit.configuration.ConfigurationSection;

public class MultiBlock {
	Integer left;
	Integer right;
	Integer up;
	Integer back;
	CustomFurniture furniture;

	public MultiBlock(CustomFurniture _furniture) {
		furniture = _furniture;
		String itemName = furniture.getId();
		ConfigurationSection config = furniture.getConfig().getConfigurationSection("items." + itemName);
		if (config.contains("behaviours.furniture.multi_block")) {
			left = config.getInt("behaviours.furniture.multi_block.left");
			right = config.getInt("behaviours.furniture.multi_block.right");
			up = config.getInt("behaviours.furniture.multi_block.up");
			back = config.getInt("behaviours.furniture.multi_block.back");
		}
	}

}
