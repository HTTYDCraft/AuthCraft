package com.httydcraft.authcraft.utils;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlayerState {
    private final Location location;
    private final ItemStack[] inventory;
    private final double health;
    private final int foodLevel;
    private final float experience;

    public PlayerState(Player player) {
        this.location = player.getLocation();
        this.inventory = player.getInventory().getContents();
        this.health = player.getHealth();
        this.foodLevel = player.getFoodLevel();
        this.experience = player.getExp();
    }

    public void restore(Player player) {
        player.teleport(location);
        player.getInventory().setContents(inventory);
        player.setHealth(health);
        player.setFoodLevel(foodLevel);
        player.setExp(experience);
    }
}