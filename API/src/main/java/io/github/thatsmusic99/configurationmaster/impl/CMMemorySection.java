package io.github.thatsmusic99.configurationmaster.impl;

import io.github.thatsmusic99.configurationmaster.api.ConfigFile;
import io.github.thatsmusic99.configurationmaster.api.ConfigSection;
import io.github.thatsmusic99.configurationmaster.api.MemorySection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CMMemorySection implements MemorySection {

    protected LinkedHashMap<String, Object> defaults;
    protected LinkedHashMap<String, Object> existingValues;
    protected LinkedHashMap<String, Object> actualValues;
    protected String path;
    protected ConfigFile parent;

    CMMemorySection() {
        if (this instanceof ConfigFile) {
            this.path = "";
            init();
        } else {
            throw new IllegalStateException("SHUT UP. SHUT UP");
        }
    }

    CMMemorySection(String path, ConfigFile parent) {
        this.path = path;
        this.parent = parent;
        init();
    }

    private void init() {
        this.defaults = new LinkedHashMap<>();
        this.existingValues = new LinkedHashMap<>();
        this.actualValues = new LinkedHashMap<>();
    }

    @Override
    public String getString(@NotNull String path, @Nullable String defaultValue) {
        Object result = get(path, defaultValue);
        if (result == null) return null;
        return String.valueOf(result);
    }

    @Override
    public int getInteger(@NotNull String path, int defaultValue) {
        String result = getString(path);
        try {
            return result == null ? defaultValue : Integer.parseInt(result);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    @Override
    public double getDouble(@NotNull String path, double defaultValue) {
        String result = getString(path);
        try {
            return result == null ? defaultValue : Double.parseDouble(result);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    @Override
    public Object get(@NotNull String path, @Nullable Object defaultValue) {
        CMMemorySection section = getSectionInternal(path, false);
        if (section == null) return defaultValue;
        String key = getKey(path);
        return section.actualValues.getOrDefault(key, section.existingValues.getOrDefault(key, defaultValue));
    }

    @Override
    public boolean getBoolean(@NotNull String path, boolean defaultValue) {
        String result = getString(path);
        if (result == null) return defaultValue;
        if (!(result.equalsIgnoreCase("false") || result.equalsIgnoreCase("true"))) return defaultValue;
        return result.equalsIgnoreCase("true");
    }

    @Override
    public long getLong(@NotNull String path, long defaultValue) {
        String result = getString(path);
        try {
            return result == null ? defaultValue : Long.parseLong(result);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    @Override
    public short getShort(@NotNull String path, short defaultValue) {
        String result = getString(path);
        try {
            return result == null ? defaultValue : Short.parseShort(result);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    @Override
    public byte getByte(@NotNull String path, byte defaultValue) {
        String result = getString(path);
        try {
            return result == null ? defaultValue : Byte.parseByte(result);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    @Override
    public float getFloat(@NotNull String path, float defaultValue) {
        String result = getString(path);
        try {
            return result == null ? defaultValue : Float.parseFloat(result);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    @Override
    public ConfigSection getConfigSection(@NotNull String path, @Nullable ConfigSection defaultValue) {
        Object value = get(path, defaultValue);
        return value instanceof ConfigSection ? (ConfigSection) value : defaultValue;
    }

    @Override
    public boolean contains(@NotNull String path) {
        CMMemorySection section = getSectionInternal(path, false);
        if (section == null) return false;
        String key = getKey(path);
        return section.existingValues.containsKey(key) || section.actualValues.containsKey(key);
    }

    @Override
    public <T> List<T> getList(@NotNull String path, @Nullable List<T> defaultValue) {
        Object value = get(path, defaultValue);
        if (value == null) return defaultValue;
        if (value.getClass().isArray()) {
            value = Arrays.asList((Object[]) value);
        } else if (!(value instanceof List)) {
            value = new ArrayList<>(Collections.singletonList(value));
        }
        return (List<T>) value;
    }

    @Override
    public void set(@NotNull String path, @Nullable Object object) {
        CMMemorySection section = getSectionInternal(path);
        if (section == null) {
            if (object == null) return;
            section = getParent().createSectionInternal(path);
        }
        String key = getKey(path);
        if (object == null) {
            section.actualValues.remove(key);
            return;
        }
        section.actualValues.put(key, object);
    }

    @Nullable
    protected CMMemorySection getSectionInternal(@NotNull String path) {
        return getSectionInternal(path, true);
    }

    protected CMMemorySection getSectionInternal(@NotNull String path, boolean add) {
        Objects.requireNonNull(path, "Path must not be null!");
        CMMemorySection section = this;
        while (path.indexOf('.') != -1 && section != null) {
            String key = path.substring(0, path.indexOf('.'));
            path = path.substring(path.indexOf('.') + 1);
            CMMemorySection tempSection;
            if (section.existingValues.get(key) instanceof CMConfigSection) {
                tempSection = (CMMemorySection) section.getConfigSection(key, (CMConfigSection) section.existingValues.get(key));
            } else {
                tempSection = (CMMemorySection) section.getConfigSection(key);
            }
            if (tempSection != null && add) section.actualValues.putIfAbsent(key, tempSection);
            section = tempSection;
        }
        return section;
    }

    @Override
    public List<String> getKeys(boolean deep, boolean useExisting) {
        List<String> keys = new ArrayList<>();
        HashMap<String, Object> map = useExisting ? existingValues : actualValues;
        for (String path : map.keySet()) {
            if (deep && map.get(path) instanceof CMConfigSection) {
                keys.addAll(((CMConfigSection) map.get(path)).getKeys(true));
            } else {
                keys.add(path);
            }
        }
        return keys;
    }

    @Override
    public String getPath() {
        return path;
    }

    protected ConfigFile getParent() {
        return this instanceof ConfigFile ? (ConfigFile) this : parent;
    }

    protected String getKey(String path) {
        return path.substring(path.lastIndexOf('.') + 1);
    }
}
