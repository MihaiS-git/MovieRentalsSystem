package ro.ccms.domain.validators;

public interface Validator<T> {
    void validate(T entity);
}
