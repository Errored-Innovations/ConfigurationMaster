package io.github.thatsmusic99.configurationmaster;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.util.*;

/**
 * CMFile is the specialised configuration file used by
 * ConfigurationMaster to carry out all of the required tasks
 * in creating a config file.<br><br>
 *
 * It can be initialised by extending the class or using the
 * provided constructors below.<br><br>
 *
 * You will be required to add all default values within the
 * {@link #loadDefaults()} method, which should be added anyways due
 * to it being an abstract method. This is so that all processes can be
 * kept in one place - failure to do this results in a NullPointerException,
 * since the plugin has not created the actual files yet, this rejects them.<br><br>
 *
 * Following initialisation, setters such as {@link #setTitle(String)}
 * and {@link #addLink(String, String)} can be called to toggle any
 * options provided by the class. Once this is all done, you need to
 * call {@link #load()}, which starts generating all the options and comments.<br><br>
 *
 * To reload the configuration file, call {@link #reload()} or {@link #load()}.
 * Both do the same thing, but {@link #reload()} is used for the sake of naming
 * conventions and to make it easier for others to use and understand.
 *
 * @author Holly (Thatsmusic99)
 */
public abstract class CMFile {

    // The actual configuration file.
    @Nullable
    private FileConfiguration config;
    // The temporary config file that is used to order nodes.
    private FileConfiguration tempConfig;
    // The file object for the config.
    private File configFile;
    // Comments to be written above the provided options.
    private HashMap<String, String> comments;
    // The currently written lines of the file.
    private List<String> currentLines;
    // If the file is newly generated or not.
    private boolean isNew;
    // Comments pending to be added.
    private List<String> pendingComments;
    // The plugin using the utility.
    private Plugin plugin;
    // The folder that the config is to be stored in.
    private File folder;
    // The name of the config file.
    private String name;
    // Pending values to be moved to this file.
    private HashMap<String, Object> toBeMoved;

    private int defaultTitleWidth;
    @Nullable
    private String title;
    @Nullable
    private String subtitle;
    private HashMap<String, String> externalLinks;
    @NotNull
    private String linkSeparator;
    @Nullable
    private String description;

    /**
     * Basic initialisation of the config file, using just the plugin and config name.<br>
     * This places the file inside the plugin's data folder.
     * 
     * @param plugin The plugin using the utility.
     * @param name The name of the config.
     */
    public CMFile(Plugin plugin, String name) {
        this(plugin, plugin.getDataFolder(), name);
    }

    /**
     * Initialisation of the config file, using the plugin, config name and folder.
     *
     * @param plugin The plugin using the utility.
     * @param folder The folder the configuration file is to be stored inside.
     * @param name The name of the config file.
     */
    public CMFile(Plugin plugin, File folder, String name) {
        this.plugin = plugin;
        this.folder = folder;
        this.name = name;

        config = null;
        toBeMoved = new HashMap<>();

        defaultTitleWidth = 75;
        title = "-<( " + plugin.getName() + " )>-";

        StringBuilder authors = new StringBuilder();
        List<String> authorsRaw = plugin.getDescription().getAuthors();
        for (int i = 0; i < authorsRaw.size(); i++) {
            String author = authorsRaw.get(i);
            authors.append(author);
            if (i < authorsRaw.size() - 2) {
                authors.append(", ");
            } else if (i == authorsRaw.size() - 2) {
                authors.append(" and ");
            }
        }
        subtitle = "Made by " + authors.toString();

        externalLinks = new HashMap<>();
        linkSeparator = " - ";
        description = plugin.getDescription().getDescription();
    }

    /**
     * Reloads the config file.
     * @see #load()
     */ 
    public void reload() {
        load();
    }

