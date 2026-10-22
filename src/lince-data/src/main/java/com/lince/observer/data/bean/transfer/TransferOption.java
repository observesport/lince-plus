package com.lince.observer.data.bean.transfer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.List;

/**
 * One per-type option of an import or export (LO-349), enough for the dashboard to build a form field.
 * Values travel as strings in {@code options} maps; {@link #type()} says how to parse them.
 *
 * @param key          option key as sent in the request {@code options} map
 * @param label        human readable label (from the resource bundles where one exists)
 * @param type         value type
 * @param defaultValue default value as a string, null when there is none
 * @param values       allowed values for {@link Type#ENUM}, null otherwise
 * @param nullable     true when the option may be omitted without a default
 * <p>
 * Created by Alberto Soto. 21/10/26
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TransferOption(String key, String label, Type type, String defaultValue, List<String> values,
                             boolean nullable) {

    public TransferOption {
        values = values == null ? null : List.copyOf(values);
    }

    public static TransferOption choice(String key, String label, String defaultValue, List<String> values) {
        return new TransferOption(key, label, Type.ENUM, defaultValue, values, false);
    }

    /**
     * @param defaultValue default, null for none (the option is then nullable)
     */
    public static TransferOption integer(String key, String label, Integer defaultValue) {
        return new TransferOption(key, label, Type.INTEGER, defaultValue == null ? null : defaultValue.toString(), null,
                defaultValue == null);
    }

    public static TransferOption string(String key, String label, String defaultValue) {
        return new TransferOption(key, label, Type.STRING, defaultValue, null, defaultValue == null);
    }

    /**
     * Option value type, serialised in lowercase.
     */
    public enum Type {
        BOOLEAN("boolean"),
        ENUM("enum"),
        STRING("string"),
        INTEGER("integer");

        private final String label;

        Type(String label) {
            this.label = label;
        }

        @JsonValue
        public String getLabel() {
            return label;
        }
    }
}
