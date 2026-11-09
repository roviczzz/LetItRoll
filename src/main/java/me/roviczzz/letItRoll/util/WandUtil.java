package me.roviczzz.letItRoll.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class WandUtil {
    private static final MiniMessage MM = MiniMessage.miniMessage();

    public enum WandType {
        CABINET,
        BASE
    }

    public static ItemStack createWand(Plugin plugin) {
        return createWand(plugin, WandType.CABINET);
    }

    public static ItemStack createWand(Plugin plugin, WandType type) {
        ItemStack wand = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = wand.getItemMeta();
        if (meta != null) {
            if (type == WandType.BASE) {
                meta.displayName(MM.deserialize("<gold><b>Slot Machine Wand</b> <gray>(Base)</gray></gold>"));
                meta.lore(List.of(
                        MM.deserialize("<gray>Right-click a </gray><yellow>Lodestone</yellow><gray> to register a</gray>"),
                        MM.deserialize("<gray>minimal slot machine with wall lever.</gray>"),
                        MM.deserialize("<dark_gray>LetItRoll Admin Tool (Base)</dark_gray>")
                ));
            } else {
                meta.displayName(MM.deserialize("<gold><b>Slot Machine Wand</b> <yellow>(Cabinet)</yellow></gold>"));
                meta.lore(List.of(
                        MM.deserialize("<gray>Right-click a </gray><yellow>Lodestone</yellow><gray> to construct a</gray>"),
                        MM.deserialize("<gray>full slot machine cabinet and wall lever.</gray>"),
                        MM.deserialize("<dark_gray>LetItRoll Admin Tool (Cabinet)</dark_gray>")
                ));
            }
            NamespacedKey wandKey = new NamespacedKey(plugin, "admin_wand");
            NamespacedKey typeKey = new NamespacedKey(plugin, "wand_type");
            meta.getPersistentDataContainer().set(wandKey, PersistentDataType.BOOLEAN, true);
            meta.getPersistentDataContainer().set(typeKey, PersistentDataType.STRING, type.name());
            wand.setItemMeta(meta);
        }
        return wand;
    }

    public static boolean isWand(Plugin plugin, ItemStack item) {
        if (item == null || item.getType() != Material.BLAZE_ROD) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        NamespacedKey wandKey = new NamespacedKey(plugin, "admin_wand");
        return meta.getPersistentDataContainer().has(wandKey, PersistentDataType.BOOLEAN);
    }

    public static WandType getWandType(Plugin plugin, ItemStack item) {
        if (!isWand(plugin, item)) return WandType.CABINET;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return WandType.CABINET;
        NamespacedKey typeKey = new NamespacedKey(plugin, "wand_type");
        String val = meta.getPersistentDataContainer().get(typeKey, PersistentDataType.STRING);
        if (val == null) return WandType.CABINET;
        try {
            return WandType.valueOf(val.toUpperCase());
        } catch (IllegalArgumentException e) {
            return WandType.CABINET;
        }
    }
}
