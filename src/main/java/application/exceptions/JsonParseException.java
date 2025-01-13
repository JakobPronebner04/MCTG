package application.exceptions;
public class JsonParseException extends Exception {

    // Konstruktor ohne Nachricht
    public JsonParseException() {
        super();
    }

    // Konstruktor mit Nachricht
    public JsonParseException(String message) {
        super(message);
    }

    // Konstruktor mit Nachricht und Ursache
    public JsonParseException(String message, Throwable cause) {
        super(message, cause);
    }

    // Konstruktor mit nur Ursache
    public JsonParseException(Throwable cause) {
        super(cause);
    }
}
