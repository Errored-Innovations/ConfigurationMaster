package io.github.thatsmusic99.configurationmaster.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ConfigSection extends MemorySection {

    /**
     * Adds a default value to an option in the config file.
     *
     * If the option does not exist, it will be added with the provided default value.
     *
     * However, if the option does exist, it will only be adjusted to its correct position. The value inside does not change.
     *
     * @param path The path of the option itself.
     *             To indicate for an option to be placed inside different sections, use a . delimiter, e.g. section.option
     * @param value The default value to be used if the option doesn't already exist.
     */
    void addDefault(@NotNull String path, @Nullable Object value);

    void addDefault(@NotNull String path, @Nullable Object value, @Nullable String comment);

    void addDefault(@NotNull String path, @Nullable Object value, @Nullable String section, @Nullable String comment);

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

    void makeSectionLenient(@NotNull String path);

    ConfigSection createConfigSection(@NotNull String path);
}
