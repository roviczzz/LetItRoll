package me.roviczzz.letItRoll.listener;

import me.roviczzz.letItRoll.display.DisplayManager;
import me.roviczzz.letItRoll.manager.SlotMachineManager;
import me.roviczzz.letItRoll.model.SlotMachine;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.EntitiesLoadEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DisplayLifecycleListener implements Listener {
    private final SlotMachineManager machineManager;
    private final DisplayManager displayManager;

    public DisplayLifecycleListener(SlotMachineManager machineManager, DisplayManager displayManager) {
        this.machineManager = machineManager;
        this.displayManager = displayManager;
    }

    @EventHandler
    public void onEntitiesLoad(EntitiesLoadEvent event) {
        Map<UUID, List<Entity>> displaysByMachine = new HashMap<>();

        for (Entity entity : event.getEntities()) {
            if (!(entity instanceof ItemDisplay)) continue;
            if (!displayManager.hasMachineIdentity(entity)) continue;

            UUID machineId = displayManager.getMachineId(entity);
            if (machineId == null) {
                entity.remove();
                continue;
            }

            SlotMachine machine = machineManager.getById(machineId);
            if (machine == null) {
                entity.remove();
                continue;
            }

            displaysByMachine.computeIfAbsent(machineId, id -> new ArrayList<>()).add(entity);
        }

        for (Map.Entry<UUID, List<Entity>> entry : displaysByMachine.entrySet()) {
            SlotMachine machine = machineManager.getById(entry.getKey());
            if (machine != null) {
                machineManager.reconcileMachine(machine, entry.getValue());
            }
        }
    }
}
