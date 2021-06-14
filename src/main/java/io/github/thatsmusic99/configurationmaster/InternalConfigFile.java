package io.github.thatsmusic99.configurationmaster;

import org.bukkit.configuration.file.YamlConstructor;
import org.bukkit.configuration.file.YamlRepresenter;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.util.HashMap;

public class InternalConfigFile extends CMConfigSection {

    private final Yaml yaml;
    private final DumperOptions yamlOptions = new DumperOptions();
    private final LoaderOptions loaderOptions = new LoaderOptions();
    private final Representer yamlRepresenter = new YamlRepresenter();
    private final File file;
    private boolean isNew;

    public InternalConfigFile(@NotNull File file) {
        yaml = new Yaml(new YamlConstructor(), yamlRepresenter, yamlOptions, loaderOptions);
        yamlOptions.setIndent(2);
        yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        yamlRepresenter.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        this.file = file;
        load();
    }

    public void load() throws FileNotFoundException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        StringBuilder content = new StringBuilder();
        try {
            String line = reader.readLine();
            while (line != null) {
                content.append(line).append("\n");
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        loadFromString(content.toString());
    }

    public void loadFromString(String str) {

    }

    public void save() {

    }

    public String saveToString() {
        String dump = this.yaml.dump(convertToMap());
    }


}
