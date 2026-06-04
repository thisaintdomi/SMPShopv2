package dev.virtualshop.commands;

import dev.virtualshop.VirtualShopPlugin;
import dev.virtualshop.manager.ShopEntry;
import dev.virtualshop.util.ColorUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopListCommand implements CommandExecutor {

    private final VirtualShopPlugin plugin;

    public ShopListCommand(VirtualShopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Csak játékosok használhatják."); return true; }
        if (!player.hasPermission("virtualshop.admin")) {
            player.sendMessage(ColorUtil.color("&cNincs jogosultságod."));
            return true;
        }

        player.sendMessage("");
        player.sendMessage(ColorUtil.color("<##7dff7d>&lVirtuális Boltok"));
        player.sendMessage(ColorUtil.color("&8━━━━━━━━━━━━━━━━━━━━"));

        for (ShopEntry entry : plugin.getShopManager().getAllShops().values()) {
            player.sendMessage(ColorUtil.color("&8• &f" + entry.getId() + " &8- &a" + (int) entry.getPrice() + "$"));
        }

        player.sendMessage(ColorUtil.color("&8━━━━━━━━━━━━━━━━━━━━"));
        player.sendMessage("");
        return true;
    }
}
