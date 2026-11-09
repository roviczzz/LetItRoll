package me.roviczzz.letItRoll.display;

import me.roviczzz.letItRoll.model.SlotIcon;
import me.roviczzz.letItRoll.model.SlotMachine;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.UUID;

public class DisplayManager {
    private final Plugin plugin;
    private final NamespacedKey machineKey;
    private final NamespacedKey slotKey;

    public DisplayManager(Plugin plugin) {
        this.plugin = plugin;
        this.machineKey = new NamespacedKey(plugin, "machine_id");
        this.slotKey = new NamespacedKey(plugin, "slot_index");
    }

    public UUID[] spawnDisplays(SlotMachine machine, SlotIcon defaultIcon) {
        Location[] slotLocations = calculateSlotLocations(machine.getLodestoneLocation(), machine.getFacing());
        UUID[] displayUuids = new UUID[3];

        for (int i = 0; i < 3; i++) {
            Location loc = slotLocations[i];
            int slotIndex = i;
            ItemDisplay display = loc.getWorld().spawn(loc, ItemDisplay.class, d -> {
                d.setItemStack(defaultIcon.createItem());
                d.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
                d.setTransformation(new Transformation(
                        new Vector3f(0, 0, 0),
                        new AxisAngle4f(),
                        new Vector3f(0.42f, 0.42f, 0.42f),
                        new AxisAngle4f()
                ));
                d.setPersistent(true);
                d.setInvulnerable(true);
                d.getPersistentDataContainer().set(machineKey, PersistentDataType.STRING, machine.getId().toString());
                d.getPersistentDataContainer().set(slotKey, PersistentDataType.INTEGER, slotIndex);
            });
            displayUuids[i] = display.getUniqueId();
            machine.setDisplayUuid(i, display.getUniqueId());
        }
        return displayUuids;
    }

    public void updateDisplay(World world, UUID displayUuid, SlotIcon icon) {
        if (world == null || displayUuid == null) return;
        Entity entity = Bukkit.getEntity(displayUuid);
        if (entity instanceof ItemDisplay display && display.isValid()) {
            display.setItemStack(icon.createItem());
        }
    }

    public void removeDisplays(SlotMachine machine) {
        if (machine == null || machine.getLodestoneLocation() == null) return;
        World world = machine.getLodestoneLocation().getWorld();
        if (world == null) return;

        for (UUID uuid : machine.getDisplayUuids()) {
            if (uuid != null) {
                try {
                    Entity entity = Bukkit.getEntity(uuid);
                    if (entity instanceof ItemDisplay display) {
                        display.remove();
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

    public static Location[] calculateSlotLocations(Location lodestoneLoc, BlockFace facing) {
        float yaw = getYawForFacing(facing);
        Vector forward = getForwardVector(facing);
        Vector backward = forward.clone().multiply(-1);
        Vector right = getRightVector(facing);

        // Center on top of lodestone, aligned within spruce shelf enclosure at Y+1.50
        Location shelfCenter = lodestoneLoc.clone()
                .add(0.5, 1.50, 0.5)
                .add(backward.clone().multiply(0.30));
        shelfCenter.setYaw(yaw);
        shelfCenter.setPitch(0);

        Location left = shelfCenter.clone().add(right.clone().multiply(-0.33));
        Location mid = shelfCenter.clone();
        Location rightLoc = shelfCenter.clone().add(right.clone().multiply(0.33));

        return new Location[]{left, mid, rightLoc};
    }

    public static float getYawForFacing(BlockFace facing) {
        return switch (facing) {
            case NORTH -> 180f;
            case EAST -> 270f;
            case SOUTH -> 0f;
            case WEST -> 90f;
            default -> 0f;
        };
    }

    public static Vector getForwardVector(BlockFace facing) {
        return switch (facing) {
            case NORTH -> new Vector(0, 0, -1);
            case SOUTH -> new Vector(0, 0, 1);
            case EAST -> new Vector(1, 0, 0);
            case WEST -> new Vector(-1, 0, 0);
            default -> new Vector(0, 0, -1);
        };
    }

    public static Vector getRightVector(BlockFace facing) {
        return switch (facing) {
            case NORTH -> new Vector(1, 0, 0);
            case SOUTH -> new Vector(-1, 0, 0);
            case EAST -> new Vector(0, 0, 1);
            case WEST -> new Vector(0, 0, -1);
            default -> new Vector(1, 0, 0);
        };
    }
}
