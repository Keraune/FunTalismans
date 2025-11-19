package ft.keraune.funtalismans.utils;

import org.bukkit.ChatColor;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern TAG_HEX_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>");
    private static final Pattern GRADIENT_PATTERN = Pattern.compile("<gradient:((?:#[A-Fa-f0-9]{6}:?)+)>(.*?)</gradient>");
    private static final Pattern TAG_PATTERN = Pattern.compile("<(/?\\w+)>");

    public static String color(String text) {
        if (text == null) return "";

        // Procesar todo: estilos + gradientes + HEX
        text = processText(text);

        // Aplicar legacy & (esto es lo que faltaba para que funcionen &c, &a, etc.)
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    private static String processText(String text) {
        StringBuilder result = new StringBuilder();
        Deque<Character> activeStyles = new ArrayDeque<>();

        int index = 0;
        while (index < text.length()) {
            // Gradiente
            Matcher gradientMatcher = GRADIENT_PATTERN.matcher(text);
            if (gradientMatcher.find(index) && gradientMatcher.start() == index) {
                String colors = gradientMatcher.group(1);
                String content = gradientMatcher.group(2);
                result.append(buildGradient(content, colors, activeStyles));
                index = gradientMatcher.end();
                continue;
            }

            // Tag HEX <#XXXXXX>
            Matcher tagHexMatcher = TAG_HEX_PATTERN.matcher(text);
            if (tagHexMatcher.find(index) && tagHexMatcher.start() == index) {
                String hex = tagHexMatcher.group(1);
                result.append(toMinecraftHex(hex));
                index = tagHexMatcher.end();
                continue;
            }

            // HEX legacy &#XXXXXX
            Matcher hexMatcher = HEX_PATTERN.matcher(text);
            if (hexMatcher.find(index) && hexMatcher.start() == index) {
                String hex = hexMatcher.group(1);
                result.append(toMinecraftHex(hex));
                index = hexMatcher.end();
                continue;
            }

            // MiniMessage tags
            Matcher tagMatcher = TAG_PATTERN.matcher(text);
            if (tagMatcher.find(index) && tagMatcher.start() == index) {
                String tag = tagMatcher.group(1).toLowerCase();
                switch (tag) {
                    case "b" -> activeStyles.add('l');
                    case "i" -> activeStyles.add('o');
                    case "u" -> activeStyles.add('n');
                    case "st" -> activeStyles.add('m');
                    case "obf" -> activeStyles.add('k');
                    case "/b" -> activeStyles.remove('l');
                    case "/i" -> activeStyles.remove('o');
                    case "/u" -> activeStyles.remove('n');
                    case "/st" -> activeStyles.remove('m');
                    case "/obf" -> activeStyles.remove('k');
                    case "reset" -> activeStyles.clear();
                    case "black" -> result.append("§0");
                    case "dark_blue" -> result.append("§1");
                    case "dark_green" -> result.append("§2");
                    case "dark_aqua" -> result.append("§3");
                    case "dark_red" -> result.append("§4");
                    case "dark_purple" -> result.append("§5");
                    case "gold" -> result.append("§6");
                    case "gray" -> result.append("§7");
                    case "dark_gray" -> result.append("§8");
                    case "blue" -> result.append("§9");
                    case "green" -> result.append("§a");
                    case "aqua" -> result.append("§b");
                    case "red" -> result.append("§c");
                    case "light_purple" -> result.append("§d");
                    case "yellow" -> result.append("§e");
                    case "white" -> result.append("§f");
                }
                index = tagMatcher.end();
                continue;
            }

            // Carácter normal
            char c = text.charAt(index);
            StringBuilder prefix = new StringBuilder();
            for (char s : activeStyles) prefix.append('§').append(s);
            result.append(prefix).append(c);
            index++;
        }

        return result.toString();
    }

    private static String buildGradient(String content, String colorList, Deque<Character> activeStyles) {
        // Separar colores
        String[] hexColors = colorList.split(":");
        List<int[]> rgbColors = new ArrayList<>();
        for (String hex : hexColors) {
            int c = Integer.parseInt(hex.replace("#", ""), 16);
            rgbColors.add(new int[]{(c >> 16) & 0xFF, (c >> 8) & 0xFF, c & 0xFF});
        }

        StringBuilder result = new StringBuilder();
        List<Character> styles = new ArrayList<>(activeStyles);
        int length = content.length();

        for (int i = 0; i < length; i++) {
            // Determinar en qué segmento de color estamos
            double pos = (double) i / (length - 1) * (rgbColors.size() - 1);
            int segment = (int) Math.floor(pos);
            double ratio = pos - segment;

            int[] start = rgbColors.get(segment);
            int[] end = rgbColors.get(Math.min(segment + 1, rgbColors.size() - 1));

            int r = (int) (start[0] + (end[0] - start[0]) * ratio);
            int g = (int) (start[1] + (end[1] - start[1]) * ratio);
            int b = (int) (start[2] + (end[2] - start[2]) * ratio);

            String hexCode = String.format("%02X%02X%02X", r, g, b);

            // Aplicar primero el color, luego los estilos activos
            StringBuilder prefix = new StringBuilder(toMinecraftHex(hexCode));
            for (char s : styles) prefix.append('§').append(s);

            result.append(prefix).append(content.charAt(i));
        }

        return result.toString();
    }

    private static String toMinecraftHex(String hex) {
        return "§x§" + hex.charAt(0) + "§" + hex.charAt(1) +
                "§" + hex.charAt(2) + "§" + hex.charAt(3) +
                "§" + hex.charAt(4) + "§" + hex.charAt(5);
    }
}