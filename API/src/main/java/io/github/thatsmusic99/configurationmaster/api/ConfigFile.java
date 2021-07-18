package io.github.thatsmusic99.configurationmaster.api;

import io.github.thatsmusic99.configurationmaster.impl.CMConfigSection;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/**
 * Represents a specialised YAML file in ConfigurationMaster.<br><br>
 *
 * It can be initialised using the following methods:<br>
 *
 * <ol>
 *     <li>{@link ConfigFile#loadConfig(File)} - this loads a file with safety precautions.
 *     If the file contains a syntax error, the API will print an error,
 *     rename the file temporarily and load a new empty file.
 *
 *     It is recommended to use this if you want your users to
 *     not lose their progress on a config file if they make a
 *     single mistake.
 *     </li>
 *
 *     <li>{@link io.github.thatsmusic99.configurationmaster.api.ConfigFile#ConfigFile(File)}
 *     - this loads a file without the safety precautions taken above.
 *     This is recommended if you want to handle YAMLExceptions.</li>
 *
 *     <li>Simply extend the class. This will not take any safety precautions, similarly to using the constructor.</li>
 * </ol>
 *
 */
public class ConfigFile extends CMConfigSection {

    private final Yaml yaml;
    private final DumperOptions yamlOptions = new DumperOptions();
    private final LoaderOptions loaderOptions = new LoaderOptions();
    private final Representer yamlRepresenter = new Representer();
    private final File file;
    private boolean isNew = false;
    private final CommentWriter writer;
    private Title title = null;
    protected List<String> pendingComments;
    protected HashMap<String, String> comments;
    protected HashSet<String> examples;
    protected List<String> lenientSections;

    /**
     * Used to load a config file without safety precautions taken by the API.
     *
     * @param file The config file to be loaded.
     * @see ConfigFile#loadConfig(File)
     * @throws YAMLException if the file being loaded contains syntax errors.
     */
    public ConfigFile(@NotNull File file) {
        yaml = new Yaml(new SafeConstructor(), yamlRepresenter, yamlOptions, loaderOptions);
        yamlOptions.setIndent(2);
        yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        yamlRepresenter.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        this.file = file;
        writer = new CommentWriter(this);
        pendingComments = new ArrayList<>();
        comments = new HashMap<>();
        examples = new HashSet<>();
        lenientSections = new ArrayList<>();
        loadWithExceptions();
    }

    /**
     * Used to load a config file with safety precautions taken by the API.<br>
     * This safety precaution checks for syntax errors in the YAML file.<br>
     * If there is one, it is renamed and an empty file is loaded.<br>
     *
     * @param file The file to be loaded.
     * @return the ConfigFile instance of the file or backup file.
     */
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

    private void loadWithExceptions() {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
        } catch (FileNotFoundException ex) {
            try {
                file.createNewFile();
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
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

    private void loadFromString(String str) {
        Map<?, ?> map = this.yaml.load(str);
        if (map != null) {
            mapToCM(map);
        }
    }

    /**
     * Saves all changes made to the config to the actual file.
     *
     * @throws IOException if something went wrong writing to the file.
     */
    public void save() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.write(saveToString());
        }
    }

    /**
     * Reloads the configuration and updates all values stored inside it.<br><br>
     *
     * @throws IOException if something went wrong saving the file.
     */
    public void reload() throws IOException {
        HashMap<String, Object> allDefaults = new LinkedHashMap<>();
        addDefaults(allDefaults);
        existingValues.clear();
        actualValues.clear();
        loadWithExceptions();

        for (String path : allDefaults.keySet()) {
            if (!examples.contains(path) || containsExisting(path)) {
                addDefault(path, allDefaults.get(path));
            }
        }

        for (String section : lenientSections) {
            makeSectionLenient(section);
        }

        save();
    }

    /**
     * Returns whether the loaded file is brand new or not.
     *
     * This is determined by whether a new file was created or if the file itself is empty.
     *
     * @return true if the file is newly loaded, false if not.
     */
    public boolean isNew() {
        return isNew;
    }

    private String saveToString() {
        String dump = this.yaml.dump(convertToMap());
        if (dump.equals("{}")) {
            dump = "";
        }
        writer.writeComments(new ArrayList<>(Arrays.asList(dump.split("\n"))));
        StringBuilder result = new StringBuilder();
        for (String line : writer.getLines()) {
            result.append(line).append("\n");
        }
        return (title != null ? title + "\n" : "") + result;
    }

    public HashMap<String, String> getComments() {
        return comments;
    }

    /**
     * Get all comments that have yet to be added. These will be added when the next default/example is set.
     *
     * @return Comments waiting to be added.
     */
    public List<String> getPendingComments() {
        return pendingComments;
    }

    public HashSet<String> getExamples() {
        return examples;
    }

    public List<String> getLenientSections() {
        return lenientSections;
    }

    public Title getTitle() {
        return title;
    }

    public void setTitle(Title title) {
        this.title = title;
    }
}
