package ua.kpi.library_lab3.ui;

import android.os.Bundle;
import android.database.sqlite.SQLiteConstraintException;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ua.kpi.library_lab3.R;
import ua.kpi.library_lab3.database.AppDatabase;
import ua.kpi.library_lab3.model.Book;

public class BookEditActivity extends AppCompatActivity {

    public static final String EXTRA_BOOK_ID = "extra_book_id";

    private TextInputLayout titleLayout;
    private TextInputLayout authorLayout;
    private TextInputLayout isbnLayout;
    private TextInputEditText titleEditText;
    private TextInputEditText authorEditText;
    private TextInputEditText isbnEditText;
    private TextInputEditText ageCategoryEditText;
    private AppDatabase appDatabase;
    private ExecutorService executorService;
    private int bookId = -1;
    private boolean currentFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_book_edit);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.editRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        appDatabase = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        titleLayout = findViewById(R.id.titleLayout);
        authorLayout = findViewById(R.id.authorLayout);
        isbnLayout = findViewById(R.id.isbnLayout);
        titleEditText = findViewById(R.id.titleEditText);
        authorEditText = findViewById(R.id.authorEditText);
        isbnEditText = findViewById(R.id.isbnEditText);
        ageCategoryEditText = findViewById(R.id.ageCategoryEditText);
        MaterialButton saveButton = findViewById(R.id.saveButton);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24);
        toolbar.setNavigationOnClickListener(v -> finish());

        bookId = getIntent().getIntExtra(EXTRA_BOOK_ID, -1);
        toolbar.setTitle(bookId == -1 ? R.string.add_book_title : R.string.edit_book_title);

        saveButton.setOnClickListener(v -> saveBook());

        if (bookId != -1) {
            loadBook();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void loadBook() {
        executorService.execute(() -> {
            Book book = appDatabase.bookDao().getBookById(bookId);
            runOnUiThread(() -> {
                if (book == null) {
                    Toast.makeText(this, R.string.book_not_found, Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                titleEditText.setText(book.getTitle());
                authorEditText.setText(book.getAuthor());
                isbnEditText.setText(book.getIsbn());
                ageCategoryEditText.setText(book.getAgeCategory());
                currentFavorite = book.isFavorite();
            });
        });
    }

    private void saveBook() {
        clearErrors();

        String title = getText(titleEditText);
        String author = getText(authorEditText);
        String isbn = getText(isbnEditText);
        String ageCategory = getText(ageCategoryEditText);

        boolean valid = true;

        if (title.isEmpty()) {
            titleLayout.setError(getString(R.string.required_fields));
            valid = false;
        }

        if (author.isEmpty()) {
            authorLayout.setError(getString(R.string.required_fields));
            valid = false;
        }

        if (isbn.isEmpty()) {
            isbnLayout.setError(getString(R.string.isbn_required));
            valid = false;
        } else if (!isValidIsbnCharacters(isbn)) {
            isbnLayout.setError(getString(R.string.isbn_allowed_characters));
            valid = false;
        }

        if (!valid) {
            Snackbar.make(findViewById(R.id.editRoot), R.string.form_has_errors, Snackbar.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            int duplicateCount = appDatabase.bookDao().countBooksWithIsbn(isbn, bookId);
            if (duplicateCount > 0) {
                runOnUiThread(() -> {
                    isbnLayout.setError(getString(R.string.isbn_must_be_unique));
                    Snackbar.make(findViewById(R.id.editRoot), R.string.form_has_errors, Snackbar.LENGTH_SHORT).show();
                });
                return;
            }

            Book book = new Book(title, author, isbn, ageCategory);
            if (bookId != -1) {
                book.setId(bookId);
                book.setFavorite(currentFavorite);
            }

            try {
                if (bookId == -1) {
                    appDatabase.bookDao().insert(book);
                } else {
                    appDatabase.bookDao().update(book);
                }
                runOnUiThread(() -> {
                    Toast.makeText(this, R.string.book_saved, Toast.LENGTH_SHORT).show();
                    finish();
                });
            } catch (SQLiteConstraintException e) {
                runOnUiThread(() -> {
                    isbnLayout.setError(getString(R.string.isbn_must_be_unique));
                    Snackbar.make(findViewById(R.id.editRoot), R.string.isbn_must_be_unique, Snackbar.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void clearErrors() {
        titleLayout.setError(null);
        authorLayout.setError(null);
        isbnLayout.setError(null);
    }

    private String getText(TextInputEditText editText) {
        if (editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }

    private boolean isValidIsbnCharacters(String isbn) {
        return isbn.matches("[0-9-]+");
    }
}