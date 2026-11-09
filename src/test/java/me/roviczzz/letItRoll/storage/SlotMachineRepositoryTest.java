package me.roviczzz.letItRoll.storage;

import me.roviczzz.letItRoll.model.SlotMachine;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class SlotMachineRepositoryTest {

    private World createMockWorld(String name) {
        return (World) Proxy.newProxyInstance(
                World.class.getClassLoader(),
                new Class<?>[]{World.class},
                (proxy, method, args) -> {
                    if ("getName".equals(method.getName())) {
                        return name;
                    }
                    if ("equals".equals(method.getName())) {
                        return proxy == args[0];
                    }
                    if ("hashCode".equals(method.getName())) {
                        return name.hashCode();
                    }
                    return null;
                }
        );
    }

    @Test
    void testSaveAndLoadWithCabinet(@TempDir Path tempDir) {
        File dataFolder = tempDir.toFile();
        Logger logger = Logger.getLogger("TestLogger");
        World mockWorld = createMockWorld("world");

        SlotMachineRepository repo = new SlotMachineRepository(dataFolder, logger, name -> "world".equals(name) ? mockWorld : null);

        UUID id = UUID.randomUUID();
        Location lodestone = new Location(mockWorld, 10, 64, 20);
        Location lever = new Location(mockWorld, 11, 64, 20);
        Location shelf = new Location(mockWorld, 10, 65, 20);
        Location canopy = new Location(mockWorld, 10, 66, 20);
        UUID[] displays = new UUID[]{UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()};

        SlotMachine machine = new SlotMachine(id, lodestone, lever, BlockFace.NORTH, displays, shelf, canopy);
        repo.save(machine);

        Map<UUID, SlotMachine> loaded = repo.loadAll();
        assertEquals(1, loaded.size());
        SlotMachine loadedMachine = loaded.get(id);
        assertNotNull(loadedMachine);
        assertEquals(BlockFace.NORTH, loadedMachine.getFacing());
        assertTrue(loadedMachine.hasShelf());
        assertTrue(loadedMachine.hasCanopy());
        assertEquals(shelf, loadedMachine.getShelfLocation());
        assertEquals(canopy, loadedMachine.getCanopyLocation());
    }

    @Test
    void testBackwardCompatibilityWithoutCabinet(@TempDir Path tempDir) {
        File dataFolder = tempDir.toFile();
        Logger logger = Logger.getLogger("TestLogger");
        World mockWorld = createMockWorld("world");

        SlotMachineRepository repo = new SlotMachineRepository(dataFolder, logger, name -> "world".equals(name) ? mockWorld : null);

        UUID id = UUID.randomUUID();
        Location lodestone = new Location(mockWorld, 10, 64, 20);
        Location lever = new Location(mockWorld, 11, 64, 20);
        UUID[] displays = new UUID[]{UUID.randomUUID(), null, null};

        SlotMachine machine = new SlotMachine(id, lodestone, lever, BlockFace.WEST, displays);
        repo.save(machine);

        Map<UUID, SlotMachine> loaded = repo.loadAll();
        assertEquals(1, loaded.size());
        SlotMachine loadedMachine = loaded.get(id);
        assertNotNull(loadedMachine);
        assertFalse(loadedMachine.hasShelf());
        assertFalse(loadedMachine.hasCanopy());
        assertNull(loadedMachine.getShelfLocation());
        assertNull(loadedMachine.getCanopyLocation());
    }
}
