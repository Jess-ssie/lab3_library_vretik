package ua.kpi.library_lab3.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ua.kpi.library_lab3.model.Book;

@Dao
public interface BookDao {

    @Query("SELECT * FROM books ORDER BY title COLLATE NOCASE ASC")
    List<Book> getAllBooks();

    @Query("SELECT * FROM books WHERE favorite = 1 ORDER BY title COLLATE NOCASE ASC")
    List<Book> getFavoriteBooks();

    @Query("SELECT * FROM books WHERE id = :id LIMIT 1")
    Book getBookById(int id);

    @Query("SELECT COUNT(*) FROM books WHERE isbn = :isbn AND id != :excludedBookId")
    int countBooksWithIsbn(String isbn, int excludedBookId);

    @Insert
    long insert(Book book);

    @Update
    void update(Book book);

    @Delete
    void delete(Book book);
}