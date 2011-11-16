package net.codjo.confluence;
/**
 *
 */
public class ConfluenceException extends Exception {

    public ConfluenceException(String message) {
        super(message);
    }


    public ConfluenceException(String message, Throwable cause) {
        super(message, cause);
    }


    public ConfluenceException(Throwable cause) {
        super(cause);
    }
}
