package me.roviczzz.letItRoll.manager;

import me.roviczzz.letItRoll.display.DisplayManager;
import me.roviczzz.letItRoll.model.SlotIcon;
import me.roviczzz.letItRoll.model.SlotMachine;
import me.roviczzz.letItRoll.storage.SlotMachineRepository;
import org.bukkit.Location;
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

    public SlotMachineManager(SlotMachineRepository repository, DisplayManager displayManager, Logger logger) {
        this.repository = repository;
        this.displayManager = displayManager;
        this.logger = logger;
    }

    public void loadMachines() {
        machinesById.clear();
        machinesByLodestone.clear();
        machinesByLever.clear();

        Map<UUID, SlotMachine> loaded = repository.loadAll();
        for (SlotMachine machine : loaded.values()) {
            registerInMemory(machine);
        }
        logger.info("Loaded " + machinesById.size() + " slot machines.");
    }

    public SlotMachine registerMachine(Location lodestone, Location lever, BlockFace facing) {
        UUID id = UUID.randomUUID();
        SlotMachine machine = new SlotMachine(id, lodestone.getBlock().getLocation(), lever.getBlock().getLocation(), facing, new UUID[3]);

        displayManager.spawnDisplays(machine, SlotIcon.AMETHYST_SHARD);
        repository.save(machine);
        registerInMemory(machine);
        return machine;
    }

    private void registerInMemory(SlotMachine machine) {
        machinesById.put(machine.getId(), machine);
        machinesByLodestone.put(machine.getLodestoneLocation().getBlock().getLocation(), machine);
        machinesByLever.put(machine.getLeverLocation().getBlock().getLocation(), machine);
    }

    public SlotMachine getByLodestone(Location location) {
        if (location == null) return null;
        return machinesByLodestone.get(location.getBlock().getLocation());
    }

    public SlotMachine getByLever(Location location) {
        if (location == null) return null;
        return machinesByLever.get(location.getBlock().getLocation());
    }

    public Collection<SlotMachine> getAllMachines() {
        return Collections.unmodifiableCollection(machinesById.values());
    }

    public void removeMachine(SlotMachine machine) {
        if (machine == null) return;
        displayManager.removeDisplays(machine);
        repository.delete(machine.getId());
        machinesById.remove(machine.getId());
        machinesByLodestone.remove(machine.getLodestoneLocation().getBlock().getLocation());
        machinesByLever.remove(machine.getLeverLocation().getBlock().getLocation());
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
