package edu.uob;

import edu.uob.entity.Character;
import edu.uob.entity.*;
import edu.uob.entity.EntityLoader.GameEntities;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class EntityLoaderTests {

    private static GameEntities gameEntities;

    @BeforeAll
    static void setup() {
        File entitiesFile = Paths.get("config" + File.separator + "basic-entities.dot").toAbsolutePath().toFile();
        gameEntities = EntityLoader.loadEntities(entitiesFile);
    }

    @AfterAll
    static void clean() {
        if (!GameEntity.getGameEntities().isEmpty()) {
            GameEntity.deleteGameEntities();
        }
    }

    @Test
    void testLocationsLoaded() {
        assertNotNull(gameEntities);

        // Check that specific locations are loaded
        assertTrue(gameEntities.getLocations().containsKey("cabin"));
        assertTrue(gameEntities.getLocations().containsKey("forest"));
        assertTrue(gameEntities.getLocations().containsKey("cellar"));
        assertTrue(gameEntities.getLocations().containsKey("storeroom"));
    }

    @Test
    void testStartingLocationSet() {
        assertNotNull(gameEntities.getCurrentLocation());
        assertEquals("cabin", gameEntities.getCurrentLocation().getName());
    }

    @Test
    void testPathsLoaded() {
        Location cabin = gameEntities.getLocations().get("cabin");
        Location forest = gameEntities.getLocations().get("forest");
        Location cellar = gameEntities.getLocations().get("cellar");

        assertTrue(cabin.getPaths().containsValue(forest));
        assertTrue(forest.getPaths().containsValue(cabin));
        assertTrue(cellar.getPaths().containsValue(cabin));
        //assertTrue(cellar.getPaths().containsValue(forest));
    }

    @Test
    void testCabinEntities() {
        Location cabin = gameEntities.getLocations().get("cabin");
        assertNotNull(cabin);

        // Check entities in the cabin location
        assertEquals("A log cabin in the woods", cabin.getDescription());

        // Characters in cabin
        assertEquals(0, cabin.getCharacters().size());

        // Artefacts in cabin
        assertEquals(2, cabin.getItems().size());
        for (Artefact artefact : cabin.getItems().values()) {
            assertTrue(artefact.getName().equalsIgnoreCase("axe")
                    || artefact.getName().equalsIgnoreCase("potion"));
            assertTrue(artefact.getDescription().toLowerCase().contains("razor sharp axe") ||
                    artefact.getDescription().toLowerCase().contains("magic potion"));
        }

        // Furniture in cabin
        assertEquals(1, cabin.getFurniture().size());
        Furniture trapdoor = cabin.getFurniture().values().iterator().next();
        assertEquals("trapdoor", trapdoor.getName());
        assertEquals("Wooden trapdoor", trapdoor.getDescription());
    }

    @Test
    void testForestEntities() {
        Location forest = gameEntities.getLocations().get("forest");
        assertNotNull(forest);

        // Check entities in the forest location
        assertEquals("A dark forest", forest.getDescription());

        // Artefacts in forest
        assertEquals(1, forest.getItems().size());
        Artefact key = forest.getItems().values().iterator().next();
        assertEquals("key", key.getName());
        assertEquals("Brass key", key.getDescription());

        // Furniture in forest
        assertEquals(1, forest.getFurniture().size());
        Furniture tree = forest.getFurniture().values().iterator().next();
        assertEquals("tree", tree.getName());
        assertEquals("A big tree", tree.getDescription());
    }

    @Test
    void testCellarEntities() {
        Location cellar = gameEntities.getLocations().get("cellar");
        assertNotNull(cellar);

        // Check entities in the cellar location
        assertEquals("A dusty cellar", cellar.getDescription());

        // Characters in cellar
        assertEquals(1, cellar.getCharacters().size());
        Character elf = cellar.getCharacters().values().iterator().next();
        assertEquals("elf", elf.getName());
        assertEquals("Angry Elf", elf.getDescription()); // Assuming same description as in 'characters' subgraph
    }

    @Test
    void testStoreroomEntities() {
        Location storeroom = gameEntities.getLocations().get("storeroom");
        assertNotNull(storeroom);

        // Check entities in the storeroom location
        assertEquals("Storage for any entities not placed in the game", storeroom.getDescription());

        // Artefacts in storeroom
        assertEquals(1, storeroom.getItems().size());
        Artefact log = storeroom.getItems().values().iterator().next();
        assertEquals("log", log.getName());
        assertEquals("A heavy wooden log", log.getDescription());

        // No characters or furniture in storeroom (based on DOT file)
        assertTrue(storeroom.getCharacters().isEmpty());
        assertTrue(storeroom.getFurniture().isEmpty());
    }
}
