package io.github.MLNaiba.simpleinvoice.exception;

public class ResourceAlreadyExistsException extends RuntimeException {
  public ResourceAlreadyExistsException(String resourceType, Object id) {
    this(resourceType, "id", id);
  }

  public ResourceAlreadyExistsException(
          String resourceType,
          String field,
          Object id) {
    super("%s with %s [%s] already exists"
            .formatted(resourceType, field, id));
  }
}
