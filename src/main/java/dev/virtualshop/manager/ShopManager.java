package dev.virtualshop.manager;

import dev.virtualshop.VirtualShopPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class ShopManager {

    private final VirtualShopPlugin plugin;
    private final File dataFile;
    private FileConfiguration data;

    // id -> ShopEntry
    private final Map<String, ShopEntry> shops = new LinkedHashMap<>();
    // entity UUID -> shop id
    private final Map<UUID, String> entityMap = new HashMap<>();
    // saved items (id -> ItemStack)
    private final Map<String, ItemStack> savedItems = new HashMap<>();

    // Per-player temp state
    private final Map<UUID, String> priceEditMap = new HashMap<>();
    private final Map<UUID, String> moveMap = new HashMap<>();
    private final Map<UUID, String> editMap = new HashMap<>();
    private final Set<UUID> buyCooldown = new HashSet<>();

    public ShopManager(VirtualShopPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "shops.yml");
    }

    public void loadShops() {
        if (!dataFile.exists()) {
            try { dataFile.createNewFile(); } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Nem sikerült létrehozni a shops.yml fájlt.", e);
            }
        }
        data = YamlConfiguration.loadConfiguration(dataFile);

        // Load saved items
        ConfigurationSection itemsSec = data.getConfigurationSection("saved-items");
        if (itemsSec != null) {
            for (String id : itemsSec.getKeys(false)) {
                ItemStack item = itemsSec.getItemStack(id);
                if (item != null) savedItems.put(id, item);
            }
        }

        // Load shops
        ConfigurationSection shopsSec = data.getConfigurationSection("shops");
        if (shopsSec == null) return;

        String shopWorld = plugin.getConfig().getString("shop-world", "SMPspawn");

        for (String id : shopsSec.getKeys(false)) {
            ConfigurationSection sec = shopsSec.getConfigurationSection(id);
            if (sec == null) continue;

            double price = sec.getDouble("price");
            String uuidStr = sec.getString("entity-uuid");
            UUID entityUUID = uuidStr != null ? parseUUID(uuidStr) : null;
            ItemStack item = savedItems.get(id);

            if (entityUUID == null) continue;

            ShopEntry entry = new ShopEntry(id, price, entityUUID, item);
            shops.put(id, entry);
            entityMap.put(entityUUID, id);
        }

        // Spawn missing armor stands (server restart recovery)
        Bukkit.getScheduler().runTaskLater(plugin, this::respawnMissingEntities, 40L);
    }

    private void respawnMissingEntities() {
        String worldName = plugin.getConfig().getString("shop-world", "SMPspawn");
        World world = Bukkit.getWorld(worldName);
        if (world == null) return;

        // Collect existing entity UUIDs
        Set<UUID> existing = new HashSet<>();
        for (Entity e : world.getEntities()) {
            existing.add(e.getUniqueId());
        }

        ConfigurationSection shopsSec = data.getConfigurationSection("shops");
        if (shopsSec == null) return;

        for (ShopEntry entry : shops.values()) {
            if (!existing.contains(entry.getEntityUUID())) {
                ConfigurationSection sec = shopsSec.getConfigurationSection(entry.getId());
                if (sec == null) continue;

                double x = sec.getDouble("x");
                double y = sec.getDouble("y");
                double z = sec.getDouble("z");
                float yaw = (float) sec.getDouble("yaw");

                Location loc = new Location(world, x, y, z, yaw, 0);
                ArmorStand stand = spawnStand(loc, entry.getItem());

                entityMap.remove(entry.getEntityUUID());
                entry.setEntityUUID(stand.getUniqueId());
                entityMap.put(stand.getUniqueId(), entry.getId());

                plugin.getHologramManager().createHologram(entry, stand.getLocation());
            } else {
                // Hologram may have been removed on restart
                Entity e = findEntityByUUID(world, entry.getEntityUUID());
                if (e != null) {
                    plugin.getHologramManager().createHologram(entry, e.getLocation());
                }
            }
        }
        saveShops();
    }

    public Entity findEntityByUUID(World world, UUID uuid) {
        for (Entity e : world.getEntities()) {
            if (e.getUniqueId().equals(uuid)) return e;
        }
        return null;
    }

    public ArmorStand spawnStand(Location loc, ItemStack helmet) {
        ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        stand.setGravity(false);
        stand.setAI(false);
        stand.setVisible(false);
        stand.setInvulnerable(true);
        if (helmet != null) stand.getEquipment().setHelmet(helmet);
        return stand;
    }

    public void saveShops() {
        if (data == null) data = YamlConfiguration.loadConfiguration(dataFile);

        // Save items
        for (Map.Entry<String, ItemStack> e : savedItems.entrySet()) {
            data.set("saved-items." + e.getKey(), e.getValue());
        }

        // Save shops
        String worldName = plugin.getConfig().getString("shop-world", "SMPspawn");
        World world = Bukkit.getWorld(worldName);

        for (ShopEntry entry : shops.values()) {
            String path = "shops." + entry.getId();
            data.set(path + ".price", entry.getPrice());
            data.set(path + ".entity-uuid", entry.getEntityUUID().toString());

            if (world != null) {
                Entity e = findEntityByUUID(world, entry.getEntityUUID());
                if (e != null) {
                    data.set(path + ".x", e.getLocation().getX());
                    data.set(path + ".y", e.getLocation().getY());
                    data.set(path + ".z", e.getLocation().getZ());
                    data.set(path + ".yaw", (double) e.getLocation().getYaw());
                }
            }
        }

        try {
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Nem sikerült menteni a shops.yml fájlt.", e);
        }
    }

    public void addSavedItem(String id, ItemStack item) {
        savedItems.put(id, item);
    }

    public ItemStack getSavedItem(String id) {
        return savedItems.get(id);
    }

    public boolean hasSavedItem(String id) {
        return savedItems.containsKey(id);
    }

    public void addShop(ShopEntry entry) {
        shops.put(entry.getId(), entry);
        entityMap.put(entry.getEntityUUID(), entry.getId());
        saveShops();
    }

    public void removeShop(String id) {
        ShopEntry entry = shops.remove(id);
        if (entry != null) entityMap.remove(entry.getEntityUUID());
        if (data != null) data.set("shops." + id, null);
        saveShops();
    }

    public ShopEntry getShopByEntityUUID(UUID uuid) {
        String id = entityMap.get(uuid);
        return id != null ? shops.get(id) : null;
    }

    public ShopEntry getShop(String id) {
        return shops.get(id);
    }

    public boolean isShopEntity(UUID uuid) {
        return entityMap.containsKey(uuid);
    }

    public Map<String, ShopEntry> getAllShops() {
        return Collections.unmodifiableMap(shops);
    }

    // Player state helpers
    public void setPriceEdit(UUID player, String shopId) { priceEditMap.put(player, shopId); }
    public String getPriceEdit(UUID player) { return priceEditMap.get(player); }
    public void clearPriceEdit(UUID player) { priceEditMap.remove(player); }

    public void setMove(UUID player, String shopId) { moveMap.put(player, shopId); }
    public String getMove(UUID player) { return moveMap.get(player); }
    public void clearMove(UUID player) { moveMap.remove(player); }

    public void setEdit(UUID player, String shopId) { editMap.put(player, shopId); }
    public String getEdit(UUID player) { return editMap.get(player); }
    public void clearEdit(UUID player) { editMap.remove(player); }

    public void addBuyCooldown(UUID player) { buyCooldown.add(player); }
    public boolean hasBuyCooldown(UUID player) { return buyCooldown.contains(player); }
    public void removeBuyCooldown(UUID player) { buyCooldown.remove(player); }

    private UUID parseUUID(String s) {
        try { return UUID.fromString(s); } catch (Exception e) { return null; }
    }
}
