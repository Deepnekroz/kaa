package org.kaaproject.kaa.server.transport;

/**
 * An exception thrown by the server if an endpoint tries to connect with an SDK
 * configured to verify endpoint credentials and fails the verification process.
 *
 * @author Bohdan Khablenko
 *
 * @since v0.9.0
 */
public class EndpointNotRegisteredException extends Exception {

    private static final long serialVersionUID = 1000L;

    /**
     * Constructs a new exception with no detail message.
     */
    public EndpointNotRegisteredException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message The detail message
     */
    public EndpointNotRegisteredException(String message) {
        super(message);
    }
}
