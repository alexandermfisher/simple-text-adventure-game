package edu.uob.command;

import edu.uob.action.BuiltInAction;
import edu.uob.action.GameAction;
import edu.uob.action.Health;
import edu.uob.entity.*;
import edu.uob.entity.Character;
import edu.uob.utils.ErrorType;
import edu.uob.utils.STAGException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class CommandHandler {
    private final Location startLocation;
    private final HashMap<String, Location> locations;
    private final HashMap<String, HashSet<GameAction>> actions;
    private final HashMap<String, Player> players;
    private Player currentPlayer = null;
    private String response = null;

    public CommandHandler(Location startLocation, HashMap<String, Location> locations,
                          HashMap<String, HashSet<GameAction>> actions) {
        this.startLocation = startLocation;
        this.locations = locations;
        this.actions = actions;
        this.players = new HashMap<>();
    }
    public String handle(String commandString) {
        response = null;
        ParseCommand.Command command = ParseCommand.parse(commandString, actions.keySet());
        setPlayer(command.playerName());

        if (command.builtInAction() != null) {
            processBuiltInAction(command.builtInAction(), command.entities());
            return response;
        }

        GameAction action = null;
        for (String triggerPhrase: command.triggerPhrase()) {
            GameAction tmp = getCustomAction(command, triggerPhrase);
            if (action != null && tmp != action)
                throw new STAGException.RuntimeException(ErrorType.MULTIPLE_TRIGGER_PHRASES);
            action = tmp;
        }

        assert action != null;
        for (GameEntity entity : action.consumedEntities()) consumeEntity(entity);
        for (GameEntity entity : action.producedEntities()) produceEntity(entity);
        if (action.healthAction() != null) Health.updateHealth(currentPlayer, action.healthAction());
        response = "\n\t" + action.narration();
        if (currentPlayer.getHealth() == 0) {response += "\n\t" + currentPlayer.processDeath(this.startLocation);}

        return response;
    }
    private void setPlayer(String playerName) {
        if ((currentPlayer = players.get(playerName.toLowerCase())) != null) return;

        // if player does not currently exist make new player and add to game:
        if (players.containsKey(playerName.toLowerCase()) ||
                GameEntity.getGameEntities().containsKey(playerName.toLowerCase()) ||
                actions.containsKey(playerName.toLowerCase()) ||
                BuiltInAction.isBuiltInAction(playerName.toLowerCase()))
            throw new STAGException.RuntimeException(ErrorType.DUPLICATED_PLAYER_NAME);

        currentPlayer = new Player(playerName, startLocation);
        players.put(playerName.toLowerCase(), currentPlayer);
        startLocation.addCharacter(currentPlayer);
    }
    private void processBuiltInAction(BuiltInAction builtInAction, ArrayList<String> entities) {
        switch (builtInAction) {
            case INVENTORY:
                processInventory(entities);
                break;
            case GET:
                processGet(entities);
                break;
            case DROP:
                processDrop(entities);
                break;
            case GOTO:
                processGoto(entities);
                break;
            case LOOK:
                processLook(entities);
                break;
            case HEALTH:
                processHealth(entities);
                break;
        }
    }
    private void processInventory(ArrayList<String> entities) {
        if (!entities.isEmpty()) throw new STAGException.RuntimeException(ErrorType.INVALID_COMMAND,
                "Invalid Command: 'inventory'/'inv' requires no additional subjects");
        response = currentPlayer.listItems();
    }
    private void processGet(ArrayList<String> entities) {
        if (entities.size() != 1)
            throw new STAGException.RuntimeException(ErrorType.INVALID_COMMAND,
                    "Invalid Command: 'get' requires one subject");

        Artefact item;
        if ((item = currentPlayer.getLocation().getItems().get(entities.get(0))) == null)
            throw new STAGException.RuntimeException(ErrorType.INVALID_COMMAND,
                    "Invalid Request: current location '" + this.currentPlayer.getLocation().getName()
                            + "' does not contain item " + entities.get(0));

        // process item into inventory and out of location and return response:
        currentPlayer.getItem(item);
        currentPlayer.getLocation().removeItem(item);
        item.setLocation(currentPlayer.getLocation());
        response = "\n\tYou have picked up item " + item;
    }
    private void processDrop(ArrayList<String> entities) {
        if (entities.size() != 1)
            throw new STAGException.RuntimeException(ErrorType.INVALID_COMMAND,
                    "Invalid Command: 'drop' requires one subject");

        Artefact item;
        if ((item = currentPlayer.dropItem(entities.get(0))) == null)
            throw new STAGException.RuntimeException(ErrorType.INVALID_COMMAND,
                    "Invalid Request: your inventory does not contain item " + entities.get(0));

        // process item into inventory and out of location and return response:
        currentPlayer.getLocation().addItem(item);
        response = "\n\tYou have dropped item " + item + " and placed in current location " +
                currentPlayer.getLocation();
    }
    private void processGoto(ArrayList<String> entities) {
        if (entities.size() != 1)
            throw new STAGException.RuntimeException(ErrorType.INVALID_COMMAND,
                    "Invalid Command: 'goto' requires one subject");

        Location location;
        if ((location = currentPlayer.getLocation().getPaths().get(entities.get(0))) == null) {
            String message;
            if (currentPlayer.getLocation().getName().equals(entities.get(0)))
                message = "Invalid request: You are already present in " + currentPlayer.getLocation().getName();
            else message = "Invalid request: A path between '" + currentPlayer.getLocation().getName() +
                    "' and '" + entities.get(0) + "' does not exist";
            throw new STAGException.RuntimeException(ErrorType.INVALID_COMMAND, message);
        }
        // move player and update locations:
        currentPlayer.getLocation().removeCharacter(currentPlayer);
        location.addCharacter(currentPlayer);
        currentPlayer.setLocation(location);
        for (Artefact item : currentPlayer.getInventory().values()) item.setLocation(location);

        response = "\n\tYou have moved and are now currently in location " + location;
    }
    private void processLook(ArrayList<String> entities) {
        if (!entities.isEmpty())
            throw new STAGException.RuntimeException(ErrorType.INVALID_COMMAND,
                    "Invalid Command: 'look' requires no additional subjects");

        Location currentLocation = currentPlayer.getLocation();
        StringBuilder responseBuilder = new StringBuilder("\n\tYou are in the '")
                .append(currentLocation.getName())
                .append("' - ")
                .append(currentLocation.getDescription())
                .append(".\n\tThe following entities are available: \n");

        // Print artefacts, furniture, characters, paths:
        printEntities(currentLocation.getItems().values(), responseBuilder, "Artefacts");
        printEntities(currentLocation.getFurniture().values(), responseBuilder, "Furniture");
        printEntities(currentLocation.getCharacters().values(), responseBuilder, "Characters");
        printEntities(currentLocation.getPaths().values(), responseBuilder, "Locations");

        response = responseBuilder.toString();
    }
    private void processHealth(ArrayList<String> entities) {
        if (!entities.isEmpty())
            throw new STAGException.RuntimeException(ErrorType.INVALID_COMMAND,
                    "Invalid Command: 'health' requires no additional subjects");
        response = "\n\t" +  currentPlayer.getName() + " currently is at health: " + currentPlayer.getHealth();
    }
    private void printEntities(Collection<? extends GameEntity> entities,
                               StringBuilder responseBuilder, String entityTypeName) {
        if (entities.isEmpty() || (entityTypeName.equals("Characters") && entities.size() == 1)) {
            return;
        }
        responseBuilder.append("\n\t| ").append(entityTypeName);
        responseBuilder.append("\n\t============================================================");

        for (GameEntity entity : entities) {
            if (entityTypeName.equals("Characters") && entity.equals(currentPlayer)) continue;
            responseBuilder.append("\n\t| ").append(entity.formatEntity());
        }

        responseBuilder.append("\n\t------------------------------------------------------------");
    }
    private GameAction getCustomAction(ParseCommand.Command command, String triggerPhrase) {
        HashSet<GameAction> actionSet = new HashSet<>(actions.get(triggerPhrase));
        ArrayList<String> entities = command.entities();

        // Each incoming command MUST contain a trigger phrase and at least one subject.
        actionSet.removeIf(action -> action.subjects().stream()
                        .noneMatch(entity -> entities.contains(entity.getName())));

        // When searching for a gameAction, you must match all subjects that are specified in the incoming command
        actionSet.removeIf(action ->
                entities.stream().map(GameEntity.getGameEntities()::get).anyMatch(entity ->
                                !action.consumedEntities().contains(entity) &&
                                !action.producedEntities().contains(entity) &&
                                !action.subjects().contains(entity)));

        // all subject entities must be available if in inventory or current location:
        Location playerLocation = currentPlayer.getLocation();
        actionSet.removeIf(action -> action.subjects().stream().anyMatch(entity ->
                                !currentPlayer.inventoryContains(entity.getName()) &&
                                !((entity instanceof Artefact ||
                                entity instanceof Character ||
                                entity instanceof Furniture) &&
                                entity.getLocation() == playerLocation) &&
                                !(entity instanceof Location && entity == playerLocation) ||
                                // Check if the entity is not in any other player's inventory
                                players.values().stream().anyMatch(otherPlayer ->
                                    otherPlayer != currentPlayer && otherPlayer.inventoryContains(entity.getName()))));

        // all other entities (consumed or produced must be in any location and not other player's inventory)
        players.values().forEach(player ->
                actionSet.removeIf(action -> action.producedEntities().stream().anyMatch(entity ->
                        player != currentPlayer &&
                        player.inventoryContains(entity.getName())) ||
                        action.consumedEntities().stream().anyMatch(entity -> player != currentPlayer &&
                                player.inventoryContains(entity.getName()))));

        if (actionSet.size() != 1) throw new STAGException.RuntimeException(ErrorType.INVALID_COMMAND);

        return actionSet.iterator().next();
    }
    private void consumeEntity(GameEntity entity) {
        if (entity instanceof Location) {
            currentPlayer.getLocation().removePath((Location) entity);
        } else if (currentPlayer.inventoryContains(entity.getName())) {
            currentPlayer.dropItem(entity.getName());
            entity.setLocation(locations.get("storeroom"));
            locations.get("storeroom").addItem((Artefact) entity);
        } else if (entity.getLocation() == currentPlayer.getLocation()) {
            if (entity instanceof Artefact) {
                currentPlayer.getLocation().removeItem((Artefact) entity);
                entity.setLocation(locations.get("storeroom"));
                locations.get("storeroom").addItem((Artefact) entity);
            } else if (entity instanceof Furniture) {
                currentPlayer.getLocation().removeFurniture((Furniture) entity);
                entity.setLocation(locations.get("storeroom"));
                locations.get("storeroom").addFurniture((Furniture) entity);
            }
        }
    }
    private void produceEntity(GameEntity entity) {
        if (entity instanceof Location) {
            currentPlayer.getLocation().addPath((Location) entity);
        } else if (entity instanceof Character) {
            if (!entity.getLocation().equals(currentPlayer.getLocation()) &&
                    !entity.getLocation().getName().equalsIgnoreCase("storeroom")) {
                if (!entity.getLocation().getPaths().containsKey(currentPlayer.getLocation().getName()))
                    throw new STAGException.RuntimeException(ErrorType.INVALID_COMMAND,
                    "Invalid Request: as no path exists between " + entity.getName() + " and yourself.");
            }
            entity.getLocation().removeCharacter((Character) entity);
            entity.setLocation(currentPlayer.getLocation());
            currentPlayer.getLocation().addCharacter((Character) entity);
        } else if (entity instanceof Furniture) {
            entity.getLocation().removeFurniture((Furniture) entity);
            entity.setLocation(currentPlayer.getLocation());
            currentPlayer.getLocation().addFurniture((Furniture) entity);
        } else if (entity instanceof Artefact) {
            entity.getLocation().removeItem((Artefact) entity);
            entity.setLocation(currentPlayer.getLocation());
            currentPlayer.getLocation().addItem((Artefact) entity);
        }
    }
}
