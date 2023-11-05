package io.github.stonley890.dreamvisitor.data;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PlayerMemory {

    public FileConfiguration toFileConfig() {
        FileConfiguration fileConfig = new YamlConfiguration();
        fileConfig.set("discordToggled", discordToggled);
        fileConfig.set("vanished", vanished);
        fileConfig.set("creative", creative);
        fileConfig.set("survivalInv", survivalInv);
        fileConfig.set("creativeInv", creativeInv);
        fileConfig.set("autoinvswap", autoinvswap);
        fileConfig.set("autoRadio", autoRadio);
        fileConfig.set("resourcePackHash", resourcePackHash);

        return fileConfig;
    }

    public static PlayerMemory getFromFileConfig(FileConfiguration fileConfig) {
        PlayerMemory memory = new PlayerMemory();
        memory.discordToggled = fileConfig.getBoolean("discordToggled");
        memory.vanished = fileConfig.getBoolean("vanished");
        memory.creative = fileConfig.getBoolean("vanished");
        memory.autoinvswap = fileConfig.getBoolean("autoinvswap");
        memory.autoRadio = fileConfig.getBoolean("autoradio");
        List<ItemStack> survivalInvList = (List<ItemStack>) fileConfig.getList("survivalInv");
        List<ItemStack> creativeInvList = (List<ItemStack>) fileConfig.getList("creativeInv");

        if (survivalInvList == null) survivalInvList = new ArrayList<>();
        if (creativeInvList == null) creativeInvList = new ArrayList<>();

        memory.survivalInv = survivalInvList.toArray(ItemStack[]::new);
        memory.creativeInv = creativeInvList.toArray(ItemStack[]::new);

        memory.resourcePackHash = fileConfig.getString("resourcePackHash");

        return memory;
    }

    /**
     * Whether the user can see messages sent from the Discord game chat.
     */
    public boolean discordToggled;
    /**
     * Whether the user is hidden from Discord.
     */
    public boolean vanished;
    /**
     * Tracks the user's current inventory. May not necessarily correlate to actual gamemode.
     */
    public boolean creative;
    /**
     * Whether the user has automatic inventory swap enabled.
     */
    public boolean autoinvswap;
    /**
     * Whether the user has automatic staff radio enabled.
     */
    public boolean autoRadio;
    public ItemStack[] survivalInv;
    public ItemStack[] creativeInv;
    /**
     * The user's last resource pack hash
     */
    public String resourcePackHash;
}
