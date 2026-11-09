package me.roviczzz.letItRoll.model;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public enum SlotIcon {
    PUFFERFISH(Material.PUFFERFISH, 40, 1, "<yellow>Pufferfish</yellow>"),
    PIGLIN_HEAD(Material.PIGLIN_HEAD, 30, 4, "<gold>Piglin Head</gold>"),
    RESIN_CLUMP(Material.RESIN_CLUMP, 20, 16, "<red>Resin Clump</red>"),
    AMETHYST_SHARD(Material.AMETHYST_SHARD, 10, 64, "<light_purple>Amethyst Shard</light_purple>");

    private final Material material;
    private final int weight;
    private final int payoutDiamonds;
    private final String displayName;

    SlotIcon(Material material, int weight, int payoutDiamonds, String displayName) {
        this.material = material;
        this.weight = weight;
        this.payoutDiamonds = payoutDiamonds;
        this.displayName = displayName;
    }

    public Material getMaterial() {
        return material;
    }

    public int getWeight() {
        return weight;
    }

    public int getPayoutDiamonds() {
        return payoutDiamonds;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ItemStack createItem() {
        return new ItemStack(material);
    }

    public static SlotIcon getRandom(Random random) {
        int totalWeight = 0;
        for (SlotIcon icon : values()) {
            totalWeight += icon.weight;
        }
        int roll = random.nextInt(totalWeight);
        int current = 0;
        for (SlotIcon icon : values()) {
            current += icon.weight;
            if (roll < current) {
                return icon;
            }
        }
        return PUFFERFISH;
    }
}
