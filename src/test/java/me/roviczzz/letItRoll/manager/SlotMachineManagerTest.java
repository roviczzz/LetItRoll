package me.roviczzz.letItRoll.manager;

import me.roviczzz.letItRoll.model.SlotMachine;
import me.roviczzz.letItRoll.storage.SlotMachineRepository;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class SlotMachineManagerTest {

    private World createMockWorld(String name) {
        return (World) Proxy.newProxyInstance(
                World.class.getClassLoader(),
                new Class<?>[]{World.class},
                (proxy, method, args) -> {
                    if ("getName".equals(method.getName())) return name;
                    if ("equals".equals(method.getName())) return proxy == args[0];
                    if ("hashCode".equals(method.getName())) return name.hashCode();
                    return null;
                }
        );
    }

    @Test
    void testCabinetBlockIndexingAndRemoval(@TempDir Path tempDir) {
        File dataFolder = tempDir.toFile();
        Logger logger = Logger.getLogger("TestLogger");
        World mockWorld = createMockWorld("world");

        SlotMachineRepository repo = new SlotMachineRepository(dataFolder, logger, name -> mockWorld);
        SlotMachineManager manager = new SlotMachineManager(repo, null, logger);

        UUID id = UUID.randomUUID();
        Location lodestone = new Location(mockWorld, 10, 64, 20);
        Location lever = new Location(mockWorld, 11, 64, 20);
        Location shelf = new Location(mockWorld, 10, 65, 20);
        Location canopy = new Location(mockWorld, 10, 66, 20);

        SlotMachine machine = new SlotMachine(id, lodestone, lever, BlockFace.NORTH, new UUID[3], shelf, canopy);
        manager.registerInMemory(machine);

        assertEquals(machine, manager.getByLodestone(lodestone));
        assertEquals(machine, manager.getByLever(lever));
        assertEquals(machine, manager.getByCabinetBlock(shelf));
        assertEquals(machine, manager.getByCabinetBlock(canopy));
        assertEquals(machine, manager.getByAnyBlock(shelf));
        assertEquals(machine, manager.getByAnyBlock(canopy));
        assertEquals(machine, manager.getByAnyBlock(lodestone));
        assertEquals(machine, manager.getByAnyBlock(lever));

        // Test unregistration cleanup
        manager.removeMachine(machine);
        assertNull(manager.getByLodestone(lodestone));
        assertNull(manager.getByLever(lever));
        assertNull(manager.getByCabinetBlock(shelf));
        assertNull(manager.getByCabinetBlock(canopy));
        assertNull(manager.getByAnyBlock(shelf));
    }

    @Test
    void testLoadMachinesReloadsFromRepository(@TempDir Path tempDir) {
        File dataFolder = tempDir.toFile();
        Logger logger = Logger.getLogger("TestLogger");
        World mockWorld = createMockWorld("world");

        SlotMachineRepository repo = new SlotMachineRepository(dataFolder, logger, name -> mockWorld);
        SlotMachineManager manager = new SlotMachineManager(repo, null, logger);

        UUID id = UUID.randomUUID();
        Location lodestone = new Location(mockWorld, 10, 64, 20);
        Location lever = new Location(mockWorld, 11, 64, 20);
        Location shelf = new Location(mockWorld, 10, 65, 20);
        Location canopy = new Location(mockWorld, 10, 66, 20);

        SlotMachine machine = new SlotMachine(id, lodestone, lever, BlockFace.NORTH, new UUID[3], shelf, canopy);
        repo.save(machine);

        assertEquals(0, manager.getAllMachines().size());
        manager.loadMachines();
        assertEquals(1, manager.getAllMachines().size());
        assertNotNull(manager.getByLodestone(lodestone));
        assertNotNull(manager.getByCabinetBlock(shelf));
        assertNotNull(manager.getByCabinetBlock(canopy));

        // Reload again
        manager.loadMachines();
        assertEquals(1, manager.getAllMachines().size());
    }
}
