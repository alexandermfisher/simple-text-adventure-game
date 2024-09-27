package edu.uob.utils;

public enum ErrorType {
    // Configuration Errors - Return to Server:
    INVALID_ENTITIES_CONFIG("INVALID ENTITIES CONFIGURATION: FAILED TO INITIALISE GAME."),
    INVALID_ACTIONS_CONFIG("INVALID ACTIONS CONFIGURATION: FAILED TO INITIALISE GAME."),
    INVALID_ENTITY_NAME("INVALID ENTITY NAME: FAILED TO INSTANTIATE ENTITY DUE TO DUPLICATE NAME."),
    ENTITY_NOT_FOUND("INVALID ACTIONS CONFIGURATION: ENTITY REFERENCED IN ACTIONS CONFIG NOT FOUND"),
    INVALID_TRIGGER_PHRASE("INVALID TRIGGER PHRASE: TRIGGER PHRASES CANNOT CONTAIN THE NAMES OF ENTITIES"),

    // Runtime Errors - Return to Client:
    MULTIPLE_TRIGGER_PHRASES("Invalid command: must contain either a single built-in-action or custom trigger phrase"),
    INVALID_PLAYER_NAME("Invalid player name: can only consist of uppercase and lowercase letters, spaces, apostrophes, and hyphens"),
    DUPLICATED_PLAYER_NAME("Invalid player name: name must not be in use by either another player, entity, trigger phrase, or built-in-action"),
    INVALID_COMMAND("Invalid command");

    private final String message;

    ErrorType(String message) { this.message = message; }

    public String getMessage() { return message; }
}