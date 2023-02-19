package io.github.thatsmusic99.configurationmaster;

import io.github.thatsmusic99.configurationmaster.api.ConfigFile;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Sample configuration setup for CommandWhitelist by YouHaveTrouble
 */
public class CommandWhitelistTest {

    @Test
    public void doCommandWhitelistTests() throws Exception {
        ConfigFile config = ConfigFile.loadConfig(new File("command-whitelist.yml"));

        config.addDefault("messages.prefix", "CommandWhitelist > ");
        config.addDefault("messages.command_denied", "No such command.");
        config.addDefault("messages.subcommand_denied", "You cannot use this subcommand");
        config.addDefault("messages.no_permission", "<red>You don't have permission to do this.");
        config.addDefault("messages.no_such_subcommand", "<red>No subcommand by that name.");
        config.addDefault("messages.config_reloaded", "<yellow>Configuration reloaded.");
        config.addDefault("messages.added_to_whitelist", "<yellow>Whitelisted command <gold>%s <yellow>for permission <gold>%s");
        config.addDefault("messages.removed_from_whitelist", "<yellow>Removed command <gold>%s <yellow>from permission <gold>%s");
        config.addDefault("messages.group_doesnt_exist", "<red>Group doesn't exist or error occured");

        config.addComment("messages", "Messages use MiniMessage formatting (https://docs.adventure.kyori.net/minimessage.html#format)");

        config.addDefault("use_protocollib", false, "Do not enable if you don't have issues with aliased commands.\nThis requires server restart to take effect.");

        config.makeSectionLenient("groups");
        List<String> exampleCommands = new ArrayList<>();
        exampleCommands.add("example");
        List<String> exampleSubCommands = new ArrayList<>();
        exampleSubCommands.add("example of");

        config.addExample("groups.example.commands", exampleCommands, "This is the WHITELIST of commands that players will be able to see/use in the group \"example\"");
        config.addExample("groups.example.subcommands", exampleSubCommands, "This is the BLACKLIST of subcommands that players will NOT be able to see/use in the group \"example\"");
        config.addComment("groups.example", "All groups except from default require commandwhitelist.group.<group_name> permission\ncommandwhitelist.group.example in this case\n If you wish to leave the list empty, put \"commands: []\" or \"subcommands: []\"");


        List<String> defaultCommands = new ArrayList<>();
        defaultCommands.add("help");
        defaultCommands.add("spawn");
        defaultCommands.add("bal");
        defaultCommands.add("balance");
        defaultCommands.add("baltop");
        defaultCommands.add("pay");
        defaultCommands.add("r");
        defaultCommands.add("msg");
        defaultCommands.add("tpa");
        defaultCommands.add("tpahere");
        defaultCommands.add("tpaccept");
        defaultCommands.add("tpdeny");
        defaultCommands.add("warp");
        List<String> defaultSubcommands = new ArrayList<>();
        defaultSubcommands.add("help about");


        config.save();
    }
}
