package edu.uob.action;

import edu.uob.entity.GameEntity;
import edu.uob.utils.ErrorType;
import edu.uob.utils.STAGException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class ActionLoader {
    private static Health healthAction = null;
    public record GameActions(HashMap<String, HashSet<GameAction>> actions) {}
    public static GameActions loadActions(File actionsConfig, HashMap<String, GameEntity> gameEntities) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            Document document = factory.newDocumentBuilder().parse(actionsConfig);
            HashMap<String, HashSet<GameAction>> actions = parseActions(document, gameEntities);
            return new GameActions(actions);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new STAGException.ConfigurationException(ErrorType.INVALID_ACTIONS_CONFIG);
        }
    }

    private static HashMap<String, HashSet<GameAction>> parseActions(Document doc, HashMap<String, GameEntity> gameEntities)
            throws STAGException.ConfigurationException {

        HashMap<String, HashSet<GameAction>> actions = new HashMap<>();
        NodeList gameActionNodes = doc.getElementsByTagName("action");
        for (int i = 0; i < gameActionNodes.getLength(); i++) {
            healthAction = null;
            Node gameActionNode = gameActionNodes.item(i);
            if (gameActionNode.getNodeType() == Node.ELEMENT_NODE) {
                Element gameActionElement = (Element) gameActionNode;
                List<String> triggerPhrases = extractTriggerPhrases(gameActionElement, gameEntities);
                Set<GameEntity> subjects = extractEntities(gameActionElement, "subjects", gameEntities);
                if (subjects.isEmpty())  throw new STAGException.ConfigurationException(ErrorType.INVALID_ACTIONS_CONFIG);
                Set<GameEntity> consumedEntities = extractEntities(gameActionElement, "consumed", gameEntities);
                Set<GameEntity> producedEntities = extractEntities(gameActionElement, "produced", gameEntities);
                String narration = extractNarration(gameActionElement);
                GameAction gameAction = new GameAction(subjects, consumedEntities, producedEntities, narration, healthAction);
                triggerPhrases.forEach(trigger -> actions.computeIfAbsent(trigger, k -> new HashSet<>()).add(gameAction));
            }
        }
        return actions;
    }

    private static List<String> extractTriggerPhrases(Element gameActionElement, HashMap<String, GameEntity> gameEntities)
            throws STAGException.ConfigurationException {
        NodeList triggerNodes = gameActionElement.getElementsByTagName("keyphrase");
        List<String> triggerPhrases = new ArrayList<>();
        for (int j = 0; j < triggerNodes.getLength(); j++) {
            Element triggerElement = (Element) triggerNodes.item(j);
            String triggerPhrase = triggerElement.getTextContent().toLowerCase();
            validateTriggerPhrase(triggerPhrase, gameEntities);
            triggerPhrases.add(triggerPhrase);
        }
        return triggerPhrases;
    }

    private static void validateTriggerPhrase(String triggerPhrase, HashMap<String, GameEntity> gameEntities) {
        String[] words = triggerPhrase.split("\\s+");
        for (String word : words) {
            if (gameEntities.containsKey(word.toLowerCase()))
                throw new STAGException.ConfigurationException(ErrorType.INVALID_TRIGGER_PHRASE);
        }
    }

    private static Set<GameEntity> extractEntities(Element element, String entityType,
                                                   HashMap<String, GameEntity> gameEntities) {
        Set<GameEntity> entities = new HashSet<>();
        NodeList entityNodes = element.getElementsByTagName(entityType);
        for (int i = 0; i < entityNodes.getLength(); i++) {
            Node entityNode = entityNodes.item(i);
            NodeList childNodes = entityNode.getChildNodes();
            for (int j = 0; j < childNodes.getLength(); j++) {
                Node childNode = childNodes.item(j);
                if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element entityElement = (Element) childNode;
                    String entityName = entityElement.getTextContent().toLowerCase();
                    if (entityName.equalsIgnoreCase("health")) {
                        if (entityType.equalsIgnoreCase("produced")) healthAction = Health.INCREASE;
                        else healthAction = Health.DECREASE;
                    } else if (!gameEntities.containsKey(entityName)) {
                        throw new STAGException.ConfigurationException(ErrorType.ENTITY_NOT_FOUND);
                    } else {
                        entities.add(gameEntities.get(entityName));
                    }
                }
            }
        }
        return entities;
    }

    private static String extractNarration(Element gameActionElement) {
        NodeList narrationNodes = gameActionElement.getElementsByTagName("narration");
        if (narrationNodes.getLength() > 0) {
            Element narrationElement = (Element) narrationNodes.item(0);
            return narrationElement.getTextContent();
        }
        return "";
    }
}