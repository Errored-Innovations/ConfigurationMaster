package io.github.thatsmusic99.configurationmaster.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public interface MemorySection {

    /**
     * Returns a string at a specified path.
     *
     * @param path The path of the option itself.
     *      To indicate for an option to be placed inside different sections, use a . delimiter, e.g. section.option
     * @return The string stored at the path. If nothing is found, a value of null is returned.
     * @throws NullPointerException if the path is null (not if it wasn't found).
     */
    @Nullable
    default String getString(@NotNull String path) {
        return getString(path, null);
    }

    /**
     * Returns a string at a specified path. If it is not found, the default value is returned.
     *
     * @param path The path of the option itself.
     *      To indicate for an option to be placed inside different sections, use a . delimiter, e.g. section.option
     * @param defaultValue The default value to be returned if nothing is found.
     * @return The string stored at the path. If nothing is found, the default value is returned.
     * @throws NullPointerException if the path is null (not if it wasn't found).
     */
    String getString(@NotNull String path, @Nullable String defaultValue);

    /**
     * Returns an integer at a specified path.
     *
     * @param path The path of the option itself.
     *      To indicate for an option to be placed inside different sections, use a . delimiter, e.g. section.option
     * @return The integer stored at the path. If nothing is found, a value of 0 is returned.
     * @throws NullPointerException if the path is null (not if it wasn't found).
     */
    default int getInteger(@NotNull String path) {
        return getInteger(path, 0);
    }

    /**
     * Returns an integer at a specified path. If it is not found, the default value is returned.
     *
     * @param path The path of the option itself.
     *      To indicate for an option to be placed inside different sections, use a . delimiter, e.g. section.option
     * @param defaultValue The default value to be returned if nothing is found.
     * @return The integer stored at the path. If nothing is found, the default value is returned.
     * @throws NullPointerException if the path is null (not if it wasn't found).
     */
    int getInteger(@NotNull String path, int defaultValue);

    /**
     * Returns a double at a specified path.
     *
     * @param path The path of the option itself.
     *      To indicate for an option to be placed inside different sections, use a . delimiter, e.g. section.option
     * @return The double stored at the path. If nothing is found, a value of 0.0 is returned.
     * @throws NullPointerException if the path is null (not if it wasn't found).
     */
    default double getDouble(@NotNull String path) {
        return getDouble(path, 0.0);
    }

    /**
     * Returns a double at a specified path. If it is not found, the default value is returned.
     *
     * @param path The path of the option itself.
     *      To indicate for an option to be placed inside different sections, use a . delimiter, e.g. section.option
     * @param defaultValue The default value to be returned if nothing is found.
     * @return The double stored at the path. If nothing is found, the default value is returned.
     * @throws NullPointerException if the path is null (not if it wasn't found).
     */
    double getDouble(@NotNull String path, double defaultValue);

    /**
     * Returns an object at a specified path.
     *
     * @param path The path of the option itself.
     *      To indicate for an option to be placed inside different sections, use a . delimiter, e.g. section.option
     * @return The object stored at the path. If nothing is found, a null value is returned.
     * @throws NullPointerException if the path is null (not if it wasn't found).
     */
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

    String getPath();


}
