package com.vergilprime.iaconnectables.connectabletypes;

import com.vergilprime.iaconnectables.enums.CouchSection;
import com.vergilprime.iaconnectables.utils.DirectionMap;
import dev.lone.itemsadder.api.CustomFurniture;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Bukkit;
import org.bukkit.Rotation;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;

public class ConnectableCouch implements Connectable {
	private final CustomFurniture furniture;
	private DirectionMap directionMap;
	private Integer middle;
	private Integer cornerInner;
	private Integer cornerOuter;
	private Integer endLeft;
	private Integer endRight;


	public ConnectableCouch(CustomFurniture furniture) {
		this.furniture = furniture;
		String itemName = furniture.getId();
		ConfigurationSection config = furniture.getConfig().getConfigurationSection("items." + itemName);

		if (!config.isConfigurationSection("behaviours.furniture.couch")) return;

		middle = config.isInt("behaviours.furniture.couch.middle") ? config.getInt("behaviours.furniture.couch.middle") : null;
		cornerInner = config.isInt("behaviours.furniture.couch.corner_inner") ? config.getInt("behaviours.furniture.couch.corner_inner") : null;
		cornerOuter = config.isInt("behaviours.furniture.couch.corner_outer") ? config.getInt("behaviours.furniture.couch.corner_outer") : null;
		endLeft = config.isInt("behaviours.furniture.couch.end_left") ? config.getInt("behaviours.furniture.couch.end_left") : null;
		endRight = config.isInt("behaviours.furniture.couch.end_right") ? config.getInt("behaviours.furniture.couch.end_right") : null;

		directionMap = new DirectionMap(this);
	}

	@Override
	public CustomFurniture getFurniture() {
		return furniture;
	}

	@Override
	public DirectionMap getDirectionMap() {
		return directionMap;
	}

	public int getMiddle() {
		return middle;
	}

	public int getCornerInner() {
		return cornerInner;
	}

	public int getCornerOuter() {
		return cornerOuter;
	}

	public int getEndLeft() {
		return endLeft;
	}

	public int getEndRight() {
		return endRight;
	}

	@Override
	public void Connect(int recursions) {
		// Get the item from the frame
		ItemFrame frame = (ItemFrame) furniture.getArmorstand();
		ItemStack displayItem = frame.getItem();

		CustomStack customStack = CustomStack.byItemStack(displayItem);

		// Item is somehow not an IA item.
		if (customStack == null) {
			Bukkit.getLogger().warning("ConnectableFurniture: Item in frame " + frame.getUniqueId() + " is not an IA item.");
			return;
		}

		// If the directionmap is null, the item in the frame probably wasn't at a 90 degree angle.
		if (directionMap == null) {
			return;
		}

		HashMap<String, Connectable> neighborConnectables = directionMap.getNeighborConnectables();

		// FIGURE OUT WHICH CONNECTIONS WORK
		boolean can_connect_left = false;
		boolean can_connect_right = false;
		boolean can_connect_front = false;
		boolean can_connect_back = false;

		DirectionMap front_directionMap;
		DirectionMap right_directionMap;
		DirectionMap back_directionMap;
		DirectionMap left_directionMap;

		///////////////// SPECIFIC PARTS ///////////////////////

		// If left frame was found and set in the previous for loop
		if (neighborConnectables.containsKey("left")) {
			// get the facing array for the left frame
			left_directionMap = neighborConnectables.get("left").getDirectionMap();
			// if the left frame is facing a cardinal direction
			if (left_directionMap != null) {
				// if the left frame is facing forward, left or right.
				if (left_directionMap.getBack() != directionMap.getFront()) {
					can_connect_left = true; // These two frames can connect!
				}
			}
		}

		// Same story but with the right frame
		if (neighborConnectables.containsKey("right")) {
			// get the facing array for the right frame
			right_directionMap = neighborConnectables.get("right").getDirectionMap();
			// if the right frame is facing a cardinal direction
			if (right_directionMap != null) {
				// if the right frame is facing forward, left or right.
				if (right_directionMap.getBack() != directionMap.getFront()) {
					can_connect_right = true; // These two frames can connect!
				}
			}
		}

		if (neighborConnectables.containsKey("back")) {
			back_directionMap = neighborConnectables.get("back").getDirectionMap();
			if (back_directionMap != null) {
				if (
						back_directionMap.getRight() == directionMap.getFront() ||
								back_directionMap.getLeft() == directionMap.getFront()
				) {
					can_connect_back = true;
				}
			}
		}

		if (neighborConnectables.containsKey("front")) {
			front_directionMap = neighborConnectables.get("front").getDirectionMap();
			if (front_directionMap != null) {
				if (front_directionMap.getRight() == directionMap.getFront() ||
						front_directionMap.getLeft() == directionMap.getFront()) {
					can_connect_front = true;
				}
			}
		}

		// Enumerator containing the type which will be associated with a new model, defaults to SINGLE
		CouchSection type = CouchSection.SINGLE;

		if (recursions > 0) {
			if (can_connect_front) neighborConnectables.get("front").Connect(recursions - 1);
			if (can_connect_back) neighborConnectables.get("back").Connect(recursions - 1);
			if (can_connect_left) neighborConnectables.get("left").Connect(recursions - 1);
			if (can_connect_right) neighborConnectables.get("right").Connect(recursions - 1);
		}

		if (can_connect_left) {
			if (can_connect_right) {
				type = CouchSection.MIDDLE;
			} else if (can_connect_front) {
				type = CouchSection.CORNER_IN;
			} else if (can_connect_back) {
				directionMap.rotate(Rotation.CLOCKWISE);
				type = CouchSection.CORNER_OUT;
			} else {
				type = CouchSection.END_RIGHT;
			}
		} else if (can_connect_right) {
			if (can_connect_front) {
				directionMap.rotate(Rotation.CLOCKWISE);
				type = CouchSection.CORNER_IN;
			} else if (can_connect_back) {
				type = CouchSection.CORNER_OUT;
			} else {
				type = CouchSection.END_LEFT;
			}
		}

		// Get the CustomModelData of the default or "single" item
		int customModelData = displayItem.getItemMeta().getCustomModelData();

		// If the type is anything other than signal then we can look to the config for the right customModelData
		customModelData = switch (type) {
			case MIDDLE -> getMiddle();
			case CORNER_IN -> getCornerInner();
			case CORNER_OUT -> getCornerOuter();
			case END_LEFT -> getEndLeft();
			case END_RIGHT -> getEndRight();
			default -> customModelData;
		};

		///////////////////// END SPECIFIC PARTS ///////////////////////

		//System.out.println("Connectable is of type " + type.toString() + ", new model: " + customModelData + ".");

		// Set the CustomModelData of the item in the frame.
		if (customModelData != displayItem.getItemMeta().getCustomModelData()) {
			ItemMeta meta = displayItem.getItemMeta();
			meta.setCustomModelData(customModelData);
			displayItem.setItemMeta(meta);
			frame.setItem(displayItem);
		}
	}


	public void Disconnect() {
		HashMap<String, Connectable> neighborConnectables = directionMap.getNeighborConnectables();
		neighborConnectables.forEach((direction, neighbor) -> {
			neighbor.Connect(0);
		});
	}
}
	
	
