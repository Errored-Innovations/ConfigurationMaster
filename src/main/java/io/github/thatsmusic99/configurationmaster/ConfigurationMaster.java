package io.github.thatsmusic99.configurationmaster;

import org.bukkit.plugin.java.JavaPlugin;

public class ConfigurationMaster extends JavaPlugin {

    @Override
    public void onEnable() {
        CMFile testFile = new CMFile(this, "config.yml") {
            @Override
            public void loadDefaults() {

                addSection("Testing");

                addDefault("testing.option", "option", "Testing 1");

                addDefault("another-test", "test", "Another test");
                addDefault("another-test.test2", "test", "And another test");
            }
        };

        testFile.load();
    }
}
