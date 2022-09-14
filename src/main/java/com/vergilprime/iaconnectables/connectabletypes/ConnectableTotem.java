package com.vergilprime.iaconnectables.connectabletypes;

import dev.lone.itemsadder.api.CustomFurniture;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;

import java.util.HashMap;
import java.util.List;

public class ConnectableTotem {
	private final CustomFurniture furniture;
	private Integer top;
	private Integer middle;
	private Integer bottom;

	public ConnectableTotem(CustomFurniture _furniture) {
		furniture = _furniture;
	}

	public CustomFurniture getFurniture() {
		return furniture;
	}

	public Integer getTop() {
		return top;
	}

	public Integer getMiddle() {
		return middle;
	}

	public Integer getBottom() {
		return bottom;
	}

	public void Connect(int recursions) {
		// TODO Auto-generated method stub
		Bukkit.getLogger().info("Unimplemented Method: ConnectableTotem.Connect()");
//		ItemFrame frame = (ItemFrame) furniture.getArmorstand();
//		ItemStack displayItem = frame.getItem();

//		HashMap<String, ConnectableTotem> neighborConnectables = getNeighborConnectables();
//
//				if (recursions > 0){
//					if( neighborConnectables.containsKey("up")) neighborConnectables.get("up").Connect(-1);
//					if( neighborConnectables.containsKey("down")) neighborConnectables.get("down").Connect(-1);
//				}

//		TotemSection section;
//		if (there is a block under){
//			if (there is a block above){
//				section = TotemSection.MIDDLE;
//			}else{
//				section = TotemSection.TOP;
//			}
//		}else{
//			if (there is a block above){
//				section = TotemSection.BOTTOM;
//			}else{
//				section = TotemSection.SINGLE;
//			}
//		}
//
//		// Get the CustomModelData of the default or "single" item
//		int customModelData = displayItem.getItemMeta().getCustomModelData();
//
//		customModelData = switch (section) {
//			case TOP -> getTop();
//			case MIDDLE -> getMiddle();
//			case BOTTOM -> getBottom();
//			default -> customModelData;
//		};
//
//		if (customModelData != displayItem.getItemMeta().getCustomModelData()) {
//			ItemMeta meta = displayItem.getItemMeta();
//			meta.setCustomModelData(customModelData);
//			displayItem.setItemMeta(meta);
//			frame.setItem(displayItem);
//		}
	}

	public void Disconnect() {
		HashMap<String, ConnectableTotem> neighborConnectables = getNeighborConnectables();
		if (!neighborConnectables.containsKey("down")) {
			neighborConnectables.get("down").Connect(0);
		}
		if (!neighborConnectables.containsKey("up")) {
			neighborConnectables.get("up").Connect(0);
		}
	}

	public static ConnectableTotem byCustomFurniture(CustomFurniture furniture) {
		String itemName = furniture.getId();
		ConfigurationSection config = furniture.getConfig().getConfigurationSection("items." + itemName);
		if (config.isConfigurationSection("behaviours.furniture.totem")) {
			return new ConnectableTotem(furniture);
		} else {
			return null;
		}
	}

	public HashMap<String, ConnectableTotem> getNeighborConnectables() {
		HashMap<String, ConnectableTotem> neighborConnectables = new HashMap<>();
		CustomFurniture furniture = getFurniture();
		Location location = furniture.getArmorstand().getLocation();
		String oid = furniture.getNamespacedID();

		Location upperFrameLocation = location.add(0, 1, 0).add(0.5, 0.03125, 0.5);
		List<Entity> neighborEntities = (List) upperFrameLocation.getWorld().getNearbyEntities(upperFrameLocation, 0.1, 0.1, 0.1);
		for (Entity entity : neighborEntities) {
			if (CustomStack.byItemStack(((ItemFrame) entity).getItem()) != null) {
				if (CustomStack.byItemStack(((ItemFrame) entity).getItem()).getNamespacedID() == oid) {
					neighborConnectables.put("up", byCustomFurniture(CustomFurniture.byAlreadySpawned(entity)));
					break;
				}
			}
		}

		Location lowerFrameLocation = location.subtract(0, 1, 0).add(0.5, 0.03125, 0.5);
		neighborEntities = (List) lowerFrameLocation.getWorld().getNearbyEntities(lowerFrameLocation, 0.1, 0.1, 0.1);
		for (Entity entity : neighborEntities) {
			if (CustomStack.byItemStack(((ItemFrame) entity).getItem()) != null) {
				if (CustomStack.byItemStack(((ItemFrame) entity).getItem()).getNamespacedID() == oid) {
					neighborConnectables.put("down", byCustomFurniture(CustomFurniture.byAlreadySpawned(entity)));
					break;
				}
			}
		}

		return neighborConnectables;
	}
}