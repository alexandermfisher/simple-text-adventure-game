package edu.uob.entity;

import edu.uob.action.BuiltInAction;
import edu.uob.utils.ErrorType;
import edu.uob.utils.STAGException;

import java.util.HashMap;

public abstract class GameEntity {
    private final String name;
    private final String description;
    private Location location;
    private static HashMap<String, GameEntity> gameEntities = new HashMap<>();
    public GameEntity(String name, String description, Location location) {
        this.name = name;
        this.description = description;
        this.location = location;
        if (BuiltInAction.isBuiltInAction(name.toLowerCase()) || gameEntities.containsKey(name.toLowerCase()))
            throw new STAGException.ConfigurationException(ErrorType.INVALID_ENTITY_NAME);
        gameEntities.put(this.name, this);
    }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }
    public static HashMap<String, GameEntity> getGameEntities() { return new HashMap<>(gameEntities); }
    public static void deleteGameEntities() { gameEntities = new HashMap<>(); }
    @Override
    public int hashCode() { return name.hashCode(); }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        GameEntity entity = (GameEntity) obj;
        return name.equals(entity.name);
    }
    @Override
    public String toString() { return "'" + name + " : " + description + "'"; }
    public String formatEntity() {
        String formattedName = String.format("%-8s", this.getName());
        return String.format("\t%s: %s", formattedName, this.description);
    }
}
