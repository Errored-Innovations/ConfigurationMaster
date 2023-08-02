package io.github.thatsmusic99.configurationmaster.api;

import io.github.thatsmusic99.configurationmaster.annotations.Example;
import io.github.thatsmusic99.configurationmaster.annotations.Option;
import io.github.thatsmusic99.configurationmaster.annotations.OptionHandler;
import io.github.thatsmusic99.configurationmaster.annotations.handlers.*;
import io.github.thatsmusic99.configurationmaster.api.comments.Comment;
import io.github.thatsmusic99.configurationmaster.impl.CMConfigSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Represents a specialised YAML file in ConfigurationMaster.<br><br>
 *
 * It can be initialised using the following methods:<br>
 *
 * <ol>
 *     <li>{@link ConfigFile#loadConfig(File)} - this loads a file with safety precautions.
 *     If the file contains a syntax error, the API will print an error,
 *     rename the file temporarily and load a new empty file.<br>
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

    @NotNull private static final HashMap<Class<?>, Class<? extends OptionHandler>> REGISTERED_HANDLERS = new HashMap<>();
    @NotNull private final Yaml yaml;
    @NotNull private final File file;
    @NotNull private final CommentWriter writer;
    @NotNull protected List<Comment> pendingComments;
    @NotNull protected HashMap<String, List<Comment>> comments;
    @NotNull protected HashSet<String> examples;
    @NotNull protected List<String> lenientSections;
    @NotNull protected Function<String, String> optionNameTranslator;
    @Nullable private Title title;
    private boolean isNew;
    protected boolean verbose;
    protected boolean reloading;
    protected static Logger logger = new CMLogger();

    static {
        REGISTERED_HANDLERS.put(boolean.class, BooleanOptionHandler.class);
        REGISTERED_HANDLERS.put(Boolean.class, BooleanOptionHandler.class);
        REGISTERED_HANDLERS.put(float.class, FloatOptionHandler.class);
        REGISTERED_HANDLERS.put(Float.class, FloatOptionHandler.class);
        REGISTERED_HANDLERS.put(int.class, IntegerOptionHandler.class);
        REGISTERED_HANDLERS.put(Integer.class, IntegerOptionHandler.class);
        REGISTERED_HANDLERS.put(long.class, LongOptionHandler.class);
        REGISTERED_HANDLERS.put(Long.class, LongOptionHandler.class);
        REGISTERED_HANDLERS.put(String.class, StringOptionHandler.class);
    }

    /**
     * Used to initialise a config file without safety precautions taken by the API.
     *
     * @param file The config file to be loaded.
     * @see ConfigFile#loadConfig(File)
     * @throws YAMLException if the file being loaded contains syntax errors.
     */
    public ConfigFile(@NotNull File file) throws IOException, IllegalAccessException {
        this(file, name -> name.replaceAll("_", "-").toLowerCase());
    }

    public ConfigFile(@NotNull File file, @NotNull Function<String, String> optionNameTranslator) throws IOException, IllegalAccessException {

        // Load the YAML configuration
        yaml = getYaml();

        // Set up the file itself
        this.file = file;
        this.optionNameTranslator = optionNameTranslator;
        this.isNew = false;
        this.reloading = false;
        this.title = null;

        // Set up internal variables
        writer = new CommentWriter(this);
        pendingComments = new ArrayList<>();
        comments = new HashMap<>();
        examples = new HashSet<>();
        lenientSections = new ArrayList<>();
    }

    /**
     * Used to load a config file with safety precautions taken by the API.<br>
     * This safety precaution checks for syntax errors in the YAML file.<br>
     * If there is one, it is renamed and an empty file is loaded.<br>
     *
     * @param file The file to be loaded.
     * @return the ConfigFile instance of the file or backup file.
     */
    public static ConfigFile loadConfig(File file) throws Exception {
        ConfigFile configFile = new ConfigFile(file);
        configFile.createFile();
        configFile.loadContent();
        return configFile;
    }

    public void load() throws Exception {

        // If the file doesn't already exist, create it
        createFile();

        // Read the file content
        loadContent();

        // Load the default options
        addDefaults();

        // Move everything to the new options
        moveToNew();

        // Then save!
        save();

        // Carry out any extra operations post-save
        postSave();
    }

    protected void loadFromString(String content) throws IOException {
        try {

            // Load everything
            Map<?, ?> map = this.yaml.load(content);
            if (map != null) {
                mapToCM(map);
            }

        } catch (YAMLException exception) {

            File tempFile = new File(file.getParentFile(),
                    file.getName().substring(0, file.getName().lastIndexOf("."))
                            + "-errored-" + System.currentTimeMillis() + ".yml");

            Files.copy(file.toPath(), tempFile.toPath());

            logger.severe("YAMLException caught - there is a syntax error in the config.");
            exception.printStackTrace();
        }
    }

    /**
     * Used to load options
     */
    public void addDefaults() throws Exception {

        handleAnnotations((field, option) -> {

            // Also get the field value and treat it as the default option
            final Object defaultOpt = field.get(this);

            // Get the option metadata
            final String name = option.path().isEmpty() ? optionNameTranslator.apply(field.getName()) : option.path();
            final String comment = option.comment().isEmpty() ? null : option.comment();
            final String section = option.section().isEmpty() ? null : option.section();
            final boolean lenient = option.lenient();
            Class<? extends OptionHandler> optionHandlerClass = option.optionHandler();
            if (optionHandlerClass == DefaultOptionHandler.class) {
                optionHandlerClass = REGISTERED_HANDLERS.getOrDefault(field.getType(), DefaultOptionHandler.class);
            }
            OptionHandler handler = optionHandlerClass.getConstructor().newInstance();

            // If there's examples to add, add them
            for (Example example : field.getAnnotationsByType(Example.class)) {
                addExample(name + "." + example.key(), example.value());
            }

            // If it's a lenient field, then make it lenient
            if (lenient) {
                if (comment != null) addComment(name, comment);
                makeSectionLenient(name);
            }

            // Add the default
            if (defaultOpt != null) handler.addDefault(this, name, defaultOpt, section, comment);

            // Set the result
            field.set(this, handler.get(this, name));
        });
    }

    public void loadContent() throws IOException {
        loadFromString(readFileContent());
    }

    public void moveToNew() {
    }

    public void postSave() {
    }

    /**
     * Saves all changes made to the config to the actual file.
     *
     * @throws IOException if something went wrong writing to the file.
     */
    public void save() throws Exception {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8))) {
            String saved = saveToString();
            writer.write(saved);
            isNew = false;
            this.existingValues.clear();
        }
    }

    protected String readFileContent() throws IOException {

        StringBuilder content = new StringBuilder();

        // Load the reader
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            if (content.length() == 0) {
                isNew = true;
            }
        }

        return content.toString();
    }

    /**
     * Reloads the configuration and updates all values stored inside it.<br><br>
     *
     * @throws IOException if something went wrong saving the file.
     */
    public void reload() throws Exception {
        debug("Reloading the configuration file " + file.getName() + "...");
        reloading = true;

        // Reset the defaults
        HashMap<String, Object> allDefaults = new LinkedHashMap<>();
        addDefaults(allDefaults);

        // Create the file
        createFile();

        // Reset internal values
        existingValues.clear();
        clear();

        // Load file content
        loadContent();

        // Add defaults
        for (String path : allDefaults.keySet()) {

            // Make sure it's not in a lenient section
            String parentPath = getParentPath(path);
            if (lenientSections.contains(parentPath)) {
                makeSectionLenient(parentPath);
                continue;
            }

            if (!examples.contains(path) || contains(path) || isNew) {
                addDefault(path, allDefaults.get(path));
            }
        }
        addDefaults();

        moveToNew();
        save();
        postSave();

        reloading = false;
    }

    /**
     * Returns whether the loaded file is brand new or not.<br>
     *
     * This is determined by whether a new file was created or if the file itself is empty.
     *
     * @return true if the file is newly loaded, false if not.
     */
    public boolean isNew() {
        return isNew;
    }

    public boolean isReloading() {
        return reloading;
    }

    protected void createFile() throws IOException {

        // If the file doesn't already exist, create it
        if (!file.exists()) {
            if (!file.createNewFile()) {
                throw new IOException("Failed to create " + file.getName() + "!");
            }

            // It's a new file
            isNew = true;
        }
    }

    public void updateAnnotations() throws Exception {

        handleAnnotations((field, option) -> {

            // Also get the field value and treat it as the default option
            final Object value = field.get(this);

            // Get the option metadata
            final String name = option.path().isEmpty() ? optionNameTranslator.apply(field.getName()) : option.path();

            // Update the values
            set(name, value);
        });
    }

    private void handleAnnotations(OptionConsumer<Field, Option> consumer) throws Exception {
        // Before doing anything else, check the fields
        Field[] fields = getClass().getFields();
        for (Field field : fields) {

            // Check if a field has the option annotation, add it
            if (!field.isAnnotationPresent(Option.class)) continue;
            final Option option = field.getAnnotation(Option.class);

            //
            consumer.accept(field, option);
        }
    }

    public String saveToString() throws Exception {

        // Update the annotations
        updateAnnotations();

        // Convert the map
        String dump = this.yaml.dump(convertToMap());
        if (dump.equals("{}")) dump = "";

        // Write the comments
        writer.writeComments(new ArrayList<>(Arrays.asList(dump.split("\n"))));

        // Write to the lines
        StringBuilder result = new StringBuilder();
        writer.getLines().forEach(line -> result.append(line).append("\n"));

        // Write the title and result
        return (title != null ? title + "\n" : "") + result;
    }

    public @NotNull HashMap<String, List<Comment>> getComments() {
        return comments;
    }

    /**
     * Get all comments that have yet to be added. These will be added when the next default/example is set.
     *
     * @return Comments waiting to be added.
     */
    public @NotNull List<Comment> getPendingComments() {
        return pendingComments;
    }

    public @NotNull HashSet<String> getExamples() {
        return examples;
    }

    public @NotNull List<String> getLenientSections() {
        return lenientSections;
    }

    public @Nullable Title getTitle() {
        return title;
    }

    public void setTitle(@Nullable Title title) {
        this.title = title;
    }

    public @NotNull File getFile() {
        return file;
    }

    /**
     * Enables the debugging mode to track what is happening within the API.
     *
     * @return The ConfigFile object with debugging being enabled.
     */
    public ConfigFile enableDebugging() {
        this.verbose = true;
        return this;
    }

    public void debug(String message) {
        if (verbose) logger.info(message);
    }

    private Yaml getYaml() {

        // Initialise dump options
        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        // Initialise representer
        Representer representerClone;
        try {
            representerClone = new Representer(options);
        } catch (NoSuchMethodError ex) {
            try {
                representerClone = Representer.class.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        representerClone.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        // Initialise YAML
        Yaml yaml;
        try {
            LoaderOptions loader = new LoaderOptions();
            loader.setCodePointLimit(1024 * 1024 * 100);
            yaml = new Yaml(new SafeConstructor(loader), representerClone, options, new LoaderOptions());
        } catch (Exception | NoSuchMethodError | NoClassDefFoundError ex) {
            // YOLO
            try {
                yaml = new Yaml(SafeConstructor.class.getConstructor().newInstance(), representerClone, options);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        return yaml;
    }

    private static class CMLogger extends Logger {

        // if you can't be 'em, join 'em
        protected CMLogger() {
            super("ConfigurationMaster", null);
        }
    }

    private interface OptionConsumer<T, R> {

        void accept(T t, R r) throws Exception;
    }
}
