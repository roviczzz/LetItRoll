package me.roviczzz.letItRoll.listener;

import me.roviczzz.letItRoll.manager.SlotMachineManager;
import me.roviczzz.letItRoll.model.SlotMachine;
import me.roviczzz.letItRoll.util.WandUtil;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.FaceAttachable;
import org.bukkit.block.data.type.Switch;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.Plugin;

public class WandListener implements Listener {
    private final Plugin plugin;
    private final SlotMachineManager machineManager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public WandListener(Plugin plugin, SlotMachineManager machineManager) {
        this.plugin = plugin;
        this.machineManager = machineManager;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onWandUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        if (!WandUtil.isWand(plugin, player.getInventory().getItemInMainHand())) return;

        if (!player.hasPermission("letitroll.admin")) {
            player.sendMessage(miniMessage.deserialize("<red>You do not have permission to use this wand!</red>"));
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || clickedBlock.getType() != Material.LODESTONE) {
            player.sendMessage(miniMessage.deserialize("<yellow>Click a </yellow><gold>Lodestone</gold><yellow> block to create a slot machine.</yellow>"));
            return;
        }

        event.setCancelled(true);

        if (machineManager.getByLodestone(clickedBlock.getLocation()) != null) {
            player.sendMessage(miniMessage.deserialize("<red>A slot machine is already registered at this Lodestone!</red>"));
            player.playSound(clickedBlock.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.5f);
            return;
        }

        // The machine's front face points toward the player creating it
        BlockFace playerFacing = player.getFacing();
        if (playerFacing == BlockFace.UP || playerFacing == BlockFace.DOWN) {
            playerFacing = BlockFace.SOUTH;
        }
        BlockFace machineFacing = playerFacing.getOppositeFace();

        WandUtil.WandType wandType = WandUtil.getWandType(plugin, player.getInventory().getItemInMainHand());

        // Look for existing attached lever or create one on the player's right side
        Block leverBlock = findOrCreateAttachedLever(clickedBlock, playerFacing, machineFacing);
        if (leverBlock == null) {
            player.sendMessage(miniMessage.deserialize("<red>Cannot place lever! Clear the space on the side of the Lodestone.</red>"));
            player.playSound(clickedBlock.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.5f);
            return;
        }

        Location shelfLoc = null;
        Location canopyLoc = null;

        if (wandType == WandUtil.WandType.CABINET) {
            Block shelfBlock = clickedBlock.getRelative(BlockFace.UP, 1);
            Block canopyBlock = clickedBlock.getRelative(BlockFace.UP, 2);

            // Validate clearance above the Lodestone
            if (!isClearOrValidShelf(shelfBlock) || !isClearOrValidCanopy(canopyBlock)) {
                player.sendMessage(miniMessage.deserialize("<red>Cannot create slot machine! Space at Y+1 and Y+2 above Lodestone must be clear.</red>"));
                player.playSound(clickedBlock.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.5f);
                return;
            }

            // Construct shelf at Y+1 if not already present
            if (shelfBlock.getType() != Material.SPRUCE_SHELF) {
                shelfBlock.setType(Material.SPRUCE_SHELF);
                if (shelfBlock.getBlockData() instanceof Directional dir) {
                    dir.setFacing(machineFacing);
                    shelfBlock.setBlockData(dir);
                }
            }

            // Construct canopy at Y+2 if not already present
            if (findFlatTrapdoor(canopyBlock) == null) {
                canopyBlock.setType(Material.SPRUCE_TRAPDOOR);
                if (canopyBlock.getBlockData() instanceof TrapDoor trapDoor) {
                    trapDoor.setHalf(TrapDoor.Half.BOTTOM);
                    trapDoor.setOpen(false);
                    trapDoor.setFacing(machineFacing);
                    canopyBlock.setBlockData(trapDoor);
                }
            }

            shelfLoc = shelfBlock.getLocation();
            canopyLoc = canopyBlock.getLocation();
        }

        SlotMachine machine = machineManager.registerMachine(
                clickedBlock.getLocation(),
                leverBlock.getLocation(),
                machineFacing,
                shelfLoc,
                canopyLoc
        );

        player.sendMessage(miniMessage.deserialize("<green><b>Slot machine created!</b> Facing: <gold>" +
                machineFacing.name() + "</gold></green>"));
        player.playSound(clickedBlock.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.2f);
        clickedBlock.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, clickedBlock.getLocation().add(0.5, 1.2, 0.5), 25, 0.4, 0.4, 0.4, 0.1);
    }

    private boolean isClearOrValidShelf(Block block) {
        if (block == null) return false;
        return isAir(block) || block.getType() == Material.SPRUCE_SHELF;
    }

    private boolean isClearOrValidCanopy(Block block) {
        if (block == null) return false;
        return isAir(block) || findFlatTrapdoor(block) != null;
    }

    private boolean isAir(Block block) {
        if (block == null) return false;
        Material type = block.getType();
        return type == Material.AIR || type == Material.CAVE_AIR || type == Material.VOID_AIR;
    }

    private Block findFlatTrapdoor(Block block) {
        if (block != null && block.getType() == Material.SPRUCE_TRAPDOOR) {
            if (block.getBlockData() instanceof TrapDoor trapDoor) {
                if (trapDoor.getHalf() == TrapDoor.Half.BOTTOM && !trapDoor.isOpen()) {
                    return block;
                }
            }
        }
        return null;
    }

    private Block findAttachedLever(Block lodestone) {
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
        for (BlockFace face : faces) {
            Block relative = lodestone.getRelative(face);
            if (relative.getType() == Material.LEVER) {
                if (relative.getBlockData() instanceof Switch leverSwitch) {
                    if (leverSwitch.getAttachedFace() == FaceAttachable.AttachedFace.WALL) {
                        if (leverSwitch.getFacing() == face) {
                            return relative;
                        }
                    }
                }
            }
        }
        return null;
    }

    private BlockFace getRightFace(BlockFace facing) {
        return switch (facing) {
            case NORTH -> BlockFace.EAST;
            case SOUTH -> BlockFace.WEST;
            case EAST -> BlockFace.SOUTH;
            case WEST -> BlockFace.NORTH;
            default -> BlockFace.EAST;
        };
    }

    private Block findOrCreateAttachedLever(Block lodestone, BlockFace playerFacing, BlockFace machineFacing) {
        Block existing = findAttachedLever(lodestone);
        if (existing != null) {
            return existing;
        }

        // Try placing on player's right side, player's left side, or machine's rear
        BlockFace rightFace = getRightFace(playerFacing);
        BlockFace[] candidateFaces = {
                rightFace,
                rightFace.getOppositeFace(),
                playerFacing // machine front is opposite playerFacing, so machine rear is playerFacing
        };

        for (BlockFace face : candidateFaces) {
            Block relative = lodestone.getRelative(face);
            if (isAir(relative)) {
                relative.setType(Material.LEVER);
                if (relative.getBlockData() instanceof Switch leverSwitch) {
                    leverSwitch.setAttachedFace(FaceAttachable.AttachedFace.WALL);
                    leverSwitch.setFacing(face);
                    relative.setBlockData(leverSwitch);
                    return relative;
                }
            }
        }
        return null;
    }
}