    /**
     * Prompts the config to initiate CM's loading process.
     *
     * The process is as follows:
     * <ul>
     *     <li>Create the config file - and required folder - if necessary.</li>
     *     <li>Load all existing options into the config to be read from.</li>
     *     <li>Load all the defaults.</li>
     *     <li>Moving all old options to their new ones.</li>
     *     <li>Saves defaults to the configuration file.</li>
     *     <li>Loads the config header.</li>
     *     <li>Writes all comments.</li>
     *     <li>Saves the final results.</li>
     *     <li>Anything required to happen post-save happens.</li>
     * </ul>
     *
     * @see #loadDefaults()
     * @see #moveTo(String, String)
     * @see #moveToNew()
     * @see #loadTitle()
     * @see #writeComments()
     * @see #postSave()
     */
    public void load() {
        // Creates the config file object
        configFile = new File(folder, name + ".yml");
        // If the folder doesn't exist, create it
        if (!folder.exists()) {
            folder.mkdirs();
        }
        // If it doesn't exist though, create it
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        // Try to load the current options from the config file
        try {
            config = new YamlConfiguration();
            config.load(configFile);
        } catch (Exception ex) {
            // Otherwise, rename it and warn the user
            plugin.getLogger().warning("Could not read " + name + ".yml:");
            plugin.getLogger().warning(ex.getMessage());
            plugin.getLogger().warning("The faulty configuration has been renamed to " + name + "-errored.yml.");
            configFile.renameTo(new File(folder, name + "-errored.yml"));
            plugin.getLogger().warning("Please use http://yaml-online-parser.appspot.com/ to correct the problems in the file.");
            plugin.getLogger().warning("If you are unsure on what to do, please contact the developers of this plugin.");
        }
        // If the config is empty, it's new
        isNew = config.saveToString().isEmpty();
        // Create a new empty configuration.
        tempConfig = new YamlConfiguration();
        currentLines = new ArrayList<>();
        comments = new HashMap<>();
        pendingComments = new ArrayList<>();

        // Get the plugin to load the default values of its config.
        loadDefaults();
        // Move any old values to their new counterparts.
        moveToNew();
        // Handle any extra values that may have been added by other files.
        handleReceivingValues();
        // Handle the saving procedure.
        initiateSave();
    }

    /**
     * Loads the title of the config file.<br><br>
     *
     * It handles the following elements in order:
     * <ul>
     *     <li>The title</li>
     *     <li>The subtitle</li>
     *     <li>The description</li>
     *     <li>Any external links</li>
     * </ul>
     */
    public void loadTitle() {
        List<String> title = new ArrayList<>();
        // Get the breaking line.
        StringBuilder breakingLineSB = new StringBuilder();
        for (int i = 0; i < getMaxTitleWidth() + 4; i++) {
            breakingLineSB.append("#");
        }
        String breakingLine = breakingLineSB.toString();

        // Get an empty line.
        StringBuilder emptyLineSB = new StringBuilder();
        emptyLineSB.append("# ");
        for (int i = 0; i < getMaxTitleWidth(); i++) {
            emptyLineSB.append(" ");
        }
        emptyLineSB.append(" #");
        String emptyLine = emptyLineSB.toString();
        //
        boolean requiresBreakingLine = true;
        if (getTitle() != null && getSubtitle() != null) {
            // Add the breaking line first.
            title.add(breakingLine);
            // Add the title and subtitle.
            title.addAll(formatStr(getTitle(), Pos.CENTER));
            title.addAll(formatStr(getSubtitle(), Pos.CENTER));
            title.add(emptyLine);
            title.add(breakingLine);
            requiresBreakingLine = false;
        }

        // Add the description and external links.
        if ((getDescription() != null && !getDescription().isEmpty()) || !getExternalLinks().isEmpty()) {
            if (requiresBreakingLine) {
                title.add(breakingLine);
            }
            title.addAll(formatStr(getDescription(), Pos.LEFT));
            title.add(emptyLine);
            for (String link : getExternalLinks().keySet()) {
                title.add(formatStr(link + getLinkSeparator() + getExternalLinks().get(link), Pos.LEFT).get(0));
            }
            title.add(breakingLine);
        }


        for (int i = 0; i < title.size(); i++) {
            currentLines.add(i, title.get(i));
        }
    }

    /** 
     * Used to align strings in a specific manner.
     *
     * Can also place them on separate lines.
     *
     * @param str The full string to be formatted.
     * @param position The alignment type to be used.
     * @return A list of broken up strings, all on their designated lines.
     */
    private List<String> formatStr(String str, Pos position) {
        // The list of lines resulting from the aligned/split string.
        List<String> lines = new ArrayList<>();
        // If the string is null, just return an empty list.
        if (str == null) return lines;
        // Split up the line into words.
        String[] words = str.split(" ");
        // Build up the sentence.
        StringBuilder sentence = new StringBuilder();
        // For each word in the line...
        for (String word : words) {
            // If the sentence is not empty...
            if (sentence.length() > 0) {
                // Add a space in front of the word.
                word = " " + word;
            }
            // If the word added onto the sentence causes an overflow though...
            if (sentence.length() + word.length() > getMaxTitleWidth()) {
                // Create the line.
                lines.add("# " + align(sentence, position) + " #");
                // Empty the sentence.
                sentence = new StringBuilder();
                // Add the word onto a new line.
                sentence.append(word.substring(1));
            } else {
                // Otherwise, add the word onto the sentence.
                sentence.append(word);
            }

        }

        // If, by the end of the list of words, there is still a sentence to be added...
        if (sentence.length() > 0) {
            // Add it.
            lines.add("# " + align(sentence, position) + " #");
        }
        // Return the title.
        return lines;
    }

