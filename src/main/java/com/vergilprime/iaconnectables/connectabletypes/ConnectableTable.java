package com.vergilprime.iaconnectables.connectabletypes;

import com.vergilprime.iaconnectables.enums.TableSection;
import com.vergilprime.iaconnectables.utils.DirectionMap;
import dev.lone.itemsadder.api.CustomFurniture;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Rotation;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;

public class ConnectableTable implements Connectable {
	private final CustomFurniture furniture;
	private DirectionMap directionMap;
	private Integer oneWay;
	private Integer twoWayStraight;
	private Integer twoWayCorner;
	private Integer threeWay;
	private Integer fourWay;


	public ConnectableTable(CustomFurniture furniture) {
		this.furniture = furniture;
		String itemName = furniture.getId();
		ConfigurationSection config = furniture.getConfig().getConfigurationSection("items." + itemName);

		if (!config.isConfigurationSection("behaviours.furniture.table")) return;

		oneWay = config.isInt("behaviours.furniture.table.one_way") ? config.getInt("behaviours.furniture.table.one_way") : null;
		twoWayStraight = config.isInt("behaviours.furniture.table.two_way_straight") ? config.getInt("behaviours.furniture.table.two_way_straight") : null;
		twoWayCorner = config.isInt("behaviours.furniture.table.two_way_corner") ? config.getInt("behaviours.furniture.table.two_way_corner") : null;
		threeWay = config.isInt("behaviours.furniture.table.three_way") ? config.getInt("behaviours.furniture.table.three_way") : null;
		fourWay = config.isInt("behaviours.furniture.table.four_way") ? config.getInt("behaviours.furniture.table.four_way") : null;

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

	public Integer getOneWay() {
		return oneWay;
	}

	public Integer getTwoWayStraight() {
		return twoWayStraight;
	}

	public Integer getTwoWayCorner() {
		return twoWayCorner;
	}

	public Integer getThreeWay() {
		return threeWay;
	}

	public Integer getFourWay() {
		return fourWay;
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
			can_connect_left = true; // These two tables can connect!
		}

		// Same story but with the right frame
		if (neighborConnectables.containsKey("right")) {
			can_connect_right = true; // These two tables can connect!
		}

		if (neighborConnectables.containsKey("back")) {
			can_connect_back = true;
		}

		if (neighborConnectables.containsKey("front")) {
			can_connect_front = true;
		}

		// Enumerator containing the type which will be associated with a new model, defaults to SINGLE
		TableSection type = TableSection.SINGLE;
		// If the type is a corner, it can be rotated 90 degrees to the right.
		DirectionMap new_directionMap = directionMap;

		if (recursions > 0) {
			if (can_connect_front) neighborConnectables.get("front").Connect(recursions - 1);
			if (can_connect_back) neighborConnectables.get("back").Connect(recursions - 1);
			if (can_connect_left) neighborConnectables.get("left").Connect(recursions - 1);
			if (can_connect_right) neighborConnectables.get("right").Connect(recursions - 1);
		}

		if (can_connect_front) { // 1
			if (can_connect_right) { // 2
				if (can_connect_back) { // 3
					if (can_connect_left) { // 4 All connected
						type = TableSection.FOUR_WAY;
					} else { // 3 connected, front, right, back
						type = TableSection.THREE_WAY;
						//directionMap.rotate(Rotation.CLOCKWISE);
					}
				} else { // 2
					if (can_connect_left) { // 3 connected, front, right, left
						type = TableSection.THREE_WAY;
					} else { // 2 connected, front, right
						type = TableSection.TWO_WAY_CORNER;
						//directionMap.rotate(Rotation.CLOCKWISE);
					}
				}
			} else { // 1
				if (can_connect_back) { // 2
					if (can_connect_left) { // 3 connected, front, back, left
						type = TableSection.THREE_WAY;
						//directionMap.rotate(Rotation.CLOCKWISE);
					} else { // 2 connected, front, back
						type = TableSection.TWO_WAY_STRAIGHT;
					}
				} else { // 1
					if (can_connect_left) { // 2 connected, front, left
						type = TableSection.TWO_WAY_CORNER;
						///directionMap.rotate(Rotation.CLOCKWISE);
					} else { // 1
						type = TableSection.ONE_WAY;
					}
				}
			}
		} else { // 0
			if (can_connect_right) { // 1
				if (can_connect_back) { // 2
					if (can_connect_left) { // 3 connected, right, back, left
						type = TableSection.THREE_WAY;
						directionMap.rotate(Rotation.CLOCKWISE);
					} else { // 2 connected, right, back
						type = TableSection.TWO_WAY_CORNER;
						//directionMap.rotate(Rotation.CLOCKWISE);
					}
				} else { // 1
					if (can_connect_left) { // 2 connected, right, left
						type = TableSection.TWO_WAY_STRAIGHT;
						//directionMap.rotate(Rotation.CLOCKWISE);
					} else { // 1 connected, right
						type = TableSection.SINGLE;
					}
				}
			} else { // 0
				if (can_connect_back) { // 1
					neighborConnectables.get("back").Connect(recursions + 1);
					if (can_connect_left) { // 2 connected, back, left
						neighborConnectables.get("left").Connect(recursions + 1);
						type = TableSection.TWO_WAY_CORNER;
						//directionMap.rotate(Rotation.CLOCKWISE);
					} else { // 1 connected, back
						type = TableSection.SINGLE;
					}
				} else { // 0
					if (can_connect_left) { // 1 connected, left
						neighborConnectables.get("left").Connect(recursions + 1);
						type = TableSection.ONE_WAY;
					} else { // 0 connected, none
						type = TableSection.SINGLE;
					}
				}
			}
		}
		int customModelData = displayItem.getItemMeta().getCustomModelData();

		// If the type is anything other than signal then we can look to the config for the right customModelData
		customModelData = switch (type) {
			case ONE_WAY -> getOneWay();
			case TWO_WAY_STRAIGHT -> getTwoWayStraight();
			case TWO_WAY_CORNER -> getTwoWayCorner();
			case THREE_WAY -> getThreeWay();
			case FOUR_WAY -> getFourWay();
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
}


