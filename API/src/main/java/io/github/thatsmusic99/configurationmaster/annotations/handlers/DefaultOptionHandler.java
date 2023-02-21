package io.github.thatsmusic99.configurationmaster.annotations.handlers;

import io.github.thatsmusic99.configurationmaster.annotations.OptionHandler;
import io.github.thatsmusic99.configurationmaster.api.ConfigFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DefaultOptionHandler implements OptionHandler {


    @Override
    public Object get(@NotNull ConfigFile file, @NotNull String name) {
        return file.get(name);
    }

    @Override
    public void set(@NotNull ConfigFile file, @NotNull String name, @NotNull Object value) {
        file.set(name, value);
    }

    @Override
    public void addDefault(@NotNull ConfigFile file, @NotNull String name, @NotNull Object value, @Nullable String section, @Nullable String comment) {
        file.addDefault(name, value, section, comment);
    }
}
