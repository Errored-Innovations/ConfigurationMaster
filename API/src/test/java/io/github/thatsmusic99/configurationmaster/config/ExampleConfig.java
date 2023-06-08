package io.github.thatsmusic99.configurationmaster.config;

import io.github.thatsmusic99.configurationmaster.annotations.Example;
import io.github.thatsmusic99.configurationmaster.annotations.Option;
import io.github.thatsmusic99.configurationmaster.api.ConfigFile;
import io.github.thatsmusic99.configurationmaster.api.ConfigSection;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.IOException;

public class ExampleConfig extends ConfigFile {

    @Option(comment = "How long a player is considered to be in combat for.", section = "Combat")
    public int COMBAT_DURATION = 30;

    @Option(comment = "Players who should not be in combat and why.", lenient = true)
    @Example(key = "Thatsmusic99", value = "Git gud")
    @Example(key = "Niestrat99", value = "Too handsome")
    public ConfigSection DENIED_PLAYERS;
    @Option(comment = "How long the grace period lasts.")
    public int GRACE_PERIOD = 30;

    /**
     * Used to load a config file without safety precautions taken by the API.
     *
     * @param file The config file to be loaded.
     * @throws YAMLException if the file being loaded contains syntax errors.
     * @see ConfigFile#loadConfig(File)
     */
    public ExampleConfig(@NotNull File file) throws IOException, IllegalAccessException {
        super(file);
    }
}
