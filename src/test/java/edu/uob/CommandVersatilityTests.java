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
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

public class CommandVersatilityTests {
    private GameServer server;
    @BeforeEach
    void setup() {
        // Create a GameServer instance
        File entitiesFile = Paths.get("config" + File.separator + "basic-entities.dot").toAbsolutePath().toFile();
        File actionsFile = Paths.get("config" + File.separator + "basic-actions.xml").toAbsolutePath().toFile();
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
    void testDecoratedCommands() {
        String response = sendCommandToServer("Justin: Look around").toLowerCase();
        assertTrue(response.contains("cabin"));
        assertTrue(response.contains("potion"));
        sendCommandToServer("Justin: get axe");
        sendCommandToServer("Justin: goto the forest");
        response = sendCommandToServer("Justin: please chop the tree using the axe").toLowerCase();
        assertTrue(response.contains("tree with the axe"));
        sendCommandToServer("Justin: get key");
        sendCommandToServer("Justin: goto cabin");
        response = sendCommandToServer("Justin: unlock the trapdoor using the key").toLowerCase();
        assertTrue(response.contains("you unlock the trapdoor"));
    }
    @Test
    void testWordOrdering() {
        sendCommandToServer("Justin: get axe");
        sendCommandToServer("Justin: goto the forest");
        String response = sendCommandToServer("Justin: use axe to chop the tree").toLowerCase();
        assertTrue(response.contains("tree with the axe"));
        sendCommandToServer("Justin: key get");
        sendCommandToServer("Justin: cabin goto");
        response = sendCommandToServer("Justin: using the key, trapdoor open it").toLowerCase();
        assertTrue(response.contains("you unlock the trapdoor"));
    }
    @Test
    void testPartialCommands() {
        sendCommandToServer("Justin: goto the forest");
        sendCommandToServer("Justin: key get");
        sendCommandToServer("Justin: cabin goto");
        String response = sendCommandToServer("Justin: unlock trapdoor").toLowerCase();
        assertTrue(response.contains("you unlock the trapdoor"));
        response = sendCommandToServer("Justin: get").toLowerCase();
        assertTrue(response.contains("error"));
    }
    @Test
    void testExtraneousEntities() {
        // Extraneous entities
        String response = sendCommandToServer("Justin: open potion with axe").toLowerCase();
        assertTrue(response.contains("error"));
        sendCommandToServer("Justin: goto the forest");
        response = sendCommandToServer("Justin: get key from forest").toLowerCase();
        assertTrue(response.contains("error"));
        sendCommandToServer("Justin: goto the cabin");
        response = sendCommandToServer("Justin: use key on axe").toLowerCase();
        assertTrue(response.contains("error"));
        response = sendCommandToServer("Justin: goto forest and then cabin").toLowerCase();
        assertTrue(response.contains("error"));
    }
    @Test
    void testAmbiguousCommands() {
        // Ambiguous commands
        String response = sendCommandToServer("Justin: open").toLowerCase();
        assertTrue(response.contains("error"));
        response = sendCommandToServer("Justin: open trapdoor with key and drink potion").toLowerCase();
        assertTrue(response.contains("error"));
    }
    @Test
    void testCompositeCommands() {
        sendCommandToServer("Justin: goto the forest");
        sendCommandToServer("Justin: key get");
        sendCommandToServer("Justin: cabin goto");
        String response = sendCommandToServer("Justin: get axe and potion").toLowerCase();
        assertTrue(response.contains("error"));
        response = sendCommandToServer("Justin: drink and unlock trapdoor").toLowerCase();
        assertTrue(response.contains("error"));
    }
    @Test
    void testUsernamesFormat() {
        String response = sendCommandToServer("Justin_Siu: look").toLowerCase();
        assertTrue(response.contains("error"));
        response = sendCommandToServer("Justin*?: look").toLowerCase();
        assertTrue(response.contains("error"));
        response = sendCommandToServer("Justin Siu: goto forest").toLowerCase();
        assertTrue(response.contains("forest"));
        response = sendCommandToServer("Justin-Siu: goto forest").toLowerCase();
        assertTrue(response.contains("forest"));
        response = sendCommandToServer("Ronnie O'Sullivan: goto forest").toLowerCase();
        assertTrue(response.contains("forest"));
    }
}
