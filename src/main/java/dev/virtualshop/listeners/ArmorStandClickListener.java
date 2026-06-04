package dev.virtualshop.listeners;

import dev.virtualshop.VirtualShopPlugin;
import dev.virtualshop.manager.ShopEntry;
import dev.virtualshop.util.ColorUtil;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class ArmorStandClickListener implements Listener {

    private final VirtualShopPlugin plugin;

    public ArmorStandClickListener(VirtualShopPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand stand)) return;

        Player player = event.getPlayer();
        String shopId = null;

        if (plugin.getShopManager().isShopEntity(stand.getUniqueId())) {
            shopId = plugin.getShopManager().getShopByEntityUUID(stand.getUniqueId()).getId();
        }

        if (shopId == null) return;
        event.setCancelled(true);

        // Admin sneak-click → open edit GUI
        if (player.isSneaking() && player.hasPermission("virtualshop.admin")) {
            openEditGui(player, shopId);
            return;
        }

        // Normal click → buy
        handleBuy(player, shopId);
    }

    private void handleBuy(Player player, String id) {
        if (plugin.getShopManager().hasBuyCooldown(player.getUniqueId())) {
            player.sendActionBar(LegacyComponentSerializer.legacySection().deserialize(
                    ColorUtil.color("<##ff4d4d>Várj egy pillanatot a következő vásárlás előtt.")));
            return;
        }

        plugin.getShopManager().addBuyCooldown(player.getUniqueId());

        ShopEntry entry = plugin.getShopManager().getShop(id);
        if (entry == null) { plugin.getShopManager().removeBuyCooldown(player.getUniqueId()); return; }

        double price = entry.getPrice();

        if (plugin.getEconomy().getBalance(player) < price) {
            plugin.getShopManager().removeBuyCooldown(player.getUniqueId());

            player.playSound(player.getLocation(), "entity.villager.no", 1f, 1f);
            player.sendTitle(
                    ColorUtil.color("<##ff4d4d>&lSIKERTELEN"),
                    ColorUtil.color("&fNincs elegendő egyenleged."),
                    5, 40, 10);

            player.sendMessage("");
            player.sendMessage(ColorUtil.color("<##ff4d4d>&lBOLT &8» &cNincs elegendő pénzed."));
            player.sendMessage(ColorUtil.color("&8┃ &7Szükséges: <##7dff7d>" + formatPrice(price) + "$"));
            player.sendMessage(ColorUtil.color("&8┃ &7Egyenleg: <##ff7070>" + formatPrice(plugin.getEconomy().getBalance(player)) + "$"));
            player.sendMessage("");
            return;
        }

        ItemStack item = entry.getItem();
        if (item == null) {
            plugin.getShopManager().removeBuyCooldown(player.getUniqueId());
            player.sendMessage(ColorUtil.color("<##ff4d4d>&lBOLT &8» &cEhhez a bolthoz nincs mentett tárgy."));
            return;
        }

        plugin.getEconomy().withdrawPlayer(player, price);
        player.getInventory().addItem(item.clone());

        player.playSound(player.getLocation(), "entity.player.levelup", 1f, 1f);
        player.playSound(player.getLocation(), "ui.toast.challenge_complete", 1f, 1f);

        player.sendTitle(
                ColorUtil.color("<##7dff7d>&lSIKERES VÁSÁRLÁS"),
                ColorUtil.color("&fTermék: <##7dff7d>" + id),
                10, 40, 10);

        player.sendMessage("");
        player.sendMessage(ColorUtil.color("<##7dff7d>&lBOLT &8» &fSikeresen megvásároltad a terméket."));
        player.sendMessage(ColorUtil.color("&8┃ &7Termék: <##7dff7d>" + id));
        player.sendMessage(ColorUtil.color("&8┃ &7Fizetett összeg: <##7dff7d>" + formatPrice(price) + "$"));
        player.sendMessage(ColorUtil.color("&8┃ &7Egyenleg: <##7dff7d>" + formatPrice(plugin.getEconomy().getBalance(player)) + "$"));
        player.sendMessage("");

        plugin.getServer().getScheduler().runTaskLater(plugin,
                () -> plugin.getShopManager().removeBuyCooldown(player.getUniqueId()),
                plugin.getConfig().getLong("buy-cooldown-ticks", 20L));
    }

    private void openEditGui(Player player, String id) {
        plugin.getShopManager().setEdit(player.getUniqueId(), id);
        ShopEntry entry = plugin.getShopManager().getShop(id);

        Inventory inv = plugin.getServer().createInventory(null, 27,
                ColorUtil.color("&8ʙᴏʟᴛ ꜱᴢᴇʀᴋᴇꜱᴢᴛéꜱ"));
        player.openInventory(inv);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta gm = glass.getItemMeta();
            gm.setDisplayName(ColorUtil.color("&r"));
            glass.setItemMeta(gm);
            for (int i = 0; i < 27; i++) inv.setItem(i, glass);

            // Price button
            ItemStack price = new ItemStack(Material.GOLD_INGOT);
            ItemMeta pm = price.getItemMeta();
            pm.setDisplayName(ColorUtil.color("<##ffcc4d>&lÁR MÓDOSÍTÁSA"));
            pm.setLore(Arrays.asList(
                    ColorUtil.color("&8Shop Kezelés"),
                    "",
                    ColorUtil.color("&7Jelenlegi ár:"),
                    ColorUtil.color("&a" + formatPrice(entry.getPrice()) + "$"),
                    "",
                    ColorUtil.color("&eKattints az ár módosításához")
            ));
            price.setItemMeta(pm);
            inv.setItem(11, price);

            // Move button
            ItemStack move = new ItemStack(Material.COMPASS);
            ItemMeta mm = move.getItemMeta();
            mm.setDisplayName(ColorUtil.color("<##4db8ff>&lÁTHELYEZÉS"));
            mm.setLore(Arrays.asList(
                    ColorUtil.color("&8Shop Kezelés"),
                    "",
                    ColorUtil.color("&7A shopot az aktuális"),
                    ColorUtil.color("&7pozíciódra helyezi át."),
                    "",
                    ColorUtil.color("&bKattints az áthelyezéshez")
            ));
            move.setItemMeta(mm);
            inv.setItem(13, move);

            // Delete button
            ItemStack delete = new ItemStack(Material.BARRIER);
            ItemMeta dm = delete.getItemMeta();
            dm.setDisplayName(ColorUtil.color("<##ff4d4d>&lTÖRLÉS"));
            dm.setLore(Arrays.asList(
                    ColorUtil.color("&8Shop Kezelés"),
                    "",
                    ColorUtil.color("&cFigyelem!"),
                    ColorUtil.color("&7A bolt véglegesen"),
                    ColorUtil.color("&7törlésre kerül."),
                    "",
                    ColorUtil.color("&cKattints a törléshez")
            ));
            delete.setItemMeta(dm);
            inv.setItem(15, delete);
        }, 1L);
    }

    private String formatPrice(double price) {
        if (price == Math.floor(price)) return String.valueOf((int) price);
        return String.valueOf(price);
    }
}
