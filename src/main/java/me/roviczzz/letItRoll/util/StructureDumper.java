package me.roviczzz.letItRoll.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class StructureDumper {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public record BlockEntry(int dx, int dy, int dz, String material, String blockData) {}

    public record DumpResult(int radius, String targetMaterial, List<BlockEntry> blocks) {}

    public static int clampRadius(int radius) {
        return Math.max(1, Math.min(radius, 5));
    }

    public static DumpResult scan(Block centerBlock, int radius) {
        int r = clampRadius(radius);
        String targetMaterial = centerBlock.getType().name();
        List<BlockEntry> blocks = new ArrayList<>();

        for (int dy = -r; dy <= r; dy++) {
            for (int dz = -r; dz <= r; dz++) {
                for (int dx = -r; dx <= r; dx++) {
                    Block b = centerBlock.getRelative(dx, dy, dz);
                    if (b.getType() != Material.AIR && b.getType() != Material.CAVE_AIR && b.getType() != Material.VOID_AIR) {
                        blocks.add(new BlockEntry(
                                dx, dy, dz,
                                b.getType().name(),
                                b.getBlockData().getAsString()
                        ));
                    }
                }
            }
        }

        return new DumpResult(r, targetMaterial, Collections.unmodifiableList(blocks));
    }

    public static String toJson(DumpResult result) {
        return GSON.toJson(result);
    }

    public static String toAsciiDiagram(DumpResult result) {
        StringBuilder sb = new StringBuilder();
        int r = result.radius();

        // Map relative coordinates to material abbreviation
        Map<String, String> blockMap = new HashMap<>();
        Map<String, Character> legend = new LinkedHashMap<>();
        char nextLegendChar = 'A';

        for (BlockEntry entry : result.blocks()) {
            String key = entry.dx() + "," + entry.dy() + "," + entry.dz();
            blockMap.put(key, entry.material());

            if (!legend.containsKey(entry.material())) {
                legend.put(entry.material(), nextLegendChar++);
            }
        }

        sb.append("=== Structure Dump (Radius: ").append(r)
                .append(", Target: ").append(result.targetMaterial()).append(") ===\n");

        for (int dy = r; dy >= -r; dy--) {
            sb.append("--- Layer dy = ").append(dy > 0 ? "+" + dy : String.valueOf(dy)).append(" ---\n");
            for (int dz = -r; dz <= r; dz++) {
                for (int dx = -r; dx <= r; dx++) {
                    String mat = blockMap.get(dx + "," + dy + "," + dz);
                    if (dx == 0 && dy == 0 && dz == 0) {
                        sb.append(mat != null ? "[" + legend.get(mat) + "]" : "[*]");
                    } else if (mat != null) {
                        sb.append(" ").append(legend.get(mat)).append(" ");
                    } else {
                        sb.append(" . ");
                    }
                }
                sb.append("\n");
            }
        }

        sb.append("\nLegend:\n");
        sb.append(" [*] Center target\n");
        for (Map.Entry<String, Character> entry : legend.entrySet()) {
            sb.append("  ").append(entry.getValue()).append("  : ").append(entry.getKey()).append("\n");
        }

        return sb.toString();
    }

    public static void dumpToFile(DumpResult result, File file) throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            writer.write(toJson(result));
        }
    }
}
