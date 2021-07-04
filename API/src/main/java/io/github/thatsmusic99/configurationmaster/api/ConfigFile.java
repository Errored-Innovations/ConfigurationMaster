package io.github.thatsmusic99.configurationmaster.api;

import com.google.common.collect.Lists;
import io.github.thatsmusic99.configurationmaster.impl.CMConfigSection;
import org.bukkit.configuration.file.YamlConstructor;
import org.bukkit.configuration.file.YamlRepresenter;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class ConfigFile extends CMConfigSection {

    private final Yaml yaml;
    private final DumperOptions yamlOptions = new DumperOptions();
    private final LoaderOptions loaderOptions = new LoaderOptions();
    private final Representer yamlRepresenter = new YamlRepresenter();
    private final File file;
    private boolean isNew = false;
    private CommentWriter writer;
    protected List<String> pendingComments;
    protected HashMap<String, String> comments;
    protected List<String> examples;
    protected List<String> lenientSections;

    /**
     * Used to load a config file without safety precautions taken by the API.
     *
     * @param file The config file to be loaded.
     */
    public ConfigFile(@NotNull File file) {
        yaml = new Yaml(new YamlConstructor(), yamlRepresenter, yamlOptions, loaderOptions);
        yamlOptions.setIndent(2);
        yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        yamlRepresenter.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        this.file = file;
        writer = new CommentWriter(this);
        pendingComments = new ArrayList<>();
        comments = new HashMap<>();
        examples = new ArrayList<>();
        lenientSections = new ArrayList<>();
        loadWithExceptions();
    }

    public static ConfigFile loadConfig(File file) {
        try {
            return new ConfigFile(file);
        } catch (YAMLException e) {
            File tempFile = new File(file.getParentFile(),
                    file.getName().substring(0, file.getName().lastIndexOf("."))
                            + "-errored-" + System.currentTimeMillis() + ".yml");

            try {
                if (!tempFile.exists()) {
                    tempFile.createNewFile();
                }
                Files.copy(file.toPath(), tempFile.toPath());
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            System.out.println("YAMLException caught - there is a syntax error in the config.");
            e.printStackTrace();
            return new ConfigFile(tempFile);
        }
    }

    public void loadWithExceptions() {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        } catch (FileNotFoundException ex) {
            try {
                file.createNewFile();
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        StringBuilder content = new StringBuilder();
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (content.length() == 0) isNew = true;
        loadFromString(content.toString());
    }

    public void loadFromString(String str) {
        Map<?, ?> map = this.yaml.load(str);
        if (map != null) {
            mapToCM(map);
        }
    }

    public void save() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(saveToString());
        }
    }

    public void reload() throws IOException {
        HashMap<String, Object> allDefaults = new LinkedHashMap<>();
        addDefaults(allDefaults);
        existingValues.clear();
        actualValues.clear();
        loadWithExceptions();

        for (String path : allDefaults.keySet()) {
            if (!examples.contains(path) || contains(path)) {
                addDefault(path, allDefaults.get(path));
            }
        }

        for (String section : lenientSections) {
            makeSectionLenient(section);
        }

        save();
    }

    public boolean isNew() {
        return isNew;
    }

    public String saveToString() {
        String dump = this.yaml.dump(convertToMap());
        if (dump.equals("{}")) {
            dump = "";
        }
        writer.writeComments(Lists.newArrayList(dump.split("\n")));
        StringBuilder result = new StringBuilder();
        for (String line : writer.getLines()) {
            result.append(line).append("\n");
        }
        return result.toString();
    }

    public HashMap<String, String> getComments() {
        return comments;
    }

    public List<String> getPendingComments() {
        return pendingComments;
    }

    public List<String> getExamples() {
        return examples;
    }

    public List<String> getLenientSections() {
        return lenientSections;
    }
}
