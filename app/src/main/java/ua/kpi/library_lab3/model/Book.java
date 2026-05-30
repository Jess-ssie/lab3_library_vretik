package ua.kpi.library_lab3.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "books", indices = {@Index(value = {"isbn"}, unique = true)})
public class Book {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private String author;
    private String isbn;
    private String ageCategory;
    @ColumnInfo(defaultValue = "0")
    private boolean favorite;

    public Book(String title, String author, String isbn, String ageCategory) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.ageCategory = ageCategory;
        this.favorite = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title == null ? "" : title;
    }

    public void setTitle(String title) {
        this.title = title == null ? "" : title;
    }

    public String getAuthor() {
        return author == null ? "" : author;
    }

    public void setAuthor(String author) {
        this.author = author == null ? "" : author;
    }

    public String getIsbn() {
        return isbn == null ? "" : isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn == null ? "" : isbn;
    }

    public String getAgeCategory() {
        return ageCategory == null ? "" : ageCategory;
    }

    public void setAgeCategory(String ageCategory) {
        this.ageCategory = ageCategory == null ? "" : ageCategory;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
}