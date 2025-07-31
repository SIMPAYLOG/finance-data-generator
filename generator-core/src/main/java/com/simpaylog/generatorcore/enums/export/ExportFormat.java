package com.simpaylog.generatorcore.enums.export;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;

@AllArgsConstructor
@Getter
public enum ExportFormat {
    CSV("csv", "text/csv"),
    JSON("json", "application/json");

    private final String value;
    private final String mimeType;

    public static Optional<ExportFormat> fromString(String text) {
        if (text == null) { // text가 비어있는 경우
            return Optional.empty();
        }
        for (ExportFormat format : ExportFormat.values()) {
            if (format.value.equalsIgnoreCase(text)) {
                return Optional.of(format);
            }
        }
        return Optional.empty(); //일치하는 text가 없는 경우
    }
}
