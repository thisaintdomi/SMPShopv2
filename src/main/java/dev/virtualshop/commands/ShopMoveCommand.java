package dev.virtualshop.commands;

import dev.virtualshop.VirtualShopPlugin;
import dev.virtualshop.manager.ShopEntry;
import dev.virtualshop.util.ColorUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class ShopMoveCommand implements CommandExecutor {

    private final VirtualShopPlugin plugin;

    public ShopMoveCommand(VirtualShopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Csak játékosok használhatják."); return true; }
        if (!player.hasPermission("virtualshop.admin")) {
            player.sendMessage(ColorUtil.color("&cNincs jogosultságod."));
            return true;
        }

        String id = plugin.getShopManager().getMove(player.getUniqueId());
        if (id == null) {
            player.sendMessage(ColorUtil.color("<##ff4d4d>&lBOLT &8» &cNincs aktív áthelyezési folyamat."));
            return true;
        }

        ShopEntry entry = plugin.getShopManager().getShop(id);
        if (entry == null) {
            plugin.getShopManager().clearMove(player.getUniqueId());
            return true;
        }

        String worldName = plugin.getConfig().getString("shop-world", "SMPspawn");
        World world = plugin.getServer().getWorld(worldName);
        if (world == null) return true;

        Entity e = plugin.getShopManager().findEntityByUUID(world, entry.getEntityUUID());
        if (e != null) {
            Location newLoc = player.getLocation().clone();
            newLoc.setY(newLoc.getY() - 1.5);
            newLoc.setPitch(0);
            e.teleport(newLoc);

            plugin.getHologramManager().removeHologram(id);
            plugin.getHologramManager().createHologram(entry, newLoc);
            plugin.getShopManager().saveShops();

            player.sendMessage(ColorUtil.color("<##7dff7d>&lBOLT &8» &fShop áthelyezve."));
        }

        plugin.getShopManager().clearMove(player.getUniqueId());
        return true;
    }
}
