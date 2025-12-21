package ft.keraune.funtalismans.utils;

import org.bukkit.ChatColor;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtil {

    private static final Pattern GRADIENT_PATTERN =
            Pattern.compile("<gradient:((?:#[A-Fa-f0-9]{6}:?)+)>([^<]*)</gradient>");

    private static final Pattern HEX_PATTERN =
            Pattern.compile("&#([A-Fa-f0-9]{6})");

    private static final Pattern TAG_HEX_PATTERN =
            Pattern.compile("<#([A-Fa-f0-9]{6})>");

    private static final Pattern STYLE_PATTERN =
            Pattern.compile("<(/?\\w+)>");

    private static final Pattern STRIP_COLOR_PATTERN =
            Pattern.compile("(?i)§[0-9A-FK-ORX]|&[0-9A-FK-ORX]|<[^>]*>");

    public static String color(String input) {
        if (input == null || input.isEmpty()) return "";

        String text = input;

        // 1) Procesar gradientes primero
        text = applyGradients(text);

        // 2) HEX con formato <#FFFFFF>
        text = TAG_HEX_PATTERN.matcher(text)
                .replaceAll(m -> toHex(m.group(1)));

        // 3) HEX con formato &#FFFFFF
        text = HEX_PATTERN.matcher(text)
                .replaceAll(m -> toHex(m.group(1)));

        // 4) Estilos tipo <b>, <i>, <red>, <reset>, etc.
        text = applyStyleTags(text);

        // 5) Legacy color codes (&a, &c, etc.)
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    // ============================================================
    // REMOVER COLORES Y FORMATOS
    // ============================================================
    public static String stripColor(String input) {
        if (input == null || input.isEmpty()) return "";

        // Primero eliminar todos los códigos de color y formato
        String stripped = STRIP_COLOR_PATTERN.matcher(input).replaceAll("");

        // También eliminar gradientes y sus etiquetas
        stripped = stripped.replaceAll("<gradient:[^>]*>", "")
                .replaceAll("</gradient>", "");

        // Eliminar cualquier otra etiqueta HTML-like
        stripped = stripped.replaceAll("<[^>]*>", "");

        return stripped.trim();
    }

    // ============================================================
    // GRADIENT
    // ============================================================
    private static String applyGradients(String text) {
        Matcher matcher = GRADIENT_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String colorList = matcher.group(1);
            String content = matcher.group(2);

            matcher.appendReplacement(buffer,
                    Matcher.quoteReplacement(buildGradient(content, colorList)));
        }
        matcher.appendTail(buffer);

        return buffer.toString();
    }

    private static String buildGradient(String content, String colorList) {
        String[] hexList = colorList.split(":");

        List<int[]> colors = new ArrayList<>();
        for (String hex : hexList) {
            int c = Integer.parseInt(hex.substring(1), 16);
            colors.add(new int[]{
                    (c >> 16) & 0xFF,
                    (c >> 8) & 0xFF,
                    c & 0xFF
            });
        }

        StringBuilder out = new StringBuilder();
        int len = content.length();

        for (int i = 0; i < len; i++) {
            // Validación para evitar división por cero si len es 1
            double progress = (len > 1) ? (double) i / (len - 1) * (colors.size() - 1) : 0;
            int index = (int) progress;
            double ratio = progress - index;

            int[] start = colors.get(index);
            int[] end = colors.get(Math.min(index + 1, colors.size() - 1));

            int r = (int) (start[0] + (end[0] - start[0]) * ratio);
            int g = (int) (start[1] + (end[1] - start[1]) * ratio);
            int b = (int) (start[2] + (end[2] - start[2]) * ratio);

            String hex = String.format("%02X%02X%02X", r, g, b);

            out.append(toHex(hex)).append("§l").append(content.charAt(i));
        }

        return out.toString();
    }


    // ============================================================
    // TAGS <b>, </b>, <red>, <reset>
    // ============================================================
    private static String applyStyleTags(String text) {

        return STYLE_PATTERN.matcher(text).replaceAll(m -> {
            String tag = m.group(1).toLowerCase();

            return switch (tag) {
                case "b" -> "§l";
                case "/b" -> "§r"; // reset style
                case "i" -> "§o";
                case "/i" -> "§r";
                case "u" -> "§n";
                case "/u" -> "§r";
                case "st" -> "§m";
                case "/st" -> "§r";
                case "obf" -> "§k";
                case "/obf" -> "§r";

                case "black" -> "§0";
                case "dark_blue" -> "§1";
                case "dark_green" -> "§2";
                case "dark_aqua" -> "§3";
                case "dark_red" -> "§4";
                case "dark_purple" -> "§5";
                case "gold" -> "§6";
                case "gray" -> "§7";
                case "dark_gray" -> "§8";
                case "blue" -> "§9";
                case "green" -> "§a";
                case "aqua" -> "§b";
                case "red" -> "§c";
                case "light_purple" -> "§d";
                case "yellow" -> "§e";
                case "white" -> "§f";

                case "reset" -> "§r";

                default -> m.group(0);
            };
        });
    }

    // ============================================================
    // HEX FORMATTER
    // ============================================================
    private static String toHex(String hex) {
        return "§x§" + hex.charAt(0) + "§" + hex.charAt(1) +
                "§" + hex.charAt(2) + "§" + hex.charAt(3) +
                "§" + hex.charAt(4) + "§" + hex.charAt(5);
    }
}