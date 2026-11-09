package me.roviczzz.letItRoll.manager;

import me.roviczzz.letItRoll.display.DisplayManager;
import me.roviczzz.letItRoll.model.SlotIcon;
import me.roviczzz.letItRoll.model.SlotMachine;
import me.roviczzz.letItRoll.storage.SlotMachineRepository;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class SlotMachineManager {
    private final SlotMachineRepository repository;
    private final DisplayManager displayManager;
    private final Logger logger;

    private final Map<UUID, SlotMachine> machinesById = new ConcurrentHashMap<>();
    private final Map<Location, SlotMachine> machinesByLodestone = new ConcurrentHashMap<>();
    private final Map<Location, SlotMachine> machinesByLever = new ConcurrentHashMap<>();
    private final Map<Location, SlotMachine> machinesByCabinetBlock = new ConcurrentHashMap<>();

    public SlotMachineManager(SlotMachineRepository repository, DisplayManager displayManager, Logger logger) {
        this.repository = repository;
        this.displayManager = displayManager;
        this.logger = logger;
    }

    public void loadMachines() {
        if (displayManager != null) {
            for (SlotMachine machine : machinesById.values()) {
                displayManager.removeDisplays(machine);
            }
        }

        machinesById.clear();
        machinesByLodestone.clear();
        machinesByLever.clear();
        machinesByCabinetBlock.clear();

        Map<UUID, SlotMachine> loaded = repository.loadAll();
        for (SlotMachine machine : loaded.values()) {
            if (displayManager != null) {
                displayManager.removeDisplays(machine);
                displayManager.spawnDisplays(machine, SlotIcon.AMETHYST_SHARD);
                repository.save(machine);
            }
            registerInMemory(machine);
        }
        logger.info("Loaded " + machinesById.size() + " slot machines.");
    }

    private Location toBlockLocation(Location loc) {
        if (loc == null) return null;
        return new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public SlotMachine registerMachine(Location lodestone, Location lever, BlockFace facing) {
        return registerMachine(lodestone, lever, facing, null, null);
    }

    public SlotMachine registerMachine(Location lodestone, Location lever, BlockFace facing, Location shelf, Location canopy) {
        UUID id = UUID.randomUUID();
        Location shelfLoc = toBlockLocation(shelf);
        Location canopyLoc = toBlockLocation(canopy);
        SlotMachine machine = new SlotMachine(id, toBlockLocation(lodestone), toBlockLocation(lever), facing, new UUID[3], shelfLoc, canopyLoc);

        if (displayManager != null) {
            displayManager.spawnDisplays(machine, SlotIcon.AMETHYST_SHARD);
        }
        repository.save(machine);
        registerInMemory(machine);
        return machine;
    }

    public void registerInMemory(SlotMachine machine) {
        machinesById.put(machine.getId(), machine);
        machinesByLodestone.put(toBlockLocation(machine.getLodestoneLocation()), machine);
        machinesByLever.put(toBlockLocation(machine.getLeverLocation()), machine);
        if (machine.hasShelf()) {
            machinesByCabinetBlock.put(toBlockLocation(machine.getShelfLocation()), machine);
        }
        if (machine.hasCanopy()) {
            machinesByCabinetBlock.put(toBlockLocation(machine.getCanopyLocation()), machine);
        }
    }

    public SlotMachine getByLodestone(Location location) {
        if (location == null) return null;
        return machinesByLodestone.get(toBlockLocation(location));
    }

    public SlotMachine getByLever(Location location) {
        if (location == null) return null;
        return machinesByLever.get(toBlockLocation(location));
    }

    public SlotMachine getByCabinetBlock(Location location) {
        if (location == null) return null;
        return machinesByCabinetBlock.get(toBlockLocation(location));
    }

    public SlotMachine getByAnyBlock(Location location) {
        if (location == null) return null;
        Location blockLoc = toBlockLocation(location);
        SlotMachine m = machinesByLodestone.get(blockLoc);
        if (m != null) return m;
        m = machinesByLever.get(blockLoc);
        if (m != null) return m;
        return machinesByCabinetBlock.get(blockLoc);
    }

    public Collection<SlotMachine> getAllMachines() {
        return Collections.unmodifiableCollection(machinesById.values());
    }

    public void removeMachine(SlotMachine machine) {
        if (machine == null) return;
        if (displayManager != null) {
            displayManager.removeDisplays(machine);
        }
        clearCabinetBlocks(machine);
        repository.delete(machine.getId());
        machinesById.remove(machine.getId());
        machinesByLodestone.remove(toBlockLocation(machine.getLodestoneLocation()));
        machinesByLever.remove(toBlockLocation(machine.getLeverLocation()));
        if (machine.hasShelf()) {
            machinesByCabinetBlock.remove(toBlockLocation(machine.getShelfLocation()));
        }
        if (machine.hasCanopy()) {
            machinesByCabinetBlock.remove(toBlockLocation(machine.getCanopyLocation()));
        }
    }

    private void clearCabinetBlocks(SlotMachine machine) {
        clearBlock(machine.getShelfLocation());
        clearBlock(machine.getCanopyLocation());
        clearBlock(machine.getLeverLocation());
    }

    private void clearBlock(Location loc) {
        if (loc == null || loc.getWorld() == null) return;
        try {
            Block block = loc.getBlock();
            if (block != null && block.getType() != Material.AIR) {
                block.setType(Material.AIR);
            }
        } catch (Exception ignored) {
            // World may not support getBlock in mock test environments
        }
    }

    public void shutdown() {
        for (SlotMachine machine : machinesById.values()) {
            if (machine.isSpinning()) {
                machine.setState(me.roviczzz.letItRoll.model.MachineState.IDLE);
            }
        }
    }

    public DisplayManager getDisplayManager() {
        return displayManager;
    }
}
