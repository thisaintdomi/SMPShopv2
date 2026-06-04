package dev.virtualshop.commands;

import dev.virtualshop.VirtualShopPlugin;
import dev.virtualshop.util.ColorUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class TeleportCommand implements CommandExecutor {

    private final VirtualShopPlugin plugin;
    private final String type; // "spawn" or "shop"

    public TeleportCommand(VirtualShopPlugin plugin, String type) {
        this.plugin = plugin;
        this.type = type;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Csak játékosok használhatják."); return true; }

        int countdown = plugin.getConfig().getInt("teleport-countdown", 3);

        player.playSound(player.getLocation(), "block.note_block.pling", 1f, 1.1f);
        player.sendMessage("");
        player.sendMessage(ColorUtil.color("<##b5ff5d>&lA<##b4ff5e>&lS<##b3ff60>&lT<##b2fe61>&lR<##b1fe62>&lA<##b1fe64>&lL<##b0fe65>&lI<##affd66>&lX<##aefd68>&lM<##adfd69>&lC &7- &fTeleport"));
        player.sendMessage(ColorUtil.color("&f &7| &fTeleportálás folyamatban..."));
        player.sendMessage("");

        new BukkitRunnable() {
            int remaining = countdown;

            @Override
            public void run() {
                if (!player.isOnline()) { cancel(); return; }

                if (remaining > 0) {
                    String title = ColorUtil.color("<##b5ff5d>&lT<##b3ff60>&lE<##b1fe64>&lL<##affd67>&lE<##adfd69>&lP<##abfc6c>&lO<##a9fc6f>&lR<##a7fb72>&lT");
                    String sub = ColorUtil.color("&7➥ &fTeleportálás " + remaining + " másodperc múlva...");
                    player.sendTitle(title, sub, 0, 22, 0);
                    player.playSound(player.getLocation(), "block.note_block.hat", 1f, 1.2f);
                    remaining--;
                } else {
                    cancel();
                    doTeleport(player);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);

        return true;
    }

    private void doTeleport(Player player) {
        String section = type.equals("spawn") ? "spawn-location" : "shop-location";
        String worldName = plugin.getConfig().getString(section + ".world", "SMPspawn");
        double x = plugin.getConfig().getDouble(section + ".x", 0);
        double y = plugin.getConfig().getDouble(section + ".y", 64);
        double z = plugin.getConfig().getDouble(section + ".z", 0);

        World world = plugin.getServer().getWorld(worldName);
        if (world == null) {
            player.sendMessage(ColorUtil.color("&cA világ nem található: " + worldName));
            return;
        }

        player.teleport(new Location(world, x, y, z));

        String successTitle = ColorUtil.color("<##b5ff5d>&lS<##b3ff60>&lI<##b1fe64>&lK<##affd67>&lE<##adfd69>&lR");
        String successSub = ColorUtil.color("&7➥ &fSikeresen teleportálva.");
        player.sendTitle(successTitle, successSub, 10, 35, 10);
        player.playSound(player.getLocation(), "entity.enderman.teleport", 1f, 1f);
    }
}
