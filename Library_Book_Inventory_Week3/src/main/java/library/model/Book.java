package library.model;

import java.time.Year;
import java.util.Objects;

/**
 * Represents a book in the library inventory.
 *
 * <p><b>Week 4 Refactoring — changes from Week 3 version:</b></p>
 * <ul>
 *   <li><b>Input validation</b> — constructor and setters now reject null/blank
 *       strings and out-of-range years, enforcing data integrity at the model
 *       layer instead of relying on callers. (Issue #3, #9)</li>
 *   <li><b>{@code equals()} / {@code hashCode()}</b> — identity is defined by
 *       {@code id}, ensuring correct behavior in hash-based collections.
 *       Previously, identity was undefined and {@code List.remove()} relied on
 *       reference equality. (Issue #4)</li>
 *   <li><b>Moved to {@code library.model} package</b> — separates the domain
 *       model from service and UI layers. (Issue #5)</li>
 * </ul>
 */
public class Book {

    private final int id;
    private String title;
    private String author;
    private String isbn;
    private int publicationYear;

    /**
     * Creates a new Book with validated fields.
     *
     * @param id              unique positive identifier
     * @param title           non-blank book title
     * @param author          non-blank author name
     * @param isbn            non-blank ISBN string
     * @param publicationYear year between 1000 and the current year (inclusive)
     * @throws IllegalArgumentException if any argument is invalid
     */
    public Book(int id, String title, String author, String isbn, int publicationYear) {
        if (id <= 0) {
            throw new IllegalArgumentException("Book ID must be positive.");
        }
        this.id = id;

        // Delegate to validated setters — DRY: validation logic is written once
        setTitle(title);
        setAuthor(author);
        setIsbn(isbn);
        setPublicationYear(publicationYear);
    }

    // ========================= Getters =========================

    public int getId() { return id; }

    public String getTitle() { return title; }

    public String getAuthor() { return author; }

    public String getIsbn() { return isbn; }

    public int getPublicationYear() { return publicationYear; }

    // ========================= Validated Setters =========================
    // Week 4: individual setters replace the raw update() method so each field
    // is validated independently, and callers can update a single field if needed.

    /**
     * Sets the book title after validation.
     *
     * @param title non-null, non-blank title
     * @throws IllegalArgumentException if title is null or blank
     */
    public void setTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be null or blank.");
        }
        this.title = title.trim();
    }

    /**
     * Sets the author name after validation.
     *
     * @param author non-null, non-blank author
     * @throws IllegalArgumentException if author is null or blank
     */
    public void setAuthor(String author) {
        if (author == null || author.isBlank()) {
            throw new IllegalArgumentException("Author cannot be null or blank.");
        }
        this.author = author.trim();
    }

    /**
     * Sets the ISBN after validation.
     *
     * @param isbn non-null, non-blank ISBN
     * @throws IllegalArgumentException if isbn is null or blank
     */
    public void setIsbn(String isbn) {
        if (isbn == null || isbn.isBlank()) {
            throw new IllegalArgumentException("ISBN cannot be null or blank.");
        }
        this.isbn = isbn.trim();
    }

    /**
     * Sets the publication year after range validation.
     *
     * @param publicationYear year between 1000 and the current year (inclusive)
     * @throws IllegalArgumentException if year is out of range
     */
    public void setPublicationYear(int publicationYear) {
        int currentYear = Year.now().getValue();
        if (publicationYear < 1000 || publicationYear > currentYear) {
            throw new IllegalArgumentException(
                    "Publication year must be between 1000 and " + currentYear + ".");
        }
        this.publicationYear = publicationYear;
    }

    /**
     * Convenience method to update all mutable fields at once.
     * Delegates to the individual validated setters — DRY.
     *
     * <p><b>Week 4 change:</b> In Week 3 this method set fields directly
     * without validation. It now routes through setters so the same validation
     * rules apply regardless of how the book is modified.</p>
     *
     * @param title           new title
     * @param author          new author
     * @param isbn            new ISBN
     * @param publicationYear new publication year
     */
    public void update(String title, String author, String isbn, int publicationYear) {
        setTitle(title);
        setAuthor(author);
        setIsbn(isbn);
        setPublicationYear(publicationYear);
    }

    // ========================= Object Overrides =========================

    /**
     * Two books are equal if and only if they have the same {@code id}.
     *
     * <p><b>Week 4 addition:</b> Without this override, {@code List.remove()}
     * and hash-based collections relied on reference equality, which could
     * produce subtle bugs if a Book instance was ever copied or serialized.</p>
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book book)) return false;
        return id == book.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("ID: %d | Title: %s | Author: %s | ISBN: %s | Year: %d",
                id, title, author, isbn, publicationYear);
    }
}
