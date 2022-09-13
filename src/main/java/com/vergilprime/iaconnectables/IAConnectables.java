package com.vergilprime.iaconnectables;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class IAConnectables extends JavaPlugin {
	private ConnectableListener listener;

	@Override
	public void onEnable() {
		listener = new ConnectableListener();
		getServer().getPluginManager().registerEvents(listener, this);
		Bukkit.getLogger().info("IAConnectables has been enabled!");

	}

	@Override
	public void onDisable() {
		listener = null;
		Bukkit.getLogger().info("IAConnectables has been disabled!");
	}
}
