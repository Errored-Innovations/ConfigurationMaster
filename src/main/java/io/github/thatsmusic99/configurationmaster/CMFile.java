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
 * in creating a config file.
 *
 * It can be initialised by extending the class or using the
 * provided constructors below.
 *
 * You will be required to add all default values within the
 * {@link #loadDefaults()} method, which should be added anyways due
 * to it being an abstract method. This is so that all processes can be
 * kept in one place - failure to do this results in a NullPointerException,
 * since the plugin has not created the actual files yet, this rejects them.
 *
 * Following initialisation, setters such as {@link #setTitle(String)}
 * and {@link #addLink(String, String)} can be called to toggle any
 * options provided by the class. Once this is all done, you need to
 * call {@link #load()}, which starts generating all the options and comments.
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
     * Basic initialisation of the config file.
     * This places the file inside the plugin's data folder.
     * 
     * @param plugin The plugin using the utility.
     * @param name The name of the config.
     */
    public CMFile(Plugin plugin, String name) {
        this(plugin, plugin.getDataFolder(), name);
    }

    /**
     * Initialisation of the config file.
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
     * - Create the config file - and required folder - if necessary.
     * - Load all existing options into the config to be read from.
     * - Load all the defaults.
     * @see #loadDefaults()
     * - Moving all old options to their new ones.
     * @see #moveTo(String, String)
     * @see #moveToNew()
     * - Saves defaults to the configuration file.
     * - Anything required to happen post-save happens.
     * @see #postSave()
     * - Loads the config header.
     * @see #loadTitle()
     * - Writes all comments.
     * @see #writeComments()
     * - Saves the final results.
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
        // Save the current default options.
        config.options().copyDefaults(true);
        save(true);
        // Do anything the plugin requires to do following saving of a config file.
        postSave();
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

    }

    /**
     * Loads the title of the config file.
     * Can be overridden.
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
        // Add the breaking line first.
        title.add(breakingLine);
        // Add the title and subtitle.
        title.addAll(formatStr(getTitle(), Pos.CENTER));
        title.addAll(formatStr(getSubtitle(), Pos.CENTER));
        title.add(emptyLine);
        title.add(breakingLine);
        // Add the description and external links.
        if ((getDescription() != null && !getDescription().isEmpty()) || !getExternalLinks().isEmpty()) {
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
     * The title width that is set by default.
     * This is the minimum of what the header can be; it can be altered by external links.
     * This is to stop the links being broken up and unusable.
     * If you don't want links overriding
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
     * @return The title to be used. By default, it returns -<( PLUGIN NAME )>-
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
     * @see #getLinkSeparator()
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
     * Adds an example option.
     *
     * Functions the same as {@link #addDefault(String, Object)}, however
     * the default is only added if the config is brand new. The value will not
     * be used either if the path already exists.
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
     * Adds an example option with a comment included.
     *
     * Functions the same as {@link #addDefault(String, Object, String)}, however
     * the default and comment is only added if the config is brand new. The value
     * will not be used either if the path already exists.
     *
     * It serves the purpose of providing a user with an example option to use
     * within a ConfigurationSection for example.
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
            addComment(path, comment);
        }
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
     * are still added regardless.
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
        addDefault(path, value);
        addSection(section);
        addComment(path, comment);
    }

    /**
     * Adds a default value to a specified path, in addition to a comment.
     * If the path already exists, the value will not be used. The comment is still added
     * regardless.
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
        addDefault(path, value);
        addComment(path, comment);
    }

    /**
     * Adds a comment to the configuration.
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
     * Adds a comment to the configuration.
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
        builder.append(comment);
        comments.put(path, builder.toString());
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
     * @param newPath the new path to be moved to.
     */
    public void moveTo(@NotNull String oldPath, @NotNull String newPath) {
        if (config.contains(oldPath)) {
            Object object = config.get(oldPath);
            tempConfig.set(newPath, object);
            config.set(oldPath, null);
        }
    }

    /**
     * The actual configuration file being processed.
     *
     * @return The config file as a FileConfiguration object.
     */
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
                if (str.startsWith("CONFIG_SECTION: ")) {
                    String section = str.split(": ")[1];
                    StringBuilder length = new StringBuilder();
                    length.append("###");
                    for (int j = 0; j < section.length(); j++) {
                        length.append("#");
                    }
                    length.append("###");
                    currentLines.add(length.toString());
                    currentLines.add("#  " + section + "  #");
                    currentLines.add(length.toString());
                    currentLines.add("");
                } else {
                    currentLines.add("# " + str);
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

    public enum Pos {
        RIGHT,
        CENTER,
        LEFT
    }
}
