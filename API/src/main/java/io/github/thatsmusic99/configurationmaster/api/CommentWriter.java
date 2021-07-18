package io.github.thatsmusic99.configurationmaster.api;

import java.util.ArrayList;
import java.util.List;

class CommentWriter {

    private final ConfigFile config;
    // The currently written lines of the file.
    private List<String> currentLines;

    protected CommentWriter(ConfigFile config) {
        this.config = config;
        this.currentLines = new ArrayList<>();
    }

    /**
     * Initiates the comment writing process.
     */
    protected void writeComments(List<String> currentLines) {
        this.currentLines = currentLines;
        // For each comment to be made...
        for (String path : config.getComments().keySet()) {
            // Write the comment at the specified path
            writeComment(path, path.split("\\."), 0, 0);
        }

        // However, if there's any comments left, write them in.
        for (String str : config.getPendingComments()) {
            if (str.isEmpty()) {
                currentLines.add("");
                continue;
            }
            currentLines.add("");
            String[] rawComment = str.split("\n");
            for (String commentPart : rawComment) {
                if (commentPart.isEmpty()) {
                    currentLines.add("");
                    continue;
                }
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
            if (line.startsWith(indent + divisions[iteration] + ":") ||
                    line.startsWith(indent + "'" + divisions[iteration] + "':")) {
                iteration += 1;
                if (iteration == divisions.length) {
                    int currentLine = i;
                    if (iteration == 1) {
                        currentLines.add(currentLine, "");
                        currentLine++;
                    }
                    String comment = config.getComments().get(path);
                    if (comment == null) continue;
                    String[] rawComment = comment.split("\n");
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

    protected List<String> getLines() {
        return currentLines;
    }
}
