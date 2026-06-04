package dev.virtualshop.hologram;

import de.oliver.fancyholograms.api.FancyHologramsPlugin;
import de.oliver.fancyholograms.api.HologramManager;
import de.oliver.fancyholograms.api.data.TextHologramData;
import de.oliver.fancyholograms.api.hologram.Hologram;
import dev.virtualshop.VirtualShopPlugin;
import dev.virtualshop.manager.ShopEntry;
import org.bukkit.Location;
import org.bukkit.entity.Display;

import java.util.ArrayList;
import java.util.List;

/**
 * FancyHolograms-backed hologram provider.
 * Only loaded when FancyHolograms is on the classpath and enabled.
 */
public class FancyHologramProvider implements HologramProvider {

    private final VirtualShopPlugin plugin;

    public FancyHologramProvider(VirtualShopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Object create(ShopEntry entry, Location location) {
        List<String> configLines = plugin.getConfig().getStringList("hologram.lines");
        if (configLines.isEmpty()) {
            configLines = List.of("&8[ &a{id} &8]", "&7Ár: &a{price}$", "&7Jobb klikk a vásárláshoz");
        }

        List<String> lines = buildLines(configLines, entry);
        String name = "vshop_" + entry.getId();

        HologramManager manager = FancyHologramsPlugin.get().getHologramManager();

        // Remove old hologram with same name if it exists
        manager.getHologram(name).ifPresent(existing -> {
            existing.deleteHologram();
            manager.removeHologram(existing);
        });

        TextHologramData data = new TextHologramData(name, location);
        data.setText(lines);
        data.setVisibilityDistance(48);
        data.setBillboard(Display.Billboard.CENTER);

        Hologram hologram = manager.create(data);
        manager.addHologram(hologram);
        hologram.createHologram();

        return name; // store the name as handle
    }

    @Override
    public void remove(Object handle) {
        if (!(handle instanceof String name)) return;
        HologramManager manager = FancyHologramsPlugin.get().getHologramManager();
        manager.getHologram(name).ifPresent(holo -> {
            holo.deleteHologram();
            manager.removeHologram(holo);
        });
    }

    @Override
    public void update(Object handle, ShopEntry entry) {
        if (!(handle instanceof String name)) return;
        HologramManager manager = FancyHologramsPlugin.get().getHologramManager();
        manager.getHologram(name).ifPresent(holo -> {
            List<String> configLines = plugin.getConfig().getStringList("hologram.lines");
            if (configLines.isEmpty()) {
                configLines = List.of("&8[ &a{id} &8]", "&7Ár: &a{price}$", "&7Jobb klikk a vásárláshoz");
            }

            List<String> lines = buildLines(configLines, entry);
            ((TextHologramData) holo.getData()).setText(lines);
            holo.queueUpdate();
        });
    }

    private List<String> buildLines(List<String> configLines, ShopEntry entry) {
        List<String> result = new ArrayList<>();
        String priceStr = formatPrice(entry.getPrice());
        for (String line : configLines) {
            result.add(line.replace("{id}", entry.getId()).replace("{price}", priceStr));
        }
        return result;
    }

    private String formatPrice(double price) {
        if (price == Math.floor(price)) return String.valueOf((int) price);
        return String.valueOf(price);
    }
}
