package net.codjo.confluence;
/**
 *
 */
public class ConfluenceTimeoutException extends ConfluenceException {
    public ConfluenceTimeoutException(Exception exception) {
        super("D�lai d'attente d�pass�.", exception);
    }
}
