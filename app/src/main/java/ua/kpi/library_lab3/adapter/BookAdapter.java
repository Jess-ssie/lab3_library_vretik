package ua.kpi.library_lab3.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ua.kpi.library_lab3.R;
import ua.kpi.library_lab3.model.Book;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    public interface OnBookActionListener {
        void onBookClick(Book book);
        void onBookLongClick(Book book);
        void onFavoriteClick(Book book);
    }

    private final List<Book> books = new ArrayList<>();
    private final OnBookActionListener listener;

    public BookAdapter(OnBookActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Book> newBooks) {
        books.clear();
        if (newBooks != null) {
            books.addAll(newBooks);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = books.get(position);
        holder.titleTextView.setText(book.getTitle());
        holder.authorTextView.setText(book.getAuthor());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBookClick(book);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onBookLongClick(book);
            }
            return true;
        });

        // favorite button state
        if (book.isFavorite()) {
            holder.favButton.setImageResource(R.drawable.ic_star_filled_24);
        } else {
            holder.favButton.setImageResource(R.drawable.ic_star_outline_24);
        }

        holder.favButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFavoriteClick(book);
            }
        });
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {

        private final TextView titleTextView;
        private final TextView authorTextView;
        private final android.widget.ImageButton favButton;

        BookViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            authorTextView = itemView.findViewById(R.id.authorTextView);
            favButton = itemView.findViewById(R.id.favButton);
        }
    }
}