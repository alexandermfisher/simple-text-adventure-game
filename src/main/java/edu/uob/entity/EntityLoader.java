package edu.uob.entity;

import com.alexmerz.graphviz.ParseException;
import com.alexmerz.graphviz.Parser;
import com.alexmerz.graphviz.objects.Graph;
import edu.uob.utils.ErrorType;
import edu.uob.utils.STAGException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class EntityLoader {
    public static class GameEntities {
        private final HashMap<String, Location> locations;
        private final Location startingLocation;

        protected GameEntities(HashMap<String, Location> locations, Location startingLocation) {
            this.locations = locations;
            this.startingLocation = startingLocation;
        }

        public HashMap<String, Location> getLocations() { return this.locations; }

        public Location getCurrentLocation() { return this.startingLocation; }
    }

    public static GameEntities loadEntities(File entitiesConfig) {
        HashMap<String, Location> locations = new HashMap<>();
        Location startingLocation = null;

        try (FileReader reader = new FileReader(entitiesConfig)) {
            Parser parser = new Parser();
            parser.parse(reader);
            ArrayList<Graph> locationGraphs = parser.getGraphs().get(0).getSubgraphs().get(0).getSubgraphs();
            // Parse each location specified in entitiesConfig:
            for (Graph locationGraph : locationGraphs) {
                String locationName = locationGraph.getNodes(false).get(0).getId().getId();
                String description = locationGraph.getNodes(false).get(0).getAttribute("description");
                Location location = new Location(locationName.toLowerCase(), description);
                parseLocation(location, locationGraph.getSubgraphs());
                locations.put(location.getName(), location);
                if (startingLocation == null) startingLocation = location;
            }
            // Parse paths subgraph:
            parsePaths(parser.getGraphs().get(0).getSubgraphs().get(1), locations);
        } catch (IOException | ParseException exception) {
            throw new STAGException.ConfigurationException(ErrorType.INVALID_ENTITIES_CONFIG);
        }
        // if no 'storeroom' in locations - make one and add to locations:
        if (!locations.containsKey("storeroom"))
             locations.put("storeroom", new Location("storeroom",
                     "Storage for any entities not placed in the game"));
        return new GameEntities(locations, startingLocation);
    }

    private static void parseLocation(Location location, ArrayList<Graph> subGraphs) {
        subGraphs.forEach(subGraph -> {
            String subGraphName = subGraph.getId().getId();
            switch (subGraphName) {
                case "characters":
                    subGraph.getNodes(false).forEach(node -> {
                        String characterName = node.getId().getId();
                        String description = node.getAttribute("description");
                        Character character = new Character(characterName.toLowerCase(), description, location);
                        location.addCharacter(character);
                    });
                    break;
                case "artefacts":
                    subGraph.getNodes(false).forEach(node -> {
                        String itemName = node.getId().getId();
                        String description = node.getAttribute("description");
                        Artefact item = new Artefact(itemName.toLowerCase(), description, location);
                        location.addItem(item);
                    });
                    break;
                case "furniture":
                    subGraph.getNodes(false).forEach(node -> {
                        String itemName = node.getId().getId();
                        String description = node.getAttribute("description");
                        Furniture furniture = new Furniture(itemName.toLowerCase(), description, location);
                        location.addFurniture(furniture);
                    });
                    break;
            }
        });
    }

    private static void parsePaths(Graph graph, HashMap<String, Location> locations) {
        List<Map.Entry<Location, Location>> pathEntries = graph.getEdges().stream()
                .map(edge -> {
                    Location sourceLocation = locations.get(edge.getSource().getNode().getId().getId());
                    Location targetLocation = locations.get(edge.getTarget().getNode().getId().getId());
                    return new AbstractMap.SimpleEntry<>(sourceLocation, targetLocation);
                })
                .filter(entry -> entry.getKey() != null && entry.getValue() != null)
                .collect(Collectors.toList());

        pathEntries.forEach(entry -> entry.getKey().addPath(entry.getValue()));
    }
}
