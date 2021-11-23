package io.github.thatsmusic99.configurationmaster.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A MemorySection represents a section in the configuration that holds data.
 */
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
        return getString(path, null, false);
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
    default String getString(@NotNull String path, @Nullable String defaultValue) {
        return getString(path, defaultValue, false);
    }

    default String getString(@NotNull String path, boolean useExisting) {
        return getString(path, null, useExisting);
    }

    String getString(@NotNull String path, @Nullable String defaultValue, boolean useExisting);

    /**
     * Returns an integer at a specified path.
     *
     * @param path The path of the option itself.
     *      To indicate for an option to be placed inside different sections, use a . delimiter, e.g. section.option
     * @return The integer stored at the path. If nothing is found, a value of 0 is returned.
     * @throws NullPointerException if the path is null (not if it wasn't found).
     */
    default int getInteger(@NotNull String path) {
        return getInteger(path, 0, false);
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
    default int getInteger(@NotNull String path, int defaultValue) {
        return getInteger(path, defaultValue, false);
    }

    default int getInteger(@NotNull String path, boolean useExisting) {
        return getInteger(path, 0, useExisting);
    }

    int getInteger(@NotNull String path, int defaultValue, boolean useExisting);

    /**
     * Returns a double at a specified path.
     *
     * @param path The path of the option itself.
     *      To indicate for an option to be placed inside different sections, use a . delimiter, e.g. section.option
     * @return The double stored at the path. If nothing is found, a value of 0.0 is returned.
     * @throws NullPointerException if the path is null (not if it wasn't found).
     */
    default double getDouble(@NotNull String path) {
        return getDouble(path, 0.0, false);
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
    default double getDouble(@NotNull String path, double defaultValue) {
        return getDouble(path, defaultValue, false);
    }

    default double getDouble(@NotNull String path, boolean useExisting) {
        return getDouble(path, 0, useExisting);
    }

    double getDouble(@NotNull String path, double defaultValue, boolean useExisting);

    /**
     * Returns an object at a specified path.
     *
     * @param path The path of the option itself.
     *      To indicate for an option to be placed inside different sections, use a . delimiter, e.g. section.option
     * @return The object stored at the path. If nothing is found, a null value is returned.
     * @throws NullPointerException if the path is null (not if it wasn't found).
     */
    default Object get(@NotNull String path) {
        return get(path, null, false);
    }

    /**
     * Returns an object at a specified path. If it is not found, the default value is returned.
     *
     * @param path The path of the option itself.
     *      To indicate for an option to be placed inside different sections, use a . delimiter, e.g. section.option
     * @param defaultValue The default value to be returned if nothing is found.
     * @return The object stored at the path. If nothing is found, the default value is returned.
     * @throws NullPointerException if the path is null (not if it wasn't found).
     */
    default Object get(@NotNull String path, @Nullable Object defaultValue) {
        return get(path, defaultValue, false);
    }

    default Object get(@NotNull String path, boolean useExisting) {
        return get(path, null, useExisting);
    }

    Object get(@NotNull String path, @Nullable Object defaultValue, boolean useExisting);

    /**
     * Returns a boolean at a specified path.
     *
     * @param path The path of the option itself.
     *      To indicate for an option to be placed inside different sections, use a . delimiter, e.g. section.option
     * @return The boolean stored at the path. If nothing is found, false is returned.
     * @throws NullPointerException if the path is null (not if it wasn't found).
     */
    default boolean getBoolean(@NotNull String path) {
        return getBoolean(path, false);
    }

    /**
     * Returns a boolean at a specified path. If it is not found, the default value is returned.
     *
     * @param path The path of the option itself.
     *      To indicate for an option to be placed inside different sections, use a . delimiter, e.g. section.option
     * @param defaultValue The default value to be returned if nothing is found.
     * @return The boolean stored at the path. If nothing is found, the default value is returned.
     * @throws NullPointerException if the path is null (not if it wasn't found).
     */
    default boolean getBoolean(@NotNull String path, boolean defaultValue) {
        return getBoolean(path, defaultValue, false);
    }

    boolean getBoolean(@NotNull String path, boolean defaultValue, boolean useExisting);

    /**
     * Returns a long at a specified path.
     *
     * @param path The path of the option itself.
     *      To indicate for an option to be placed inside different sections, use a . delimiter, e.g. section.option
     * @return The long stored at the path. If nothing is found, a value of 0 is returned.
     * @throws NullPointerException if the path is null (not if it wasn't found).
     */
    default long getLong(@NotNull String path) {
        return getLong(path, 0, false);
    }

    /**
     * Returns a long at a specified path. If it is not found, the default value is returned.
     *
     * @param path The path of the option itself.
     *      To indicate for an option to be placed inside different sections, use a . delimiter, e.g. section.option
     * @param defaultValue The default value to be returned if nothing is found.
     * @return The long stored at the path. If nothing is found, the default value is returned.
     * @throws NullPointerException if the path is null (not if it wasn't found).
     */
    default long getLong(@NotNull String path, long defaultValue) {
        return getLong(path, defaultValue, false);
    }

    default long getLong(@NotNull String path, boolean useExisting) {
        return getLong(path, 0, useExisting);
    }

    long getLong(@NotNull String path, long defaultValue, boolean useExisting);

    /**
     * Returns a short at a specified path.
     *
     * @param path The path of the option itself.
     *      To indicate for an option to be placed inside different sections, use a . delimiter, e.g. section.option
     * @return The short stored at the path. If nothing is found, a value of 0 is returned.
     * @throws NullPointerException if the path is null (not if it wasn't found).
     */
    default short getShort(@NotNull String path) {
        return getShort(path, (short) 0, false);
    }

    /**
     * Returns a short at a specified path. If it is not found, the default value is returned.
     *
     * @param path The path of the option itself.
     *      To indicate for an option to be placed inside different sections, use a . delimiter, e.g. section.option
     * @param defaultValue The default value to be returned if nothing is found.
     * @return The short stored at the path. If nothing is found, the default value is returned.
     * @throws NullPointerException if the path is null (not if it wasn't found).
     */
    default short getShort(@NotNull String path, short defaultValue) {
        return getShort(path, defaultValue, false);
    }

    default short getShort(@NotNull String path, boolean useExisting) {
        return getShort(path, (short) 0, useExisting);
    }

    short getShort(@NotNull String path, short defaultValue, boolean useExisting);

    /**
     * Returns a byte at a specified path.
     *
     * @param path The path of the option itself.
     *      To indicate for an option to be placed inside different sections, use a . delimiter, e.g. section.option
     * @return The byte stored at the path. If nothing is found, a value of 0 is returned.
     * @throws NullPointerException if the path is null (not if it wasn't found).
     */
    default byte getByte(@NotNull String path) {
        return getByte(path, (byte) 0, false);
    }

    /**
     * Returns a byte at a specified path. If it is not found, the default value is returned.
     *
     * @param path The path of the option itself.
     *      To indicate for an option to be placed inside different sections, use a . delimiter, e.g. section.option
     * @param defaultValue The default value to be returned if nothing is found.
     * @return The byte stored at the path. If nothing is found, the default value is returned.
     * @throws NullPointerException if the path is null (not if it wasn't found).
     */
    default byte getByte(@NotNull String path, byte defaultValue) {
        return getByte(path, defaultValue, false);
    }

    default byte getByte(@NotNull String path, boolean useExisting) {
        return getByte(path, (byte) 0, useExisting);
    }

    byte getByte(@NotNull String path, byte defaultValue, boolean useExisting);

    /**
     * Returns a float at a specified path.
     *
     * @param path The path of the option itself.
     *      To indicate for an option to be placed inside different sections, use a . delimiter, e.g. section.option
     * @return The float stored at the path. If nothing is found, a value of 0 is returned.
     * @throws NullPointerException if the path is null (not if it wasn't found).
     */
    default float getFloat(@NotNull String path) {
        return getFloat(path, 0f, false);
    }

    /**
     * Returns a float at a specified path. If it is not found, the default value is returned.
     *
     * @param path The path of the option itself.
     *      To indicate for an option to be placed inside different sections, use a . delimiter, e.g. section.option
     * @param defaultValue The default value to be returned if nothing is found.
     * @return The float stored at the path. If nothing is found, the default value is returned.
     * @throws NullPointerException if the path is null (not if it wasn't found).
     */
    default float getFloat(@NotNull String path, float defaultValue) {
        return getFloat(path, defaultValue, false);
    }

    default float getFloat(@NotNull String path, boolean useExisting) {
        return getFloat(path, 0.0f, useExisting);
    }

    float getFloat(@NotNull String path, float defaultValue, boolean useExisting);

    /**
     * Returns a configuration file at a specified path.
     *
     * @param path The path of the option itself.
     *      To indicate for an option to be placed inside different sections, use a . delimiter, e.g. section.option
     * @return The configuration section stored at the path. If nothing is found, a null value is returned.
     * @throws NullPointerException if the path is null (not if it wasn't found).
     */
    default ConfigSection getConfigSection(@NotNull String path) {
        return getConfigSection(path, null, false);
    }

    /**
     * Returns a configuration section at a specified path. If it is not found, the default value is returned.
     *
     * @param path The path of the option itself.
     *      To indicate for an option to be placed inside different sections, use a . delimiter, e.g. section.option
     * @param defaultValue The default value to be returned if nothing is found.
     * @return The configuration section stored at the path. If nothing is found, the default value is returned.
     * @throws NullPointerException if the path is null (not if it wasn't found).
     */
    default ConfigSection getConfigSection(@NotNull String path, @Nullable ConfigSection defaultValue) {
        return getConfigSection(path, defaultValue, false);
    }

    default ConfigSection getConfigSection(@NotNull String path, boolean useExisting) {
        return getConfigSection(path, null, useExisting);
    }

    ConfigSection getConfigSection(@NotNull String path, @Nullable ConfigSection defaultValue, boolean useExisting);

    /**
     * Returns whether or not the memory section contains a specific path.
     *
     * @param path The path to search for.
     *      To indicate for an option to be placed inside different sections, use a . delimiter, e.g. section.option
     * @return true if the memory section contains the path, false if not.
     */
    default boolean contains(@NotNull String path) {
        return contains(path, false);
    }

    boolean contains(@NotNull String path, boolean useExisting);

    /**
     * Returns a list at a given path. If the provided path does not point to a list but a different data type,
     * a new list is created containing that single element. If the path itself is not found, an empty list is created.
     *
     * @param path The path of the option itself.
     *      To indicate for an option to be placed inside different sections, use a . delimiter, e.g. section.option
     * @param <T> The list type you want returned.
     * @return The list stored at the path.
     * @throws NullPointerException if the path is null (not if it wasn't found).
     */
    @NotNull
    default <T> List<T> getList(@NotNull String path) {
        return getList(path, new ArrayList<>());
    }

    /**
     * Returns a string list at a given path. If the provided path does not point to a list but a different data type,
     * a new list is created containing that single element. If the path itself is not found, an empty list is created.
     *
     * @param path The path of the option itself.
     *      To indicate for an option to be placed inside different sections, use a . delimiter, e.g. section.option
     * @return The string list stored at the path.
     * @throws NullPointerException if the path is null (not if it wasn't found).
     */
    @NotNull
    default List<String> getStringList(@NotNull String path) {
        return getList(path, new ArrayList<>(), false);
    }

    /**
     * Returns a list at a given path. If the provided path does not point to a list but a different data type,
     * a new list is created containing that single element. If the path itself is not found, the default value is returned.
     *
     * @param path The path of the option itself.
     *      To indicate for an option to be placed inside different sections, use a . delimiter, e.g. section.option
     * @param defaultValue The default value to be returned if nothing is found.
     * @param <T> The list type you want returned.
     * @return The list stored at the path.
     * @throws NullPointerException if the path is null (not if it wasn't found).
     */
    default <T> List<T> getList(@NotNull String path, @Nullable List<T> defaultValue) {
        return getList(path, defaultValue, false);
    }

    default <T> List<T> getList(@NotNull String path, boolean useExisting) {
        return getList(path, new ArrayList<>(), useExisting);
    }

    <T> List<T>  getList(@NotNull String path, @Nullable List<T> defaultValue, boolean useExisting);

    /**
     * Sets a value at a specified path.
     *
     * @param path The path of the option itself.
     *      To indicate for an option to be placed inside different sections, use a . delimiter, e.g. section.option
     * @param object The object the option will be set to.
     */
    void set(@NotNull String path, @Nullable Object object);

    /**
     * Returns a list of option/config section keys stored within this memory section.
     *
     * @param deep true if options within configuration sections should be included, false if you just want to collect
     *      keys from this memory section only.
     * @return A list of keys to the given paths.
     */
    default List<String> getKeys(boolean deep) {
        return getKeys(deep, false);
    }

    List<String> getKeys(boolean deep, boolean useExisting);

    /**
     * The path of this section.
     *
     * @return The full path - periods included - of this section.
     */
    String getPath();


}
