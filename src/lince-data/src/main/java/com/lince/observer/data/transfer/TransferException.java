package com.lince.observer.data.transfer;

import java.util.List;

/**
 * Base of the unchecked import/export failures (LO-349). Carries a {@link Reason} code the REST layer maps to
 * an HTTP status and the JavaFX menu maps to a message, plus the offending ids when the reason is about ids.
 * <p>
 * Suggested HTTP mapping: {@code UNKNOWN_TYPE}, {@code UNREADABLE}, {@code INSTRUMENT_REQUIRED},
 * {@code EMPTY_SELECTION}, {@code UNKNOWN_CRITERIA}, {@code UNKNOWN_RESEARCH}, {@code INVALID_OPTION} → 400; {@code TYPE_MISMATCH} → 415;
 * {@code IO} → 500.
 * <p>
 * Created by Alberto Soto. 21/10/26
 */
public abstract class TransferException extends RuntimeException {

    /**
     * Why an import or export failed.
     */
    public enum Reason {
        /** No type given and none detectable from the extension, or an unknown type id. */
        UNKNOWN_TYPE,
        /** The explicit type contradicts the file extension. */
        TYPE_MISMATCH,
        /** The content could not be parsed as the selected type. */
        UNREADABLE,
        /** The import needs an instrument the project does not have. */
        INSTRUMENT_REQUIRED,
        /** The type needs at least one criterion and the selection is empty. */
        EMPTY_SELECTION,
        /** Some requested criteria ids are not in the project. */
        UNKNOWN_CRITERIA,
        /** The requested research/register id is not in the project. */
        UNKNOWN_RESEARCH,
        /** An option value is not valid for the type; the offending ids are the option keys. */
        INVALID_OPTION,
        /** Reading or writing failed. */
        IO
    }

    private final Reason reason;
    private final List<String> offendingIds;

    protected TransferException(Reason reason, String message, List<String> offendingIds, Throwable cause) {
        super(message, cause);
        this.reason = reason;
        this.offendingIds = offendingIds == null ? List.of() : List.copyOf(offendingIds);
    }

    public Reason getReason() {
        return reason;
    }

    /**
     * @return the ids that caused {@link Reason#UNKNOWN_CRITERIA} / {@link Reason#UNKNOWN_RESEARCH}, the option keys of
     * {@link Reason#INVALID_OPTION}, empty otherwise
     */
    public List<String> getOffendingIds() {
        return offendingIds;
    }
}
