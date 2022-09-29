package com.vergilprime.iaconnectables;

import dev.lone.itemsadder.api.Events.FurnitureBreakEvent;
import dev.lone.itemsadder.api.Events.FurniturePlaceSuccessEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ConnectableListener implements Listener {

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onFurniturePlaceEvent(FurniturePlaceSuccessEvent event) {
		Bukkit.getLogger().info("FurniturePlaceSuccessEvent");
		Entity entity = event.getBukkitEntity();
		if (!(entity instanceof ItemFrame)) {
			Bukkit.getLogger().info("Not a frame");
			return;
		} else {
			Bukkit.getLogger().info("Is a frame");
		}
		String itemName = event.getFurniture().getId();
		ConfigurationSection config = event.getFurniture().getConfig().getConfigurationSection("items." + itemName);
		if (config.isConfigurationSection("behaviours.furniture.couch")) {
			Bukkit.getLogger().info("behaviours.furniture.couch is a section");
			ConnectableFurniture connectable = new ConnectableFurniture(event.getFurniture());
			connectable.Connect(1);
		} else {
			Bukkit.getLogger().info("behaviours.furniture.couch is not a section");
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onFurnitureBreakEvent(FurnitureBreakEvent event) {
		Entity entity = event.getBukkitEntity();
		if (!(entity instanceof ItemFrame)) {
			return;
		}
		ConfigurationSection config = event.getFurniture().getConfig();
		if (config.isConfigurationSection("behaviours.furniture.couch")) {
			ConnectableFurniture connectable = new ConnectableFurniture(event.getFurniture());
			connectable.Disconnect();
		}
	}
}
