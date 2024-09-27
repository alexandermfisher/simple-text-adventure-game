package edu.uob;

import edu.uob.command.CommandHandler;

import edu.uob.entity.GameEntity;
import edu.uob.entity.Player;
import edu.uob.utils.STAGException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class BuiltInActionTests {
    private GameServer server;
    private CommandHandler commandHandler;

    @BeforeEach
    void setup() {
        // Create a GameServer instance
        File entitiesFile = Paths.get("config" + File.separator + "basic-entities.dot").toAbsolutePath().toFile();
        File actionsFile = Paths.get("config" + File.separator + "basic-actions.xml").toAbsolutePath().toFile();
        server = new GameServer(entitiesFile, actionsFile);
        commandHandler = server.getCommandHandler();
    }

    @AfterAll
    static void clean() {
        if (!GameEntity.getGameEntities().isEmpty()) {
            GameEntity.deleteGameEntities();
        }
    }

    private Player getCurrentPlayer() {
        try {
            Class<?> commandHandlerClass = CommandHandler.class;
            Field currentPlayerField = commandHandlerClass.getDeclaredField("currentPlayer");
            currentPlayerField.setAccessible(true);
            return (Player) currentPlayerField.get(commandHandler);
        } catch (NoSuchFieldException | IllegalAccessException ignore) {}
        return null;
    }

    String sendCommandToServer(String command) {
        // Try to send a command to the server - this call will time out if it takes too long (in case the server enters an infinite loop)
        return assertTimeoutPreemptively(Duration.ofMillis(1000000000), () -> server.handleCommand(command),
                "Server took too long to respond (probably stuck in an infinite loop)");
    }

    @Test
    void testGettingDroppingArtefacts() {
        String response;

        // player has nothing in inventory at start
        sendCommandToServer("Justin: look");
        assertTrue(Objects.requireNonNull(getCurrentPlayer()).getInventory().isEmpty());

        // pick up available item
        sendCommandToServer("Justin: get potion");
        response = sendCommandToServer("Justin: inv").toLowerCase();
        assertTrue(response.contains("potion"));
        assertNull(getCurrentPlayer().getLocation().getItems().get("potion"));
        assertThrows(STAGException.RuntimeException.class, () -> commandHandler.handle("Justin: get trapdoor"));
        assertTrue(sendCommandToServer("Justin: get trapdoor").toLowerCase().contains("error"));

        // player drops item, now it's in the current location
        sendCommandToServer("Justin: drop potion");
        assertFalse(getCurrentPlayer().getInventory().containsKey("potion"));
        assertNotNull(getCurrentPlayer().getLocation().getItems().get("potion"));

        // player picks up two items, has both
        sendCommandToServer("Justin: get potion");
        sendCommandToServer("Justin: goto forest");
        sendCommandToServer("Justin: get key");
        response = sendCommandToServer("Justin: inv").toLowerCase();
        assertTrue(response.contains("potion") && response.contains("key"));

        // player drops one item, has one and the other is in the current location
        sendCommandToServer("Justin: drop potion");
        assertTrue(getCurrentPlayer().getInventory().containsKey("key"));
        assertNotNull(getCurrentPlayer().getLocation().getItems().get("potion"));

        // test picking up and dropping items that don't exist
        assertThrows(STAGException.RuntimeException.class, () ->
                commandHandler.handle("Justin: get fake_item"));
        assertTrue(sendCommandToServer("Justin: drop fake_item").toLowerCase().contains("error"));
    }

    @Test
    void testInvalidCommands() {
        // Invalid look tests
        //assertTrue(sendCommandToServer("Justin: look look").toLowerCase().contains("error"));
        assertTrue(sendCommandToServer("Justin: look for axe").toLowerCase().contains("error"));

        // Invalid inventory tests
        //assertTrue(sendCommandToServer("Justin: inv inventory").toLowerCase().contains("error"));
        assertTrue(sendCommandToServer("Justin: is the axe in my inventory?").toLowerCase().contains("error"));

        // Invalid get tests
        //assertTrue(sendCommandToServer("Justin: get the potion, get it now!").toLowerCase().contains("error"));
        assertTrue(sendCommandToServer("Justin: get the trapdoor").toLowerCase().contains("error"));
        assertTrue(sendCommandToServer("Justin: get cue").toLowerCase().contains("error"));
        assertTrue(sendCommandToServer("Justin: get axe and potion").toLowerCase().contains("error"));

        // Invalid drop tests
        assertTrue(sendCommandToServer("Justin: drop trapdoor").toLowerCase().contains("error"));
        assertTrue(sendCommandToServer("Justin: drop axe").toLowerCase().contains("error"));
        sendCommandToServer("Justin: get axe");
        sendCommandToServer("Justin: get potion");
        assertTrue(sendCommandToServer("Justin: drop potion and axe").toLowerCase().contains("error"));

        // Invalid goto tests
        assertTrue(sendCommandToServer("Justin: goto next location").toLowerCase().contains("error"));
        assertTrue(sendCommandToServer("Justin: goto axe").toLowerCase().contains("error"));
        assertTrue(sendCommandToServer("Justin: trapdoor goto").toLowerCase().contains("error"));
        sendCommandToServer("Justin: goto forest");
        sendCommandToServer("Justin: get key");
        sendCommandToServer("Justin: goto cabin");
        sendCommandToServer("Justin: unlock trapdoor");
        assertTrue(sendCommandToServer("Justin: goto forest or cellar").toLowerCase().contains("error"));
    }
}
