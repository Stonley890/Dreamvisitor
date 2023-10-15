package io.github.stonley890.dreamvisitor.data;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

public class PlayerMemory {

    public FileConfiguration toFileConfig() {
        FileConfiguration fileConfig = new YamlConfiguration();
        fileConfig.set("discordToggled", discordToggled);
        fileConfig.set("vanished", vanished);
        fileConfig.set("creative", creative);
        fileConfig.set("survivalInv", survivalInv);
        fileConfig.set("creativeInv", creativeInv);

        return fileConfig;
    }

    public static PlayerMemory getFromFileConfig(FileConfiguration fileConfig) {
        PlayerMemory memory = new PlayerMemory();
        memory.discordToggled = fileConfig.getBoolean("discordToggled");
        memory.vanished = fileConfig.getBoolean("vanished");
        memory.creative = fileConfig.getBoolean("vanished");
        memory.survivalInv = (ItemStack[]) fileConfig.get("survivalInv");
        memory.creativeInv = (ItemStack[]) fileConfig.get("creativeInv");

        return memory;
    }

    public boolean discordToggled;
    public boolean vanished;
    public boolean creative;
    public ItemStack[] survivalInv;
    public ItemStack[] creativeInv;
}