    private String align(StringBuilder str, Pos position) {
        int remainder = getMaxTitleWidth() - str.length();
        switch (position) {
            case LEFT:
                for (int i = 0; i < remainder; i++) {
                    str.append(" ");
                }
                break;
            case CENTER:
                for (int i = 0; i < remainder / 2; i++) {
                    str.insert(0, " ");
                }
                if (remainder % 2 == 1) {
                    remainder++;
                }
                for (int i = 0; i < remainder / 2; i++) {
                    str.append(" ");
                }
                break;
            case RIGHT:
                for (int i = 0; i < remainder; i++) {
                    str.insert(0, " ");
                }
                break;
        }
        return str.toString();
    }


    /**
     * The maximum width the title can go up to.
     * If there are no external links, then 75 is returned by default.
     * However, if there are external links, the title is expanded to hold it.
     *
     * @return The number of characters wide the title can be.
     * @see #getDefaultTitleWidth() 
     * @see #setDefaultTitleWidth(int) 
     */
    public int getMaxTitleWidth() {
        // If there are no external links, return the default value.
        if (getExternalLinks().isEmpty()) {
            return getDefaultTitleWidth();
        } else {
            int maxWidth = getDefaultTitleWidth();
            // For each external link...
            for (String key : getExternalLinks().keySet()) {
                // The width of it is determined by the key length, the separator length and the link length.
                int currentWidth = key.length() + getLinkSeparator().length() + getExternalLinks().get(key).length();
                // If this is bigger than the current maximum width, update the maximum width.
                if (currentWidth > maxWidth) {
                    maxWidth = currentWidth;
                }
            }
            return maxWidth;
        }
    }

    /**
     * The title width that is set by default.<br>
     * This is the minimum of what the header can be; it can be altered by external links.
     * This is to stop the links being broken up and unusable.
     *
     * @return The number of characters wide the title can be by default. This is set to 75.
     * @see #setDefaultTitleWidth(int) 
     * @see #getMaxTitleWidth() 
     */
    public int getDefaultTitleWidth() {
        return defaultTitleWidth;
    }

    /**
     * The title used at the top of the config file.
     *
     * @return The title to be used. By default, it returns -&lt;( PLUGIN NAME )&gt;-
     * @see #setTitle(String) 
     */
    @Nullable
    public String getTitle() {
        return title;
    }

    /**
     * The characters used to separate external sources and their links.
     *
     * @return The characters that separate sources and links. Be default, it returns " - "
     * @see #setLinkSeparator(String) 
     */
    @NotNull
    public String getLinkSeparator() {
        return linkSeparator;
    }

    /**
     * The subtitle displayed under the plugin title.
     *
     * @return The subtitle to be used. By default, it returns "Made by XXX, YYY and ZZZ" where XXX, YYY and ZZZ are plugin developers.
     * The default value adjusts to the number of plugin developers listed.
     * @see #setSubtitle(String) 
     */
    @Nullable
    public String getSubtitle() {
        return subtitle;
    }

    /**
     * The description underneath the title and subtitle of the config.
     *
     * @return The description to be used. By default, it uses the plugin's description from the plugin.yml file.
     * @see #setDescription(String) 
     */
    @Nullable
    public String getDescription() {
        return description;
    }

    /**
     * A list of external sources with their links to them. 
     *
     * @return A hashmap of the sources and links. The key is the source name itself, 
     * such as "Github", and the value is the link, e.g. "https://github.com".
     * 
     * @see #addLink(String, String)
     * @see #removeLink(String)
     */
    public HashMap<String, String> getExternalLinks() {
        return externalLinks;
    }

    /**
     * Sets the default title width to a different minimum.
     *
     * @param defaultTitleWidth The minimum to be set to.
     * @throws IllegalArgumentException if the provided width is shorter than 20 characters.
     *
     * Having a width of at least 20 characters ensures that the header can be formatted correctly.
     * This limit may be removed - or reduced - once the justified alignment is introduced.
     *
     * @see #getDefaultTitleWidth()
     * @see #getMaxTitleWidth()
     */
    public void setDefaultTitleWidth(int defaultTitleWidth) {
        if (defaultTitleWidth < 20) {
            throw new IllegalArgumentException("Default title width cannot be shorter than 20 characters.");
        }
        this.defaultTitleWidth = defaultTitleWidth;
    }

    /**
     * Sets the description used in the configuration header.
     *
     * To get rid of the description, set it to null.
     *
     * @param description The new description to be used.
     * @see #getDescription()
     */
    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    /**
     * Sets the subtitle that sits underneath the title.
     *
     * To get rid of this, set it to null.
     *
     * @param subtitle The new subtitle to be used.
     * @see #getSubtitle()
     */
    public void setSubtitle(@Nullable String subtitle) {
        this.subtitle = subtitle;
    }

    /**
     * Sets the title of the config header.
     *
     * To get rid of it, set it to null.
     *
     * @param title The new title to be used.
     * @see #getTitle()
     */
    public void setTitle(@Nullable String title) {
        this.title = title;
    }

