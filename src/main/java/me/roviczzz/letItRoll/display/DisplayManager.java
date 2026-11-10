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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

    public boolean hasMachineIdentity(Entity entity) {
        return entity != null
                && entity.getPersistentDataContainer().has(machineKey, PersistentDataType.STRING);
    }

    public UUID getMachineId(Entity entity) {
        if (entity == null) return null;
        String raw = entity.getPersistentDataContainer().get(machineKey, PersistentDataType.STRING);
        if (raw == null) return null;
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public Integer getSlotIndex(Entity entity) {
        if (entity == null) return null;
        return entity.getPersistentDataContainer().get(slotKey, PersistentDataType.INTEGER);
    }

    public List<DisplayDescriptor> scanDisplayDescriptors(SlotMachine machine) {
        List<DisplayDescriptor> descriptors = new ArrayList<>();
        if (machine == null || machine.getLodestoneLocation() == null) return descriptors;

        Location lodestone = machine.getLodestoneLocation();
        World world = lodestone.getWorld();
        if (world == null) return descriptors;

        Location center = calculateSlotLocations(lodestone, machine.getFacing())[1];
        for (Entity entity : world.getNearbyEntities(center, 1.0, 1.0, 1.0)) {
            if (!(entity instanceof ItemDisplay)) continue;
            if (!machine.getId().equals(getMachineId(entity))) continue;
            Integer slotIndex = getSlotIndex(entity);
            if (slotIndex == null) continue;
            descriptors.add(new DisplayDescriptor(entity.getUniqueId(), slotIndex));
        }
        return descriptors;
    }

    public boolean reconcile(SlotMachine machine, SlotIcon defaultIcon) {
        if (machine == null || machine.getLodestoneLocation() == null) return false;
        if (!isChunkLoaded(machine)) return false;

        List<DisplayDescriptor> found = scanDisplayDescriptors(machine);
        DisplayPlan plan = DisplayPlanner.plan(machine.getDisplayUuids(), found);
        return applyPlan(machine, plan, defaultIcon);
    }

    public boolean reconcile(SlotMachine machine, Collection<Entity> entities, SlotIcon defaultIcon) {
        if (machine == null || machine.getLodestoneLocation() == null) return false;
        if (!isChunkLoaded(machine)) return false;

        World machineWorld = machine.getLodestoneLocation().getWorld();
        if (machineWorld == null) return false;

        List<DisplayDescriptor> found = new ArrayList<>();
        if (entities != null) {
            for (Entity entity : entities) {
                if (!(entity instanceof ItemDisplay)) continue;
                if (!machineWorld.equals(entity.getWorld())) continue;
                if (!machine.getId().equals(getMachineId(entity))) continue;
                Integer slotIndex = getSlotIndex(entity);
                if (slotIndex == null) continue;
                found.add(new DisplayDescriptor(entity.getUniqueId(), slotIndex));
            }
        }

        DisplayPlan plan = DisplayPlanner.plan(machine.getDisplayUuids(), found);
        return applyPlan(machine, plan, defaultIcon);
    }

    private boolean applyPlan(SlotMachine machine, DisplayPlan plan, SlotIcon defaultIcon) {
        World world = machine.getLodestoneLocation().getWorld();
        if (world == null) return false;

        for (UUID uuid : plan.toDelete()) {
            Entity entity = Bukkit.getEntity(uuid);
            if (entity instanceof ItemDisplay) {
                entity.remove();
            }
        }

        UUID[] updated = plan.survivors();
        if (!plan.toSpawn().isEmpty()) {
            Location[] slotLocations = calculateSlotLocations(machine.getLodestoneLocation(), machine.getFacing());
            for (int slot : plan.toSpawn()) {
                updated[slot] = spawnDisplay(machine, slot, slotLocations[slot], defaultIcon);
            }
        }

        for (int slot = 0; slot < DisplayPlan.SLOT_COUNT; slot++) {
            machine.setDisplayUuid(slot, updated[slot]);
        }
        return plan.uuidSetChanged();
    }

    private UUID spawnDisplay(SlotMachine machine, int slotIndex, Location location, SlotIcon icon) {
        ItemDisplay display = location.getWorld().spawn(location, ItemDisplay.class, d -> {
            d.setItemStack(icon.createItem());
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
        return display.getUniqueId();
    }

    private boolean isChunkLoaded(SlotMachine machine) {
        Location location = machine.getLodestoneLocation();
        World world = location.getWorld();
        if (world == null) return false;

        for (Location slot : calculateSlotLocations(location, machine.getFacing())) {
            if (!world.isChunkLoaded(slot.getBlockX() >> 4, slot.getBlockZ() >> 4)) {
                return false;
            }
        }
        return true;
    }

    public void updateDisplay(World world, UUID displayUuid, SlotIcon icon) {
        if (world == null || displayUuid == null) return;
        Entity entity = Bukkit.getEntity(displayUuid);
        if (entity instanceof ItemDisplay display && display.isValid()) {
            display.setItemStack(icon.createItem());
        }
    }

    public void removeDisplays(SlotMachine machine) {
        if (machine == null) return;
        purgeDisplays(machine.getId());
    }

    public void purgeDisplays(UUID machineId) {
        if (machineId == null) return;

        List<Entity> matches = new ArrayList<>();
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof ItemDisplay && machineId.equals(getMachineId(entity))) {
                    matches.add(entity);
                }
            }
        }
        for (Entity entity : matches) {
            entity.remove();
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
