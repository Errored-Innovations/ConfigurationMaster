package io.github.thatsmusic99.configurationmaster.impl;

import io.github.thatsmusic99.configurationmaster.api.ConfigFile;
import io.github.thatsmusic99.configurationmaster.api.ConfigSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CMConfigSection extends CMMemorySection implements ConfigSection {

    public CMConfigSection() {
        super();
    }

    public CMConfigSection(String path, ConfigFile file) {
        super(path, file);
    }

    public void addDefault(@NotNull String path, Object defaultOption) {
        addDefault(path, defaultOption, null, null);
    }

    public void addDefault(@NotNull String path, Object defaultOption, @NotNull String comment) {
        addDefault(path, defaultOption, null, comment);
    }

    public void addDefault(@NotNull String path, Object defaultOption, @Nullable String section, @Nullable String comment) {
        //
        String fullPath = getPathWithKey(path);
        CMMemorySection cmSection = getSectionInternal(path);
        if (cmSection == null) cmSection = createSectionInternal(path);
        String key = path.substring(path.lastIndexOf('.') + 1);
        // Move comments to parent option
        List<String> comments = new ArrayList<>(getParent().getPendingComments());
        String parentSection = path.substring(0, path.indexOf('.') == -1 ? path.length() : path.indexOf('.'));
        addComments(parentSection, comments.toArray(new String[]{}));
        comments.clear();
        // Then handle the comments for the actual option
        if (getParent().getComments().containsKey(fullPath)) {
            comments.add(getParent().getComments().get(fullPath));
        }
        // TODO - should probably be done using objects
        if (section != null) {
            comments.add("CONFIG_SECTION: " + section);
        }
        if (comment != null) {
            comments.add(comment);
        }
        getParent().getPendingComments().clear();
        if (comments.size() > 0) {
            StringBuilder builder = new StringBuilder();
            builder.append(comments.get(0));
            for (int i = 1; i < comments.size(); i++) {
                builder.append("\n\n").append(comments.get(i));
            }
            getParent().getComments().put(fullPath, builder.toString());
        }
        cmSection.defaults.put(key, defaultOption);
        cmSection.actualValues.put(key, cmSection.existingValues.getOrDefault(key, defaultOption));
    }

    public void addComment(@NotNull String comment) {
        getParent().getPendingComments().add(comment);
    }

    @Override
    public void moveTo(@NotNull String oldPath, @NotNull String newPath) {
        moveTo(oldPath, newPath, getParent());
    }

    @Override
    public void moveTo(@NotNull String oldPath, @NotNull String newPath, @NotNull ConfigFile otherFile) {
        if (!contains(oldPath)) return;
        CMMemorySection oldCmSection = getSectionInternal(oldPath);
        if (oldCmSection == null) return;
        CMMemorySection newCmSection = otherFile.getSectionInternal(newPath);
        if (newCmSection == null) newCmSection = otherFile.createSectionInternal(newPath);
        String oldKey = oldPath.substring(oldPath.lastIndexOf('.') + 1);
        Object movingValue = oldCmSection.existingValues.get(oldKey);
        String newKey = newPath.substring(newPath.lastIndexOf('.') + 1);
        newCmSection.actualValues.put(newKey, movingValue);
        oldCmSection.set(oldKey, null);
    }

    public void addComment(@NotNull String path, @NotNull String comment) {
        if (getParent().getComments().containsKey(path)) {
            String newComment = getParent().getComments().get(path) + "\n\n" + comment;
            getParent().getComments().put(getPathWithKey(path), newComment);
        } else {
            getParent().getComments().put(getPathWithKey(path), comment);
        }
    }

    public void addComments(@NotNull String path, @NotNull String... comments) {
        if (comments.length == 0) return;
        StringBuilder builder = new StringBuilder();
        builder.append(comments[0]);
        for (int i = 1; i < comments.length; i++) {
            builder.append("\n\n").append(comments[i]);
        }
        addComment(path, builder.toString());
    }

    public void addExample(@NotNull String path, Object object) {
        addExample(path, object, null);
    }

    public void addExample(@NotNull String path, Object object, String comment) {
        if (!getParent().isNew()) {
            CMMemorySection section = getSectionInternal(path);
            if (section == null) return;
            String key = path.substring(path.lastIndexOf('.') + 1);
            if (!section.existingValues.containsKey(key)) return;
        }
        getParent().getExamples().add(getPathWithKey(path));
        addDefault(path, object, null, comment);
    }

    public void makeSectionLenient(String path) {
        CMConfigSection section = (CMConfigSection) getSectionInternal(path + ".haha");
        if (section == null) section = createSectionInternal(path + ".haha");
        for (String key : section.existingValues.keySet()) {
            section.actualValues.put(key, section.existingValues.get(key));
        }
        if (getParent().getLenientSections().contains(getPathWithKey(path))) return;
        getParent().getLenientSections().add(getPathWithKey(path));
    }

    public void addSection(@NotNull String section) {
        // TODO - use objects
        getParent().getPendingComments().add("CONFIG_SECTION: " + section);
    }

    protected CMConfigSection createSectionInternal(@NotNull String path) {
        return createConfigSection(path.substring(0, path.lastIndexOf('.')));
    }

    public CMConfigSection createConfigSection(@NotNull String path) {
        String[] sections = path.split("\\.");
        CMConfigSection toEdit = this;
        for (int i = 0; i < sections.length; i++) {
            Object option = toEdit.actualValues.get(sections[i]);
            if (option == null) {
                option = new CMConfigSection(
                        toEdit.getPath().length() == 0 ? sections[i] : toEdit.getPath() + "." + sections[i],
                        toEdit.getParent());
                toEdit.actualValues.put(sections[i], option);
                toEdit = (CMConfigSection) option;
            } else if (option instanceof CMConfigSection) {
                toEdit = (CMConfigSection) option;
            } else {
                throw new IllegalStateException(path + " cannot be made into a configuration section due to already containing data!");
            }
        }
        return toEdit;
    }

    public Map<String, Object> convertToMap() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        for (String path : actualValues.keySet()) {
            if (actualValues.get(path) instanceof CMConfigSection) {
                map.put(path, ((CMConfigSection) actualValues.get(path)).convertToMap());
            } else {
                map.put(path, actualValues.get(path));
            }
        }
        return map;
    }

    public void mapToCM(Map<?, ?> map) {
        for (Object keyObj : map.keySet()) {
            String key = keyObj.toString();
            Object value = map.get(keyObj);
            if (value instanceof Map) {
                CMConfigSection section = new CMConfigSection(getPathWithKey(key), getParent());
                section.mapToCM((Map<?, ?>) value);
                existingValues.put(key, section);
            } else {
                existingValues.put(key, value);
            }
        }
    }

    private String getPathWithKey(String key) {
        if (getPath().isEmpty()) return key;
        return getPath() + "." + key;
    }

    protected void addDefaults(HashMap<String, Object> map) {
        for (String key : actualValues.keySet()) {
            if (actualValues.get(key) instanceof CMConfigSection) {
                ((CMConfigSection) actualValues.get(key)).addDefaults(map);
            } else {
                map.put(getPathWithKey(key), defaults.get(key));
            }
        }
    }
}
