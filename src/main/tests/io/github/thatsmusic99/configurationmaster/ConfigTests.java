package io.github.thatsmusic99.configurationmaster;

import org.junit.Test;

import java.io.File;

public class ConfigTests {

    @Test
    public void testConfig() {
        File file = new File("test.yml");
        InternalConfigFile config = new InternalConfigFile(file);

    }
}
