package edu.uob.action;

import edu.uob.entity.Player;

public enum Health {
    INCREASE,
    DECREASE;
    public static void updateHealth(Player player, Health healthType) {
        if (healthType == INCREASE && player.getHealth() < 3) {
            player.setHealth(INCREASE);
        } else if (healthType == DECREASE) {
            player.setHealth(DECREASE);
        }
    }
}