    /**
     * Sets the link separator in between the source name and its link.
     *
     * Cannot be null due to the risk of malformed links.
     *
     * @param linkSeparator The new link separator to be used.
     * @see #getLinkSeparator()
     */
    public void setLinkSeparator(@NotNull String linkSeparator) {
        this.linkSeparator = linkSeparator;
    }

    /**
     * Adds a new external link to be displayed in the configuration.
     *
     * @param name The name of the source to be used, such as Github.
     * @param link The actual link, such as https://github.com
     * @see #getExternalLinks()
     * @see #removeLink(String) 
     */
    public void addLink(@NotNull String name, @NotNull String link) {
        externalLinks.put(name, link);
    }

    /**
     * Removes an external link which may have been added beforehand.
     * 
     * @param name The name of the source to be removed.
     * @see #getExternalLinks() 
     * @see #addLink(String, String) 
     */
    public void removeLink(@NotNull String name) {
        externalLinks.remove(name);
    }

    /**
     * The method where all default values, comments and sections should be added inside.
     * 
     * @see #addComment(String) 
     * @see #addComment(String, String) 
     * @see #addDefault(String, Object) 
     * @see #addDefault(String, Object, String) 
     * @see #addDefault(String, Object, String, String) 
     * @see #addExample(String, Object) 
     * @see #addExample(String, Object, String) 
     * @see #addSection(String) 
     */
    public abstract void loadDefaults();

    /**
     * Adds a default value to a specified path.
     * If the path already exists, the value will not be used.
     *
     * @param path The path of the value to be set.
     * @param value The actual value itself.
     *
     * @see org.bukkit.configuration.MemoryConfiguration#addDefault(String, Object)
     * @throws NullPointerException if the config has not been initialised yet.
     */
    public void addDefault(@NotNull String path, Object value) {
        if (config == null) {
            throw new NullPointerException("Configuration is not loading yet, please use addDefault within the loadDefaults method.");
        }
        if (!pendingComments.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (String str : pendingComments) {
                builder.append(str).append("\n\n");
            }
            pendingComments.clear();
            comments.put(path, builder.toString());
        }
        config.addDefault(path, value);
        tempConfig.set(path, config.get(path));
    }

    /**
     * Adds an example option.<br><br>
     *
     * Functions the same as {@link #addDefault(String, Object)}, however
     * the default is only added if the config is brand new. The value will not
     * be used either if the path already exists.<br><br>
     *
     * It serves the purpose of providing a user with an example option to use
     * within a ConfigurationSection for example.
     *
     * @param path The path of the option to be set.
     * @param value The example value.
     * @see #addDefault(String, Object)
     */
    public void addExample(@NotNull String path, Object value) {
        if (isNew) {
            addDefault(path, value);
        }
    }

    /**
     * Adds an example option with a comment included.<br><br>
     *
     * Functions the same as {@link #addDefault(String, Object, String)}, however
     * the default and comment is only added if the config is brand new. The value
     * will not be used either if the path already exists.<br><br>
     *
     * It serves the purpose of providing a user with an example option to use
     * within a ConfigurationSection for example.<br><br>
     *
     * To make a comment multiple lines, use \n.
     *
     * @param path The path of the option to be set.
     * @param value The example value.
     * @param comment The comment to be included.
     * @see #addDefault(String, Object, String)
     */
    public void addExample(@NotNull String path, Object value, String comment) {
        if (isNew) {
            addDefault(path, value);
        }
        addComment(path, comment);
    }

    /**
     * Creates what is considered to be a "lenient" section.<br><br>
     *
     * Because ConfigurationMaster is very strict with what the user adds and
     * removes, a lenient section lets them add and remove what they like.<br><br>
     *
     * An example usage of this is in AdvancedTeleport when creating per-world
     * spawning and teleportation rules.
     *
     * @param path The path of the section.
     */
    public void addLenientSection(@NotNull String path) {
        if (getConfig().get(path) == null) {
            getConfig().createSection(path);
        }
        tempConfig.set(path, getConfig().get(path));
    }

    /**
     * Sets a specific value to a specified path.
     *
     * @param path The path of the option to be set.
     * @param value The actual value itself.
     *
     * @see org.bukkit.configuration.MemorySection#set(String, Object)
     * @throws NullPointerException if the config has not been initialised yet.
     */
    public void set(@NotNull String path, Object value) {
        if (config == null) {
            throw new NullPointerException("Configuration is not loading yet, please use set within the loadDefaults method.");
        }
        config.set(path, value);
        tempConfig.set(path, config.get(path));
    }

