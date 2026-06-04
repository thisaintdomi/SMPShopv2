package dev.virtualshop.commands;

import dev.virtualshop.VirtualShopPlugin;
import dev.virtualshop.manager.ShopEntry;
import dev.virtualshop.util.ColorUtil;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class ShopRemoveCommand implements CommandExecutor {

    private final VirtualShopPlugin plugin;

    public ShopRemoveCommand(VirtualShopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Csak játékosok használhatják."); return true; }
        if (!player.hasPermission("virtualshop.admin")) {
            player.sendMessage(ColorUtil.color("&cNincs jogosultságod."));
            return true;
        }
        if (args.length < 1) { player.sendMessage(ColorUtil.color("&cHasználat: /shopremove <id>")); return true; }

        String id = args[0];
        ShopEntry entry = plugin.getShopManager().getShop(id);
        if (entry == null) {
            player.sendMessage(ColorUtil.color("<##ff4d4d>&lBOLT &8» &cNincs ilyen bolt."));
            return true;
        }

        String worldName = plugin.getConfig().getString("shop-world", "SMPspawn");
        World world = plugin.getServer().getWorld(worldName);
        if (world != null) {
            Entity e = plugin.getShopManager().findEntityByUUID(world, entry.getEntityUUID());
            if (e != null) e.remove();
        }

        plugin.getHologramManager().removeHologram(id);
        plugin.getShopManager().removeShop(id);

        player.sendMessage(ColorUtil.color("<##7dff7d>&lBOLT &8» &fA bolt sikeresen törölve lett."));
        return true;
    }
}
