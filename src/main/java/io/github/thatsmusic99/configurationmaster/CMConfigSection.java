package io.github.thatsmusic99.configurationmaster;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CMConfigSection extends CMConfigOption {

    protected LinkedHashMap<String, CMConfigOption> defaults;
    protected LinkedHashMap<String, CMConfigOption> existingValues;
    protected LinkedHashMap<String, CMConfigOption> actualValues;
    protected List<String> pendingComments;
    protected CMConfigSection parent;

    public CMConfigSection() {
        super("", null);
        if (this instanceof InternalConfigFile) {
            this.pendingComments = new ArrayList<>();
            this.parent = null;
        } else {
            throw new IllegalStateException("");
        }
    }

    public CMConfigSection(String path, CMConfigSection parent) {
        super(path, null);
        this.parent = parent;
    }

    @Override
    public Object get() {
        return this;
    }

    public void addDefault(String path, Object defaultOption) {
        CMConfigSection section = getConfigSection(path);
        CMConfigOption option = new CMConfigOption(path, defaultOption);
        section.defaults.put(path, option);
        section.actualValues.put(path, section.existingValues.getOrDefault(path, option));
    }

    public void addDefaults(CMConfigOption... options) {
        for (CMConfigOption option : options) {
            if (!pendingComments.isEmpty()) {
                option.addComments(pendingComments);
                pendingComments.clear();
            }
            String path = option.getPath();
            CMConfigSection section = getConfigSection(path);
            section.defaults.put(path, option);
            section.actualValues.put(path, section.existingValues.getOrDefault(path, option));
        }
    }

    public CMConfigSection createConfigSection(String path) {
        String[] sections = path.split("\\.");
        CMConfigSection toEdit = this;
        for (int i = 1; i < sections.length; i++) {
            Object option = toEdit.existingValues.get(sections[i]);
            if (option == null) {
                option = new CMConfigSection(toEdit);

            }
        }
    }

    public CMConfigSection getConfigSection(String path) {
        String[] sections = path.split("\\.");
        CMConfigSection toEdit = this;
        for (int i = 1; i < sections.length; i++) {
            Object option = toEdit.existingValues.get(sections[i]);
            if (option == null) {
                option =
            }
        }
    }

    public Map<String, Object> convertToMap() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        for (String path : actualValues.keySet()) {
            if (actualValues.get(path) instanceof CMConfigSection) {
                map.put(path, ((CMConfigSection) actualValues.get(path)).convertToMap());
            } else {
                map.put(path, actualValues.get(path).get());
            }
        }
        return map;
    }
}