    /**
     * Adds a default value to a specified path, in addition to a comment and section.
     * If the path already exists, the value will not be used. The comment and section
     * are still added regardless.<br><br>
     *
     * To make a comment multiple lines, use \n.
     *
     * @param path The path of the option to be set.
     * @param value The actual value itself.
     * @param section The section that the option will be put under.
     * @param comment The comment that is placed above the option.
     *                
     * @see #addDefault(String, Object) 
     * @see #addSection(String) 
     * @see #addComment(String) 
     */
    public void addDefault(@NotNull String path, Object value, @NotNull String section, @NotNull String comment) {
        if (config == null) {
            throw new NullPointerException("Configuration is not loading yet, please use addDefault within the loadDefaults method.");
        }
        addSection(section);
        addComment(path, comment);
        addDefault(path, value);

    }

    /**
     * Adds a default value to a specified path, in addition to a comment.
     * If the path already exists, the value will not be used. The comment is still added
     * regardless.<br><br>
     *
     * To make a comment multiple lines, use \n.
     *
     * @param path The path of the option to be set.
     * @param value The actual value itself.
     * @param comment The comment that is placed above the option.
     *                
     * @see #addDefault(String, Object) 
     * @see #addComment(String) 
     */
    public void addDefault(@NotNull String path, Object value, @NotNull String comment) {
        if (config == null) {
            throw new NullPointerException("Configuration is not loading yet, please use addDefault within the loadDefaults method.");
        }
        addComment(path, comment);
        addDefault(path, value);
    }

    /**
     * Adds a comment to the configuration.<br><br>
     *
     * The comment is placed underneath the last default option to
     * be declared and above the next declared default option.
     * 
     * @param comment The comment to be added.
     */
    public void addComment(@NotNull String comment) {
        pendingComments.add(comment);
    }

    /**
     * Adds a comment to the configuration.<br><br>
     *
     * The path provided is the option that the comment will be
     * placed above.
     *
     * @param path The path that the comment will be under.
     * @param comment The comment itself.
     */
    public void addComment(@NotNull String path, @NotNull String comment) {
        StringBuilder builder = new StringBuilder();
        for (String str : pendingComments) {
            builder.append(str).append("\n\n");
        }
        pendingComments.clear();
        String parent = path.split("\\.")[0];
        if (comments.containsKey(parent)) {
            comments.put(parent, comments.get(parent) + builder.toString());
        } else {
            comments.put(parent, builder.toString());
        }
        builder = new StringBuilder();
        builder.append(comment);
        if (comments.containsKey(path)) {
            comments.put(path, comments.get(path) + builder.toString());
        } else {
            comments.put(path, builder.toString());
        }
    }

    /**
     * Adds a section to the configuration.
     *
     * @param section The name of the section to be added.
     */
    public void addSection(@NotNull String section) {
        pendingComments.add("CONFIG_SECTION: " + section);
    }

    /**
     * Returns an integer value specified at the given path. If an integer value is not found, the specified default value is returned.
     *
     * @param path The path to be used.
     * @param defaultValue The value to be returned if one is not found at the specified path.
     * @return The integer stored in path, returns defaultValue if not found.
     */
    public int getInteger(@NotNull String path, int defaultValue) {
        if (config == null) {
            throw new NullPointerException("Configuration is not loaded yet, please use this method after calling the load method.");
        }
        if (!config.contains(path)) return defaultValue;
        try {
            return Integer.parseInt(getString(path));
        } catch (NumberFormatException | NullPointerException ex) {
            return defaultValue;
        }
    }

    /**
     * Returns an integer value specified at the given path.
     *
     * @param path The path to be used.
     * @return The value stored in the path. If not found, it returns 0.
     */
    public int getInteger(@NotNull String path) {
        return getInteger(path, 0);
    }

    /**
     * Returns a double value specified at the given path. If a double value is not found, the specified default value is returned.
     *
     * @param path The path to be used.
     * @param defaultValue The value to be returned if one is not found at the specified path.
     * @return The double stored in path, returns defaultValue if not found.
     */
    public double getDouble(@NotNull String path, double defaultValue) {
        if (config == null) {
            throw new NullPointerException("Configuration is not loaded yet, please use this method after calling the load method.");
        }
        if (!config.contains(path)) return defaultValue;
        try {
            return Double.parseDouble(getString(path));
        } catch (NumberFormatException | NullPointerException ex) {
            return defaultValue;
        }
    }

    /**
     * Returns a double value specified at the given path.
     *
     * @param path The path to be used.
     * @return The value stored in the path. If not found, it returns 0.
     */
    public double getDouble(@NotNull String path) {
        return getDouble(path, 0);
    }

