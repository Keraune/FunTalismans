package ft.keraune.funtalismans.items;

import ft.keraune.funtalismans.utils.ColorParser;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.inventory.meta.*;

import java.lang.reflect.Method;

/**
 * Aplica un color a un ItemMeta de forma segura y compatible con múltiples versiones.
 */
public class ColorApplier {

    /**
     * Aplica un color (HEX, RGB o nombre) al ItemMeta.
     */
    public static ItemMeta apply(ItemMeta meta, String colorString) {
        if (meta == null || colorString == null) return meta;

        Color color = ColorParser.parse(colorString);
        if (color == null) return meta;

        // -------------------------
        // 1) Leather Armor
        // -------------------------
        if (meta instanceof LeatherArmorMeta leatherMeta) {
            leatherMeta.setColor(color);
            return leatherMeta;
        }

        // -------------------------
        // 2) PotionMeta (pociones / tipped arrows)
        // -------------------------
        if (meta instanceof PotionMeta potionMeta) {
            try {
                potionMeta.setColor(color);
            } catch (Throwable ignored) {}
            return potionMeta;
        }

        // -------------------------
        // 3) MapMeta (mapas coloreados)
        // -------------------------
        if (meta instanceof MapMeta mapMeta) {
            try {
                mapMeta.setColor(color);
            } catch (Throwable ignored) {}
            return mapMeta;
        }

        // -------------------------
        // 4) SuspiciousStewMeta (setColor existe en Paper)
        // -------------------------
        try {
            Method setColor = meta.getClass().getMethod("setColor", Color.class);
            setColor.invoke(meta, color);
            return meta;
        } catch (NoSuchMethodException ignored) {
            // normal
        } catch (Throwable ignored) {
        }

        // -------------------------
        // 5) BannerMeta (usa DyeColor más cercano)
        // -------------------------
        if (meta instanceof BannerMeta bannerMeta) {
            DyeColor dye = nearestDye(color);
            try {
                bannerMeta.setPatterns(java.util.List.of(
                        new org.bukkit.block.banner.Pattern(dye, org.bukkit.block.banner.PatternType.BASE)
                ));
            } catch (Throwable ignored) {}
            return bannerMeta;
        }

        // -------------------------
        // 6) ShieldMeta
        // -------------------------
        if (meta instanceof ShieldMeta shieldMeta) {
            DyeColor dye = nearestDye(color);
            try {
                shieldMeta.setPattern(0, new org.bukkit.block.banner.Pattern(dye, org.bukkit.block.banner.PatternType.BASE));
            } catch (Throwable ignored) {}
            return shieldMeta;
        }

        // -------------------------
        // 7) FireworkEffectMeta (sin toBuilder)
        // -------------------------
        if (meta instanceof FireworkEffectMeta fireworkMeta) {
            try {
                org.bukkit.FireworkEffect effect = org.bukkit.FireworkEffect.builder()
                        .withColor(color)
                        .withFade(color)
                        .build();

                fireworkMeta.setEffect(effect);
            } catch (Throwable ignored) {}
            return fireworkMeta;
        }

        return meta;
    }

    /**
     * Encuentra DyeColor más cercano a un Color RGB.
     */
    private static DyeColor nearestDye(Color color) {
        DyeColor best = DyeColor.WHITE;
        double bestDist = Double.MAX_VALUE;

        for (DyeColor dye : DyeColor.values()) {
            java.awt.Color dc = dyeToAwt(dye);
            double dr = color.getRed() - dc.getRed();
            double dg = color.getGreen() - dc.getGreen();
            double db = color.getBlue() - dc.getBlue();
            double dist = dr * dr + dg * dg + db * db;

            if (dist < bestDist) {
                bestDist = dist;
                best = dye;
            }
        }

        return best;
    }

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
}
