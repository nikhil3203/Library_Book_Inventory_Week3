package library.persistence;

import library.model.Book;

import java.util.List;

/**
 * Abstraction for loading and saving the book inventory.
 *
 * <p><b>Week 5 — Strategy Pattern:</b> By programming to this interface,
 * {@link library.service.LibraryService} is decoupled from any specific
 * storage mechanism. Implementations can persist to a JSON file, a
 * relational database, a remote API, or even an in-memory store for
 * testing — all without modifying the service layer.</p>
 *
 * @see JsonDataStore
 */
public interface DataStore {

    /**
     * Loads all books from the backing store.
     *
     * @return a mutable list of books (may be empty, never null)
     */
    List<Book> load();

    /**
     * Persists the given list of books, replacing any previous state.
     *
     * @param books the complete list of books to save
     */
    void save(List<Book> books);
}
