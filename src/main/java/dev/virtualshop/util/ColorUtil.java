package dev.virtualshop.util;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ColorUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("<##([A-Fa-f0-9]{6})>");
    private static final boolean SUPPORTS_HEX;

    static {
        boolean hex = false;
        try {
            String[] ver = Bukkit.getBukkitVersion().split("-")[0].split("\\.");
            int minor = Integer.parseInt(ver[1]);
            hex = minor >= 16;
        } catch (Exception ignored) {}
        SUPPORTS_HEX = hex;
    }

    private ColorUtil() {}

    public static String color(String text) {
        if (text == null) return "";
        if (SUPPORTS_HEX) {
            Matcher m = HEX_PATTERN.matcher(text);
            StringBuilder sb = new StringBuilder();
            while (m.find()) {
                m.appendReplacement(sb, ChatColor.of("#" + m.group(1)).toString());
            }
            m.appendTail(sb);
            text = sb.toString();
        } else {
            // Strip hex tags on older versions
            text = HEX_PATTERN.matcher(text).replaceAll("");
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static String formatPrice(double price) {
        if (price == Math.floor(price)) return String.valueOf((int) price);
        return String.valueOf(price);
    }
}
