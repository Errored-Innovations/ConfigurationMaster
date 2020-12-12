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
 */
public abstract class ConfigurationMaster {

    private FileConfiguration config;
    private FileConfiguration tempConfig;
    private File configFile;
    private HashMap<String, String> comments;
    private List<String> currentLines;
    private HashMap<String, String> sections;
    private List<String> nodeOrder;
    private final boolean isNew;
    private List<String> pendingComments;
    private Plugin plugin;

    /**
     *
     * @param name
     */
    public ConfigurationMaster(Plugin plugin, String name) {
        this(plugin, plugin.getDataFolder(), name);
    }

    public ConfigurationMaster(Plugin plugin, File folder, String name) {
        this.plugin = plugin;
        // Creates the config file object
        configFile = new File(folder, name + ".yml");
        // If it doesn't exist though, create it
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        //
        config = YamlConfiguration.loadConfiguration(configFile);
        isNew = config.saveToString().isEmpty();
        tempConfig = new YamlConfiguration();
        currentLines = new ArrayList<>();
        comments = new HashMap<>();
        nodeOrder = new ArrayList<>();
        sections = new HashMap<>();
        pendingComments = new ArrayList<>();

        loadDefaults();
        moveToNew();
        config.options().copyDefaults(true);
        save(true);
        postSave();
        loadTitle();
        //    writeSections();
        writeComments();
        save(false);

    }

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

        title.add(breakingLine);
        title.addAll(formatStr(getTitle(), Pos.CENTER));
        title.addAll(formatStr(getSubtitle(), Pos.CENTER));
        title.add(emptyLine);
        title.add(breakingLine);
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

    private List<String> formatStr(String str, Pos position) {
        List<String> lines = new ArrayList<>();
        if (str == null) return lines;
        String[] words = str.split(" ");
        StringBuilder sentence = new StringBuilder();
        for (String word : words) {
            if (sentence.length() > 0) {
                word = " " + word;
            }
            if (sentence.length() + word.length() > getMaxTitleWidth()) {
                lines.add("# " + align(sentence, position) + " #");
                sentence = new StringBuilder();
            }
            sentence.append(word.substring(1));
        }

        if (sentence.length() > 0) {
            lines.add("# " + align(sentence, position) + " #");
        }

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


    public int getMaxTitleWidth() {
        if (getExternalLinks().isEmpty()) {
            return 75;
        } else {
            int maxWidth = 75;
            for (String key : getExternalLinks().keySet()) {
                int currentWidth = key.length() + getLinkSeparator().length() + getExternalLinks().get(key).length();
                if (currentWidth > maxWidth) {
                    maxWidth = currentWidth;
                }
            }
            return maxWidth;
        }
    }

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

    public abstract void postSave();

    public void moveToNew() {}

    public void moveTo(String oldPath, String newPath) {
        Object object = config.get(oldPath);
        config.set(newPath, object);
        config.set(oldPath, null);
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
