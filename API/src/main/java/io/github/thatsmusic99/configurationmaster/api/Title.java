package io.github.thatsmusic99.configurationmaster.api;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * A subsection of the API which is used to make pretty
 * titles easier to make and implement.
 */
public class Title {

    private final List<TitlePart> parts;
    private int width;
    private boolean addPadding;

    /**
     * The constructor used to initialise the title object.
     */
    public Title() {
        width = 75;
        parts = new ArrayList<>();
        addPadding = true;
    }

    /**
     * Changes the character width of the title object. Default value is 75.<br><br>
     *
     * This method cannot be used after using {@link Title#addLine(String)},
     * {@link Title#addLine(String, Pos)}, {@link Title#addSolidLine()} or
     * {@link Title#addSolidLine(char)}.
     *
     * @param width The width to be set.
     * @return the modified title object.
     * @throws IllegalStateException when the width is adjusted after title parts have been added.
     */
    public Title withWidth(int width) {
        if (!parts.isEmpty())
            throw new IllegalStateException("Cannot adjust the title width after title content has been added!");
        this.width = width;
        return this;
    }

    /**
     * Changes whether or not an extra # should be added at
     * the end of each line. The API does this by default.<br><br>
     *
     * This method cannot be used after using {@link Title#addLine(String)},
     * {@link Title#addLine(String, Pos)}, {@link Title#addSolidLine()} or
     * {@link Title#addSolidLine(char)}.
     *
     * @param padding Whether or not to add padding (#).
     * @return the modified title object.
     * @throws IllegalStateException when the padding is adjusted after title parts have been added.
     */
    public Title withPadding(boolean padding) {
        if (!parts.isEmpty())
            throw new IllegalStateException("Cannot adjust padding status after title content has been added!");
        this.addPadding = padding;
        return this;
    }

    /**
     * Adds a solid line of # characters that match the title width.
     *
     * @return The modified title object.
     */
    public Title addSolidLine() {
        return addSolidLine('#');
    }

    /**
     * Adds a solid line of a specified character that matches the title width,
     * but adjusted so padding can be included (or not).
     *
     * @param character The character to make a solid line of.
     * @return The modified title object.
     */
    public Title addSolidLine(char character) {
        parts.add(new LineTitlePart(character));
        return this;
    }

    /**
     * Adds a text line oriented to the left.
     *
     * @param content The text to be added to the title.
     * @return The modified title object.
     * @throws NullPointerException if the content is null.
     * @see #addLine(String, Pos) 
     */
    public Title addLine(@NotNull String content) {
        return addLine(content, Pos.LEFT);
    }

    /**
     * Adds a text line oriented to the right.
     * 
     * @param content The text to be added to the title.
     * @param position The position it is oriented to.
     * @return The modified title object.
     * @throws NullPointerException if the content or position is null.
     * @throws IllegalArgumentException if a word in the content is longer than what the title can accept (width - 4)
     * @see #addLine(String) 
     */
    public Title addLine(@NotNull String content, @NotNull Pos position) {
        // Null checks
        Objects.requireNonNull(content, "Title content must not be null!");
        Objects.requireNonNull(position, "Position must not be null!");
        // If all the stuff added fits on one line, just dump it in.
        if (content.length() < width - 3) {
            parts.add(new TextTitlePart(content, position));
            return this;
        }
        // However, if it doesn't, shorten it down.
        StringJoiner joiner = new StringJoiner(" ");
        for (String word : content.split(" ")) {
            // If the word itself is waaaay too long, throw an error.
            if (word.length() > width - 4) {
                throw new IllegalArgumentException(String.format("Word %s of size %s is too long to be fit into the title (%s)!", word, word.length(), width - 4));
            }
            if ((joiner + " " + word).length() < width - 3) {
                joiner.add(word);
                continue;
            }
            parts.add(new TextTitlePart(joiner.toString(), position));
            joiner = new StringJoiner(" ").add(word);
        }
        if (joiner.length() == 0) return this;
        parts.add(new TextTitlePart(joiner.toString(), position));
        return this;
    }

    /**
     * Converts the title to a string.
     *
     * @return the string representation of the title.
     */
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (TitlePart part : parts) {
            if (builder.length() != 0) builder.append("\n");
            builder.append(part.toString());
        }
        return builder.toString();
    }

    private String align(String content, Pos position) {
        int remainder = width - 4 - content.length();
        switch (position) {
            case LEFT:
                return "# " + content + repeat(" ", remainder);
            case RIGHT:
                return "# " + repeat(" ", remainder) + content;
            case CENTER:
                return "# " + repeat(" ", remainder / 2) + content + repeat(" ",
                        remainder % 2 == 1 ? remainder / 2 + 1 : remainder / 2);
        }
        return content;
    }

    /**
     * Used to represent an alignment position in a title text part.
     */
    public enum Pos {
        LEFT,
        CENTER,
        RIGHT
    }

    private class TextTitlePart extends TitlePart {
        private final String content;

        public TextTitlePart(String content, Pos pos) {
            super(pos);
            this.content = content;
        }

        @Override
        public String toString() {
            return align(content, position) + (addPadding ? " #" : "");
        }
    }

    private class LineTitlePart extends TitlePart {

        private final char character;

        public LineTitlePart(char character) {
            super(Pos.LEFT);
            this.character = character;
        }

        @Override
        public String toString() {
            if (character == '#') {
                return repeat("#", width);
            } else {
                return "# " + repeat(String.valueOf(character), width - 4) + (addPadding ? " #" : "");
            }
        }
    }

    /**
     * Used to represent a title part to be added.
     */
    public abstract static class TitlePart {
        protected Pos position;

        public TitlePart(Pos pos) {
            this.position = pos;
        }

        public abstract String toString();
    }

    private static String repeat(String str, int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append(str);
        }
        return builder.toString();
    }
}
