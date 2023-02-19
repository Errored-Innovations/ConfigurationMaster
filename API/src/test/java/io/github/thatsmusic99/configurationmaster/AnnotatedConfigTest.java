package io.github.thatsmusic99.configurationmaster;

import io.github.thatsmusic99.configurationmaster.config.ExampleConfig;
import org.junit.Test;

import java.io.File;

public class AnnotatedConfigTest {

    @Test
    public void testAnnotatedConfig() throws Exception {
        ExampleConfig config = new ExampleConfig(new File("test-config.yml"));
        config.load();
        assert config.COMBAT_DURATION == 30;

        config.COMBAT_DURATION = 20;
        config.save();
    }
}
