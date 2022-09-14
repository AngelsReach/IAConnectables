package com.vergilprime.iaconnectables.utils;

import com.vergilprime.iaconnectables.connectabletypes.Connectable;
import dev.lone.itemsadder.api.CustomFurniture;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Location;
import org.bukkit.Rotation;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;

import java.util.HashMap;
import java.util.List;

public class DirectionMap {
	private Connectable connectable;
	private HashMap<String, BlockFace> faces = new HashMap<>();

	public DirectionMap(Connectable _connectable) {
		connectable = _connectable;
		ItemFrame frame = (ItemFrame) connectable.getFurniture().getArmorstand();
		switch (frame.getRotation()) {
			case FLIPPED -> {
				faces.put("front", BlockFace.NORTH);
				faces.put("right", BlockFace.EAST);
				faces.put("back", BlockFace.SOUTH);
				faces.put("left", BlockFace.WEST);
			}
			case COUNTER_CLOCKWISE -> {
				faces.put("front", BlockFace.EAST);
				faces.put("right", BlockFace.SOUTH);
				faces.put("back", BlockFace.WEST);
				faces.put("left", BlockFace.NORTH);
			}
			case NONE -> {
				faces.put("front", BlockFace.SOUTH);
				faces.put("right", BlockFace.WEST);
				faces.put("back", BlockFace.NORTH);
				faces.put("left", BlockFace.EAST);
			}
			case CLOCKWISE -> {
				faces.put("front", BlockFace.WEST);
				faces.put("right", BlockFace.NORTH);
				faces.put("back", BlockFace.EAST);
				faces.put("left", BlockFace.SOUTH);
			}
			default -> {
				faces = null;
			}
		}
	}

	public HashMap<String, BlockFace> getFaces() {
		return faces;
	}

	public BlockFace getFront() {
		return faces.get("front");
	}

	public BlockFace getRight() {
		return faces.get("right");
	}

	public BlockFace getBack() {
		return faces.get("back");
	}

	public BlockFace getLeft() {
		return faces.get("left");
	}

	public void rotate(Rotation rotation) {
		ItemFrame frame = (ItemFrame) connectable.getFurniture().getArmorstand();
		switch (rotation) {
			case CLOCKWISE -> {
				BlockFace front = faces.get("front");
				BlockFace right = faces.get("right");
				BlockFace back = faces.get("back");
				BlockFace left = faces.get("left");
				faces.put("front", left);
				faces.put("right", front);
				faces.put("back", right);
				faces.put("left", back);
				switch (frame.getRotation()) {
					case FLIPPED -> frame.setRotation(Rotation.COUNTER_CLOCKWISE);
					case COUNTER_CLOCKWISE -> frame.setRotation(Rotation.NONE);
					case NONE -> frame.setRotation(Rotation.CLOCKWISE);
					case CLOCKWISE -> frame.setRotation(Rotation.FLIPPED);
				}
			}
			case FLIPPED -> {
				BlockFace front = faces.get("front");
				BlockFace right = faces.get("right");
				BlockFace back = faces.get("back");
				BlockFace left = faces.get("left");
				faces.put("front", back);
				faces.put("right", left);
				faces.put("back", front);
				faces.put("left", right);
				switch (frame.getRotation()) {
					case FLIPPED -> frame.setRotation(Rotation.NONE);
					case COUNTER_CLOCKWISE -> frame.setRotation(Rotation.CLOCKWISE);
					case NONE -> frame.setRotation(Rotation.FLIPPED);
					case CLOCKWISE -> frame.setRotation(Rotation.COUNTER_CLOCKWISE);
				}
			}
			case COUNTER_CLOCKWISE -> {
				BlockFace front = faces.get("front");
				BlockFace right = faces.get("right");
				BlockFace back = faces.get("back");
				BlockFace left = faces.get("left");
				faces.put("front", right);
				faces.put("right", back);
				faces.put("back", left);
				faces.put("left", front);
				switch (frame.getRotation()) {
					case FLIPPED -> frame.setRotation(Rotation.CLOCKWISE);
					case COUNTER_CLOCKWISE -> frame.setRotation(Rotation.FLIPPED);
					case NONE -> frame.setRotation(Rotation.COUNTER_CLOCKWISE);
					case CLOCKWISE -> frame.setRotation(Rotation.NONE);
				}
			}
		}
	}

	public HashMap<String, Connectable> getNeighborConnectables() {
		HashMap<String, Connectable> neighborConnectables = new HashMap<>();
		if (faces == null) return neighborConnectables;
		CustomFurniture furniture = connectable.getFurniture();
		Location location = furniture.getArmorstand().getLocation();
		String oid = furniture.getNamespacedID();
		// For every direction in the directionMap
		getFaces().forEach((direction, blockface) -> {
			// Get the block space off the face of the central block
			Block neighbor = location.getBlock().getRelative(blockface);

			// Get the exact location where the frame would be
			Location frameLocation = neighbor.getLocation();
			frameLocation.add(0.5, 0.03125, 0.5);

			// get a list of frames at that exact point (should only be one)
			List<Entity> neighborEntities = (List) frameLocation.getWorld().getNearbyEntities(frameLocation, 0.5, 0.5, 0.5);
			for (Entity entity : neighborEntities) {
				CustomFurniture n_furniture = CustomFurniture.byAlreadySpawned(entity);
				if (n_furniture != null) {
					if (CustomStack.byItemStack(((ItemFrame) n_furniture.getArmorstand()).getItem()).getNamespacedID() == oid) {
						neighborConnectables.put(direction, Connectable.byCustomFurniture(n_furniture));
						break;
					}
				}
			}
		});

		return neighborConnectables;
	}
}