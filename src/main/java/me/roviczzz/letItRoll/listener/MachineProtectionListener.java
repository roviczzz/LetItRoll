package me.roviczzz.letItRoll.listener;

import me.roviczzz.letItRoll.manager.SlotMachineManager;
import me.roviczzz.letItRoll.model.SlotMachine;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class MachineProtectionListener implements Listener {
    private final SlotMachineManager machineManager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public MachineProtectionListener(SlotMachineManager machineManager) {
        this.machineManager = machineManager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        if (block.getType() == Material.LODESTONE) {
            SlotMachine machine = machineManager.getByLodestone(block.getLocation());
            if (machine != null) {
                if (!event.getPlayer().hasPermission("letitroll.admin")) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(miniMessage.deserialize("<red>You don't have permission to break this slot machine!</red>"));
                    return;
                }
                machineManager.removeMachine(machine);
                event.getPlayer().sendMessage(miniMessage.deserialize("<yellow>Slot machine unregistered and displays removed.</yellow>"));
            }
        } else if (block.getType() == Material.LEVER) {
            SlotMachine machine = machineManager.getByLever(block.getLocation());
            if (machine != null) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(miniMessage.deserialize("<red>This lever belongs to an active slot machine. Break the Lodestone base to remove it.</red>"));
            }
        } else if (block.getType() == Material.SPRUCE_SHELF || block.getType() == Material.SPRUCE_TRAPDOOR) {
            SlotMachine machine = machineManager.getByCabinetBlock(block.getLocation());
            if (machine != null) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(miniMessage.deserialize("<red>This block is part of an active slot machine cabinet. Break the Lodestone base to remove it.</red>"));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        // Protect active slot machines (base, lever, and cabinet blocks) from explosion damage
        event.blockList().removeIf(block -> machineManager.getByAnyBlock(block.getLocation()) != null);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        // Protect active slot machines (base, lever, and cabinet blocks) from explosion damage
        event.blockList().removeIf(block -> machineManager.getByAnyBlock(block.getLocation()) != null);
    }
}
