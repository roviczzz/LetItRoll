package me.roviczzz.letItRoll;

import me.roviczzz.letItRoll.command.LetItRollCommand;
import me.roviczzz.letItRoll.display.DisplayManager;
import me.roviczzz.letItRoll.listener.LeverInteractListener;
import me.roviczzz.letItRoll.listener.MachineProtectionListener;
import me.roviczzz.letItRoll.listener.WandListener;
import me.roviczzz.letItRoll.manager.SlotMachineManager;
import me.roviczzz.letItRoll.storage.SlotMachineRepository;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class LetItRoll extends JavaPlugin {
    private SlotMachineRepository repository;
    private DisplayManager displayManager;
    private SlotMachineManager machineManager;

    @Override
    public void onEnable() {
        // Initialize Storage & Managers
        this.repository = new SlotMachineRepository(getDataFolder(), getLogger());
        this.displayManager = new DisplayManager(this);
        this.machineManager = new SlotMachineManager(repository, displayManager, getLogger());
        this.machineManager.loadMachines();

        // Register Listeners
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new LeverInteractListener(this, machineManager), this);
        pm.registerEvents(new MachineProtectionListener(machineManager), this);
        pm.registerEvents(new WandListener(this, machineManager), this);

        // Register Command
        PluginCommand cmd = getCommand("letitroll");
        if (cmd != null) {
            LetItRollCommand commandExecutor = new LetItRollCommand(this, machineManager);
            cmd.setExecutor(commandExecutor);
            cmd.setTabCompleter(commandExecutor);
        }

        getLogger().info("LetItRoll slot machine plugin enabled successfully!");
    }

    @Override
    public void onDisable() {
        if (machineManager != null) {
            machineManager.shutdown();
        }
        getLogger().info("LetItRoll slot machine plugin disabled.");
    }

    public SlotMachineManager getMachineManager() {
        return machineManager;
    }

    public DisplayManager getDisplayManager() {
        return displayManager;
    }
}
