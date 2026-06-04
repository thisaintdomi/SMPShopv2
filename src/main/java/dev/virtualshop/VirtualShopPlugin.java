package dev.virtualshop;

import dev.virtualshop.commands.*;
import dev.virtualshop.hologram.HologramManager;
import dev.virtualshop.listeners.*;
import dev.virtualshop.manager.ShopManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class VirtualShopPlugin extends JavaPlugin {

    private static VirtualShopPlugin instance;
    private Economy economy;
    private ShopManager shopManager;
    private HologramManager hologramManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        if (!setupEconomy()) {
            getLogger().severe("Vault / Economy plugin nem található! Plugin letiltva.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        shopManager = new ShopManager(this);
        hologramManager = new HologramManager(this);

        shopManager.loadShops();

        registerCommands();
        registerListeners();

        getLogger().info("VirtualShop sikeresen betöltve!");
        if (hologramManager.isFancyHologramsEnabled()) {
            getLogger().info("FancyHolograms integráció aktív.");
        } else {
            getLogger().info("FancyHolograms nem található — saját hologram rendszer aktív.");
        }
    }

    @Override
    public void onDisable() {
        if (shopManager != null) {
            shopManager.saveShops();
        }
        if (hologramManager != null) {
            hologramManager.removeAllHolograms();
        }
        getLogger().info("VirtualShop leállítva.");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp =
                getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return true;
    }

    private void registerCommands() {
        getCommand("shopitemsave").setExecutor(new ShopItemSaveCommand(this));
        getCommand("shopgive").setExecutor(new ShopGiveCommand(this));
        getCommand("shopcreate").setExecutor(new ShopCreateCommand(this));
        getCommand("shopremove").setExecutor(new ShopRemoveCommand(this));
        getCommand("shoplist").setExecutor(new ShopListCommand(this));
        getCommand("shopmove").setExecutor(new ShopMoveCommand(this));
        getCommand("shopprice").setExecutor(new ShopPriceCommand(this));
        getCommand("spawn").setExecutor(new TeleportCommand(this, "spawn"));
        getCommand("shop").setExecutor(new TeleportCommand(this, "shop"));
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new ArmorStandClickListener(this), this);
        getServer().getPluginManager().registerEvents(new ArmorStandDamageListener(this), this);
        getServer().getPluginManager().registerEvents(new ArmorStandTargetListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);
    }

    public static VirtualShopPlugin getInstance() {
        return instance;
    }

    public Economy getEconomy() {
        return economy;
    }

    public ShopManager getShopManager() {
        return shopManager;
    }

    public HologramManager getHologramManager() {
        return hologramManager;
    }
}
