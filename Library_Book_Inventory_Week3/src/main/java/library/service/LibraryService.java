package library.service;

import library.exception.BookNotFoundException;
import library.exception.DuplicateBookException;
import library.model.Book;
import library.persistence.DataStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Provides CRUD operations for the library inventory.
 *
 * <p><b>Week 4 Refactoring — changes from Week 3 version:</b></p>
 * <ul>
 *   <li><b>Data-structure optimization</b> — the backing store changed from a
 *       single {@code ArrayList<Book>} to a {@code HashMap<Integer, Book>}
 *       (keyed by ID) plus a secondary {@code HashMap<String, Integer>} index
 *       (ISBN → ID). This drops every ID lookup and ISBN uniqueness check from
 *       <b>O(n)</b> to <b>O(1)</b>. (Issue #1, #2)</li>
 *   <li><b>DRY</b> — ISBN uniqueness validation is extracted into the private
 *       helper {@link #validateIsbnUnique(String, int)}, eliminating the
 *       duplicated loop that existed in both {@code addBook()} and
 *       {@code updateBook()}. (Issue #2)</li>
 *   <li><b>Optional pattern</b> — {@link #findById(int)} now returns
 *       {@code Optional<Book>} instead of a nullable reference, making the
 *       API's intent explicit and eliminating null-checks in callers.
 *       (Issue #8)</li>
 *   <li><b>Custom exceptions</b> — {@link BookNotFoundException} and
 *       {@link DuplicateBookException} replace the generic
 *       {@code IllegalArgumentException}, enabling callers to catch specific
 *       error types. (Issue #7)</li>
 *   <li><b>Moved to {@code library.service}</b> package — separates service
 *       logic from the model and UI layers. (Issue #5)</li>
 * </ul>
 *
 * <p><b>Week 5 Integration — Persistence:</b> An optional {@link DataStore}
 * can be injected via the constructor. When present, the service loads data
 * on startup and auto-saves after every mutation (add, update, delete).
 * The no-arg constructor retains pure in-memory mode for backward
 * compatibility and unit testing.</p>
 */
public class LibraryService {

    /*
     * Week 4 optimization: dual-index storage.
     *
     * booksById   — primary store; O(1) lookup/insert/remove by ID.
     * isbnToId    — secondary index mapping ISBN (lowercase) → book ID;
     *               keeps ISBN uniqueness checks at O(1).
     */
    private final Map<Integer, Book> booksById = new HashMap<>();
    private final Map<String, Integer> isbnToId = new HashMap<>();

    /**
     * Week 5: optional persistence back-end. {@code null} means in-memory only.
     */
    private final DataStore dataStore;

    /**
     * Creates a service with pure in-memory storage (no persistence).
     * This constructor preserves backward compatibility with Week 4 code
     * and is used by unit tests.
     */
    public LibraryService() {
        this.dataStore = null;
    }

    /**
     * Creates a service backed by the given {@link DataStore}.
     *
     * <p><b>Week 5 Integration:</b> On construction, any previously
     * persisted books are loaded into the in-memory indexes.</p>
     *
     * @param dataStore the persistence back-end (must not be null)
     */
    public LibraryService(DataStore dataStore) {
        this.dataStore = dataStore;
        // Load persisted data into the in-memory indexes
        for (Book book : dataStore.load()) {
            booksById.put(book.getId(), book);
            isbnToId.put(book.getIsbn().toLowerCase(), book.getId());
        }
    }

    /**
     * Returns {@code true} if the inventory already contains data
     * (useful for skipping sample-data seeding on restart).
     *
     * @return true if at least one book exists
     */
    public boolean hasBooks() {
        return !booksById.isEmpty();
    }

    /**
     * Adds a book after checking ID and ISBN uniqueness.
     *
     * <p><b>Week 4 changes:</b></p>
     * <ul>
     *   <li>ID check: {@code booksById.containsKey()} — O(1) vs. former O(n)
     *       {@code findById()} scan.</li>
     *   <li>ISBN check: delegated to {@link #validateIsbnUnique(String, int)}
     *       — shared with {@code updateBook()}, eliminating DRY violation.</li>
     *   <li>Throws {@link DuplicateBookException} instead of generic
     *       {@code IllegalArgumentException}.</li>
     * </ul>
     *
     * @param book the book to add
     * @throws DuplicateBookException if the book's ID or ISBN already exists
     */
    public void addBook(Book book) {
        // O(1) ID uniqueness check (was O(n) linear scan in Week 3)
        if (booksById.containsKey(book.getId())) {
            throw new DuplicateBookException("ID", String.valueOf(book.getId()));
        }

        // O(1) ISBN uniqueness check via shared helper (DRY — Issue #2)
        validateIsbnUnique(book.getIsbn(), -1);

        booksById.put(book.getId(), book);
        isbnToId.put(book.getIsbn().toLowerCase(), book.getId());
        persist();
    }

    /**
     * Returns an unmodifiable view of all books.
     *
     * <p><b>Week 4 change:</b> Still returns a defensive copy, but built from
     * the HashMap's {@code values()} collection rather than copying an ArrayList.</p>
     *
     * @return list of all books (safe to iterate; mutation has no effect on the store)
     */
    public List<Book> getAllBooks() {
        return Collections.unmodifiableList(new ArrayList<>(booksById.values()));
    }

    /**
     * Finds a book by its numeric ID.
     *
     * <p><b>Week 4 change:</b> Returns {@code Optional<Book>} instead of a
     * nullable reference. Callers use {@code .orElse()}, {@code .ifPresent()},
     * or {@code .orElseThrow()} — no more null-checks scattered across the
     * codebase. (Issue #8)</p>
     *
     * <p>Also O(1) via HashMap lookup, down from O(n) linear scan.</p>
     *
     * @param id the book ID to search for
     * @return an {@code Optional} containing the book, or empty if not found
     */
    public Optional<Book> findById(int id) {
        return Optional.ofNullable(booksById.get(id));
    }

    /**
     * Searches books by title or author (case-insensitive substring match).
     *
     * <p>This operation remains O(n) because keyword search inherently requires
     * scanning every book's title and author. A full-text index could improve
     * this in a future iteration, but for an in-memory inventory of typical
     * library size, O(n) is acceptable.</p>
     *
     * <p><b>Week 4 change:</b> Uses Java Streams for a more idiomatic,
     * declarative style. The null-keyword guard from the Week 3 bugfix is
     * preserved.</p>
     *
     * @param keyword the search term (matched against title and author)
     * @return list of matching books (may be empty, never null)
     * @throws IllegalArgumentException if keyword is null
     */
    public List<Book> search(String keyword) {
        if (keyword == null) {
            throw new IllegalArgumentException("Search keyword cannot be null.");
        }
        String key = keyword.toLowerCase();
        return booksById.values().stream()
                .filter(book -> book.getTitle().toLowerCase().contains(key)
                        || book.getAuthor().toLowerCase().contains(key))
                .toList();
    }

    /**
     * Updates an existing book's fields.
     *
     * <p><b>Week 4 changes:</b></p>
     * <ul>
     *   <li>O(1) book lookup via HashMap (was O(n)).</li>
     *   <li>ISBN conflict check uses the shared {@link #validateIsbnUnique}
     *       helper — DRY. (Issue #2)</li>
     *   <li>ISBN index is updated when the ISBN changes.</li>
     *   <li>Throws {@link BookNotFoundException} / {@link DuplicateBookException}
     *       instead of generic exceptions.</li>
     * </ul>
     *
     * @param id     ID of the book to update
     * @param title  new title
     * @param author new author
     * @param isbn   new ISBN
     * @param year   new publication year
     * @throws BookNotFoundException  if no book exists with the given ID
     * @throws DuplicateBookException if the new ISBN belongs to another book
     */
    public void updateBook(int id, String title, String author, String isbn, int year) {
        Book book = booksById.get(id);
        if (book == null) {
            throw new BookNotFoundException(id);
        }

        // O(1) ISBN conflict check, excluding the book being updated (DRY — Issue #2)
        validateIsbnUnique(isbn, id);

        // Update the ISBN index: remove old key, insert new key
        isbnToId.remove(book.getIsbn().toLowerCase());
        book.update(title, author, isbn, year);
        isbnToId.put(isbn.toLowerCase(), id);
        persist();
    }

    /**
     * Deletes a book by ID.
     *
     * <p><b>Week 4 change:</b> O(1) removal via {@code HashMap.remove()} —
     * the Week 3 version did an O(n) {@code findById()} scan followed by an
     * O(n) {@code List.remove()} scan, totaling O(n).</p>
     *
     * @param id the ID of the book to delete
     * @throws BookNotFoundException if no book exists with the given ID
     */
    public void deleteBook(int id) {
        Book book = booksById.remove(id);
        if (book == null) {
            throw new BookNotFoundException(id);
        }
        isbnToId.remove(book.getIsbn().toLowerCase());
        persist();
    }

    // ========================= Private Helpers =========================

    /**
     * Validates that the given ISBN is not already in use by another book.
     *
     * <p><b>Week 4 DRY refactoring (Issue #2):</b> This logic was previously
     * duplicated as separate O(n) loops inside both {@code addBook()} and
     * {@code updateBook()}. It is now a single O(1) helper that both methods
     * delegate to.</p>
     *
     * @param isbn      the ISBN to check
     * @param excludeId the ID of the book being updated (pass -1 for new books)
     * @throws DuplicateBookException if the ISBN belongs to a different book
     */
    private void validateIsbnUnique(String isbn, int excludeId) {
        /*
         * BEFORE (Week 3 — duplicated in addBook AND updateBook):
         *   for (Book existing : books) {
         *       if (existing.getIsbn().equalsIgnoreCase(isbn)) {
         *           throw new IllegalArgumentException("ISBN already exists.");
         *       }
         *   }
         *
         * AFTER (Week 4 — single shared helper, O(1)):
         */
        Integer existingId = isbnToId.get(isbn.toLowerCase());
        if (existingId != null && existingId != excludeId) {
            throw new DuplicateBookException("ISBN", isbn);
        }
    }

    /**
     * Saves the current inventory to the data store, if one is configured.
     *
     * <p><b>Week 5 Integration:</b> Called automatically after every
     * mutation (add, update, delete). If no {@link DataStore} was injected
     * (i.e., pure in-memory mode), this method is a no-op.</p>
     */
    private void persist() {
        if (dataStore != null) {
            dataStore.save(new ArrayList<>(booksById.values()));
        }
    }
}