    /**
     * Returns a float value specified at the given path. If a float value is not found, the specified default value is returned.
     *
     * @param path The path to be used.
     * @param defaultValue The value to be returned if one is not found at the specified path.
     * @return The float stored in path, returns defaultValue if not found.
     */
    public float getFloat(@NotNull String path, float defaultValue) {
        if (config == null) {
            throw new NullPointerException("Configuration is not loaded yet, please use this method after calling the load method.");
        }
        if (!config.contains(path)) return defaultValue;
        try {
            return Float.parseFloat(getString(path));
        } catch (NumberFormatException | NullPointerException ex) {
            return defaultValue;
        }
    }

    /**
     * Returns a float value specified at the given path.
     *
     * @param path The path to be used.
     * @return The value stored in the path. If not found, it returns 0.
     */
    public float getFloat(@NotNull String path) {
        return getFloat(path, 0);
    }

    /**
     * Returns a string specified at the given path. If the path does not exist, the specified default value is returned.
     *
     * @param path The path to be used.
     * @param defaultValue The value to be returned if one is not found at the specified path.
     * @return The string stored in path, returns defaultValue if not found.
     */
    public String getString(@NotNull String path, @Nullable String defaultValue) {
        if (config == null) {
            throw new NullPointerException("Configuration is not loaded yet, please use this method after calling the load method.");
        }
        if (!config.contains(path)) return defaultValue;
        return config.getString(path);
    }

    /**
     * Returns a string value specified at the given path.
     *
     * @param path The path to be used.
     * @return The value stored in the path. If not found, it returns null.
     */
    @Nullable
    public String getString(@NotNull String path) {
        return getString(path, null);
    }

    /**
     * Returns an object specified at the given path. If the path does not exist, the specified default value is returned.
     *
     * @param path The path to be used.
     * @param defaultValue The value to be returned if one is not found at the specified path.
     * @return The object stored in path, returns defaultValue if not found.
     */
    public Object get(@NotNull String path, @Nullable Object defaultValue) {
        if (config == null) {
            throw new NullPointerException("Configuration is not loaded yet, please use this method after calling the load method.");
        }
        return config.get(path, defaultValue);
    }

    /**
     * Returns an object value specified at the given path.
     *
     * @param path The path to be used.
     * @return The value stored in the path. If not found, it returns null.
     */
    public Object get(@NotNull String path) {
        return get(path, null);
    }

    /**
     * Returns a long specified at the given path. If a long value is not found, the specified default value is returned.
     *
     * @param path The path to be used.
     * @param defaultValue The value to be returned if one is not found at the specified path.
     * @return The long stored in path, returns defaultValue if not found.
     */
    public long getLong(@NotNull String path, long defaultValue) {
        if (config == null) {
            throw new NullPointerException("Configuration is not loaded yet, please use this method after calling the load method.");
        }
        if (!config.contains(path)) return defaultValue;
        try {
            return Long.parseLong(getString(path));
        } catch (NumberFormatException | NullPointerException ex) {
            return defaultValue;
        }
    }

    /**
     * Returns a long value specified at the given path.
     *
     * @param path The path to be used.
     * @return The value stored in the path. If not found, it returns 0.
     */
    public long getLong(@NotNull String path) {
        return getLong(path, 0);
    }

    /**
     * Returns a byte specified at the given path. If a byte is not found, the specified default value is returned.
     *
     * @param path The path to be used.
     * @param defaultValue The value to be returned if one is not found at the specified path.
     * @return The byte stored in path, returns defaultValue if not found.
     */
    public byte getByte(@NotNull String path, byte defaultValue) {
        if (config == null) {
            throw new NullPointerException("Configuration is not loaded yet, please use this method after calling the load method.");
        }
        if (!config.contains(path)) return defaultValue;
        try {
            return Byte.parseByte(getString(path));
        } catch (NumberFormatException | NullPointerException ex) {
            return defaultValue;
        }
    }

    /**
     * Returns a byte value specified at the given path.
     *
     * @param path The path to be used.
     * @return The value stored in the path. If not found, it returns 0.
     */
    public byte getByte(@NotNull String path) {
        return getByte(path, (byte) 0);
    }

    /**
     * Returns a short specified at the given path. If a short is not found, the specified default value is returned.
     *
     * @param path The path to be used.
     * @param defaultValue The value to be returned if one is not found at the specified path.
     * @return The short stored in path, returns defaultValue if not found.
     */
    public short getShort(@NotNull String path, short defaultValue) {
        if (config == null) {
            throw new NullPointerException("Configuration is not loaded yet, please use this method after calling the load method.");
        }
        if (!config.contains(path)) return defaultValue;
        try {
            return Short.parseShort(getString(path));
        } catch (NumberFormatException | NullPointerException ex) {
            return defaultValue;
        }
    }

    /**
     * Returns a short value specified at the given path.
     *
     * @param path The path to be used.
     * @return The value stored in the path. If not found, it returns 0.
     */
    public short getShort(@NotNull String path) {
        return getShort(path, (short) 0);
    }

