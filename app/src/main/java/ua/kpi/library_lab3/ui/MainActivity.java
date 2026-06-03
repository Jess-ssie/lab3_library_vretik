package ua.kpi.library_lab3.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;
import android.view.MotionEvent;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ua.kpi.library_lab3.R;
import ua.kpi.library_lab3.adapter.BookAdapter;
import ua.kpi.library_lab3.database.AppDatabase;
import ua.kpi.library_lab3.model.Book;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MaterialTextView emptyView;
    private TextView statsTextView;
    private ChipGroup filterChips;
    private TextInputEditText searchEditText;
    private BookAdapter bookAdapter;
    private AppDatabase appDatabase;
    private ExecutorService executorService;
    private enum FilterMode { ALL, FAVORITES }
    private FilterMode currentFilter = FilterMode.ALL;
    private String searchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        appDatabase = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerView);
        emptyView = findViewById(R.id.emptyView);
        statsTextView = findViewById(R.id.statsTextView);
        filterChips = findViewById(R.id.filterChips);
        searchEditText = findViewById(R.id.searchEditText);
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);

        setSupportActionBar(toolbar);

        bookAdapter = new BookAdapter(new BookAdapter.OnBookActionListener() {
            @Override
            public void onBookClick(Book book) {
                openBookEditor(book.getId());
            }

            @Override
            public void onBookLongClick(Book book) {
                showDeleteDialog(book);
            }

            @Override
            public void onFavoriteClick(Book book) {
                toggleFavorite(book);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(bookAdapter);

        fabAdd.setOnClickListener(v -> openBookEditor(-1));

        filterChips.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipFavorites) {
                currentFilter = FilterMode.FAVORITES;
            } else {
                currentFilter = FilterMode.ALL;
            }
            loadBooks();
        });

        findViewById(R.id.chipAll).performClick();

        findViewById(R.id.main).setOnClickListener(v -> searchEditText.clearFocus());
        recyclerView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                searchEditText.clearFocus();
            }
            return false;
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                searchQuery = s == null ? "" : s.toString().trim();
                loadBooks();
            }
        });

        searchEditText.clearFocus();
        findViewById(R.id.main).requestFocus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        searchEditText.clearFocus();
        findViewById(R.id.main).requestFocus();
        loadBooks();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }

    private void loadBooks() {
        executorService.execute(() -> {
            List<Book> books;
            if (currentFilter == FilterMode.FAVORITES) {
                books = appDatabase.bookDao().getFavoriteBooks();
            } else {
                books = appDatabase.bookDao().getAllBooks();
            }

            List<Book> filteredBooks = new ArrayList<>();
            if (searchQuery.isEmpty()) {
                filteredBooks.addAll(books);
            } else {
                String lowerQuery = searchQuery.toLowerCase();
                for (Book book : books) {
                    if (book.getTitle().toLowerCase().contains(lowerQuery) || book.getIsbn().toLowerCase().contains(lowerQuery)) {
                        filteredBooks.add(book);
                    }
                }
            }

            final int total = appDatabase.bookDao().getAllBooks().size();
            final int favCount = appDatabase.bookDao().getFavoriteBooks().size();
            runOnUiThread(() -> {
                bookAdapter.submitList(filteredBooks);
                boolean shouldShowEmpty = filteredBooks.isEmpty();
                emptyView.setVisibility(shouldShowEmpty ? android.view.View.VISIBLE : android.view.View.GONE);
                recyclerView.setVisibility(shouldShowEmpty ? android.view.View.GONE : android.view.View.VISIBLE);
                if (shouldShowEmpty) {
                    if (!searchQuery.isEmpty()) {
                        emptyView.setText(R.string.empty_search);
                    } else if (currentFilter == FilterMode.FAVORITES) {
                        emptyView.setText(R.string.empty_favorites);
                    } else {
                        emptyView.setText(R.string.empty_books);
                    }
                } else {
                    emptyView.setText(R.string.empty_books);
                }
                statsTextView.setText(getString(R.string.stats_format, total, favCount));
            });
        });
    }

    private void toggleFavorite(Book book) {
        executorService.execute(() -> {
            book.setFavorite(!book.isFavorite());
            appDatabase.bookDao().update(book);
            runOnUiThread(() -> {
                loadBooks();
                String msg = book.isFavorite() ? getString(R.string.added_to_favorites) : getString(R.string.removed_from_favorites);
                Snackbar.make(findViewById(R.id.main), msg, Snackbar.LENGTH_SHORT).show();
            });
        });
    }

    private void openBookEditor(int bookId) {
        Intent intent = new Intent(this, BookEditActivity.class);
        intent.putExtra(BookEditActivity.EXTRA_BOOK_ID, bookId);
        startActivity(intent);
    }

    private void showDeleteDialog(Book book) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_book_title)
                .setMessage(R.string.delete_book_message)
                .setPositiveButton(R.string.delete, (dialog, which) -> deleteBook(book))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void deleteBook(Book book) {
        executorService.execute(() -> {
            appDatabase.bookDao().delete(book);
            runOnUiThread(() -> {
                loadBooks();
                android.widget.Toast.makeText(this, R.string.book_deleted, android.widget.Toast.LENGTH_SHORT).show();
            });
        });
    }
}