package me.roviczzz.letItRoll.display;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DisplayManagerTest {

    @Test
    void testCalculateSlotLocationsFlushOnShelf() {
        Location lodestone = new Location(null, 100, 60, 200);
        Location[] slots = DisplayManager.calculateSlotLocations(lodestone, BlockFace.NORTH);

        assertEquals(3, slots.length);

        // Y coordinate should be 60 + 1.50 = 61.50 for all slots within the spruce shelf enclosure
        assertEquals(61.50, slots[0].getY(), 0.0001);
        assertEquals(61.50, slots[1].getY(), 0.0001);
        assertEquals(61.50, slots[2].getY(), 0.0001);

        // Center slot should be pushed backward (for NORTH, backward is south = +Z)
        // lodestone Z + 0.5 + 0.30 = 200.80
        assertEquals(200.80, slots[1].getZ(), 0.0001);
        assertEquals(100.50, slots[1].getX(), 0.0001);

        // Left slot should be offset along -right vector (for NORTH, right is east = +X, so left is -X)
        assertEquals(100.50 - 0.33, slots[0].getX(), 0.0001);
        assertEquals(100.50 + 0.33, slots[2].getX(), 0.0001);
    }

    @Test
    void testCalculateSlotLocationsAllFacings() {
        Location lodestone = new Location(null, 100, 60, 200);

        for (BlockFace facing : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
            Location[] slots = DisplayManager.calculateSlotLocations(lodestone, facing);
            assertEquals(3, slots.length);
            for (Location slot : slots) {
                assertEquals(61.50, slot.getY(), 0.0001);
                assertEquals(DisplayManager.getYawForFacing(facing), slot.getYaw(), 0.0001);
            }
        }
    }
}
