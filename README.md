![](https://i.imgur.com/eop5G6m.png)

# ConfigurationMaster
ConfigurationMaster is a lightweight Minecraft plugin addition that gives plugin developers more control over their configuration files. It fixes a lot of issues that developers face on a regular basis, which include:
- Commenting specific options, or even just about anywhere in the file.
- Separating options into specific sections.
- Forcing an order upon options so that new options can be added in the middle, at the bottom or even at the top of the configuration.
- Add example options/sections which don't reappear unless a new file is generated.
- Temporarily storing a file which has bad syntax as a separate configuration file.
- A customisable title, subtitle, description and set of external links to include in the header.
- The ability to move options around, especially when creating a new configuration format.

## Use within plugins
CM can be used either as a separate plugin to be installed or a shaded dependency. 

To manage your dependencies, it is recommended to use either Maven or Gradle. In Maven, just add the repository and dependency:

```xml
    <repositories>
	<repository>
	    <id>cm-repo</id>
	    <url>https://ci.pluginwiki.us/plugin/repository/everything/</url>
	</repository>
    </repositories>
    <dependencies>
        <dependency>
            <groupId>com.github.Thatsmusic99</groupId>
	    <artifactId>ConfigurationMaster</artifactId>
	    <version>v1.0.1</version>
	</dependency>
    </dependencies>
```
