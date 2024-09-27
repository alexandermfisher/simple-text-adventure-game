package edu.uob.action;

import edu.uob.entity.GameEntity;

import java.util.Set;

public record GameAction(Set<GameEntity> subjects,
                         Set<GameEntity> consumedEntities,
                         Set<GameEntity> producedEntities,
                         String narration,
                         Health healthAction) {}

