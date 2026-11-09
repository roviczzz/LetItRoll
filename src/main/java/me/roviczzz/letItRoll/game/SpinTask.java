package me.roviczzz.letItRoll.game;

import me.roviczzz.letItRoll.display.DisplayManager;
import me.roviczzz.letItRoll.model.MachineState;
import me.roviczzz.letItRoll.model.SlotIcon;
import me.roviczzz.letItRoll.model.SlotMachine;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Switch;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class SpinTask extends BukkitRunnable {
    private final Plugin plugin;
    private final SlotMachine machine;
    private final DisplayManager displayManager;
    private final Player player;
    private final Random random = new Random();

    private final SlotIcon[] finalIcons = new SlotIcon[3];
    private int tick = 0;

    public SpinTask(Plugin plugin, SlotMachine machine, DisplayManager displayManager, Player player) {
        this.plugin = plugin;
        this.machine = machine;
        this.displayManager = displayManager;
        this.player = player;

        // Pre-determine final icons
        this.finalIcons[0] = SlotIcon.getRandom(random);
        this.finalIcons[1] = SlotIcon.getRandom(random);
        this.finalIcons[2] = SlotIcon.getRandom(random);
    }

    @Override
    public void run() {
        tick += 2;
        World world = machine.getLodestoneLocation().getWorld();
        if (world == null) {
            finishSpin();
            cancel();
            return;
        }

        UUID[] displayUuids = machine.getDisplayUuids();
        Location soundLoc = machine.getLodestoneLocation().clone().add(0.5, 1.0, 0.5);

        // Reel 0 cycling or locked
        if (tick < 25) {
            displayManager.updateDisplay(world, displayUuids[0], SlotIcon.getRandom(random));
        } else if (tick == 26) {
            displayManager.updateDisplay(world, displayUuids[0], finalIcons[0]);
            world.playSound(soundLoc, Sound.BLOCK_IRON_TRAPDOOR_CLOSE, 0.8f, 1.2f);
        }

        // Reel 1 cycling or locked
        if (tick < 45) {
            displayManager.updateDisplay(world, displayUuids[1], SlotIcon.getRandom(random));
        } else if (tick == 46) {
            displayManager.updateDisplay(world, displayUuids[1], finalIcons[1]);
            world.playSound(soundLoc, Sound.BLOCK_IRON_TRAPDOOR_CLOSE, 0.8f, 1.4f);
        }

        // Reel 2 cycling or locked
        if (tick < 65) {
            displayManager.updateDisplay(world, displayUuids[2], SlotIcon.getRandom(random));
        } else if (tick == 66) {
            displayManager.updateDisplay(world, displayUuids[2], finalIcons[2]);
            world.playSound(soundLoc, Sound.BLOCK_IRON_TRAPDOOR_CLOSE, 0.8f, 1.6f);
        }

        // Ticking audio while spinning
        if (tick < 65 && tick % 4 == 0) {
            float pitch = 1.0f + (tick / 70.0f);
            world.playSound(soundLoc, Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, pitch);
        }

        // Finish at tick 70
        if (tick >= 70) {
            finishSpin();
            cancel();
        }
    }

    private void finishSpin() {
        // Reset physical lever
        Block leverBlock = machine.getLeverLocation().getBlock();
        if (leverBlock.getBlockData() instanceof Switch leverSwitch) {
            leverSwitch.setPowered(false);
            leverBlock.setBlockData(leverSwitch);
            leverBlock.getWorld().playSound(leverBlock.getLocation(), Sound.BLOCK_LEVER_CLICK, 0.6f, 0.6f);
        }

        // Evaluate outcome
        boolean isWin = (finalIcons[0] == finalIcons[1] && finalIcons[1] == finalIcons[2]);
        Location lodestoneLoc = machine.getLodestoneLocation().clone().add(0.5, 1.2, 0.5);

        if (isWin) {
            handleWin(finalIcons[0], lodestoneLoc);
        } else {
            handleLoss(lodestoneLoc);
        }

        machine.setState(MachineState.IDLE);
    }

    private void handleWin(SlotIcon icon, Location location) {
        int payout = icon.getPayoutDiamonds();
        World world = location.getWorld();
        MiniMessage mm = MiniMessage.miniMessage();

        // Dispense diamonds to player
        if (player.isOnline()) {
            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(new ItemStack(Material.DIAMOND, payout));
            for (ItemStack drop : leftover.values()) {
                world.dropItem(player.getLocation(), drop);
            }
        } else {
            world.dropItem(location, new ItemStack(Material.DIAMOND, payout));
        }

        // Audio & Visual Effects by Tier
        switch (icon) {
            case AMETHYST_SHARD -> {
                world.playSound(location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                world.spawnParticle(Particle.TOTEM_OF_UNDYING, location, 40, 0.5, 0.5, 0.5, 0.1);
                if (player.isOnline()) {
                    Title title = Title.title(
                            mm.deserialize("<gold><b>JACKPOT!</b></gold>"),
                            mm.deserialize("<light_purple>+64 Diamonds!</light_purple>"),
                            Title.Times.times(Duration.ofMillis(200), Duration.ofMillis(2500), Duration.ofMillis(500))
                    );
                    player.showTitle(title);
                }
            }
            case RESIN_CLUMP -> {
                world.playSound(location, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                world.spawnParticle(Particle.HAPPY_VILLAGER, location, 30, 0.4, 0.4, 0.4, 0.1);
                if (player.isOnline()) {
                    Title title = Title.title(
                            mm.deserialize("<red><b>BIG WIN!</b></red>"),
                            mm.deserialize("<gold>+16 Diamonds!</gold>"),
                            Title.Times.times(Duration.ofMillis(200), Duration.ofMillis(2000), Duration.ofMillis(400))
                    );
                    player.showTitle(title);
                }
            }
            case PIGLIN_HEAD -> {
                world.playSound(location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                world.spawnParticle(Particle.WAX_ON, location, 20, 0.3, 0.3, 0.3, 0.05);
                if (player.isOnline()) {
                    Title title = Title.title(
                            mm.deserialize("<gold><b>WIN!</b></gold>"),
                            mm.deserialize("<yellow>+4 Diamonds</yellow>"),
                            Title.Times.times(Duration.ofMillis(200), Duration.ofMillis(1500), Duration.ofMillis(300))
                    );
                    player.showTitle(title);
                }
            }
            case PUFFERFISH -> {
                world.playSound(location, Sound.ENTITY_PUFFER_FISH_BLOW_UP, 0.8f, 1.2f);
                if (player.isOnline()) {
                    player.sendActionBar(mm.deserialize("<yellow>Consolation match! 1 Diamond returned.</yellow>"));
                }
            }
        }
    }

    private void handleLoss(Location location) {
        World world = location.getWorld();
        world.playSound(location, Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.5f);
        if (player.isOnline()) {
            player.sendActionBar(MiniMessage.miniMessage().deserialize("<gray>No match. Better luck next time!</gray>"));
        }
    }
}
