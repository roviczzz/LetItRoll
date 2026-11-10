package me.roviczzz.letItRoll.display;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public final class DisplayPlan {
    public static final int SLOT_COUNT = 3;

    private final UUID[] survivors;
    private final List<UUID> toDelete;
    private final List<Integer> toSpawn;
    private final boolean uuidSetChanged;

    public DisplayPlan(UUID[] survivors, Collection<UUID> toDelete, Collection<Integer> toSpawn, boolean uuidSetChanged) {
        UUID[] copy = new UUID[SLOT_COUNT];
        if (survivors != null) {
            System.arraycopy(survivors, 0, copy, 0, Math.min(survivors.length, SLOT_COUNT));
        }
        this.survivors = copy;
        this.toDelete = List.copyOf(toDelete);
        this.toSpawn = List.copyOf(toSpawn);
        this.uuidSetChanged = uuidSetChanged;
    }

    public UUID survivor(int slot) {
        if (slot < 0 || slot >= SLOT_COUNT) return null;
        return survivors[slot];
    }

    public UUID[] survivors() {
        return Arrays.copyOf(survivors, SLOT_COUNT);
    }

    public List<UUID> toDelete() {
        return toDelete;
    }

    public List<Integer> toSpawn() {
        return toSpawn;
    }

    public boolean uuidSetChanged() {
        return uuidSetChanged;
    }

    public boolean isEmpty() {
        return toDelete.isEmpty() && toSpawn.isEmpty();
    }
}
