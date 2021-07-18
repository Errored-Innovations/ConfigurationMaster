package io.github.thatsmusic99.configurationmaster.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ConfigSection extends MemorySection {

    void addDefault(@NotNull String path, @Nullable Object value);

    void addDefault(@NotNull String path, @Nullable Object value, @NotNull String comment);

    void addDefault(@NotNull String path, @Nullable Object value, @NotNull String section, @NotNull String comment);

    void addComment(@NotNull String path, @NotNull String comment);

    void addComment(@NotNull String comment);

    void addComments(@NotNull String path, @NotNull String... comments);

    void moveTo(@NotNull String oldPath, @NotNull String newPath);

    void moveTo(@NotNull String oldPath, @NotNull String newPath, @NotNull ConfigFile otherFile);

    void addSection(@NotNull String section);

    void addExample(@NotNull String path, @Nullable Object value);

    void addExample(@NotNull String path, @Nullable Object value, @Nullable String comment);

    void createExampleSection(@NotNull String path);

    void forceExample(@NotNull String path, @Nullable Object value);

    void forceExample(@NotNull String path, @Nullable Object value, @Nullable String comment);

    ConfigSection createConfigSection(@NotNull String path);
}
