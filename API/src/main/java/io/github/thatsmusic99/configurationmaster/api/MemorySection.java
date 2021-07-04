package io.github.thatsmusic99.configurationmaster.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public interface MemorySection {

    default String getString(@NotNull String path) {
        return getString(path, null);
    }

    String getString(@NotNull String path, @Nullable String defaultValue);

    default int getInteger(@NotNull String path) {
        return getInteger(path, 0);
    }

    int getInteger(@NotNull String path, int defaultValue);

    default double getDouble(@NotNull String path) {
        return getDouble(path, 0.0);
    }

    double getDouble(@NotNull String path, double defaultValue);

    default Object get(@NotNull String path) {
        return get(path, null);
    }

    Object get(@NotNull String path, @Nullable Object defaultValue);

    default boolean getBoolean(@NotNull String path) {
        return getBoolean(path, false);
    }

    boolean getBoolean(@NotNull String path, boolean defaultValue);

    default long getLong(@NotNull String path) {
        return getLong(path, 0);
    }

    long getLong(@NotNull String path, long defaultValue);

    default short getShort(@NotNull String path) {
        return getShort(path, (short) 0);
    }

    short getShort(@NotNull String path, short defaultValue);

    default byte getByte(@NotNull String path) {
        return getByte(path, (byte) 0);
    }

    byte getByte(@NotNull String path, byte defaultValue);

    default float getFloat(@NotNull String path) {
        return getFloat(path, 0f);
    }

    float getFloat(@NotNull String path, float defaultValue);

    default ConfigSection getConfigSection(@NotNull String path) {
        return getConfigSection(path, null);
    }

    ConfigSection getConfigSection(@NotNull String path, @Nullable ConfigSection defaultValue);

    boolean contains(@NotNull String path);

    @NotNull
    default <T> List<T> getList(@NotNull String path) {
        return getList(path, new ArrayList<>());
    }

    @NotNull
    default List<String> getStringList(@NotNull String path) {
        return getList(path, new ArrayList<>());
    }

    <T> List<T> getList(@NotNull String path, @Nullable List<T> defaultValue);

    void set(@NotNull String path, @Nullable Object object);

    List<String> getKeys(boolean deep);


}
