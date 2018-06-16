package net.ghosttrails.www.mydetic.exceptions;

/** Exception thrown when a write operation to the underlying API has failed. */
public class MyDeticWriteFailedException extends MyDeticException {
  public MyDeticWriteFailedException(String message) {
    super(message);
  }
}
