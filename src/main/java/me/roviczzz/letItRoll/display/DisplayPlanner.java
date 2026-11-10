package me.roviczzz.letItRoll.display;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class DisplayPlanner {
    private DisplayPlanner() {
    }

    public static DisplayPlan plan(UUID[] recorded, List<DisplayDescriptor> found) {
        UUID[] recordedSlots = new UUID[DisplayPlan.SLOT_COUNT];
        if (recorded != null) {
            System.arraycopy(recorded, 0, recordedSlots, 0, Math.min(recorded.length, DisplayPlan.SLOT_COUNT));
        }

        Map<Integer, List<DisplayDescriptor>> bySlot = new HashMap<>();
        LinkedHashSet<UUID> toDelete = new LinkedHashSet<>();

        if (found != null) {
            for (DisplayDescriptor descriptor : found) {
                if (descriptor == null || descriptor.uuid() == null) continue;
                if (descriptor.slotIndex() < 0 || descriptor.slotIndex() >= DisplayPlan.SLOT_COUNT) {
                    toDelete.add(descriptor.uuid());
                    continue;
                }
                bySlot.computeIfAbsent(descriptor.slotIndex(), slot -> new ArrayList<>()).add(descriptor);
            }
        }

        UUID[] survivors = new UUID[DisplayPlan.SLOT_COUNT];
        List<Integer> toSpawn = new ArrayList<>();

        for (int slot = 0; slot < DisplayPlan.SLOT_COUNT; slot++) {
            List<DisplayDescriptor> members = bySlot.get(slot);
            if (members == null || members.isEmpty()) {
                toSpawn.add(slot);
                continue;
            }

            DisplayDescriptor survivor = chooseSurvivor(members, recordedSlots[slot]);
            survivors[slot] = survivor.uuid();
            for (DisplayDescriptor member : members) {
                if (!member.uuid().equals(survivor.uuid())) {
                    toDelete.add(member.uuid());
                }
            }
        }

        boolean uuidSetChanged = !Arrays.equals(recordedSlots, survivors) || !toSpawn.isEmpty();
        return new DisplayPlan(survivors, toDelete, toSpawn, uuidSetChanged);
    }

    private static DisplayDescriptor chooseSurvivor(List<DisplayDescriptor> members, UUID preferred) {
        if (preferred != null) {
            for (DisplayDescriptor member : members) {
                if (preferred.equals(member.uuid())) {
                    return member;
                }
            }
        }
        return members.get(0);
    }
}
