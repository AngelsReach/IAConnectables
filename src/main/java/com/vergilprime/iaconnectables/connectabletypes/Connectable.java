package com.vergilprime.iaconnectables.connectabletypes;

import com.vergilprime.iaconnectables.utils.DirectionMap;
import dev.lone.itemsadder.api.CustomFurniture;
import org.bukkit.configuration.ConfigurationSection;

public interface Connectable {
	public CustomFurniture getFurniture();

	public DirectionMap getDirectionMap();

	public static Connectable byCustomFurniture(CustomFurniture furniture) {
		String itemName = furniture.getId();
		ConfigurationSection config = furniture.getConfig().getConfigurationSection("items." + itemName);
		if (config.isConfigurationSection("behaviours.furniture.couch")) {
			return new ConnectableCouch(furniture);
		} else if (config.isConfigurationSection("behaviours.furniture.table")) {
			return new ConnectableTable(furniture);
		} else {
			return null;
		}
	}

	void Connect(int recursions);
}
