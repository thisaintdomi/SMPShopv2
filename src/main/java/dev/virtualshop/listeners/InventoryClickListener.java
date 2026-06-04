package dev.virtualshop.listeners;

import dev.virtualshop.VirtualShopPlugin;
import dev.virtualshop.manager.ShopEntry;
import dev.virtualshop.util.ColorUtil;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryClickListener implements Listener {

    private final VirtualShopPlugin plugin;
    private static final String GUI_TITLE = "\u1D07\u1D0F\u029F\u1D1B \u209B\u1D22\u1D07\u0280\u1D0B\u1D07\u209B\u1D22\u1D1B\u00E9\u209B";

    public InventoryClickListener(VirtualShopPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = event.getView().getTitle();
        if (title.contains("ʙᴏʟᴛ ꜱᴢᴇʀᴋᴇꜱᴢᴛéꜱ")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = event.getView().getTitle();
        if (!title.contains("ʙᴏʟᴛ ꜱᴢᴇʀᴋᴇꜱᴢᴛéꜱ")) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String name = clicked.getItemMeta().getDisplayName();

        if (name.contains("ÁR MÓDOSÍTÁSA")) {
            String id = plugin.getShopManager().getEdit(player.getUniqueId());
            if (id == null) return;
            plugin.getShopManager().setPriceEdit(player.getUniqueId(), id);
            plugin.getShopManager().clearEdit(player.getUniqueId());
            player.closeInventory();

            player.sendMessage("");
            player.sendMessage(ColorUtil.color("<##ffcc4d>&lBOLT &8» &fAdd meg az új árat:"));
            player.sendMessage(ColorUtil.color("&8┃ &f/shopprice <ár>"));
            player.sendMessage("");

        } else if (name.contains("ÁTHELYEZÉS")) {
            String id = plugin.getShopManager().getEdit(player.getUniqueId());
            if (id == null) return;
            plugin.getShopManager().setMove(player.getUniqueId(), id);
            plugin.getShopManager().clearEdit(player.getUniqueId());
            player.closeInventory();

            player.sendMessage("");
            player.sendMessage(ColorUtil.color("<##4db8ff>&lBOLT &8» &fÁllj a kívánt helyre."));
            player.sendMessage(ColorUtil.color("&8┃ &f/shopmove"));
            player.sendMessage("");

        } else if (name.contains("TÖRLÉS")) {
            String id = plugin.getShopManager().getEdit(player.getUniqueId());
            if (id == null) return;

            ShopEntry entry = plugin.getShopManager().getShop(id);
            if (entry != null) {
                String worldName = plugin.getConfig().getString("shop-world", "SMPspawn");
                World world = plugin.getServer().getWorld(worldName);
                if (world != null) {
                    Entity e = plugin.getShopManager().findEntityByUUID(world, entry.getEntityUUID());
                    if (e != null) e.remove();
                }
                plugin.getHologramManager().removeHologram(id);
                plugin.getShopManager().removeShop(id);
            }

            plugin.getShopManager().clearEdit(player.getUniqueId());
            player.closeInventory();

            player.sendTitle(
                    ColorUtil.color("<##7dff7d>&lSIKERES"),
                    ColorUtil.color("&fBolt törölve."),
                    10, 35, 10);
            player.playSound(player.getLocation(), "entity.player.levelup", 1f, 1f);
        }
    }
}
