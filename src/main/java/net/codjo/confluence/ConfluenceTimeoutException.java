package net.codjo.confluence;
/**
 *
 */
public class ConfluenceTimeoutException extends ConfluenceException {
    public ConfluenceTimeoutException(Exception exception) {
        super("Délai d'attente dépassé.", exception);
    }
}
