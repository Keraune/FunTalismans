package ft.keraune.funtalismans.items;

import org.bukkit.Bukkit;
import org.bukkit.inventory.meta.SkullMeta;
import ft.keraune.funtalismans.FunTalismans;

import java.util.Base64;
import java.util.UUID;

public class SkullUtil {

    private static boolean spigotWarningShown = false;

    public static SkullMeta applyTexture(SkullMeta meta, String texture) {
        if (isPaperServer()) {
            return applyTexturePaper(meta, texture);
        } else {
            return applyTextureSpigot(meta);
        }
    }

    private static SkullMeta applyTexturePaper(SkullMeta meta, String texture) {
        try {
            com.destroystokyo.paper.profile.PlayerProfile profile = meta.getPlayerProfile();
            if (profile == null) {
                profile = Bukkit.createProfile(UUID.randomUUID(), null);
            }

            String value;
            if (texture.startsWith("base64:")) {
                value = texture.substring(7);
            } else if (texture.startsWith("http")) {
                String json = "{\"textures\":{\"SKIN\":{\"url\":\"" + texture + "\"}}}";
                value = Base64.getEncoder().encodeToString(json.getBytes());
            } else {
                value = texture;
            }

            profile.setProperty(new com.destroystokyo.paper.profile.ProfileProperty("textures", value));
            meta.setPlayerProfile(profile);

        } catch (Exception e) {
            // Silenciar el error, simplemente no aplicar la textura
        }
        return meta;
    }

    private static SkullMeta applyTextureSpigot(SkullMeta meta) {
        try {
            String uniqueName = "texture_" + UUID.randomUUID().toString().substring(0, 8);

            // Usar setOwningPlayer en lugar del deprecated setOwner
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(uniqueName));

            if (!spigotWarningShown) {
                spigotWarningShown = true;
                Bukkit.getScheduler().runTaskLater(FunTalismans.getInstance(), () -> {
                    // Usar el logger del plugin en lugar de Bukkit.getLogger()
                    FunTalismans.getInstance().getLogger().info("Custom textures require Paper for full functionality");
                }, 2L);
            }

        } catch (Exception e) {
            // Silenciar el error
        }
        return meta;
    }

    private static boolean isPaperServer() {
        try {
            Class.forName("com.destroystokyo.paper.PaperConfig");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static void resetWarning() {
        spigotWarningShown = false;
    }
}