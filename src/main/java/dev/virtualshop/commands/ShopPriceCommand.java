package dev.virtualshop.commands;

import dev.virtualshop.VirtualShopPlugin;
import dev.virtualshop.manager.ShopEntry;
import dev.virtualshop.util.ColorUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopPriceCommand implements CommandExecutor {

    private final VirtualShopPlugin plugin;

    public ShopPriceCommand(VirtualShopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Csak játékosok használhatják."); return true; }
        if (!player.hasPermission("virtualshop.admin")) {
            player.sendMessage(ColorUtil.color("&cNincs jogosultságod."));
            return true;
        }
        if (args.length < 1) { player.sendMessage(ColorUtil.color("&cHasználat: /shopprice <ár>")); return true; }

        String id = plugin.getShopManager().getPriceEdit(player.getUniqueId());
        if (id == null) {
            player.sendMessage(ColorUtil.color("<##ff4d4d>&lBOLT &8» &cNincs aktív árszerkesztési folyamat."));
            return true;
        }

        double newPrice;
        try {
            newPrice = Double.parseDouble(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage(ColorUtil.color("<##ff4d4d>&lBOLT &8» &cÉrvénytelen ár."));
            return true;
        }

        ShopEntry entry = plugin.getShopManager().getShop(id);
        if (entry == null) {
            plugin.getShopManager().clearPriceEdit(player.getUniqueId());
            return true;
        }

        entry.setPrice(newPrice);
        plugin.getShopManager().saveShops();
        plugin.getHologramManager().updateHologram(entry);
        plugin.getShopManager().clearPriceEdit(player.getUniqueId());

        player.sendMessage(ColorUtil.color("<##7dff7d>&lBOLT &8» &fÚj ár beállítva: &a" + (int) newPrice + "$"));
        return true;
    }
}
