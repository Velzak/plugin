package me.glitch.abilitys;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;

public class AutoUpdater {
    private final JavaPlugin plugin;
    private final String repo = "glitchspring548/plugin"; // e.g., "GlitchDev/MyPlugin"
    private final String currentVersion;

    public AutoUpdater(JavaPlugin plugin) {
        this.plugin = plugin;
        this.currentVersion = plugin.getDescription().getVersion();
    }

    public void checkAndUpdate() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URL url = new URL("https://api.github.com/repos/" + repo + "/releases/latest");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
                conn.setRequestProperty("User-Agent", "MinecraftPlugin-Updater");

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();

                String response = content.toString();
                String latestTag = extract(response, "\"tag_name\":\"", "\"");
                String normalizedLatest = latestTag.replaceFirst("^v", "");

                if (!latestTag.equalsIgnoreCase(currentVersion)) {
                    Bukkit.getLogger().info("[Updater] New version found: " + latestTag);
                    String downloadUrl = extract(response, "\"browser_download_url\":\"", "\"");

                    File pluginsFolder = plugin.getDataFolder().getParentFile();
                    File newFile = new File(pluginsFolder, plugin.getName() + "-update.jar");

                    try (InputStream inStream = new URL(downloadUrl).openStream()) {
                        Files.copy(inStream, newFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    }
                    File currentJar = new File(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
                    if (currentJar.exists()) {
                        File toDelete = new File(currentJar.getParentFile(), plugin.getName() + "-old.jar");
                        if (currentJar.renameTo(toDelete)) {
                            Bukkit.getLogger().info("[Updater] Old plugin jar renamed to " + toDelete.getName() + " (will be replaced on restart).");
                        } else {
                            Bukkit.getLogger().warning("[Updater] Could not rename old plugin jar. It might be locked by the server.");
                        }
                    }
                    Bukkit.getLogger().info("[Updater] Update downloaded. Restart the server to apply.");
                } else {
                    Bukkit.getLogger().info("[Updater] Plugin is up to date.");
                }

            } catch (Exception e) {
                Bukkit.getLogger().warning("[Updater] Failed to check for updates: " + e.getMessage());
            }
        });
    }

    private String extract(String text, String start, String end) {
        int s = text.indexOf(start) + start.length();
        int e = text.indexOf(end, s);
        return text.substring(s, e);
    }
}
