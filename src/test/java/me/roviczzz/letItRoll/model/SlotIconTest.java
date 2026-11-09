package me.roviczzz.letItRoll.model;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class SlotIconTest {

    @Test
    void testSlotIconProperties() {
        assertEquals(1, SlotIcon.PUFFERFISH.getPayoutDiamonds());
        assertEquals(4, SlotIcon.PIGLIN_HEAD.getPayoutDiamonds());
        assertEquals(16, SlotIcon.RESIN_CLUMP.getPayoutDiamonds());
        assertEquals(64, SlotIcon.AMETHYST_SHARD.getPayoutDiamonds());

        assertTrue(SlotIcon.PUFFERFISH.getWeight() > SlotIcon.AMETHYST_SHARD.getWeight());
    }

    @Test
    void testGetRandomGeneratesAllIcons() {
        Random random = new Random(42);
        boolean foundPuffer = false;
        boolean foundPiglin = false;
        boolean foundResin = false;
        boolean foundAmethyst = false;

        for (int i = 0; i < 500; i++) {
            SlotIcon icon = SlotIcon.getRandom(random);
            assertNotNull(icon);
            switch (icon) {
                case PUFFERFISH -> foundPuffer = true;
                case PIGLIN_HEAD -> foundPiglin = true;
                case RESIN_CLUMP -> foundResin = true;
                case AMETHYST_SHARD -> foundAmethyst = true;
            }
        }

        assertTrue(foundPuffer, "PUFFERFISH should be rolled");
        assertTrue(foundPiglin, "PIGLIN_HEAD should be rolled");
        assertTrue(foundResin, "RESIN_CLUMP should be rolled");
        assertTrue(foundAmethyst, "AMETHYST_SHARD should be rolled");
    }
}
