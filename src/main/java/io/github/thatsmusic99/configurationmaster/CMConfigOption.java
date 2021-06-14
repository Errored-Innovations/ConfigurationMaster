package io.github.thatsmusic99.configurationmaster;

import java.util.List;

public class CMConfigOption {

    private String path;
    private Object option;
    private StringBuilder comment = new StringBuilder();
    private String section = "";

    public CMConfigOption(String path, Object option) {
        this.path = path;
        this.option = option;
    }

    public CMConfigOption(String path, Object option, String comment) {

    }

    public Object get() {
        return option;
    }

    public String getPath() {
        return path;
    }

    public void addComments(List<String> str) {
        
        comment.insert().append("\n\n").append(str);
    }
}
