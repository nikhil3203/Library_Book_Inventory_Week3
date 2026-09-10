package library.exception;

/**
 * Thrown when an attempt is made to add or update a book with an ID or ISBN
 * that already exists in the library inventory.
 *
 * <p><b>Week 4 Refactoring:</b> Replaces the generic {@link IllegalArgumentException}
 * that was previously thrown for duplicate-ID and duplicate-ISBN scenarios.
 * The {@link #getField()} method lets callers determine which constraint was
 * violated ("ID" vs. "ISBN") without parsing the message string — a cleaner,
 * more maintainable approach.</p>
 *
 * @see library.service.LibraryService#addBook(library.model.Book)
 * @see library.service.LibraryService#updateBook(int, String, String, String, int)
 */
public class DuplicateBookException extends RuntimeException {

    private final String field;
    private final String value;

    /**
     * Constructs a DuplicateBookException.
     *
     * @param field the field that has a duplicate value (e.g. "ID", "ISBN")
     * @param value the duplicate value itself
     */
    public DuplicateBookException(String field, String value) {
        super("Duplicate book " + field + ": " + value);
        this.field = field;
        this.value = value;
    }

    /** Returns the name of the field that caused the conflict (e.g. "ID", "ISBN"). */
    public String getField() {
        return field;
    }

    /** Returns the conflicting value. */
    public String getValue() {
        return value;
    }
}
