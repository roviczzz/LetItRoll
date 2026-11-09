package me.roviczzz.letItRoll.model;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public class SlotMachine {
    private final UUID id;
    private final Location lodestoneLocation;
    private final Location leverLocation;
    private final Location shelfLocation;
    private final Location canopyLocation;
    private final BlockFace facing;
    private final UUID[] displayUuids;
    private MachineState state;

    public SlotMachine(UUID id, Location lodestoneLocation, Location leverLocation, BlockFace facing, UUID[] displayUuids) {
        this(id, lodestoneLocation, leverLocation, facing, displayUuids, null, null);
    }

    public SlotMachine(UUID id, Location lodestoneLocation, Location leverLocation, BlockFace facing, UUID[] displayUuids,
                       Location shelfLocation, Location canopyLocation) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.lodestoneLocation = Objects.requireNonNull(lodestoneLocation, "lodestoneLocation must not be null");
        this.leverLocation = Objects.requireNonNull(leverLocation, "leverLocation must not be null");
        this.facing = Objects.requireNonNull(facing, "facing must not be null");
        this.displayUuids = displayUuids != null ? Arrays.copyOf(displayUuids, 3) : new UUID[3];
        this.shelfLocation = shelfLocation != null ? shelfLocation.clone() : null;
        this.canopyLocation = canopyLocation != null ? canopyLocation.clone() : null;
        this.state = MachineState.IDLE;
    }

    public UUID getId() {
        return id;
    }

    public Location getLodestoneLocation() {
        return lodestoneLocation.clone();
    }

    public Location getLeverLocation() {
        return leverLocation.clone();
    }

    public Location getShelfLocation() {
        return shelfLocation != null ? shelfLocation.clone() : null;
    }

    public Location getCanopyLocation() {
        return canopyLocation != null ? canopyLocation.clone() : null;
    }

    public boolean hasShelf() {
        return shelfLocation != null;
    }

    public boolean hasCanopy() {
        return canopyLocation != null;
    }

    public BlockFace getFacing() {
        return facing;
    }

    public UUID[] getDisplayUuids() {
        return Arrays.copyOf(displayUuids, displayUuids.length);
    }

    public void setDisplayUuid(int index, UUID uuid) {
        if (index >= 0 && index < 3) {
            this.displayUuids[index] = uuid;
        }
    }

    public MachineState getState() {
        return state;
    }

    public void setState(MachineState state) {
        this.state = state;
    }

    public boolean isIdle() {
        return state == MachineState.IDLE;
    }

    public boolean isSpinning() {
        return state == MachineState.SPINNING;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SlotMachine that = (SlotMachine) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
