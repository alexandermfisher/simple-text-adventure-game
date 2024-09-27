package edu.uob.utils;
import java.io.Serial;

public class STAGException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1;

    public STAGException(ErrorType error, String message) { super(message); }

    public STAGException(ErrorType error) { this(error, error.getMessage()); }

    // Subclass for configuration exceptions
    public static class ConfigurationException extends STAGException {
        @Serial
        private static final long serialVersionUID = 1;

        public ConfigurationException(ErrorType error) { super(error); }
    }

    // Subclass for runtime exceptions
    public static class RuntimeException extends STAGException {
        @Serial
        private static final long serialVersionUID = 1;

        public RuntimeException(ErrorType error) { super(error); }

        public RuntimeException(ErrorType error, String message) { super(error, message); }
    }
}

