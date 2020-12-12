package io.github.thatsmusic99.configurationmaster;

import org.bukkit.plugin.java.JavaPlugin;

public class ConfigurationMaster extends JavaPlugin {

    @Override
    public void onEnable() {
        new CMFile(this, "config") {
            @Override
            public void loadDefaults() {
                addDefault("hi", true, "Hi there!");
            }

            @Override
            public void postSave() {

            }
        };
    }
}
