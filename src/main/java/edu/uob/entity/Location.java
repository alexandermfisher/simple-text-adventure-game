package edu.uob.entity;

import java.util.HashMap;

public class Location extends GameEntity {
    private final HashMap<String, Artefact> items;
    private final HashMap<String, Furniture> furniture;
    private final HashMap<String, Character> characters;
    private final HashMap<String, Location> paths;

    public Location(String name, String description) {
        super(name, description, null);
        this.items = new HashMap<>();
        this.furniture = new HashMap<>();
        this.characters = new HashMap<>();
        this.paths = new HashMap<>();
    }
    public void addItem(Artefact item) { this.items.put(item.getName(), item); }

    public void addFurniture(Furniture furniture) { this.furniture.put(furniture.getName(), furniture); }

    public void addCharacter(Character character) { this.characters.put(character.getName(), character); }

    public void addPath(Location location) { this.paths.put(location.getName(), location); }

    public void removeItem(Artefact item) { this.items.remove(item.getName()); }

    public void removeFurniture(Furniture furniture) { this.furniture.remove(furniture.getName()); }

    public void removeCharacter(Character character) { this.characters.remove(character.getName()); }

    public void removePath(Location location) { this.paths.remove(location.getName()); }

    public HashMap<String, Artefact> getItems() { return items; }

    public HashMap<String, Furniture> getFurniture() { return furniture; }

    public HashMap<String, Character> getCharacters() { return characters; }

    public HashMap<String, Location> getPaths() { return this.paths; }
}
