package dev.virtualshop.listeners;

import dev.virtualshop.VirtualShopPlugin;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;

public class ArmorStandTargetListener implements Listener {

    private final VirtualShopPlugin plugin;

    public ArmorStandTargetListener(VirtualShopPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onTarget(EntityTargetEvent event) {
        if (!(event.getTarget() instanceof ArmorStand stand)) return;
        if (plugin.getShopManager().isShopEntity(stand.getUniqueId())) {
            event.setCancelled(true);
        }
    }
}
