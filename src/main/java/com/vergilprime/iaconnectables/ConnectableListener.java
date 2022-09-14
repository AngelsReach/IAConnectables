package com.vergilprime.iaconnectables;

import com.vergilprime.iaconnectables.connectabletypes.ConnectableCouch;
import com.vergilprime.iaconnectables.connectabletypes.ConnectableTable;
import com.vergilprime.iaconnectables.connectabletypes.ConnectableTotem;
import dev.lone.itemsadder.api.Events.FurnitureBreakEvent;
import dev.lone.itemsadder.api.Events.FurniturePlaceSuccessEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ConnectableListener implements Listener {
	private final IACPlugin plugin;

	public ConnectableListener(IACPlugin _plugin) {
		plugin = _plugin;
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onFurniturePlaceEvent(FurniturePlaceSuccessEvent event) {
		String itemName = event.getFurniture().getId();
		ConfigurationSection config = event.getFurniture().getConfig().getConfigurationSection("items." + itemName);
		if (config.isConfigurationSection("behaviours.furniture.couch")) {
			ConnectableCouch couch = new ConnectableCouch(event.getFurniture());
			couch.Connect(1);
		} else if (config.isConfigurationSection("behaviours.furniture.table")) {
			ConnectableTable table = new ConnectableTable(event.getFurniture());
			table.Connect(1);
		} else if (config.isConfigurationSection("behaviours.furniture.totem")) {
			ConnectableTotem totem = new ConnectableTotem(event.getFurniture());
			totem.Connect(1);
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onFurnitureBreakEvent(FurnitureBreakEvent event) {
		String itemName = event.getFurniture().getId();
		ConfigurationSection config = event.getFurniture().getConfig().getConfigurationSection("items." + itemName);
		if (config.isConfigurationSection("behaviours.furniture.couch")) {
			ConnectableCouch couch = new ConnectableCouch(event.getFurniture());
			Bukkit.getScheduler().runTaskLater(plugin, couch::Disconnect, 1);
		} else if (config.isConfigurationSection("behaviours.furniture.table")) {
			ConnectableTable table = new ConnectableTable(event.getFurniture());
			Bukkit.getScheduler().runTaskLater(plugin, table::Disconnect, 1);
		} else if (config.isConfigurationSection("behaviours.furniture.totem")) {
			ConnectableTotem totem = new ConnectableTotem(event.getFurniture());
			Bukkit.getScheduler().runTaskLater(plugin, totem::Disconnect, 1);
		}
	}
}
