package edu.uob.utils;

public class ServerResponse {
    private static final String ERROR = "\n\t[ERROR]: ";

    public static String success(String result) { return result + "\n"; }

    public static String error(STAGException exception) {
        return ERROR + exception.getMessage() + "\n";
    }

    public static String internalError() { return ERROR + "Internal server error occurred. Please try again later \n";}
}
