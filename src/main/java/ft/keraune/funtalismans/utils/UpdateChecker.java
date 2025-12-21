package ft.keraune.funtalismans.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateChecker {

    private final JavaPlugin plugin;
    private final String projectSlug;
    private final String projectUrl;

    public UpdateChecker(JavaPlugin plugin, String projectSlug) {
        this.plugin = plugin;
        this.projectSlug = projectSlug;
        this.projectUrl = "https://modrinth.com/plugin/" + projectSlug;
    }

    public void checkForUpdates() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URL url = new URL("https://api.modrinth.com/v2/project/" + projectSlug + "/version");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "FunTalismans-UpdateChecker");

                if (conn.getResponseCode() != 200) return;

                JsonArray versions = JsonParser.parseReader(
                        new InputStreamReader(conn.getInputStream())
                ).getAsJsonArray();

                if (versions.isEmpty()) return;

                JsonObject latest = versions.get(0).getAsJsonObject();
                String latestVersion = latest.get("version_number").getAsString();
                String currentVersion = plugin.getDescription().getVersion();

                if (isNewerVersion(latestVersion, currentVersion)) {

                    plugin.getLogger().warning("========================================");
                    plugin.getLogger().warning("A new version of FunTalismans is available!");
                    plugin.getLogger().warning("Current: " + currentVersion + " | Latest: " + latestVersion);
                    plugin.getLogger().warning("Download: " + projectUrl);
                    plugin.getLogger().warning("========================================");
                }

            } catch (Exception e) {
                plugin.getLogger().warning("Could not check for updates on Modrinth: " + e.getMessage());
            }
        });
    }

    private boolean isNewerVersion(String latest, String current) {
        return new Version(latest).compareTo(new Version(current)) > 0;
    }

    // =========================
    // VERSION COMPARATOR
    // =========================
    private static class Version implements Comparable<Version> {

        private final int[] numbers;
        private final String suffix;

        Version(String version) {
            String[] parts = version.split("-", 2);
            suffix = parts.length > 1 ? parts[1].toLowerCase() : "";
            String[] nums = parts[0].split("\\.");
            numbers = new int[nums.length];

            for (int i = 0; i < nums.length; i++) {
                try {
                    numbers[i] = Integer.parseInt(nums[i]);
                } catch (NumberFormatException e) {
                    numbers[i] = 0;
                }
            }
        }

        @Override
        public int compareTo(Version other) {
            int max = Math.max(numbers.length, other.numbers.length);

            for (int i = 0; i < max; i++) {
                int a = i < numbers.length ? numbers[i] : 0;
                int b = i < other.numbers.length ? other.numbers[i] : 0;
                if (a != b) return Integer.compare(a, b);
            }

            if (suffix.equals(other.suffix)) return 0;
            if (suffix.isEmpty()) return 1;
            if (other.suffix.isEmpty()) return -1;

            return suffixRank(suffix) - suffixRank(other.suffix);
        }

        private int suffixRank(String s) {
            if (s.contains("dev")) return 0;
            if (s.contains("snapshot")) return 1;
            if (s.contains("alpha")) return 2;
            if (s.contains("beta")) return 3;
            if (s.contains("rc")) return 4;
            return 5;
        }
    }
}
