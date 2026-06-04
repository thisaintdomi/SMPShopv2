package dev.virtualshop.hologram;

import dev.virtualshop.manager.ShopEntry;
import org.bukkit.Location;

public interface HologramProvider {
    /** Create a new hologram and return an opaque handle. */
    Object create(ShopEntry entry, Location location);
    /** Remove/delete the hologram. */
    void remove(Object handle);
    /** Update displayed lines after price change etc. */
    void update(Object handle, ShopEntry entry);
}
