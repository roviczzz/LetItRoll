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

    public static ItemStack createWand(Plugin plugin) {
        ItemStack wand = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = wand.getItemMeta();
        if (meta != null) {
            meta.displayName(MM.deserialize("<gold><b>Slot Machine Wand</b></gold>"));
            meta.lore(List.of(
                    MM.deserialize("<gray>Right-click a </gray><yellow>Lodestone</yellow><gray> with an attached</gray>"),
                    MM.deserialize("<gray>lever to register a new slot machine.</gray>"),
                    MM.deserialize("<dark_gray>LetItRoll Admin Tool</dark_gray>")
            ));
            NamespacedKey wandKey = new NamespacedKey(plugin, "admin_wand");
            meta.getPersistentDataContainer().set(wandKey, PersistentDataType.BOOLEAN, true);
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
}
