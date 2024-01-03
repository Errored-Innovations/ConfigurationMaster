package io.github.thatsmusic99.configurationmaster.impl;

import io.github.thatsmusic99.configurationmaster.api.ConfigFile;
import io.github.thatsmusic99.configurationmaster.api.ConfigSection;
import io.github.thatsmusic99.configurationmaster.api.comments.Comment;
import io.github.thatsmusic99.configurationmaster.api.comments.Section;
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

    public void addDefault(@NotNull String path, @Nullable Object defaultOption) {
        addDefault(path, defaultOption, null, null);
    }

    public void addDefault(@NotNull String path, @Nullable Object defaultOption, @Nullable String comment) {
        addDefault(path, defaultOption, null, comment);
    }

    public void addDefault(@NotNull String path, @Nullable Object defaultOption, @Nullable String section, @Nullable String comment) {

        // Null checks
        Objects.requireNonNull(path, "The path cannot be null!");

        // Get the full path of the key in question
        String fullPath = getPathWithKey(path);

        // Get the section to be created - if it's null, create it
        CMMemorySection cmSection = getSectionInternal(path);
        if (cmSection == null) cmSection = createSectionInternal(path);
        String key = getKey(path);

        if (!getParent().isReloading()) {

            addPendingCommentsToPath(path);

            final List<Comment> comments = new ArrayList<>();

            // Then handle the comments for the actual option
            if (getParent().getComments().containsKey(fullPath)) comments.addAll(getParent().getComments().get(fullPath));

            // Add the section
            if (section != null) comments.add(new Section(section));

            // Add the comment
            if (comment != null) comments.add(new Comment(comment));

            // Clear any pending comments
            getParent().getPendingComments().clear();

            // If there's comments to add,
            if (comments.size() > 0) getParent().getComments().put(fullPath, comments);
        }

        // Add the defaults
        cmSection.defaults.put(key, defaultOption);
        cmSection.put(key, cmSection.existingValues.getOrDefault(key, defaultOption));
    }

    @Override
    public void addComment(@NotNull String comment) {
        if (getParent().isReloading()) return;
        getParent().getPendingComments().add(new Comment(comment));
    }

    @Override
    public void moveTo(@NotNull String oldPath, @NotNull String newPath) {
        moveTo(oldPath, newPath, getParent());
    }

    @Override
    public void moveTo(@NotNull String oldPath, @NotNull String newPath, @NotNull ConfigFile otherFile) {
        Objects.requireNonNull(oldPath, "The old path cannot be null!");
        Objects.requireNonNull(newPath, "The new path cannot be null!");
        Objects.requireNonNull(otherFile, "The file being transferred to cannot be null!");

        if (!contains(oldPath)) return;
        CMMemorySection oldCmSection = getSectionInternal(oldPath, false);
        if (oldCmSection == null) return;
        CMMemorySection newCmSection = otherFile.getSectionInternal(newPath);
        if (newCmSection == null) newCmSection = otherFile.createSectionInternal(newPath);
        String oldKey = oldPath.substring(oldPath.lastIndexOf('.') + 1);
        Object movingValue = oldCmSection.existingValues.get(oldKey);
        String newKey = newPath.substring(newPath.lastIndexOf('.') + 1);
        newCmSection.put(newKey, movingValue);
        oldCmSection.set(oldKey, null);
    }

    @Override
    public void addComment(@NotNull String path, @NotNull String comment) {
        addComment(path, comment, true);
    }

    private void addComment(final @NotNull String path, final @NotNull String comment, final boolean addPending) {
        Objects.requireNonNull(path, "The path cannot be null!");
        Objects.requireNonNull(comment, "The comment cannot be null!");
        if (getParent().isReloading()) return;
        if (addPending) addPendingCommentsToPath(path);

        // If a specified path already has comments, add this one onto the existing comment, otherwise just add it
        if (getParent().getComments().containsKey(path)) {
            getParent().getComments().get(path).add(new Comment(comment));
        } else {
            getParent().getComments().put(getPathWithKey(path), new ArrayList<>(Collections.singletonList(new Comment(comment))));
        }
    }

    @Override
    public void addComments(@NotNull String path, @NotNull String... comments) {
        Objects.requireNonNull(path, "The path cannot be null!");
        Objects.requireNonNull(comments, "The comments array cannot be null!");

        for (String comment : comments) addComment(path, comment);
    }

    @Override
    public void addExample(@NotNull String path, Object object) {
        addExample(path, object, null);
    }

    @Override
    public void addExample(@NotNull String path, Object object, @Nullable String comment) {
        Objects.requireNonNull(path, "The path cannot be null!");

        // Check if any of the sections are lenient
        String[] sections = path.split("\\.");
        List<String> visited = new ArrayList<>();
        for (String section : sections) {
            visited.add(section);
            String parentPath = String.join(".", visited);

            // If it's lenient, check if it exists
            if (getParent().getLenientSections().contains(parentPath)) {
                CMConfigSection parentSection = (CMConfigSection) getSectionInternal(parentPath, false);
                if (parentSection == null || parentSection.existingValues.get(section) == null) {
                    forceExample(path, object, comment);
                    return;
                }
            }
        }

        getParent().getExamples().add(getPathWithKey(path));
    }

    @Override
    public void createExampleSection(@NotNull String path) {
        Objects.requireNonNull(path, "The path cannot be null!");

        getParent().getExamples().add(getPathWithKey(path));
        // See if the base section exists - if not, force it
        CMMemorySection section = getSectionInternal(path);
        if (section == null) createConfigSection(path);
    }

    @Override
    public void forceExample(@NotNull String path, @Nullable Object value) {
        forceExample(path, value, null);
    }

    @Override
    public void forceExample(@NotNull String path, @Nullable Object value, @Nullable String comment) {
        Objects.requireNonNull(path, "The path cannot be null!");
        getParent().getExamples().add(getPathWithKey(path));
        addDefault(path, value, null, comment);
    }

    @Override
    public void makeSectionLenient(@NotNull String path) {
        // TODO - allow null/empty path to signify making the whole ass file lenient
        Objects.requireNonNull(path, "The path cannot be null!");
        // TODO - don't use internals here
        CMConfigSection section = (CMConfigSection) getSectionInternal(path + ".haha");
        if (section == null) section = createSectionInternal(path + ".haha");
        section.forceExistingIntoActual();
        if (getParent().getLenientSections().contains(getPathWithKey(path))) return;
        getParent().getLenientSections().add(getPathWithKey(path));

        // Add it as a default option to the parent section
        CMConfigSection parent = (CMConfigSection) getSectionInternal(path);
        String key = getKey(path);
        if (parent != null) parent.defaults.put(key, new CMConfigSection(parent.getPathWithKey(key), getParent()));

        // Check for any pending comments
        if (getParent().isReloading()) return;
        String fullPath = getPathWithKey(path);

        addPendingCommentsToPath(path);

        final List<Comment> comments = new ArrayList<>();

        // Then handle the comments for the actual option
        if (getParent().getComments().containsKey(fullPath)) comments.addAll(getParent().getComments().get(fullPath));

        // Clear any pending comments
        getParent().getPendingComments().clear();

        // If there's comments to add,
        if (comments.size() > 0) getParent().getComments().put(fullPath, comments);
    }

    private void forceExistingIntoActual() {
        if (!getParent().isNew()) {
            clear();
        }
        for (String key : existingValues.keySet()) {
            if (existingValues.get(key) instanceof CMConfigSection) {
                ((CMConfigSection) existingValues.get(key)).forceExistingIntoActual();
            }
            put(key, existingValues.get(key));
        }
    }

    @Override
    public void addSection(@NotNull String section) {
        if (getParent().isReloading()) return;
        getParent().getPendingComments().add(new Section(section));
    }

    @Override
    public void addSection(@NotNull String path, @NotNull String section) {
        addSection(path, section, true);
    }

    private void addSection(final @NotNull String path, final @NotNull String section, final boolean addPending) {
        Objects.requireNonNull(path, "The path cannot be null!");
        Objects.requireNonNull(section, "The section cannot be null!");

        if (getParent().isReloading()) return;
        if (addPending) addPendingCommentsToPath(path);
        // If a specified path already has comments, add this one onto the existing comment, otherwise just add it
        if (getParent().getComments().containsKey(path)) {
            getParent().getComments().get(path).add(new Section(section));
        } else {
            getParent().getComments().put(getPathWithKey(path), new ArrayList<>(Collections.singletonList(new Section(section))));
        }
    }

    protected CMConfigSection createSectionInternal(@NotNull String path) {
        return createConfigSection(path.substring(0, path.lastIndexOf('.')));
    }

    @Override
    public CMConfigSection createConfigSection(@NotNull String path) {
        Objects.requireNonNull(path, "The path must not be null!");
        String[] sections = path.split("\\.");
        CMConfigSection toEdit = this;
        for (String section : sections) {
            Object option = toEdit.get(section);
            if (option == null) {
                option = new CMConfigSection(
                        toEdit.getPath().length() == 0 ? section : toEdit.getPath() + "." + section,
                        toEdit.getParent());
                toEdit.put(section, option);
                toEdit = (CMConfigSection) option;
            } else if (option instanceof CMConfigSection) {
                toEdit = (CMConfigSection) option;
            } else {
                throw new IllegalStateException(path + " cannot be made into a configuration section due to already containing data!");
            }
        }
        return toEdit;
    }

    protected Map<String, Object> convertToMap() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        for (String path : keySet()) {
            if (get(path) instanceof CMConfigSection) {
                map.put(path, ((CMConfigSection) get(path)).convertToMap());
            } else {
                map.put(path, get(path));
            }
        }
        return map;
    }

    protected void mapToCM(Map<?, ?> map) {
        for (Object keyObj : map.keySet()) {
            if (keyObj == null) keyObj = "null";
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

    protected String getParentPath(String path) {
        int finalIndex = path.lastIndexOf('.');
        if (finalIndex != -1) {
            return path.substring(0, finalIndex);
        }
        return null;
    }

    private String getPathWithKey(String key) {
        if (getPath().isEmpty()) return key;
        return getPath() + "." + key;
    }

    protected void addDefaults(HashMap<String, Object> map) {
        for (String key : keySet()) {
            if (get(key) instanceof CMConfigSection) {
                ((CMConfigSection) get(key)).addDefaults(map);
            } else {
                map.put(getPathWithKey(key), defaults.get(key));
            }
        }

        // If the section is empty but lenient, keep it
        if (getParent().getLenientSections().contains(getPath()) && keySet().size() == 0) {
            map.put(getPath(), this);
        }
    }

    private void addPendingCommentsToPath(final @NotNull String path) {

        // Get the root path for pending commands
        final int index = path.indexOf('.');
        final String root = path.substring(0, index == -1 ? path.length() : index);

        for (Comment pendingComment : getParent().getPendingComments()) {
            if (pendingComment instanceof Section) {
                addSection(root, pendingComment.getComment(), false);
            } else {
                addComment(root, pendingComment.getComment(), false);
            }
        }
        getParent().getPendingComments().clear();
    }
}
