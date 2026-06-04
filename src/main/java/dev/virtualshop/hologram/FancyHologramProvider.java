package dev.virtualshop.hologram;

import de.oliver.fancyholograms.api.FancyHolograms;
import de.oliver.fancyholograms.api.HologramManager;
import de.oliver.fancyholograms.api.data.TextHologramData;
import de.oliver.fancyholograms.api.hologram.Hologram;
import dev.virtualshop.VirtualShopPlugin;
import dev.virtualshop.manager.ShopEntry;
import dev.virtualshop.util.ColorUtil;
import org.bukkit.Location;

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

        HologramManager manager = FancyHolograms.get().getHologramManager();

        // Remove old hologram with same name if it exists
        Hologram existing = manager.getHologram(name);
        if (existing != null) {
            manager.removeHologram(existing);
        }

        TextHologramData data = new TextHologramData(name, location);
        data.setText(lines);
        data.setVisibilityDistance(48);
        data.setBillboard(de.oliver.fancyholograms.api.data.HologramData.Billboard.CENTER);

        Hologram hologram = FancyHolograms.get().getHologramManager().create(data);
        manager.addHologram(hologram);
        hologram.createHologram();

        return name; // store the name as handle
    }

    @Override
    public void remove(Object handle) {
        if (!(handle instanceof String name)) return;
        HologramManager manager = FancyHolograms.get().getHologramManager();
        Hologram holo = manager.getHologram(name);
        if (holo != null) {
            holo.deleteHologram();
            manager.removeHologram(holo);
        }
    }

    @Override
    public void update(Object handle, ShopEntry entry) {
        if (!(handle instanceof String name)) return;
        HologramManager manager = FancyHolograms.get().getHologramManager();
        Hologram holo = manager.getHologram(name);
        if (holo == null) return;

        List<String> configLines = plugin.getConfig().getStringList("hologram.lines");
        if (configLines.isEmpty()) {
            configLines = List.of("&8[ &a{id} &8]", "&7Ár: &a{price}$", "&7Jobb klikk a vásárláshoz");
        }

        List<String> lines = buildLines(configLines, entry);
        ((TextHologramData) holo.getData()).setText(lines);
        holo.refreshHologram(null);
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
