package io.github.thatsmusic99.configurationmaster;

import io.github.thatsmusic99.configurationmaster.api.ConfigFile;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class HeadsPlusTest {

    @Test
    public void animationsTest() {
        ConfigFile animations = ConfigFile.loadConfig(new File("animations.yml"));
        animations.addComment("This is the config where you can make head animations come to life.\n" +
                "For technical reasons, these will only work in inventories and masks.");

        animations.makeSectionLenient("animations");

        animations.addExample("animations.creeper.looping-mode", "loop-reverse", "");
        animations.addExample("animations.creeper.pausing-period", 4);
        animations.addExample("animations.creeper.textures", new ArrayList<>(Arrays.asList(
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vaS5pbWd1ci5jb20vQXZ1Z29aeC5wbmcifX19",
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vaS5pbWd1ci5jb20vSlFLeHYzNy5wbmcifX19",
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vaS5pbWd1ci5jb20vRVRKalhYVS5wbmcifX19",
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vaS5pbWd1ci5jb20vdVNGTlhway5wbmcifX19",
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vaS5pbWd1ci5jb20vS0ZGTjJjVy5wbmcifX19",
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vaS5pbWd1ci5jb20vUUVPUWxYcy5wbmcifX19",
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vaS5pbWd1ci5jb20vUlQ5TEFkYS5wbmcifX19")));

        animations.makeSectionLenient("testing-1.testing-2");
        animations.createConfigSection("testing-1.testing-2.testing-3");

        try {
            animations.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
