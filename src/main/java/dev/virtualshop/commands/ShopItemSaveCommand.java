package dev.virtualshop.commands;

import dev.virtualshop.VirtualShopPlugin;
import dev.virtualshop.util.ColorUtil;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopItemSaveCommand implements CommandExecutor {

    private final VirtualShopPlugin plugin;

    public ShopItemSaveCommand(VirtualShopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Csak játékosok használhatják."); return true; }
        if (!player.hasPermission("virtualshop.admin")) {
            player.sendMessage(ColorUtil.color("&cNincs jogosultságod."));
            return true;
        }
        if (args.length < 1) { player.sendMessage(ColorUtil.color("&cHasználat: /shopitemsave <id>")); return true; }

        if (player.getInventory().getItemInMainHand().getType() == Material.AIR) {
            player.sendMessage(ColorUtil.color("<##ff4d4d>&lBOLT &8» &cTarts a kezedben egy tárgyat."));
            return true;
        }

        String id = args[0];
        plugin.getShopManager().addSavedItem(id, player.getInventory().getItemInMainHand().clone());
        plugin.getShopManager().saveShops();

        player.sendMessage("");
        player.sendMessage(ColorUtil.color("<##7dff7d>&lBOLT &8» &fTárgy sikeresen elmentve."));
        player.sendMessage(ColorUtil.color("&8┃ &7Név: <##7dff7d>" + id));
        player.sendMessage("");
        return true;
    }
}
