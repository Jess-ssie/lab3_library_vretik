package ua.kpi.library_lab3.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textview.MaterialTextView;

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
    private BookAdapter bookAdapter;
    private AppDatabase appDatabase;
    private ExecutorService executorService;

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
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(bookAdapter);

        fabAdd.setOnClickListener(v -> openBookEditor(-1));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBooks();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }

    private void loadBooks() {
        executorService.execute(() -> {
            List<Book> books = appDatabase.bookDao().getAllBooks();
            runOnUiThread(() -> {
                bookAdapter.submitList(books);
                emptyView.setVisibility(books.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
                recyclerView.setVisibility(books.isEmpty() ? android.view.View.GONE : android.view.View.VISIBLE);
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