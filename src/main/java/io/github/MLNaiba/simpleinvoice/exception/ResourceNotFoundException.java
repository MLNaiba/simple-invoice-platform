package io.github.MLNaiba.simpleinvoice.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resourceType, Object id) {
        this(resourceType, "id", id);
    }

    public ResourceNotFoundException(
            String resourceType,
            String field,
            Object id) {
        super("%s with %s [%s] not found"
                .formatted(resourceType, field, id));
    }
}
