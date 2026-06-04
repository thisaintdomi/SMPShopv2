package dev.virtualshop.commands;

import dev.virtualshop.VirtualShopPlugin;
import dev.virtualshop.util.ColorUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ShopGiveCommand implements CommandExecutor {

    private final VirtualShopPlugin plugin;

    public ShopGiveCommand(VirtualShopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Csak játékosok használhatják."); return true; }
        if (!player.hasPermission("virtualshop.admin")) {
            player.sendMessage(ColorUtil.color("&cNincs jogosultságod."));
            return true;
        }
        if (args.length < 1) { player.sendMessage(ColorUtil.color("&cHasználat: /shopgive <id>")); return true; }

        String id = args[0];
        ItemStack item = plugin.getShopManager().getSavedItem(id);
        if (item == null) {
            player.sendMessage(ColorUtil.color("<##ff4d4d>&lBOLT &8» &cNincs ilyen mentett tárgy."));
            return true;
        }

        player.getInventory().addItem(item.clone());
        return true;
    }
}
