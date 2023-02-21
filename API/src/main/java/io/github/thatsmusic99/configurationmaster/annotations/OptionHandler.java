package io.github.thatsmusic99.configurationmaster.annotations;

import io.github.thatsmusic99.configurationmaster.api.ConfigFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface OptionHandler {

    Object get(
            @NotNull ConfigFile file,
            @NotNull String name
    );

    void set(
            @NotNull ConfigFile file,
            @NotNull String name,
            @NotNull Object value
    );

    void addDefault(
            @NotNull ConfigFile file,
            @NotNull String name,
            @NotNull Object value,
            @Nullable String section,
            @Nullable String comment
    );
}
