package me.roviczzz.letItRoll.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class StructureDumperTest {

    @Test
    void testClampRadius() {
        assertEquals(1, StructureDumper.clampRadius(0));
        assertEquals(1, StructureDumper.clampRadius(-5));
        assertEquals(3, StructureDumper.clampRadius(3));
        assertEquals(5, StructureDumper.clampRadius(5));
        assertEquals(5, StructureDumper.clampRadius(10));
    }

    @Test
    void testToJsonAndAsciiDiagram() {
        StructureDumper.BlockEntry center = new StructureDumper.BlockEntry(0, 0, 0, "LODESTONE", "minecraft:lodestone");
        StructureDumper.BlockEntry shelf = new StructureDumper.BlockEntry(0, 1, 0, "SPRUCE_TRAPDOOR", "minecraft:spruce_trapdoor[half=bottom]");
        StructureDumper.BlockEntry lever = new StructureDumper.BlockEntry(1, 0, 0, "LEVER", "minecraft:lever[facing=east]");

        StructureDumper.DumpResult result = new StructureDumper.DumpResult(1, "LODESTONE", List.of(center, shelf, lever));

        String json = StructureDumper.toJson(result);
        assertNotNull(json);
        assertTrue(json.contains("\"radius\": 1"));
        assertTrue(json.contains("\"material\": \"LODESTONE\""));
        assertTrue(json.contains("\"material\": \"SPRUCE_TRAPDOOR\""));
        assertTrue(json.contains("\"material\": \"LEVER\""));

        String ascii = StructureDumper.toAsciiDiagram(result);
        assertNotNull(ascii);
        assertTrue(ascii.contains("=== Structure Dump (Radius: 1, Target: LODESTONE) ==="));
        assertTrue(ascii.contains("--- Layer dy = +1 ---"));
        assertTrue(ascii.contains("--- Layer dy = 0 ---"));
        assertTrue(ascii.contains("Legend:"));
        assertTrue(ascii.contains("LODESTONE"));
    }

    @Test
    void testDumpToFile(@TempDir Path tempDir) throws IOException {
        StructureDumper.BlockEntry center = new StructureDumper.BlockEntry(0, 0, 0, "LODESTONE", "minecraft:lodestone");
        StructureDumper.DumpResult result = new StructureDumper.DumpResult(1, "LODESTONE", List.of(center));

        File targetFile = tempDir.resolve("captures/build-dump.json").toFile();
        StructureDumper.dumpToFile(result, targetFile);

        assertTrue(targetFile.exists());
        String content = Files.readString(targetFile.toPath());
        assertTrue(content.contains("LODESTONE"));
    }
}
