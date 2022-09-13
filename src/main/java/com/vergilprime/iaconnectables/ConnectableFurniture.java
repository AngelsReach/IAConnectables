package com.vergilprime.iaconnectables;

import dev.lone.itemsadder.api.CustomFurniture;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;

public class ConnectableFurniture {
	private final CustomFurniture furniture;
	private Integer middle;
	private Integer cornerInner;
	private Integer cornerOuter;
	private Integer endLeft;
	private Integer endRight;

	public ConnectableFurniture(CustomFurniture furniture) {
		this.furniture = furniture;
		String itemName = furniture.getId();
		ConfigurationSection config = furniture.getConfig().getConfigurationSection("items." + itemName);
		if (!config.isConfigurationSection("behaviours")) return;

		if (!config.isConfigurationSection("behaviours.connectable")) return;

		Bukkit.getLogger().info("ConnectableFurniture: " + furniture.getNamespacedID() + " grabbing other model IDs");

		middle = config.isInt("behaviours.connectable.middle") ? config.getInt("behaviours.connectable.middle") : null;
		cornerInner = config.isInt("behaviours.connectable.corner_inner") ? config.getInt("behaviours.connectable.corner_inner") : null;
		cornerOuter = config.isInt("behaviours.connectable.corner_outer") ? config.getInt("behaviours.connectable.corner_outer") : null;
		endLeft = config.isInt("behaviours.connectable.end_left") ? config.getInt("behaviours.connectable.end_left") : null;
		endRight = config.isInt("behaviours.connectable.end_right") ? config.getInt("behaviours.connectable.end_right") : null;

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

		DirectionMap directionMap = DirectionMap.getDirectionMap(frame);

		// If the directionmap is null, the item in the frame probably wasn't at a 90 degree angle.
		if (directionMap == null) {
			return;
		}

		HashMap<String, ItemFrame> neighborConnectables = getNeighborConnectables(directionMap);

		// FIGURE OUT WHICH CONNECTIONS WORK

		boolean can_connect_left = false;
		boolean can_connect_right = false;
		boolean can_connect_front = false;
		boolean can_connect_back = false;

		DirectionMap front_directionMap;
		DirectionMap right_directionMap;
		DirectionMap back_directionMap;
		DirectionMap left_directionMap;

		// If left frame was found and set in the previous for loop
		if (neighborConnectables.get("left") != null) {
			// get the facing array for the left frame
			left_directionMap = DirectionMap.getDirectionMap(neighborConnectables.get("left"));
			// if the left frame is facing a cardinal direction
			if (left_directionMap != null) {
				// if the left frame is facing forward, left or right.
				if (left_directionMap.getBack() != directionMap.getFront()) {
					can_connect_left = true; // These two frames can connect!
				}
			}
		}

		// Same story but with the right frame
		if (neighborConnectables.get("right") != null) {
			// get the facing array for the right frame
			right_directionMap = DirectionMap.getDirectionMap(neighborConnectables.get("right"));
			// if the right frame is facing a cardinal direction
			if (right_directionMap != null) {
				// if the right frame is facing forward, left or right.
				if (right_directionMap.getBack() != directionMap.getFront()) {
					can_connect_right = true; // These two frames can connect!
				}
			}
		}

		if (neighborConnectables.get("back") != null) {
			back_directionMap = DirectionMap.getDirectionMap(neighborConnectables.get("back"));
			if (back_directionMap != null) {
				if (
						back_directionMap.getRight() == directionMap.getFront() ||
								back_directionMap.getLeft() == directionMap.getFront()
				) {
					can_connect_back = true;
				}
			}
		}

		if (neighborConnectables.get("front") != null) {
			front_directionMap = DirectionMap.getDirectionMap(neighborConnectables.get("front"));
			if (front_directionMap != null) {
				if (front_directionMap.getRight() == directionMap.getFront() ||
						front_directionMap.getLeft() == directionMap.getFront()) {
					can_connect_front = true;
				}
			}
		}

		// Enumerator containing the type which will be associated with a new model, defaults to SINGLE
		ConnectableType type = ConnectableType.SINGLE;
		// If the type is a corner, it can be rotated 90 degrees to the right.
		DirectionMap new_directionMap = directionMap;

		// If can connect left and right, this is a middle
		if (can_connect_left) {
			if (can_connect_right) {

				//System.out.println("Determined to be a middle.");
				type = ConnectableType.MIDDLE;
				if (recursions > 0) {
					new ConnectableFurniture(CustomFurniture.byAlreadySpawned(neighborConnectables.get("left"))).Connect(recursions - 1);
					new ConnectableFurniture(CustomFurniture.byAlreadySpawned(neighborConnectables.get("right"))).Connect(recursions - 1);
				}
				// If can connect left and front, this is an inner corner
			} else if (can_connect_front) {

				//System.out.println("Determined to be an inner corner (front and left).");
				type = ConnectableType.CORNER_IN;
				if (recursions > 0) {
					new ConnectableFurniture(CustomFurniture.byAlreadySpawned(neighborConnectables.get("front"))).Connect(recursions - 1);
				}

				// If can connect left and back, this is an outer corner
			} else if (can_connect_back) {

				//System.out.println("Determined to be an outer corner (back and left).");
				new_directionMap = DirectionMap.getDirectionMap(directionMap.getRight());
				type = ConnectableType.CORNER_OUT;
				if (recursions > 0) {
					new ConnectableFurniture(CustomFurniture.byAlreadySpawned(neighborConnectables.get("back"))).Connect(recursions - 1);
				}
			} else {

				//System.out.println("Can only connect on the left, therefore this is a right end.");
				type = ConnectableType.END_RIGHT;
			}
			if (recursions > 0) {
				new ConnectableFurniture(CustomFurniture.byAlreadySpawned(neighborConnectables.get("left"))).Connect(recursions - 1);
			}
		} else if (can_connect_right) {
			if (can_connect_front) {

				//Rotate the directionmap 90 degrees clockwise.
				//System.out.println("Determined to be an inner corner (front and right).");
				new_directionMap = DirectionMap.getDirectionMap(directionMap.getRight());
				type = ConnectableType.CORNER_IN;
				if (recursions > 0) {
					new ConnectableFurniture(CustomFurniture.byAlreadySpawned(neighborConnectables.get("front"))).Connect(recursions - 1);
				}
				// inner
			} else if (can_connect_back) {

				//System.out.println("Determined to be an outer corner (back and right).");
				type = ConnectableType.CORNER_OUT;
				if (recursions > 0) {
					new ConnectableFurniture(CustomFurniture.byAlreadySpawned(neighborConnectables.get("back"))).Connect(recursions - 1);
				}
				// outer
			} else {
				//System.out.println("Can only connect on the right, therefore this is a left end.");
				type = ConnectableType.END_LEFT;
			}
			if (recursions > 0) {
				new ConnectableFurniture(CustomFurniture.byAlreadySpawned(neighborConnectables.get("right"))).Connect(recursions - 1);
			}
		}

		if (new_directionMap != directionMap) {

			//System.out.println("Rotating twice clockwise.");
			// I guess this rotates 90 directions according to the docs, which makes no sense but whatever.
			// Edit: Docs are wrong, rotates 45 degrees. I called it.
			frame.setRotation(frame.getRotation().rotateClockwise());
			frame.setRotation(frame.getRotation().rotateClockwise());
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

		// Get nearby entities
		HashMap<String, ItemFrame> neighborConnectables = getNeighborConnectables(DirectionMap.getDirectionMap(BlockFace.NORTH));

		// For every nearby entity
		neighborConnectables.forEach((direction, itemFrame) -> {
			if (itemFrame != null) {
				new ConnectableFurniture(CustomFurniture.byAlreadySpawned(itemFrame)).Connect(0);
			}
		});
	}

	private HashMap<String, ItemFrame> getNeighborConnectables(DirectionMap directionMap) {
		HashMap<String, ItemFrame> itemFrames = new HashMap<>();
		itemFrames.put("front", null);
		itemFrames.put("right", null);
		itemFrames.put("back", null);
		itemFrames.put("left", null);
		Location location = furniture.getArmorstand().getLocation();
		String oid = furniture.getNamespacedID();
		// For every direction in the directionMap
		directionMap.getFaces().forEach((direction, blockface) -> {
			// Get the block space off the face of the central block
			Block neighbor = location.getBlock().getRelative(blockface);

			// Get the exact location where the frame would be
			Location frameLocation = neighbor.getLocation();
			frameLocation.add(0.5, 0.03125, 0.5);

			// get a list of frames at that exact point (should only be one)
			List<Entity> neighborEntities = (List) frameLocation.getWorld().getNearbyEntities(frameLocation, 0.5, 0.5, 0.5);
			Bukkit.getLogger().info("Found " + neighborEntities.size() + " entities at " + frameLocation.toString());
			for (Entity entity : neighborEntities) {
				if (entity instanceof ItemFrame) {
					ItemFrame itemFrame = (ItemFrame) entity;
					if (CustomStack.byItemStack(((ItemFrame) entity).getItem()) != null) {
						if (CustomStack.byItemStack(((ItemFrame) entity).getItem()).getNamespacedID() == oid) {
							itemFrames.put(direction, itemFrame);
							break;
						}
					}
				}
			}
		});

		return itemFrames;
	}

	public CustomFurniture getFurniture() {
		return furniture;
	}
}
	
	
