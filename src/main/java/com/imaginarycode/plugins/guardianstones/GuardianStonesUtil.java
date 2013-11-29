package com.imaginarycode.plugins.guardianstones;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * @author tux-amd64
 */
public class GuardianStonesUtil {
    public static YamlConfiguration loadYaml(String file) {
        File map = new File(GuardianStones.self.getDataFolder(), file);
        if (!map.exists()) {
            GuardianStones.self.getLogger().info(file + " not found, creating...");
            try {
                GuardianStones.self.getDataFolder().mkdirs();
                map.createNewFile();
                GuardianStones.self.getLogger().info("File created successfully!");
            } catch (IOException e) {
                GuardianStones.self.getLogger().info("Unable to create " + file + ", this may be harmless:");
                e.printStackTrace();
            }
        }
        return YamlConfiguration.loadConfiguration(map);
    }

    public static void saveYaml(YamlConfiguration y, String file) {
        File map = new File(GuardianStones.self.getDataFolder(), file);
        try {
            GuardianStones.self.getDataFolder().mkdirs();
            y.save(map);
        } catch (IOException e) {
            GuardianStones.self.getLogger().info("Unable to create " + file + ", this may be harmless:");
            e.printStackTrace();
        }
    }

    public static String getLocationAsStringGuid(Location loc) {
        return "_" + loc.getBlockX() + "_" + loc.getBlockY() + "_" + loc.getBlockZ();
    }

    public static String serializedLoc(Location loc) {
        return "" + loc.getBlockX() + "," + Math.round(loc.getY()) + "," + loc.getBlockZ();
    }
}
