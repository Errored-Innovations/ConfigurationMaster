package io.github.thatsmusic99.configurationmaster;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.util.*;

/**
 * ConfigurationMaster
 *
 * This makes it easier to update and manage configurations.
 * It includes commenting and a title, all updated dynamically.
 * 
 * @author Thatsmusic99 (Holly)
 */
public abstract class CMFile {

    // The actual configuration file.
    private FileConfiguration config;
    // The temporary config file that is used to order nodes.
    private FileConfiguration tempConfig;
    // The file object for the config.
    private File configFile;
    // Comments to be written above the provided options.
    private HashMap<String, String> comments;
    // The currently written lines of the file.
    private List<String> currentLines;
    // TODO: Remove
    @Deprecated
    private HashMap<String, String> sections;
    // TODO: Maybe remove
    @Deprecated
    private List<String> nodeOrder;
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
    }

    /**
     * Reloads the config file.
     */ 
    public void reload() {
        load();
    }

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
        nodeOrder = new ArrayList<>();
        sections = new HashMap<>();
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
     * Can be overriden.
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
        if (getDescription() != null || !getExternalLinks().isEmpty()) {
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
     * Can also place them on separate lines.
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
     */
    public int getMaxTitleWidth() {
        // If there are no external links, return 75.
        if (getExternalLinks().isEmpty()) {
            return 75;
        } else {
            int maxWidth = 75;
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
     * The title used at the top of the config file.
     */
    public String getTitle() {
        return "-<( " + plugin.getName() + " )>-";
    }

    public String getLinkSeparator() {
        return " - ";
    }

    public String getSubtitle() {
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
        return "Made by " + authors.toString();
    }

    public String getDescription() {
        return plugin.getDescription().getDescription();
    }

    public HashMap<String, String> getExternalLinks() {
        return new HashMap<>();
    }

    public abstract void loadDefaults();

    public void addDefault(String path, Object value) {
        config.addDefault(path, value);
        tempConfig.set(path, config.get(path));
        nodeOrder.add(path);
    }

    public void addExample(String path, Object value) {
        if (isNew) {
            addDefault(path, value);
        }
    }

    public void addExample(String path, Object value, String comment) {
        if (isNew) {
            addDefault(path, value);
            addComment(path, comment);
        }
    }

    public void set(String path, Object value) {
        config.set(path, value);
        tempConfig.set(path, config.get(path));
    }

    public void addDefault(String path, Object value, String section, String comment) {
        addDefault(path, value);
        addSection(section);
        addComment(path, comment);
    }

    public void addDefault(String path, Object value, String comment) {
        addDefault(path, value);
        addComment(path, comment);
    }

    public void addComment(String comment) {
        pendingComments.add(comment);
    }

    public void addComment(String path, String comment) {
        StringBuilder builder = new StringBuilder();
        for (String str : pendingComments) {
            builder.append(str).append("\n\n");
        }
        pendingComments.clear();
        builder.append(comment);
        comments.put(path, builder.toString());
    }

    public void addSection(String beforePath, String section) {
        sections.put(beforePath, section);
    }

    public void addSection(String section) {
        pendingComments.add("CONFIG_SECTION: " + section);
    }

    public void postSave() {}

    public void moveToNew() {}

    public void moveTo(String oldPath, String newPath) {
        if (config.contains(oldPath)) {
            Object object = config.get(oldPath);
            tempConfig.set(newPath, object);
            config.set(oldPath, null);
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void writeComments() {
        // For each comment to be made...
        for (String path : comments.keySet()) {
            // Get all the divisions made in the config
            String[] divisions = path.split("\\.");

            writeComment(path, divisions, 0, 0);
        }

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

    private void writeComment(String path, String[] divisions, int iteration, int startingLine) {
        StringBuilder indent = new StringBuilder();
        for (int j = 0; j < iteration; j++) {
            indent.append("  ");
        }
        // Go through each line in the file
        for (int i = startingLine; i < currentLines.size(); i++) {
            String line = currentLines.get(i);
            if (!line.startsWith(indent.toString())) return;
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

    public void save(boolean isConfig) {
        try {
            if (isConfig) {
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
