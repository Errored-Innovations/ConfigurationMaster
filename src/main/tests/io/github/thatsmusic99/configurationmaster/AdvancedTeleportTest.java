package io.github.thatsmusic99.configurationmaster;

import com.google.common.collect.Lists;
import io.github.thatsmusic99.configurationmaster.api.ConfigFile;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class AdvancedTeleportTest {

    @Test
    public void initATConfig() throws IOException {
        File file = new File("test-config.yml");
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();
        ConfigFile config = ConfigFile.loadConfig(file);

        config.addComment("Another comment at the very top for all you lads :)");
        config.addDefault("use-basic-teleport-features", true, "Features", "Whether basic teleportation features should be enabled or not." +
                "\nThis includes /tpa, /tpahere, /tpblock, /tpunblock and /back." +
                "\nThis does not disable the command for other plugins - if you want other plugins to use the provided commands, use Bukkit's commands.yml file." +
                "\nPlease refer to https://bukkit.gamepedia.com/Commands.yml for this!");

        config.addDefault("use-warps", true, "Whether warps should be enabled in the plugin.");
        config.addDefault("use-spawn", true, "Whether the plugin should modify spawn/spawn properties.");
        config.addDefault("use-randomtp", true, "Whether the plugin should allow random teleportation.");
        config.addDefault("use-homes", true, "Whether homes should be enabled in the plugin.");
        config.addDefault("disabled-commands", new ArrayList<>(), "The commands that AT should not register upon starting up.\n" +
                "In other words, this gives up the command for other plugins to use.\n" +
                "NOTE: If you are using Essentials with AT and want AT to give up its commands to Essentials, Essentials does NOT go down without a fight. Jesus Christ. You'll need to restart the server for anything to change.");

        config.addSection("Teleport Requesting");
        config.addDefault("request-lifetime", 60, "\u2770 How long tpa and tpahere requests last before expiring.");
        config.addDefault("allow-multiple-requests", true, "Whether or not the plugin should enable the use of multiple requests.\n" +
                "When enabled, user 1 may get TPA requests from user 2 and 3, but user 1 is prompted to select a specific request.\n" +
                "When this is disabled and user 1 receives requests from user 2 and then 3, they will only have user 3's request to respond to.");
        config.addDefault("notify-on-expire", true, "Let the player know when their request has timed out or been displaced by another user's request.\n" +
                "Displacement only occurs when allow-multiple-requests is disabled.");
        // addDefault("tpa-restrict-movement-on", "requester");
        // addDefault("tpahere-restrict-movement-on", "requester");

        config.addDefault("warm-up-timer-duration", 3, "Warm-Up Timers", "The number of seconds it takes for the teleportation to take place following confirmation.\n" +
                "(i.e. \"You will teleport in 3 seconds!\")\n" +
                "This acts as the default option for the per-command warm-ups.");
        config.addDefault("cancel-warm-up-on-rotation", true, "Whether or not teleportation should be cancelled if the player rotates or moves.");
        config.addDefault("cancel-warm-up-on-movement", true, "Whether or not teleportation should be cancelled upon movement only.");

        config.addComment("per-command-warm-ups", "Command-specific warm-ups.");
        config.addDefault("per-command-warm-ups.tpa", "default", "Warm-up timer for /tpa.");
        config.addDefault("per-command-warm-ups.tpahere", "default", "Warm-up timer for /tpahere");
        config.addDefault("per-command-warm-ups.tpr", "default", "Warm-up timer for /tpr, or /rtp.");
        config.addDefault("per-command-warm-ups.warp", "default", "Warm-up timer for /warp");
        config.addDefault("per-command-warm-ups.spawn", "default", "Warm-up timer for /spawn");
        config.addDefault("per-command-warm-ups.home", "default", "Warm-up timer for /home");
        config.addDefault("per-command-warm-ups.back", "default", "Warm-up timer for /back");

        config.addDefault("cooldown-duration", 5, "Cooldowns", "How long before the user can use a command again.\n" +
                "This stops users spamming commands repeatedly.\n" +
                "This is also the default cooldown period for all commands.");
        config.addDefault("add-cooldown-duration-to-warm-up", true, "Adds the warm-up duration to the cooldown duration.\n" +
                "For example, if the cooldown duration was 5 seconds but the warm-up was 3, the cooldown becomes 8 seconds long.");
        config.addDefault("apply-cooldown-to-all-commands", false, "Whether or not the cooldown of one command will stop a user from using all commands.\n" +
                "For example, if a player used /tpa with a cooldown of 10 seconds but then used /tpahere with a cooldown of 5, the 10-second cooldown would still apply.\n" +
                "On the other hand, if a player used /tpahere, the cooldown of 5 seconds would apply to /tpa and other commands.");
        config.addDefault("apply-cooldown-after", "request", "When to apply the cooldown\n" +
                "Options include:\n" +
                "- request - Cooldown starts as soon as any teleport command is made and still applies even if no teleport takes place (i.e. cancelled by movement or not accepted).\n" +
                "- accept - Cooldown starts only when the teleport request is accepted (with /tpyes) and still applies even if no teleport takes place (i.e. cancelled by movement).\n" +
                "- teleport - Cooldown starts only when the teleport actually happens.\n" +
                "Note:\n" +
                "'request' and 'accept' behave the same for /rtp, /back, /spawn, /warp, and /home\n" +
                "cooldown for /tpall always starts when the command is ran, regardless if any player accepts or teleports");

        config.addComment("per-command-cooldowns", "Command-specific cooldowns.");
        config.addDefault("per-command-cooldowns.tpa", "default", "Cooldown for /tpa.");
        config.addDefault("per-command-cooldowns.tpahere", "default", "Cooldown for /tpahere");
        config.addDefault("per-command-cooldowns.tpr", "default", "Cooldown for /tpr, or /rtp.");
        config.addDefault("per-command-cooldowns.warp", "default", "Cooldown for /warp");
        config.addDefault("per-command-cooldowns.spawn", "default", "Cooldown for /spawn");
        config.addDefault("per-command-cooldowns.home", "default", "Cooldown for /home");
        config.addDefault("per-command-cooldowns.back", "default", "Cooldown for /back");
        // addDefault("per-command-cooldowns.sethome", "default", "Cooldown for /sethome");
        // addDefault("per-command-cooldowns.setwarp", "default", "Cooldown for /setwarp");

        config.addDefault("cost-amount", 100.0, "Teleportation Costs", "The amount it costs to teleport somewhere." +
                "\nIf you want to use Vault Economy, use 100.0 to charge $100." +
                "\nIf you want to use Minecraft EXP points, use 10EXP for 10 EXP Points." +
                "\nIf you want to use Minecraft EXP levels, use 5LVL for 5 levels." +
                "\nIf you want to use items, use the format MATERIAL:AMOUNT or MATERIAL:AMOUNT:BYTE." +
                "\nFor example, on 1.13+, ORANGE_WOOL:3 for 3 orange wool, but on versions before 1.13, WOOL:3:1." +
                "\nIf you're on a legacy version and unsure on what byte to use, see https://minecraftitemids.com/types" +
                "\nTo use multiple methods of charging, use a ; - e.g. '100.0;10LVL' for $100 and 10 EXP levels." +
                "\nTo disable, just put an empty string, i.e. ''");

        config.addComment("per-command-cost", "Command-specific costs.");
        config.addDefault("per-command-cost.tpa", "default", "Cost for /tpa.");
        config.addDefault("per-command-cost.tpahere", "default", "Cost for /tpahere.");
        config.addDefault("per-command-cost.tpr", "default", "Cost for /tpr, or /rtp.");
        config.addDefault("per-command-cost.warp", "default", "Cost for /warp");
        config.addDefault("per-command-cost.spawn", "default", "Cost for /spawn");
        config.addDefault("per-command-cost.home", "default", "Cost for /home");
        config.addDefault("per-command-cost.back", "default", "Cost for /back");
        //addDefault("per-command-cost.sethome", "default", "Cost for /sethome");
        //addDefault("pet-command-cost.setwarp", "default", "Cost for /setwarp");

        config.addSection("SQL Storage");

        config.addDefault("use-mysql", false, "Whether the plugin should use SQL storage or not.\n" +
                "By default, AT uses SQLite storage, which stores data in a .db file locally.");
        config.addDefault("mysql-host", "127.0.0.1", "The MySQL host to connect to.");
        config.addDefault("mysql-port", 3306, "The port to connect to.");
        config.addDefault("mysql-database", "database", "The database to connect to.");
        config.addDefault("mysql-username", "username", "The username to use when connecting.");
        config.addDefault("mysql-password", "password", "The password to use when connecting.");
        config.addDefault("mysql-table-prefix", "advancedtp", "The prefix of all AT tables. \n" +
                "If you're on Bungee, you may want to add your server's name to the end.");

        config.addDefault("enable-distance-limitations", false, "Distance Limitations",
                "Enables the distance limiter to stop players teleporting over a large distance.\n" +
                        "This is only applied when people are teleporting in the same world.");
        config.addDefault("maximum-teleport-distance", 1000, "The maximum distance that a player can teleport.\n" +
                "This is the default distance applied to all commands when specified.");
        config.addDefault("monitor-all-teleports-distance", false, "Whether or not all teleportations - not just AT's - should be checked for distance.");

        config.addComment("per-command-distance-limitations", "Determines the distance limit for each command.");
        config.addDefault("per-command-distance-limitations.tpa", "default", "Distance limit for /tpa");
        config.addDefault("per-command-distance-limitations.tpahere", "default", "Distance limit for /tpahere");
        config.addDefault("per-command-distance-limitations.tpr", "default", "Distance limit for /tpr");
        config.addDefault("per-command-distance-limitations.warp", "default", "Distance limit for /warp");
        config.addDefault("per-command-distance-limitations.spawn", "default", "Distance limit for /spawn");
        config.addDefault("per-command-distance-limitations.home", "default", "Distance limit for /home");
        config.addDefault("per-command-distance-limitations.back", "default", "Distance limit for /back");

        config.addSection("Teleportation Limitations");

        config.addComment("WARNING: A lot of the options below are considered advanced and use special syntax that is not often accepted in YAML.\n" +
                "When using such options, wrap them in quotes: ''\n" +
                "As an example, 'stop-teleportation-out:world,world_nether'");

        config.addDefault("enable-teleport-limitations", false,
                "Enables teleport limitations. This means cross-world or even world teleportation can be limited within specific worlds.");
        config.addDefault("monitor-all-teleports-limitations", false, "Whether or not all teleportation - not just AT's - should be checked to see if teleportation is allowed.");

        config.addComment("world-rules", "The teleportation rules defined for each world.\n" +
                "Rules include:\n" +
                "- stop-teleportation-out - Stops players teleporting to another world when they are in this world.\n" +
                "- stop-teleportation-within - Stops players teleporting within the world.\n" +
                "- stop-teleportation-into - Stops players teleporting into this world.\n" +
                "To combine multiple rules, use a ; - e.g. stop-teleportation-out;stop-teleportation-within\n" +
                "For out and into rules, you can make it so that rules only initiate when in or going to a specific world using :, e.g. stop-teleportation-out:world stops players teleporting to \"world\" in the world they're currently in.\n" +
                "To do the opposite (i.e. initiates the rule when users are not in the specified world), use !, e.g. stop-teleportation-into!world stops teleportation into a specific world if they are not in \"world\". If ! and : are used in the same rule, then : is given top priority." +
                "To make this rule work with multiple worlds, use a comma (,), e.g. stop-teleportation-into:world,world_nether");

        config.makeSectionLenient("world-rules");
        config.addDefault("world-rules.default", "stop-teleportation-within");
        config.addExample("world-rules.world", "default");
        config.addExample("world-rules.world_nether", "stop-teleportation-into!world", "Stops people teleporting into the Nether if they're not coming from \"world\"");

        config.addComment("command-rules", "The teleportation rules defined for each AT command.\n" +
                "Rules include:\n" +
                "- override - The command will override world rules and run regardless.\n" +
                "- ignore - The command will refuse to run regardless of world rules.\n" +
                "To combine multiple rules, use a ;.\n" +
                "To make rules behave differently in different worlds, use : to initiate the rule in a specific world (e.g. override:world to make the command override \"world\"'s rules.)\n" +
                "To initiate rules outside of a specific world, use ! (e.g. override!world to make the command override world rules everywhere but in world)\n" +
                "To use multiple worlds, use a comma (,).\n" +
                "By default, all commands will comply with the world rules. If no rules are specified, they will comply.\n" +
                "All worlds specified will be considered the world in which the player is currently in. For worlds being teleported to, add > to the start of the world name.\n" +
                "For example, ignore:world,>world_nether will not run if the player is in \"world\" or if the player is going into the Nether.");
        config.addDefault("command-rules.tpa", "");
        config.addDefault("command-rules.tpahere", "");
        config.addDefault("command-rules.tpr", "");
        config.addDefault("command-rules.warp", "");
        config.addDefault("command-rules.spawn", "");
        config.addDefault("command-rules.home", "");
        config.addDefault("command-rules.back", "");

        config.addDefault("maximum-x", 5000, "RandomTP", "The maximum X coordinate to go up to when selecting a random location.");
        config.addDefault("maximum-z", 5000, "The maximum Z coordinate to go up to when selecting a random location.");
        config.addDefault("minimum-x", -5000, "The minimum X coordinate to go down to when selecting a random location.");
        config.addDefault("minimum-z", -5000, "The minimum Z coordinate to go down to when selecting a random location.");
        config.addDefault("use-world-border", true, "When WorldBorder is installed, AT will check the border of each world instead rather than using the minimum and maximum coordinates.");
        config.addDefault("use-rapid-response", true, "Use the new rapid response system for RTP.\n" +
                "This means valid locations are prepared before a user chooses to use /tpr or interact with a sign, meaning they are ready for use and can instantly TP a player.\n" +
                "This feature allows you to use the \"tpr\" death option in the death management section further down.\n" +
                "IMPORTANT NOTE - this feature only works on the Paper server type and any of its forks. It is not considered safe to use on Spigot or Bukkit.");
        config.addDefault("prepared-locations-limit", 3, "How many locations can be prepared per world when using AT's Rapid Response system.\n" +
                "These are immediately prepared upon startup and when a world is loaded.");
        config.addDefault("ignore-world-generators", new ArrayList<>(Arrays.asList(
                "us.talabrek.ultimateskyblock.world.SkyBlockChunkGenerator",
                "us.talabrek.ultimateskyblock.world.SkyBlockNetherChunkGenerator",
                "world.bentobox.bskyblock.generators.ChunkGeneratorWorld",
                "world.bentobox.acidisland.world.ChunkGeneratorWorld",
                "world.bentobox.oneblock.generators.ChunkGeneratorWorld",
                "com.wasteofplastic.askyblock.generators.ChunkGeneratorWorld",
                "com.wasteofplastic.acidisland.generators.ChunkGeneratorWorld",
                "b.a",
                "com.chaseoes.voidworld.VoidWorld.VoidWorldGenerator",
                "club.bastonbolado.voidgenerator.EmptyChunkGenerator")), "AT's Rapid Response system automatically loads locations for each world, but can be problematic on some worlds, mostly SkyBlock worlds.\n" +
                "In response, this list acts as pro-active protection and ignores worlds generated using the following generators.\n" +
                "This is provided as an option so you can have control over which worlds have locations load.");
        config.addDefault("avoid-blocks", new ArrayList<>(Arrays.asList("WATER", "LAVA", "STATIONARY_WATER", "STATIONARY_LAVA")),
                "Blocks that people must not be able to land in when using /tpr.");
        config.addDefault("avoid-biomes", new ArrayList<>(Arrays.asList("OCEAN", "DEEP_OCEAN")), "Biomes that the plugin should avoid when searching for a location.");
        config.addDefault("whitelist-worlds", false, "Whether or not /tpr should only be used in the worlds listed below.");
        config.addDefault("redirect-to-whitelisted-worlds", true, "Whether or not players should be directed to a whitelisted world when using /tpr.\n" +
                "When this option is disabled and the player tries to use /tpr in a non-whitelisted world, the command simply won't work.");
        config.addDefault("allowed-worlds", new ArrayList<>(Arrays.asList("world", "world_nether")), "Worlds you can use /tpr in.\n" +
                "If a player uses /tpr in a world that doesn't allow it, they will be teleported in the first world on the list instead.\n" +
                "To make this feature effective, turn on \"whitelist-worlds\" above.");


        config.addDefault("default-homes-limit", -1, "Homes", "The default maximum of homes people can have.\n" +
                "This can be overridden by giving people permissions such as at.member.homes.10.\n" +
                "To disable this, use -1 as provided by default.");
        config.addDefault("add-bed-to-homes", true, "Whether or not the bed home should be added to /homes.");
        config.addDefault("deny-homes-if-over-limit", false, "Whether or not players should be denied access to some of their homes if they exceed their homes limit.\n" +
                "The homes denied access to will end up being their most recently set homes.\n" +
                "For example, having homes A, B, C, D and E with a limit of 3 will deny access to D and E.");
        config.addDefault("hide-homes-if-denied", false, "If homes should be hidden from /homes should they be denied access.\n" +
                "If this is false, they will be greyed out in the /homes list.");

        config.addDefault("tpa-request-received", "none", "Notifications/Sounds",
                "The sound played when a player receives a teleportation (tpa) request.\n" +
                        "For 1.16+, check https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html for a list of sounds you can use\n" +
                        "For 1.15 and below, check https://www.spigotmc.org/threads/sounds-spigot-1-7-1-14-4-sound-enums.340452/ for a list of sounds down to 1.7.\n" +
                        "(Friendly reminder that 1.7.x is not supported though!)\n" +
                        "Set to \"none\" if you want no sound playing.");
        config.addDefault("tpa-request-sent", "none", "The sound played when a player sends a teleportation (tpa) request.");
        config.addDefault("tpahere-request-received", "none", "The sound played when a player receives a teleportation (tpahere) request.");
        config.addDefault("tpahere-request-sent", "none", "The sound played when a player sends a teleportation (tpahere) request.");

        config.addDefault("used-teleport-causes", new ArrayList<>(Arrays.asList("COMMAND", "PLUGIN", "SPECTATE")), "Back",
                "The teleport causes that the plugin must listen to allow players to teleport back to the previous location.\n" +
                        "You can see a full list of these causes at https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/player/PlayerTeleportEvent.TeleportCause.html");
        config.addDefault("back-search-radius", 5, "The cubic radius to search for a safe block when using /back.\n" +
                "If a player teleports from an unsafe location and uses /back to return to it, the plugin will search all blocks within this radius to see if it is a safe place for the player to be moved to.\n" +
                "It is recommend to avoid setting this option too high as this can have a worst case execution time of O(n^3) (e.g. run 27 times, 64, 125, 216 and so on).\n" +
                "To disable, either set to 0 or -1.");


        config.addDefault("teleport-to-spawn-on-first-join", true, "Spawn Management",
                "Whether the player should be teleported to the spawnpoint when they join for the first time.");
        config.addDefault("teleport-to-spawn-on-every-join", false,
                "Whether the player should be teleported to the spawnpoint every time they join.");

        config.addComment("death-management", "Determines how and where players teleport when they die.\n" +
                "Options include:\n" +
                "- spawn - Teleports the player to the spawnpoint of either the world or specified by the plugin.\n" +
                "- bed - Teleports to the player's bed.\n" +
                "- anchor - 1.16+ only, teleports to the player's respawn anchor. However, due to limitations with Spigot's API, it may or may not always work. (add Player#getRespawnAnchor pls)\n" +
                "- warp:Warp Name - Teleports the player to a specified warp. For example, if you want to teleport to Hub, you'd type warp:Hub\n" +
                "- tpr - Teleports the player to a random location. Can only be used when the rapid response system is enabled." +
                "- {default} - Uses the default respawn option, which is spawn unless set differently.\n" +
                "If you're using EssentialsX Spawn and want AT to take over respawn mechanics, set respawn-listener-priority in EssX's config.yml file to lowest.");

        config.makeSectionLenient("death-management");
        config.addDefault("death-management.default", "spawn");
        config.addExample("death-management.world", "{default}");
        config.addExample("death-management.special-world", "warp:Special");
        config.addExample("death-management.another-world", "bed");

        config.addDefault("default-permissions", new ArrayList<>(Arrays.asList("at.member.*", "at.member.warp.*")), "Permissions",
                "The default permissions given to users without OP.\n" +
                        "By default, Advanced Teleport allows users without OP to use all member features.\n" +
                        "This allows for permission management without a permissions plugin, especially if a user doesn't understand how such plugins work.\n" +
                        "However, if you have a permissions plugin and Vault installed, you cannot make admin permissions work by default.");
        config.addDefault("allow-admin-permissions-as-default-perms", false, "Allows admin permissions to be allowed as default permissions by default.\n" +
                "If you want to use admin permissions, it's often recommended to use a permissions plugin such as LuckPerms.\n" +
                "Do not enable this if you are unsure of the risks this option proposes.");

        config.save();
        config.reload();

        Assert.assertTrue(config.getBoolean("use-basic-teleport-features"));
        Assert.assertTrue(config.getBoolean("use-warps"));
        Assert.assertTrue(config.getBoolean("use-randomtp"));
        Assert.assertTrue(config.getBoolean("use-homes"));
        Assert.assertEquals(new ArrayList<>(), config.getStringList("disabled-commands"));

        Assert.assertEquals(60, config.getInteger("request-lifetime"));
        Assert.assertTrue(config.getBoolean("allow-multiple-requests"));
        Assert.assertTrue(config.getBoolean("notify-on-expire"));

        Assert.assertEquals(3, config.getInteger("warm-up-timer-duration"));
        Assert.assertTrue(config.getBoolean("cancel-warm-up-on-rotation"));
        Assert.assertTrue(config.getBoolean("cancel-warm-up-on-movement"));

        Assert.assertEquals("default", config.getString("per-command-warm-ups.tpa"));

        config.set("cooldown-duration", "60");
        Assert.assertEquals("60", config.getString("cooldown-duration"));
        Assert.assertEquals(60, config.getInteger("cooldown-duration"));
        Assert.assertEquals(Lists.newArrayList(), config.getList("disabled-commands"));
    }
}
