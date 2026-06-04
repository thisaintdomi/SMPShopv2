package dev.virtualshop.hologram;

import dev.virtualshop.VirtualShopPlugin;
import dev.virtualshop.manager.ShopEntry;
import dev.virtualshop.util.ColorUtil;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

/**
 * Fallback hologram implementation using invisible named armor stands.
 * Each line is a separate armor stand stacked vertically.
 */
public class BuiltinHologramProvider implements HologramProvider {

    private static final double LINE_SPACING = 0.25;

    private final VirtualShopPlugin plugin;

    public BuiltinHologramProvider(VirtualShopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Object create(ShopEntry entry, Location location) {
        List<String> configLines = plugin.getConfig().getStringList("hologram.lines");
        if (configLines.isEmpty()) {
            configLines = List.of("&8[ &a{id} &8]", "&7Ár: &a{price}$", "&7Jobb klikk a vásárláshoz");
        }

        List<ArmorStand> stands = new ArrayList<>();
        Location current = location.clone();

        // Render lines from top down (first line = highest)
        for (int i = 0; i < configLines.size(); i++) {
            String raw = configLines.get(i)
                    .replace("{id}", entry.getId())
                    .replace("{price}", formatPrice(entry.getPrice()));

            ArmorStand stand = spawnLineStand(current, raw);
            stands.add(stand);
            current = current.clone().subtract(0, LINE_SPACING, 0);
        }

        return stands;
    }

    @Override
    public void remove(Object handle) {
        if (!(handle instanceof List<?> list)) return;
        for (Object o : list) {
            if (o instanceof ArmorStand as && !as.isDead()) {
                as.remove();
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void update(Object handle, ShopEntry entry) {
        if (!(handle instanceof List<?> list)) return;
        List<String> configLines = plugin.getConfig().getStringList("hologram.lines");
        if (configLines.isEmpty()) {
            configLines = List.of("&8[ &a{id} &8]", "&7Ár: &a{price}$", "&7Jobb klikk a vásárláshoz");
        }

        for (int i = 0; i < list.size() && i < configLines.size(); i++) {
            if (list.get(i) instanceof ArmorStand as && !as.isDead()) {
                String raw = configLines.get(i)
                        .replace("{id}", entry.getId())
                        .replace("{price}", formatPrice(entry.getPrice()));
                as.setCustomName(ColorUtil.color(raw));
            }
        }
    }

    private ArmorStand spawnLineStand(Location loc, String text) {
        ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        stand.setGravity(false);
        stand.setVisible(false);
        stand.setSmall(true);
        stand.setCustomNameVisible(true);
        stand.setCustomName(ColorUtil.color(text));
        stand.setInvulnerable(true);
        stand.setCanPickupItems(false);
        stand.setBasePlate(false);
        stand.setArms(false);
        stand.setSilent(true);
        return stand;
    }

    private String formatPrice(double price) {
        if (price == Math.floor(price)) return String.valueOf((int) price);
        return String.valueOf(price);
    }
}
