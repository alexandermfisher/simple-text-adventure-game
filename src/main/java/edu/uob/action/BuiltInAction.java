package edu.uob.action;

import java.util.Arrays;
import java.util.List;

public enum BuiltInAction {
    INVENTORY("inventory", "inv"),
    GET("get"),
    DROP("drop"),
    GOTO("goto"),
    LOOK("look"),
    HEALTH("health");

    public final List<String> actions;
    BuiltInAction(String... actions) { this.actions = Arrays.asList(actions); }
    public static boolean isBuiltInAction(String name) {
        for (BuiltInAction action : BuiltInAction.values()) if (action.actions.contains(name)) return true;
        return false;
    }
}