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
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        for (Block block : event.blockList()) {
            if (block.getType() == Material.LODESTONE) {
                SlotMachine machine = machineManager.getByLodestone(block.getLocation());
                if (machine != null) {
                    machineManager.removeMachine(machine);
                }
            }
        }
    }
}
