package io.github.thatsmusic99.configurationmaster.annotations.handlers;

import io.github.thatsmusic99.configurationmaster.api.ConfigFile;
import org.jetbrains.annotations.NotNull;

public class IntegerOptionHandler extends DefaultOptionHandler {

    @Override
    public Object get(@NotNull ConfigFile file, @NotNull String name) {
        return file.getInteger(name);
    }
}
