package me.roviczzz.letItRoll.listener;

import me.roviczzz.letItRoll.manager.SlotMachineManager;
import me.roviczzz.letItRoll.model.SlotMachine;
import me.roviczzz.letItRoll.util.WandUtil;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.FaceAttachable;
import org.bukkit.block.data.type.Switch;
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

        // Look for an attached lever around the lodestone
        Block leverBlock = findAttachedLever(clickedBlock);
        if (leverBlock == null) {
            player.sendMessage(miniMessage.deserialize("<red>No attached lever found! Place a lever on the side of the Lodestone first.</red>"));
            player.playSound(clickedBlock.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.5f);
            return;
        }

        // The machine's front face points toward the player creating it
        BlockFace machineFacing = player.getFacing().getOppositeFace();
        if (machineFacing == BlockFace.UP || machineFacing == BlockFace.DOWN) {
            machineFacing = BlockFace.NORTH;
        }

        SlotMachine machine = machineManager.registerMachine(clickedBlock.getLocation(), leverBlock.getLocation(), machineFacing);

        player.sendMessage(miniMessage.deserialize("<green><b>Slot machine created!</b> Facing: <gold>" + machineFacing.name() + "</gold></green>"));
        player.playSound(clickedBlock.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.2f);
        clickedBlock.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, clickedBlock.getLocation().add(0.5, 1.2, 0.5), 25, 0.4, 0.4, 0.4, 0.1);
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
}
