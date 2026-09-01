package io.github.MLNaiba.simpleinvoice.exception;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ErrorResponse {
    private Integer statusCode;
    private String message;
    private LocalDateTime timestamp;
    private List<String> errors;
}
