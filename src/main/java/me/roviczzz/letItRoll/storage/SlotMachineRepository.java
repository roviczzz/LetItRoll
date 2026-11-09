package me.roviczzz.letItRoll.storage;

import me.roviczzz.letItRoll.model.SlotMachine;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SlotMachineRepository {
    private final File file;
    private final Logger logger;
    private YamlConfiguration config;

    public SlotMachineRepository(File dataFolder, Logger logger) {
        this.file = new File(dataFolder, "machines.yml");
        this.logger = logger;
        loadConfig();
    }

    private void loadConfig() {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Could not create machines.yml", e);
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public Map<UUID, SlotMachine> loadAll() {
        Map<UUID, SlotMachine> machines = new HashMap<>();
        ConfigurationSection section = config.getConfigurationSection("machines");
        if (section == null) {
            return machines;
        }

        for (String key : section.getKeys(false)) {
            try {
                UUID id = UUID.fromString(key);
                ConfigurationSection machineSec = section.getConfigurationSection(key);
                if (machineSec == null) continue;

                Location lodestone = deserializeLocation(machineSec.getConfigurationSection("lodestone"));
                Location lever = deserializeLocation(machineSec.getConfigurationSection("lever"));
                String facingStr = machineSec.getString("facing", "NORTH");
                BlockFace facing = BlockFace.valueOf(facingStr);

                List<String> displayUuidStrings = machineSec.getStringList("displays");
                UUID[] displays = new UUID[3];
                for (int i = 0; i < Math.min(3, displayUuidStrings.size()); i++) {
                    try {
                        displays[i] = UUID.fromString(displayUuidStrings.get(i));
                    } catch (IllegalArgumentException ignored) {}
                }

                if (lodestone != null && lever != null) {
                    SlotMachine machine = new SlotMachine(id, lodestone, lever, facing, displays);
                    machines.put(id, machine);
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to load slot machine with key: " + key, e);
            }
        }
        return machines;
    }

    public void save(SlotMachine machine) {
        String path = "machines." + machine.getId().toString();
        config.set(path + ".lodestone", serializeLocation(machine.getLodestoneLocation()));
        config.set(path + ".lever", serializeLocation(machine.getLeverLocation()));
        config.set(path + ".facing", machine.getFacing().name());

        List<String> displayStrings = new ArrayList<>();
        for (UUID u : machine.getDisplayUuids()) {
            if (u != null) {
                displayStrings.add(u.toString());
            }
        }
        config.set(path + ".displays", displayStrings);
        saveFile();
    }

    public void delete(UUID machineId) {
        config.set("machines." + machineId.toString(), null);
        saveFile();
    }

    public void saveAll(Collection<SlotMachine> machines) {
        config.set("machines", null);
        for (SlotMachine machine : machines) {
            save(machine);
        }
    }

    private void saveFile() {
        try {
            config.save(file);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to save machines.yml", e);
        }
    }

    private Map<String, Object> serializeLocation(Location loc) {
        Map<String, Object> map = new HashMap<>();
        if (loc.getWorld() != null) {
            map.put("world", loc.getWorld().getName());
        }
        map.put("x", loc.getBlockX());
        map.put("y", loc.getBlockY());
        map.put("z", loc.getBlockZ());
        return map;
    }

    private Location deserializeLocation(ConfigurationSection section) {
        if (section == null) return null;
        String worldName = section.getString("world");
        if (worldName == null) return null;
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;
        int x = section.getInt("x");
        int y = section.getInt("y");
        int z = section.getInt("z");
        return new Location(world, x, y, z);
    }
}
