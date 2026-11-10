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
                WandUtil.WandType type = WandUtil.WandType.CABINET;
                if (args.length >= 2) {
                    String typeArg = args[1].toLowerCase();
                    if (typeArg.equals("cabinet")) {
                        type = WandUtil.WandType.CABINET;
                    } else if (typeArg.equals("base")) {
                        type = WandUtil.WandType.BASE;
                    } else {
                        player.sendMessage(mm.deserialize("<red>Invalid wand type. Use: /letitroll wand [cabinet|base]</red>"));
                        return true;
                    }
                }
                player.getInventory().addItem(WandUtil.createWand(plugin, type));
                String typeName = type == WandUtil.WandType.BASE ? "Base" : "Cabinet";
                player.sendMessage(mm.deserialize("<green>Received the <gold>Slot Machine Wand (" + typeName + ")</gold>!</green>"));
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
                plugin.reloadConfig();
                machineManager.loadMachines();
                machineManager.reconcileLoadedMachines();
                sender.sendMessage(mm.deserialize("<green>LetItRoll configuration and machines reloaded.</green>"));
            }
            case "dump" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Only players can target a block to dump.");
                    return true;
                }
                Block targetBlock = player.getTargetBlockExact(5);
                if (targetBlock == null) {
                    player.sendMessage(mm.deserialize("<red>Look at a target block within 5 blocks to dump its structure.</red>"));
                    return true;
                }

                int radius = 2;
                if (args.length >= 2) {
                    try {
                        radius = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        player.sendMessage(mm.deserialize("<red>Invalid radius. Using default radius of 2.</red>"));
                        radius = 2;
                    }
                }
                radius = me.roviczzz.letItRoll.util.StructureDumper.clampRadius(radius);

                me.roviczzz.letItRoll.util.StructureDumper.DumpResult result =
                        me.roviczzz.letItRoll.util.StructureDumper.scan(targetBlock, radius);

                try {
                    java.io.File captureFile = new java.io.File(plugin.getDataFolder(), "captures/build-dump.json");
                    me.roviczzz.letItRoll.util.StructureDumper.dumpToFile(result, captureFile);
                    String ascii = me.roviczzz.letItRoll.util.StructureDumper.toAsciiDiagram(result);
                    plugin.getLogger().info("\n" + ascii);
                    player.sendMessage(mm.deserialize("<green>Structure dumped (<gold>" + result.blocks().size() +
                            "</gold> blocks, radius <gold>" + radius + "</gold>) to <gold>captures/build-dump.json</gold> and console.</green>"));
                } catch (java.io.IOException e) {
                    player.sendMessage(mm.deserialize("<red>Failed to save structure dump: " + e.getMessage() + "</red>"));
                    plugin.getLogger().severe("Error writing structure dump: " + e.getMessage());
                }
            }
            default -> sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(mm.deserialize("<gold><b>LetItRoll Slot Machine Commands:</b></gold>"));
        sender.sendMessage(mm.deserialize("<yellow>/letitroll wand [cabinet|base]</yellow> <gray>- Get the machine creation wand</gray>"));
        sender.sendMessage(mm.deserialize("<yellow>/letitroll list</yellow> <gray>- List all registered machines</gray>"));
        sender.sendMessage(mm.deserialize("<yellow>/letitroll remove</yellow> <gray>- Remove the targeted machine</gray>"));
        sender.sendMessage(mm.deserialize("<yellow>/letitroll reload</yellow> <gray>- Reload machines from storage</gray>"));
        sender.sendMessage(mm.deserialize("<yellow>/letitroll dump [radius]</yellow> <gray>- Dump target build to JSON and console</gray>"));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("letitroll.admin")) return List.of();
        if (args.length == 1) {
            List<String> subs = List.of("wand", "list", "remove", "reload", "dump", "help");
            List<String> completions = new ArrayList<>();
            for (String s : subs) {
                if (s.startsWith(args[0].toLowerCase())) {
                    completions.add(s);
                }
            }
            return completions;
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("wand")) {
                List<String> types = List.of("cabinet", "base");
                List<String> completions = new ArrayList<>();
                for (String t : types) {
                    if (t.startsWith(args[1].toLowerCase())) {
                        completions.add(t);
                    }
                }
                return completions;
            }
            if (args[0].equalsIgnoreCase("dump")) {
                return List.of("1", "2", "3", "4", "5");
            }
        }
        return List.of();
    }
}
