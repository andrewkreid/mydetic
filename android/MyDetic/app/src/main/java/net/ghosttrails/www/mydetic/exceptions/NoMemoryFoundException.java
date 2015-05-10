package net.ghosttrails.www.mydetic.exceptions;

import net.ghosttrails.www.mydetic.api.Utils;

import java.util.Date;

/**
 * Exception thrown when we expected a memory on a date but there wasn't one.
 */
public class NoMemoryFoundException extends MyDeticException {

    public NoMemoryFoundException(String userId, Date date) {
        super(String.format("No memory for %s on %s", userId, Utils.isoFormat(date)));
    }
}
