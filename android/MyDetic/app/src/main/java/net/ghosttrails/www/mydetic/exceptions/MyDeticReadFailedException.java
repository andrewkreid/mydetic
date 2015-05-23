package net.ghosttrails.www.mydetic.exceptions;

/**
 * Exception thrown when a read operation on the underlying API
 * has failed.
 */
public class MyDeticReadFailedException extends MyDeticException {
    public MyDeticReadFailedException(String message) {
        super(message);
    }
}
