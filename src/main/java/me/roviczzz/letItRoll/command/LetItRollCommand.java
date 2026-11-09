package me.roviczzz.letItRoll.command;

import me.roviczzz.letItRoll.manager.SlotMachineManager;
import me.roviczzz.letItRoll.model.SlotMachine;
import me.roviczzz.letItRoll.util.WandUtil;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class LetItRollCommand implements CommandExecutor, TabCompleter {
    private final Plugin plugin;
    private final SlotMachineManager machineManager;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public LetItRollCommand(Plugin plugin, SlotMachineManager machineManager) {
        this.plugin = plugin;
        this.machineManager = machineManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("letitroll.admin")) {
            sender.sendMessage(mm.deserialize("<red>You do not have permission to use this command.</red>"));
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "wand" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Only players can receive the wand.");
                    return true;
                }
                player.getInventory().addItem(WandUtil.createWand(plugin));
                player.sendMessage(mm.deserialize("<green>Received the <gold>Slot Machine Wand</gold>!</green>"));
            }
            case "list" -> {
                var machines = machineManager.getAllMachines();
                if (machines.isEmpty()) {
                    sender.sendMessage(mm.deserialize("<yellow>No slot machines currently registered.</yellow>"));
                    return true;
                }
                sender.sendMessage(mm.deserialize("<gold>--- Active Slot Machines (" + machines.size() + ") ---</gold>"));
                for (SlotMachine m : machines) {
                    Location loc = m.getLodestoneLocation();
                    sender.sendMessage(mm.deserialize("<gray>- <yellow>" + m.getId().toString().substring(0, 8) + "</yellow> at <white>" +
                            loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + " (" + m.getFacing() + ")</white></gray>"));
                }
            }
            case "remove" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Only players can target and remove machines.");
                    return true;
                }
                Block targetBlock = player.getTargetBlockExact(5);
                if (targetBlock == null) {
                    player.sendMessage(mm.deserialize("<red>Look at a Lodestone or Lever within 5 blocks to remove it.</red>"));
                    return true;
                }
                SlotMachine machine = machineManager.getByLodestone(targetBlock.getLocation());
                if (machine == null) {
                    machine = machineManager.getByLever(targetBlock.getLocation());
                }
                if (machine == null) {
                    player.sendMessage(mm.deserialize("<red>No slot machine found at that target block.</red>"));
                    return true;
                }
                machineManager.removeMachine(machine);
                player.sendMessage(mm.deserialize("<green>Slot machine successfully removed.</green>"));
            }
            case "reload" -> {
                machineManager.loadMachines();
                sender.sendMessage(mm.deserialize("<green>LetItRoll configuration and machines reloaded.</green>"));
            }
            default -> sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(mm.deserialize("<gold><b>LetItRoll Slot Machine Commands:</b></gold>"));
        sender.sendMessage(mm.deserialize("<yellow>/letitroll wand</yellow> <gray>- Get the machine creation wand</gray>"));
        sender.sendMessage(mm.deserialize("<yellow>/letitroll list</yellow> <gray>- List all registered machines</gray>"));
        sender.sendMessage(mm.deserialize("<yellow>/letitroll remove</yellow> <gray>- Remove the targeted machine</gray>"));
        sender.sendMessage(mm.deserialize("<yellow>/letitroll reload</yellow> <gray>- Reload machines from storage</gray>"));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("letitroll.admin")) return List.of();
        if (args.length == 1) {
            List<String> subs = List.of("wand", "list", "remove", "reload", "help");
            List<String> completions = new ArrayList<>();
            for (String s : subs) {
                if (s.startsWith(args[0].toLowerCase())) {
                    completions.add(s);
                }
            }
            return completions;
        }
        return List.of();
    }
}
