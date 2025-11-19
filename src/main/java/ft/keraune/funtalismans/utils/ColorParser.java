package ft.keraune.funtalismans.utils;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;

public class ColorParser {

    /**
     * Convierte una cadena en un Color (HEX, RGB, nombres de color).
     */
    public static Color parse(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty()) return null;

        // -----------------------
        // 1) HEX: #FFFFFF
        // -----------------------
        if (s.startsWith("#") && s.length() == 7) {
            try {
                int r = Integer.parseInt(s.substring(1, 3), 16);
                int g = Integer.parseInt(s.substring(3, 5), 16);
                int b = Integer.parseInt(s.substring(5, 7), 16);
                return Color.fromRGB(r, g, b);
            } catch (Exception ignored) {}
        }

        // -----------------------
        // 2) RGB: "255,100,30"
        // -----------------------
        if (s.contains(",")) {
            try {
                String[] parts = s.split(",");
                if (parts.length == 3) {
                    int r = Integer.parseInt(parts[0].trim());
                    int g = Integer.parseInt(parts[1].trim());
                    int b = Integer.parseInt(parts[2].trim());
                    return Color.fromRGB(r, g, b);
                }
            } catch (Exception ignored) {}
        }

        // Normalizar
        String upper = s.toUpperCase().replace(" ", "_");

        // -----------------------
        // 3) ChatColor NAME (RED, BLUE, WHITE, GOLD…)
        // -----------------------
        try {
            ChatColor cc = ChatColor.valueOf(upper);
            if (cc.asBungee() != null && cc.asBungee().getColor() != null) {
                java.awt.Color awt = cc.asBungee().getColor();
                return Color.fromRGB(awt.getRed(), awt.getGreen(), awt.getBlue());
            }
        } catch (Exception ignored) {}

        // -----------------------
        // 4) DyeColor (BLACK, LIGHT_BLUE, MAGENTA…)
        // -----------------------
        try {
            DyeColor dye = DyeColor.valueOf(upper);
            java.awt.Color awt = dyeToAwt(dye);
            return Color.fromRGB(awt.getRed(), awt.getGreen(), awt.getBlue());
        } catch (Exception ignored) {}

        // -----------------------
        // 5) Minecraft JSON colors (dark_red, aqua, light_purple…)
        // -----------------------
        java.awt.Color json = minecraftJsonColor(upper);
        if (json != null) {
            return Color.fromRGB(json.getRed(), json.getGreen(), json.getBlue());
        }

        return null;
    }


    // ---------------------------------------------------------
    // DyeColor → RGB aproximado
    // ---------------------------------------------------------
    private static java.awt.Color dyeToAwt(DyeColor dye) {
        return switch (dye) {
            case WHITE -> new java.awt.Color(0xF9F9F9);
            case ORANGE -> new java.awt.Color(0xF9801D);
            case MAGENTA -> new java.awt.Color(0xC74EBD);
            case LIGHT_BLUE -> new java.awt.Color(0x3AB3DA);
            case YELLOW -> new java.awt.Color(0xFED83D);
            case LIME -> new java.awt.Color(0x80C71F);
            case PINK -> new java.awt.Color(0xF38BAA);
            case GRAY -> new java.awt.Color(0x474F52);
            case LIGHT_GRAY -> new java.awt.Color(0x9D9D97);
            case CYAN -> new java.awt.Color(0x169C9C);
            case PURPLE -> new java.awt.Color(0x8932B8);
            case BLUE -> new java.awt.Color(0x3C44AA);
            case BROWN -> new java.awt.Color(0x835432);
            case GREEN -> new java.awt.Color(0x5E7C16);
            case RED -> new java.awt.Color(0xB02E26);
            case BLACK -> new java.awt.Color(0x191919);
        };
    }

    // ---------------------------------------------------------
    // Minecraft JSON Colors (como los usados en chat.json)
    // ---------------------------------------------------------
    private static java.awt.Color minecraftJsonColor(String name) {
        return switch (name.toLowerCase()) {
            case "black" -> new java.awt.Color(0, 0, 0);
            case "dark_blue" -> new java.awt.Color(0, 0, 170);
            case "dark_green" -> new java.awt.Color(0, 170, 0);
            case "dark_aqua" -> new java.awt.Color(0, 170, 170);
            case "dark_red" -> new java.awt.Color(170, 0, 0);
            case "dark_purple" -> new java.awt.Color(170, 0, 170);
            case "gold" -> new java.awt.Color(255, 170, 0);
            case "gray" -> new java.awt.Color(170, 170, 170);
            case "dark_gray" -> new java.awt.Color(85, 85, 85);
            case "blue" -> new java.awt.Color(85, 85, 255);
            case "green" -> new java.awt.Color(85, 255, 85);
            case "aqua" -> new java.awt.Color(85, 255, 255);
            case "red" -> new java.awt.Color(255, 85, 85);
            case "light_purple" -> new java.awt.Color(255, 85, 255);
            case "yellow" -> new java.awt.Color(255, 255, 85);
            case "white" -> new java.awt.Color(255, 255, 255);
            default -> null;
        };
    }
}