    /**
     * Returns a boolean specified at the given path. If a boolean is not found, the specified default value is returned.
     *
     * @param path The path to be used.
     * @param defaultValue The value to be returned if one is not found at the specified path.
     * @return The boolean stored in path, returns defaultValue if not found.
     */
    public boolean getBoolean(@NotNull String path, boolean defaultValue) {
        if (config == null) {
            throw new NullPointerException("Configuration is not loaded yet, please use this method after calling the load method.");
        }
        if (!config.contains(path)) return defaultValue;
        try {
            return Boolean.parseBoolean(getString(path));
        } catch (NumberFormatException | NullPointerException ex) {
            return defaultValue;
        }
    }

    /**
     * Returns a boolean value specified at the given path.
     *
     * @param path The path to be used.
     * @return The value stored in the path. If not found, it returns false.
     */
    public boolean getBoolean(@NotNull String path) {
        return getBoolean(path, false);
    }

    /**
     * Returns a list specified at the given path. If a list is not found, the specified default value is returned.
     *
     * @param path The path to be used.
     * @param defaultValue The value to be returned if one is not found at the specified path.
     * @return The list stored in path, returns defaultValue if not found.
     */
    public List<?> getList(@NotNull String path, List<?> defaultValue) {
        if (config == null) {
            throw new NullPointerException("Configuration is not loaded yet, please use this method after calling the load method.");
        }
        return config.getList(path, defaultValue);
    }

    /**
     * Returns a list specified at the given path.
     *
     * @param path The path to be used.
     * @return The list stored in the path. If not found, it returns a new empty ArrayList.
     */
    public List<?> getList(@NotNull String path) {
        return getList(path, new ArrayList<>());
    }

    /**
     * Returns a list of strings specified at the given path. If such a list is not found, the specified default value is returned.
     *
     * @param path The path to be used.
     * @param defaultValue The value to be returned if one is not found at the specified path.
     * @return The list stored in path, returns defaultValue if not found.
     */
    public List<String> getStringList(@NotNull String path, List<String> defaultValue) {
        if (config == null) {
            throw new NullPointerException("Configuration is not loaded yet, please use this method after calling the load method.");
        }
        if (!config.contains(path)) return defaultValue;
        if (!(config.get(path) instanceof List)) return defaultValue;
        return config.getStringList(path);
    }

    /**
     * Returns a list of strings specified at the given path.
     *
     * @param path The path to be used.
     * @return The value stored in the path. If not found, it returns a new empty ArrayList.
     */
    public List<String> getStringList(@NotNull String path) {
        return getStringList(path, new ArrayList<>());
    }

    /**
     * Anything that the plugin may want to do after finishing the loading
     * process.
     */
    public void postSave() {}

    /**
     * A method that can be used as an opportunity to move options to
     * new paths.
     * 
     * @see #moveTo(String, String) 
     */
    public void moveToNew() {}

    /**
     * Moves an option from an old path to a new one.
     *
     * Afterwards, the old path is removed.
     *
     * @param oldPath the currently existing path that needs to be moved.
     * @param newPath the new path that the option is to be moved to.
     */
    public void moveTo(@NotNull String oldPath, @NotNull String newPath) {
        if (config == null) {
            throw new NullPointerException("Configuration is not loading yet, please use moveTo within the moveToNew method.");
        }
        // If the option exists...
        if (config.contains(oldPath)) {
            Object object = config.get(oldPath);
            tempConfig.set(newPath, object);
            config.set(oldPath, null);
        }
    }

    /**
     * Moves an option from an old path to a new one in a different file.<br><br>
     *
     * Afterwards, the old path in the previous file (this one) is removed.
     *
     * @param oldPath the currently existing path that needs to be moved.
     * @param newPath the new path that the option is to be moved to.
     * @param newFile the new file that will contain the new path.
     */
    public void moveTo(@NotNull String oldPath, @NotNull String newPath, @NotNull CMFile newFile) {
        if (config == null) {
            throw new NullPointerException("Configuration is not loading yet, please use moveTo within the moveToNew method.");
        }
        if (config.contains(oldPath)) {
            Object object = config.get(oldPath);
            newFile.toBeMoved.put(newPath, object);
            config.set(oldPath, null);
        }
    }

    /**
     * Used to handle all transferring values.
     *
     * @see #moveTo(String, String)
     * @see #moveTo(String, String, CMFile)
     */
    public void handleReceivingValues() {
        for (String path : toBeMoved.keySet()) {
            tempConfig.set(path, toBeMoved.get(path));
        }
        toBeMoved.clear();
    }

    /**
     * Indicates whether the file is newly generated or not.
     *
     * @return true if the file is brand new.
     */
    public boolean isNew() {
        return isNew;
    }

