package com.lince.observer.desktop.spring.controller.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lince.observer.data.transfer.TransferException;

import java.util.List;
import java.util.Locale;

/**
 * JSON body of a 4xx answer from {@code /api/import} and {@code /api/export} (LO-349).
 *
 * @param reason  lowercase reason code ({@code unknown_type}, {@code type_mismatch}, {@code unknown_criteria}, ...)
 * @param message human readable detail
 * @param ids     offending criteria or research ids, omitted when there are none
 * <p>
 * Created by Alberto Soto. 21/10/26
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TransferError(String reason, String message, List<String> ids) {

    static TransferError of(TransferException e) {
        List<String> ids = e.getOffendingIds().isEmpty() ? null : e.getOffendingIds();
        return new TransferError(code(e.getReason()), e.getMessage(), ids);
    }

    static String code(TransferException.Reason reason) {
        return reason.name().toLowerCase(Locale.ROOT);
    }
}
