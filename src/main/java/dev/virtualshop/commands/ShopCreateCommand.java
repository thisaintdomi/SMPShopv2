package dev.virtualshop.commands;

import dev.virtualshop.VirtualShopPlugin;
import dev.virtualshop.manager.ShopEntry;
import dev.virtualshop.util.ColorUtil;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ShopCreateCommand implements CommandExecutor {

    private final VirtualShopPlugin plugin;

    public ShopCreateCommand(VirtualShopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Csak játékosok használhatják."); return true; }
        if (!player.hasPermission("virtualshop.admin")) {
            player.sendMessage(ColorUtil.color("&cNincs jogosultságod."));
            return true;
        }
        if (args.length < 2) {
            player.sendMessage(ColorUtil.color("&cHasználat: /shopcreate <id> <ár>"));
            return true;
        }

        String shopWorld = plugin.getConfig().getString("shop-world", "SMPspawn");
        if (!player.getWorld().getName().equals(shopWorld)) {
            player.sendMessage(ColorUtil.color("<##ff4d4d>&lBOLT &8» &cEbben a worldben nem hozhatsz létre boltot."));
            return true;
        }

        String id = args[0];
        double price;
        try {
            price = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(ColorUtil.color("<##ff4d4d>&lBOLT &8» &cÉrvénytelen ár."));
            return true;
        }

        if (!plugin.getShopManager().hasSavedItem(id)) {
            player.sendMessage(ColorUtil.color("<##ff4d4d>&lBOLT &8» &cElőbb ments el egy tárgyat ezzel:"));
            player.sendMessage(ColorUtil.color("&8┃ &f/shopitemsave " + id));
            return true;
        }

        ItemStack helmet = plugin.getShopManager().getSavedItem(id);

        Location loc = player.getLocation();
        loc.setY(loc.getY() - 1.5);
        loc.setPitch(0);

        ArmorStand stand = plugin.getShopManager().spawnStand(loc, helmet);

        ShopEntry entry = new ShopEntry(id, price, stand.getUniqueId(), helmet);
        plugin.getShopManager().addShop(entry);
        plugin.getHologramManager().createHologram(entry, stand.getLocation());

        player.sendMessage("");
        player.sendMessage(ColorUtil.color("<##7dff7d>&lBOLT &8» &fVirtuális bolt sikeresen létrehozva."));
        player.sendMessage(ColorUtil.color("&8┃ &7ID: <##7dff7d>" + id));
        player.sendMessage(ColorUtil.color("&8┃ &7Ár: <##7dff7d>" + (int) price + "$"));
        player.sendMessage("");
        return true;
    }
}
