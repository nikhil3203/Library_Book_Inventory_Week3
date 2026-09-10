package library.exception;

/**
 * Thrown when a requested book cannot be found in the library inventory.
 *
 * <p><b>Week 4 Refactoring:</b> Replaces the generic {@link IllegalArgumentException}
 * that was previously thrown for "book not found" scenarios. Using a custom exception
 * allows callers to distinguish "not found" errors from other argument errors
 * programmatically, improving error handling and code clarity.</p>
 *
 * @see library.service.LibraryService#findById(int)
 * @see library.service.LibraryService#updateBook(int, String, String, String, int)
 * @see library.service.LibraryService#deleteBook(int)
 */
public class BookNotFoundException extends RuntimeException {

    private final int bookId;

    /**
     * Constructs a BookNotFoundException for the given ID.
     *
     * @param bookId the ID that was not found
     */
    public BookNotFoundException(int bookId) {
        super("Book not found with ID: " + bookId);
        this.bookId = bookId;
    }

    /**
     * Returns the book ID that triggered this exception.
     *
     * @return the missing book ID
     */
    public int getBookId() {
        return bookId;
    }
}
