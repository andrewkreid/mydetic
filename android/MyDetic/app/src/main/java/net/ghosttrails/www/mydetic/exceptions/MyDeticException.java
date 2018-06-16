package net.ghosttrails.www.mydetic.exceptions;

/** Base Exception class for MyDetic-specific problems. */
public class MyDeticException extends Exception {

  public MyDeticException(String message) {
    super(message);
  }

  public MyDeticException(String detailMessage, Throwable throwable) {
    super(detailMessage, throwable);
  }
}
