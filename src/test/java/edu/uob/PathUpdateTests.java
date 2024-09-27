package edu.uob;

import edu.uob.command.CommandHandler;
import edu.uob.entity.GameEntity;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class PathUpdateTests {
    private GameServer server;

    @BeforeEach
    void setup() {
        // Create a GameServer instance
        File entitiesFile = Paths.get("config" + File.separator + "extended-entities.dot").toAbsolutePath().toFile();
        File actionsFile = Paths.get("config" + File.separator + "extended-actions.xml").toAbsolutePath().toFile();
        server = new GameServer(entitiesFile, actionsFile);
        CommandHandler commandHandler = server.getCommandHandler();
    }

    @AfterAll
    static void clean() {
        if (!GameEntity.getGameEntities().isEmpty()) {
            GameEntity.deleteGameEntities();
        }
    }

    String sendCommandToServer(String command) {
        // Try to send a command to the server - this call will time out if it takes too long (in case the server enters an infinite loop)
        return assertTimeoutPreemptively(Duration.ofMillis(1000000000), () -> { return server.handleCommand(command);},
                "Server took too long to respond (probably stuck in an infinite loop)");
    }

    @Test
    void testPathProduction() {
        sendCommandToServer("Justin: get axe");
        String cabinPaths = sendCommandToServer("Justin: look");
        assertTrue(cabinPaths.contains("forest"));

        sendCommandToServer("Justin: goto forest");
        String forestPaths = sendCommandToServer("Justin: look");
        assertTrue(forestPaths.contains("cabin"));
        assertTrue(forestPaths.contains("riverbank"));
        sendCommandToServer("Justin: get key");
        sendCommandToServer("Justin: chop tree");
        sendCommandToServer("Justin: get log");

        sendCommandToServer("Justin: goto riverbank");
        sendCommandToServer("Justin: get horn");
        String riverbankPaths = sendCommandToServer("Justin: look");
        assertTrue(riverbankPaths.contains("forest"));

        sendCommandToServer("Justin: goto forest");
        sendCommandToServer("Justin: goto cabin");
        sendCommandToServer("Justin: open trapdoor");
        cabinPaths = sendCommandToServer("Justin: look");
        assertTrue(cabinPaths.contains("forest"));
        assertTrue(cabinPaths.contains("cellar"));

        sendCommandToServer("Justin: goto cellar");
        String cellarPaths = sendCommandToServer("Justin: look");
        assertTrue(cellarPaths.contains("cabin"));

        // goto forest check paths goto riverbank check paths
        sendCommandToServer("Justin: goto cabin");
        sendCommandToServer("Justin: goto forest");
        forestPaths = sendCommandToServer("Justin: look");
        assertTrue(forestPaths.contains("cabin"));
        assertTrue(forestPaths.contains("riverbank"));

        // bridge river and check paths in all locations
        sendCommandToServer("Justin: goto riverbank");
        sendCommandToServer("Justin: bridge river");
        riverbankPaths = sendCommandToServer("Justin: look");
        assertTrue(riverbankPaths.contains("forest"));
        assertTrue(riverbankPaths.contains("clearing"));

        sendCommandToServer("Justin: goto forest");
        sendCommandToServer("Justin: goto cabin");
        sendCommandToServer("Justin: goto cellar");
        cellarPaths = sendCommandToServer("Justin: look");
        assertTrue(cellarPaths.contains("cabin"));

        sendCommandToServer("Justin: goto cabin");
        cabinPaths = sendCommandToServer("Justin: look");
        assertTrue(cabinPaths.contains("forest"));
        assertTrue(cabinPaths.contains("cellar"));

        sendCommandToServer("Justin: goto forest");
        forestPaths = sendCommandToServer("Justin: look");
        assertTrue(forestPaths.contains("cabin"));
        assertTrue(forestPaths.contains("riverbank"));

        sendCommandToServer("Justin: goto clearing");
        String clearingPaths = sendCommandToServer("Justin: look");
        assertTrue(clearingPaths.contains("riverbank"));

        // call lumberjack at clearing then destroy path
        sendCommandToServer("Justin: blow horn");
        assertTrue(sendCommandToServer("Justin: look").contains("lumberjack"));
        sendCommandToServer("Justin: goto riverbank");
        sendCommandToServer("Justin: destroy path over river");
        riverbankPaths = sendCommandToServer("Justin: look");
        assertTrue(riverbankPaths.contains("forest"));
        assertFalse(riverbankPaths.contains("clearing"));
        assertTrue(sendCommandToServer("Justin: goto clearing").toLowerCase().contains("error"));

        sendCommandToServer("Justin: goto forest");
        forestPaths = sendCommandToServer("Justin: look");
        assertTrue(forestPaths.contains("riverbank"));
        assertFalse(forestPaths.contains("clearing"));

        sendCommandToServer("Justin: goto cabin");
        cabinPaths = sendCommandToServer("Justin: look");
        assertTrue(cabinPaths.contains("forest"));
        assertFalse(cabinPaths.contains("clearing"));
    }
}
