package ru.zaralx.griefmod.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.zaralx.griefmod.GriefMod;

import java.io.File;
import java.io.IOException;

public class MainConfig {
    private static File file;
    private static FileConfiguration configFile;

    public static void setup(){
        file = new File(GriefMod.getInstance().getDataFolder(), "config.yml");
        file.getParentFile().mkdirs();

        if (!file.exists()) {
            GriefMod.getInstance().saveResource("config.yml", false);
        }
        configFile = YamlConfiguration.loadConfiguration(file);
    }

    public static FileConfiguration get() {
        return configFile;
    }

    public static void edit(String what, Object to) {
        configFile.set(what, to);
        save();
    }

    public static void save() {
        try {
            configFile.save(file);
        } catch (IOException ex) {
            System.out.println("SAVE FILE ERROR");
        }
    }

    public static void reload() {
        configFile = YamlConfiguration.loadConfiguration(file);
    }
}
