package dev.virtualshop.listeners;

import dev.virtualshop.VirtualShopPlugin;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class ArmorStandDamageListener implements Listener {

    private final VirtualShopPlugin plugin;

    public ArmorStandDamageListener(VirtualShopPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof ArmorStand stand)) return;
        if (plugin.getShopManager().isShopEntity(stand.getUniqueId())) {
            event.setCancelled(true);
        }
    }
}
