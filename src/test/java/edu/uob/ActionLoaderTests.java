package edu.uob;

import edu.uob.action.ActionLoader;
import edu.uob.action.Health;
import edu.uob.entity.EntityLoader;
import edu.uob.entity.GameEntity;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class ActionLoaderTests {

    private static ActionLoader.GameActions gameActions;
    private static HashMap<String, GameEntity> gameEntities;

    @BeforeAll
    static void setup() {
        // Load entities from the specified DOT file
        File entitiesFile = Paths.get("config" + File.separator + "basic-entities.dot").toAbsolutePath().toFile();
        EntityLoader.GameEntities entities = EntityLoader.loadEntities(entitiesFile);
        gameEntities = GameEntity.getGameEntities();

        // Load actions from the specified XML configuration file
        File actionsFile = Paths.get("config" + File.separator + "basic-actions.xml").toAbsolutePath().toFile();
        gameActions = ActionLoader.loadActions(actionsFile, gameEntities);
    }

    @AfterAll
    static void clean() {
        if (!GameEntity.getGameEntities().isEmpty()) {
            GameEntity.deleteGameEntities();
        }
    }

    @Test
    void testActionsLoaded() {
        assertNotNull(gameActions);

        // Check that specific trigger phrases are loaded
        assertTrue(gameActions.actions().containsKey("open"));
        assertTrue(gameActions.actions().containsKey("unlock"));
        assertTrue(gameActions.actions().containsKey("chop"));
        assertTrue(gameActions.actions().containsKey("cut"));
        assertTrue(gameActions.actions().containsKey("cutdown"));
        assertTrue(gameActions.actions().containsKey("drink"));
        assertTrue(gameActions.actions().containsKey("fight"));
        assertTrue(gameActions.actions().containsKey("hit"));
        assertTrue(gameActions.actions().containsKey("attack"));
    }

    @Test
    void testActionExecution() {
        assertNotNull(gameActions);

        // Action: "open" or "unlock" with "trapdoor" and "key" as subjects
        assertTrue(gameActions.actions().containsKey("open"));
        assertTrue(gameActions.actions().containsKey("unlock"));
        assertTrue(gameActions.actions().get("open").stream().anyMatch(action ->
                action.subjects().contains(gameEntities.get("trapdoor")) &&
                        action.subjects().contains(gameEntities.get("key"))));
        gameActions.actions().get("open").forEach(action -> {
            assertEquals(1, action.consumedEntities().size());
            assertTrue(action.consumedEntities().contains(gameEntities.get("key")));
            assertEquals(1, action.producedEntities().size());
            assertTrue(action.producedEntities().contains(gameEntities.get("cellar")));
        });

        // Action: "chop" or "cut" or "cutdown" with "tree" and "axe" as subjects
        assertTrue(gameActions.actions().containsKey("chop"));
        assertTrue(gameActions.actions().containsKey("cut"));
        assertTrue(gameActions.actions().containsKey("cutdown"));
        assertTrue(gameActions.actions().get("chop").stream().anyMatch(action ->
                action.subjects().contains(gameEntities.get("tree")) &&
                        action.subjects().contains(gameEntities.get("axe"))));
        gameActions.actions().get("chop").forEach(action -> {
            assertEquals(1, action.consumedEntities().size());
            assertTrue(action.consumedEntities().contains(gameEntities.get("tree")));
            assertEquals(1, action.producedEntities().size());
            assertTrue(action.producedEntities().contains(gameEntities.get("log")));
        });

        // Action: "drink" with "potion" as subject
        assertTrue(gameActions.actions().containsKey("drink"));
        assertTrue(gameActions.actions().get("drink").stream().anyMatch(action ->
                action.subjects().contains(gameEntities.get("potion"))));
        gameActions.actions().get("drink").forEach(action -> {
            assertEquals(1, action.consumedEntities().size());
            assertTrue(action.consumedEntities().contains(gameEntities.get("potion")));
            assertEquals(0, action.producedEntities().size());
            assertEquals(Health.INCREASE, action.healthAction());
        });

        // Action: "fight" or "hit" or "attack" with "elf" as subject
        assertTrue(gameActions.actions().containsKey("fight"));
        assertTrue(gameActions.actions().containsKey("hit"));
        assertTrue(gameActions.actions().containsKey("attack"));
        assertTrue(gameActions.actions().get("fight").stream().anyMatch(action ->
                action.subjects().contains(gameEntities.get("elf"))));
        gameActions.actions().get("fight").forEach(action -> {
            assertEquals(0, action.consumedEntities().size());
            assertEquals(0, action.producedEntities().size());
            assertEquals(Health.DECREASE, action.healthAction());
        });
    }
}