    /**
     * The actual configuration file being processed.
     *
     * @return The config file as a FileConfiguration object.
     */
    @Nullable
    public FileConfiguration getConfig() {
        return config;
    }

    /**
     * Initiates the comment writing process.
     */
    public void writeComments() {
        // For each comment to be made...
        for (String path : comments.keySet()) {
            // Get all the divisions made in the config
            String[] divisions = path.split("\\.");

            writeComment(path, divisions, 0, 0);
        }

        // However, if there's any comments left, write them in.
        for (String str : pendingComments) {
            if (str.isEmpty()) {
                currentLines.add("");
            } else {
                currentLines.add("");
                String[] rawComment = str.split("\n");
                for (String commentPart : rawComment) {
                    if (commentPart.isEmpty()) {
                        currentLines.add("");
                    } else {
                        if (commentPart.startsWith("CONFIG_SECTION: ")) {
                            String section = commentPart.split(": ")[1];
                            StringBuilder length = new StringBuilder();
                            length.append("###");
                            for (int j = 0; j < section.length(); j++) {
                                length.append("#");
                            }
                            length.append("###");
                            currentLines.add(length.toString());
                            currentLines.add("#  " + section + "  #");
                            currentLines.add(length.toString());
                        } else {
                            currentLines.add("# " + commentPart);
                        }
                    }
                }
            }
        }
    }

    /**
     * Method used to write a specified comment.
     *
     * @param path The path the comment must be written at.
     * @param divisions The number of sections the part can be split up into.
     * @param iteration How far we're down the pathway (in terms of different sections).
     * @param startingLine The line we're starting from.
     */
    private void writeComment(String path, String[] divisions, int iteration, int startingLine) {
        StringBuilder indent = new StringBuilder();
        for (int j = 0; j < iteration; j++) {
            indent.append("  ");
        }
        // Go through each line in the file
        for (int i = startingLine; i < currentLines.size(); i++) {
            String line = currentLines.get(i);
            // If the line doesn't have an equal or larger indent, then the line could not be found.
            if (!line.startsWith(indent.toString())) return;
            // If it's already a comment, leave it be.
            if (line.startsWith("#")) continue;
            if (line.startsWith(indent.toString() + divisions[iteration] + ":") ||
                    line.startsWith(indent.toString() + "'" + divisions[iteration] + "':")) {
                iteration += 1;
                if (iteration == divisions.length) {
                    int currentLine = i;
                    if (iteration == 1) {
                        currentLines.add(currentLine, "");
                        currentLine++;
                    }
                    String[] rawComment = comments.get(path).split("\n");
                    for (String commentPart : rawComment) {
                        if (commentPart.isEmpty()) {
                            currentLines.add(currentLine, "");
                        } else {
                            if (commentPart.startsWith("CONFIG_SECTION: ")) {
                                String section = commentPart.split(": ")[1];
                                StringBuilder length = new StringBuilder();
                                length.append("###");
                                for (int j = 0; j < section.length(); j++) {
                                    length.append("#");
                                }
                                length.append("###");
                                currentLines.add(currentLine, length.toString());
                                currentLines.add(currentLine, "#  " + section + "  #");
                                currentLines.add(currentLine, length.toString());
                                currentLine += 3;
                                continue;
                            } else {
                                currentLines.add(currentLine, indent + "# " + commentPart);
                            }

                        }
                        currentLine++;
                    }
                    break;
                } else {
                    writeComment(path, divisions, iteration, i + 1);
                }
            }
        }
    }

    /**
     * Saves the changes or comments made to the configuration to the file.
     *
     * @param isConfigSaving true if saving default values, false if saving comments.
     */
    public void save(boolean isConfigSaving) {
        try {
            if (isConfigSaving) {
                tempConfig.save(configFile);
                BufferedReader reader = new BufferedReader(new FileReader(configFile));
                String currentLine;
                while ((currentLine = reader.readLine()) != null) {
                    if (currentLine.startsWith("#")) continue;
                    currentLines.add(currentLine);
                }
                reader.close();
            } else {
                // Opens up a new file writer
                FileWriter writer = new FileWriter(configFile);
                // For each line to write...
                for (String line : currentLines) {
                    // Write that and add in a break.
                    writer.write(line);
                    writer.write("\n");
                }
                // Close the writer.
                writer.close();
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Used to begin all saving procedures, such as saving options and writing comments.
     */
    public void initiateSave() {
        // Save the current default options.
        config.options().copyDefaults(true);
        save(true);
        // Load the config title.
        loadTitle();
        // Write all the comments.
        writeComments();
        // Save the new comments.
        save(false);
        // Load the new options into the config that may have been changed from
        try {
            config.load(configFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Do anything the plugin requires to do following saving of a config file.
        postSave();
    }

    public enum Pos {
        RIGHT,
        CENTER,
        LEFT
    }
}
