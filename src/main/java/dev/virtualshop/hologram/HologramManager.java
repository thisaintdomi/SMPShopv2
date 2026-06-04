package dev.virtualshop.hologram;

import dev.virtualshop.VirtualShopPlugin;
import dev.virtualshop.manager.ShopEntry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public class HologramManager {

    private final VirtualShopPlugin plugin;
    private final boolean fancyEnabled;
    private HologramProvider provider;

    // shop id -> hologram handle
    private final Map<String, Object> holograms = new HashMap<>();

    public HologramManager(VirtualShopPlugin plugin) {
        this.plugin = plugin;

        Plugin fancy = Bukkit.getPluginManager().getPlugin("FancyHolograms");
        if (fancy != null && fancy.isEnabled()) {
            fancyEnabled = true;
            provider = new FancyHologramProvider(plugin);
            plugin.getLogger().info("FancyHolograms found — using FancyHologram provider.");
        } else {
            fancyEnabled = false;
            provider = new BuiltinHologramProvider(plugin);
            plugin.getLogger().info("FancyHolograms not found — using built-in ArmorStand hologram provider.");
        }
    }

    public boolean isFancyHologramsEnabled() {
        return fancyEnabled;
    }

    public void createHologram(ShopEntry entry, Location standLocation) {
        removeHologram(entry.getId());
        double yOffset = plugin.getConfig().getDouble("hologram.y-offset", 1.8);
        Location holoLoc = standLocation.clone().add(0, yOffset, 0);
        Object handle = provider.create(entry, holoLoc);
        if (handle != null) holograms.put(entry.getId(), handle);
    }

    public void removeHologram(String shopId) {
        Object handle = holograms.remove(shopId);
        if (handle != null) provider.remove(handle);
    }

    public void updateHologram(ShopEntry entry) {
        Object handle = holograms.get(entry.getId());
        if (handle != null) provider.update(handle, entry);
    }

    public void removeAllHolograms() {
        for (Map.Entry<String, Object> e : holograms.entrySet()) {
            provider.remove(e.getValue());
        }
        holograms.clear();
    }
}
