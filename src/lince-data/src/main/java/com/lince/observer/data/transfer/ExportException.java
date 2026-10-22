package com.lince.observer.data.transfer;

import java.util.List;

/**
 * Unchecked export failure with a {@link Reason} code (LO-349). See {@link TransferException}.
 * <p>
 * Created by Alberto Soto. 21/10/26
 */
public class ExportException extends TransferException {

    public ExportException(Reason reason, String message) {
        super(reason, message, null, null);
    }

    public ExportException(Reason reason, String message, Throwable cause) {
        super(reason, message, null, cause);
    }

    public ExportException(Reason reason, String message, List<String> offendingIds) {
        super(reason, message, offendingIds, null);
    }
}
