package edu.uob.command;

import edu.uob.action.BuiltInAction;
import edu.uob.entity.GameEntity;
import edu.uob.utils.ErrorType;
import edu.uob.utils.STAGException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ParseCommand {
    public record Command(String playerName, BuiltInAction builtInAction, HashSet<String> triggerPhrase,
                          ArrayList<String> entities) {}

    public static Command parse(String command, Set<String> availableTriggerPhrases) {
        // Extract players name:
        StringTokenizer tokenizer = new StringTokenizer(command, ":");
        String playerName = tokenizer.nextToken().trim();
        validatePlayerName(playerName);

        // Reconstruct command and remove punctuation ready for parsing:
        StringBuilder reconstructedCommand = new StringBuilder();
        while (tokenizer.hasMoreTokens()) reconstructedCommand.append(tokenizer.nextToken()).append(" ");
        String commandAndEntities = reconstructedCommand.toString().trim();
        Pattern punctuationPattern = Pattern.compile("[\\p{Punct}&&[^']]");
        commandAndEntities = punctuationPattern.matcher(commandAndEntities).replaceAll(" ");

        // Tokenize the command and entities to handle decorated commands
        StringTokenizer commandTokenizer = new StringTokenizer(commandAndEntities, " ");
        ArrayList<String> words = new ArrayList<>();
        while (commandTokenizer.hasMoreTokens()) {
            String word = commandTokenizer.nextToken().toLowerCase();
            words.add(word);
        }
        // Extracting entities from the command
        Set<String> gameEntities = GameEntity.getGameEntities().keySet();
        ArrayList<String> entities = new ArrayList<>();
        for (String word : words) {
            if (gameEntities.contains(word) && !entities.contains(word)) {
                entities.add(word);
            }
        }
        // Extracting built-in action keywords from the command
        HashSet<BuiltInAction> builtInActions = findBuiltInActions(words);

        // Extracting trigger phrases from the command
        HashSet<String> triggerPhrases = findTriggerPhrases(words, availableTriggerPhrases);

        // Check if any trigger phrases or built in actions are found
        if (builtInActions.size() == 1 && triggerPhrases.isEmpty())
            return new Command(playerName, builtInActions.stream().toList().get(0), null, entities);

        if (builtInActions.isEmpty() && !triggerPhrases.isEmpty())
            return new Command(playerName, null, triggerPhrases, entities);

        throw new STAGException.RuntimeException(ErrorType.MULTIPLE_TRIGGER_PHRASES);
    }
    private static void validatePlayerName(String playerName) {
        String PLAYER_NAME_PATTERN = "^[a-zA-Z\\s'-]+$";
        Pattern playerNamePattern = Pattern.compile(PLAYER_NAME_PATTERN);
        Matcher matcher = playerNamePattern.matcher(playerName);
        if (!matcher.matches()) throw new STAGException.RuntimeException(ErrorType.INVALID_PLAYER_NAME);
    }
    private static HashSet<BuiltInAction> findBuiltInActions(ArrayList<String> words) {
        return words.stream()
                .map(String::toLowerCase)
                .flatMap(word -> Arrays.stream(BuiltInAction.values()).filter(action -> action.actions.contains(word)))
                .collect(Collectors.toCollection(HashSet::new));
    }
    private static HashSet<String> findTriggerPhrases(ArrayList<String> words, Set<String> availableTriggerPhrases) {
        return availableTriggerPhrases.stream()
                .filter(trigger -> Arrays.stream(trigger.split(" "))
                        .allMatch(word -> words.contains(word.toLowerCase())))
                .map(String::toLowerCase)
                .collect(Collectors.toCollection(HashSet::new));
    }
}
