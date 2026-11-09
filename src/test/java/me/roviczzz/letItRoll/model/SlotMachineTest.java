package me.roviczzz.letItRoll.model;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class SlotMachineTest {

    @Test
    void testSlotMachineCreationAndState() {
        UUID id = UUID.randomUUID();
        Location lodestone = new Location(null, 10, 64, 20);
        Location lever = new Location(null, 11, 64, 20);
        BlockFace facing = BlockFace.NORTH;

        SlotMachine machine = new SlotMachine(id, lodestone, lever, facing, new UUID[3]);

        assertEquals(id, machine.getId());
        assertEquals(BlockFace.NORTH, machine.getFacing());
        assertEquals(MachineState.IDLE, machine.getState());
        assertTrue(machine.isIdle());
        assertFalse(machine.isSpinning());

        machine.setState(MachineState.SPINNING);
        assertTrue(machine.isSpinning());
        assertFalse(machine.isIdle());
    }
}
