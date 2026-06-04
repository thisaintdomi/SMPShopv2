package dev.virtualshop.manager;

import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ShopEntry {

    private final String id;
    private double price;
    private UUID entityUUID;
    private ItemStack item;

    public ShopEntry(String id, double price, UUID entityUUID, ItemStack item) {
        this.id = id;
        this.price = price;
        this.entityUUID = entityUUID;
        this.item = item;
    }

    public String getId() { return id; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public UUID getEntityUUID() { return entityUUID; }
    public void setEntityUUID(UUID uuid) { this.entityUUID = uuid; }
    public ItemStack getItem() { return item; }
    public void setItem(ItemStack item) { this.item = item; }
}
