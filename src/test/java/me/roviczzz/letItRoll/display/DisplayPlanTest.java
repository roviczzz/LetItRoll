package me.roviczzz.letItRoll.display;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DisplayPlanTest {

    private static UUID uuid(int value) {
        return new UUID(0L, value);
    }

    @Test
    void alreadyCorrectMachineProducesEmptyPlan() {
        UUID[] recorded = {uuid(1), uuid(2), uuid(3)};
        List<DisplayDescriptor> found = List.of(
                new DisplayDescriptor(uuid(1), 0),
                new DisplayDescriptor(uuid(2), 1),
                new DisplayDescriptor(uuid(3), 2)
        );

        DisplayPlan plan = DisplayPlanner.plan(recorded, found);

        assertTrue(plan.isEmpty());
        assertFalse(plan.uuidSetChanged());
        assertTrue(plan.toDelete().isEmpty());
        assertTrue(plan.toSpawn().isEmpty());
        assertEquals(uuid(1), plan.survivor(0));
        assertEquals(uuid(2), plan.survivor(1));
        assertEquals(uuid(3), plan.survivor(2));
    }

    @Test
    void duplicateSlotKeepsRecordedUuidAndDeletesExtras() {
        UUID[] recorded = {uuid(1), uuid(2), uuid(3)};
        List<DisplayDescriptor> found = List.of(
                new DisplayDescriptor(uuid(1), 0),
                new DisplayDescriptor(uuid(10), 0),
                new DisplayDescriptor(uuid(11), 0),
                new DisplayDescriptor(uuid(2), 1),
                new DisplayDescriptor(uuid(3), 2)
        );

        DisplayPlan plan = DisplayPlanner.plan(recorded, found);

        assertEquals(uuid(1), plan.survivor(0));
        assertEquals(List.of(uuid(10), uuid(11)), plan.toDelete());
        assertTrue(plan.toSpawn().isEmpty());
        assertFalse(plan.uuidSetChanged());
    }

    @Test
    void duplicateSlotWithoutRecordedUuidKeepsOneAndDeletesTheRest() {
        UUID[] recorded = {null, uuid(2), uuid(3)};
        List<DisplayDescriptor> found = List.of(
                new DisplayDescriptor(uuid(10), 0),
                new DisplayDescriptor(uuid(11), 0),
                new DisplayDescriptor(uuid(2), 1),
                new DisplayDescriptor(uuid(3), 2)
        );

        DisplayPlan plan = DisplayPlanner.plan(recorded, found);

        assertEquals(uuid(10), plan.survivor(0));
        assertEquals(List.of(uuid(11)), plan.toDelete());
        assertTrue(plan.toSpawn().isEmpty());
        assertTrue(plan.uuidSetChanged());
    }

    @Test
    void missingSlotIsTheOnlySlotMarkedForSpawn() {
        UUID[] recorded = {uuid(1), null, uuid(3)};
        List<DisplayDescriptor> found = List.of(
                new DisplayDescriptor(uuid(1), 0),
                new DisplayDescriptor(uuid(3), 2)
        );

        DisplayPlan plan = DisplayPlanner.plan(recorded, found);

        assertEquals(List.of(1), plan.toSpawn());
        assertTrue(plan.toDelete().isEmpty());
        assertEquals(uuid(1), plan.survivor(0));
        assertNull(plan.survivor(1));
        assertEquals(uuid(3), plan.survivor(2));
        assertTrue(plan.uuidSetChanged());
    }

    @Test
    void descriptorsOutsideSlotsZeroToTwoAreDeletable() {
        UUID[] recorded = {uuid(1), uuid(2), uuid(3)};
        List<DisplayDescriptor> found = List.of(
                new DisplayDescriptor(uuid(1), 0),
                new DisplayDescriptor(uuid(2), 1),
                new DisplayDescriptor(uuid(3), 2),
                new DisplayDescriptor(uuid(99), 3),
                new DisplayDescriptor(uuid(100), -1)
        );

        DisplayPlan plan = DisplayPlanner.plan(recorded, found);

        assertEquals(List.of(uuid(99), uuid(100)), plan.toDelete());
        assertTrue(plan.toSpawn().isEmpty());
        assertFalse(plan.uuidSetChanged());
    }

    @Test
    void staleRecordedUuidAdoptsFoundDisplayWithoutSpawning() {
        UUID[] recorded = {uuid(1), uuid(2), uuid(3)};
        List<DisplayDescriptor> found = List.of(
                new DisplayDescriptor(uuid(10), 0),
                new DisplayDescriptor(uuid(2), 1),
                new DisplayDescriptor(uuid(3), 2)
        );

        DisplayPlan plan = DisplayPlanner.plan(recorded, found);

        assertEquals(uuid(10), plan.survivor(0));
        assertTrue(plan.toDelete().isEmpty());
        assertTrue(plan.toSpawn().isEmpty());
        assertTrue(plan.uuidSetChanged());
    }

    @Test
    void multipleDuplicatesAcrossSlotsCollapseToOneEach() {
        UUID[] recorded = {uuid(1), uuid(2), uuid(3)};
        List<DisplayDescriptor> found = List.of(
                new DisplayDescriptor(uuid(1), 0),
                new DisplayDescriptor(uuid(4), 0),
                new DisplayDescriptor(uuid(2), 1),
                new DisplayDescriptor(uuid(5), 1),
                new DisplayDescriptor(uuid(6), 1),
                new DisplayDescriptor(uuid(3), 2),
                new DisplayDescriptor(uuid(7), 2)
        );

        DisplayPlan plan = DisplayPlanner.plan(recorded, found);

        assertEquals(uuid(1), plan.survivor(0));
        assertEquals(uuid(2), plan.survivor(1));
        assertEquals(uuid(3), plan.survivor(2));
        assertEquals(List.of(uuid(4), uuid(5), uuid(6), uuid(7)), plan.toDelete());
        assertTrue(plan.toSpawn().isEmpty());
    }
}
