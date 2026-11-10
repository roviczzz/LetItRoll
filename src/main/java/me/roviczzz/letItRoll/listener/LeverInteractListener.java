package me.roviczzz.letItRoll.listener;

import me.roviczzz.letItRoll.game.SpinTask;
import me.roviczzz.letItRoll.manager.SlotMachineManager;
import me.roviczzz.letItRoll.model.MachineState;
import me.roviczzz.letItRoll.model.SlotMachine;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Switch;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class LeverInteractListener implements Listener {
    private final Plugin plugin;
    private final SlotMachineManager machineManager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public LeverInteractListener(Plugin plugin, SlotMachineManager machineManager) {
        this.plugin = plugin;
        this.machineManager = machineManager;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onLeverInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || clickedBlock.getType() != Material.LEVER) return;

        SlotMachine machine = machineManager.getByLever(clickedBlock.getLocation());
        if (machine == null) return;

        // Cancel vanilla lever interaction to take full control
        event.setCancelled(true);

        Player player = event.getPlayer();

        if (machine.isSpinning()) {
            player.sendActionBar(miniMessage.deserialize("<red>Machine is already spinning!</red>"));
            player.playSound(clickedBlock.getLocation(), Sound.BLOCK_NOTE_BLOCK_SNARE, 0.5f, 0.5f);
            return;
        }

        // Check if player has at least 1 diamond
        if (!player.getInventory().containsAtLeast(new ItemStack(Material.DIAMOND), 1)) {
            player.sendActionBar(miniMessage.deserialize("<red>You need 1 Diamond in your inventory to roll!</red>"));
            player.playSound(clickedBlock.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 0.6f);
            return;
        }

        machineManager.reconcileMachine(machine);

        // Deduct 1 Diamond
        player.getInventory().removeItem(new ItemStack(Material.DIAMOND, 1));
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.6f, 1.2f);
        player.sendActionBar(miniMessage.deserialize("<green>1 Diamond spent! Good luck!</green>"));

        // Animate lever down
        if (clickedBlock.getBlockData() instanceof Switch leverSwitch) {
            leverSwitch.setPowered(true);
            clickedBlock.setBlockData(leverSwitch);
            clickedBlock.getWorld().playSound(clickedBlock.getLocation(), Sound.BLOCK_LEVER_CLICK, 0.8f, 0.8f);
        }

        // Lock state and launch spin
        machine.setState(MachineState.SPINNING);
        new SpinTask(plugin, machine, machineManager.getDisplayManager(), player).runTaskTimer(plugin, 0L, 2L);
    }
}
