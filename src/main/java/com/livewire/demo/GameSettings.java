package com.livewire.demo;

import com.livewire.config.ExposedConfig;

public class GameSettings {
    @ExposedConfig(description = "Maximum player health")
    public int maxHealth = 100;

    @ExposedConfig(description = "Player movement speed")
    public double moveSpeed = 4.5;

    @ExposedConfig(description = "Enable double-jump")
    public boolean doubleJumpEnabled = true;

    @ExposedConfig(description = "Player name shown in HUD")
    public String playerName = "Adventurer";

    @ExposedConfig(description = "Difficulty level")
    public Difficulty difficulty = Difficulty.NORMAL;

    @ExposedConfig(description = "Static example - shared across all instances")
    public static int globalSpawnRate = 5;

    public enum Difficulty { EASY, NORMAL, HARD, NIGHTMARE }
}
