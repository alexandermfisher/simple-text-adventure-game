package edu.uob.entity;

import edu.uob.action.Health;

import java.util.HashMap;

public class Player extends Character {
    private final HashMap<String, Artefact> inventory;
    private int health;
    public Player(String name, Location location) {
        super(name, "A player with the name: '" + name + "'", location);
        this.inventory = new HashMap<>();
        this.health = 3;
    }
    public boolean inventoryContains(String entityName) { return inventory.containsKey(entityName); }
    public HashMap<String, Artefact> getInventory() { return inventory; }
    public void getItem(Artefact item) { this.inventory.put(item.getName(), item); }
    public Artefact dropItem(String itemName) {return inventory.remove(itemName); }
    public String listItems() {
        if (inventory.isEmpty()) { return "\n\tYour inventory is currently empty"; }
        final StringBuilder responseBuilder = new StringBuilder();
        responseBuilder.append("\n\tYour inventory currently contains:\n");
        responseBuilder.append("\n\t| ").append(this.getName()).append("'s Inventory: ");
        responseBuilder.append("\n\t============================================================");
        for (Artefact item : inventory.values()) { responseBuilder.append("\n\t| ").append(item.formatEntity()); }
        responseBuilder.append("\n\t------------------------------------------------------------");

        return responseBuilder.toString();
    }
    public int getHealth() { return this.health; }
    public void setHealth(Health type) {
        if (type == Health.INCREASE) this.health++;
        if (type == Health.DECREASE) this.health--;
    }
    public String processDeath(Location spawnLocation) {
        for (String itemName : this.inventory.keySet().stream().toList()) {
            Artefact item = dropItem(itemName);
            item.setLocation(this.getLocation());
            this.getLocation().addItem(item);
        }
        this.getLocation().removeCharacter(this);
        spawnLocation.addCharacter(this);
        this.setLocation(spawnLocation);
        this.health = 3;
        return "\n\tYou have died! You have now re-spawned in '" + spawnLocation.getName() + "' with an empty inventory.";
    }
}