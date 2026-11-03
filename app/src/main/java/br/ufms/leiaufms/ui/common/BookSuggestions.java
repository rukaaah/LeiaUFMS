package br.ufms.leiaufms.ui.common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import br.ufms.leiaufms.data.local.AppDatabase;
import br.ufms.leiaufms.data.local.CatalogLoader;
import br.ufms.leiaufms.data.local.entity.LivroCatalogo;
import br.ufms.leiaufms.data.local.entity.Obra;

/**
 * Sugestões ao digitar o título de um livro (RF-45): catálogo de referência + obras já cadastradas no app.
 * Escolher uma sugestão preenche título e autor iguais aos do catálogo, para o mesmo livro não aparecer
 * escrito de vários jeitos.
 */
public final class BookSuggestions {

    /** Uma sugestão: título e autor como devem ficar gravados. */
    public static final class Item {
        public final String titulo;
        public final String autor;
        public final String extra;

        Item(String titulo, String autor, String extra) {
            this.titulo = titulo;
            this.autor = autor;
            this.extra = extra;
        }

        @NonNull
        @Override
        public String toString() {
            return titulo;
        }
    }

    public interface OnPick {
        void onPick(Item item);
    }

    private BookSuggestions() {
    }

    public static void attach(MaterialAutoCompleteTextView title, EditText author) {
        attach(title, author, null);
    }

    public static void attach(MaterialAutoCompleteTextView title, EditText author, OnPick onPick) {
        Adapter adapter = new Adapter(title.getContext());
        title.setThreshold(2);
        title.setAdapter(adapter);
        title.setOnItemClickListener((parent, v, pos, id) -> {
            Item it = adapter.getItem(pos);
            if (it == null) return;
            title.setText(it.titulo, false);
            title.setSelection(it.titulo.length());
            if (author != null) author.setText(it.autor);
            if (onPick != null) onPick.onPick(it);
        });
    }

    private static final class Adapter extends ArrayAdapter<Item> {
        private final List<Item> items = new ArrayList<>();
        private final AppDatabase db;

        Adapter(Context c) {
            super(c, android.R.layout.simple_list_item_2);
            db = AppDatabase.get(c);
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Item getItem(int position) {
            return position < items.size() ? items.get(position) : null;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            View v = convertView != null ? convertView
                    : LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            Item it = items.get(position);
            TextView t1 = v.findViewById(android.R.id.text1);
            TextView t2 = v.findViewById(android.R.id.text2);
            t1.setText(it.titulo);
            t1.setMaxLines(2);
            t2.setText(it.extra == null ? it.autor : it.autor + " · " + it.extra);
            return v;
        }

        @NonNull
        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    // Roda fora da thread principal (Filter usa uma thread própria)
                    FilterResults r = new FilterResults();
                    List<Item> found = new ArrayList<>();
                    if (constraint != null && constraint.toString().trim().length() >= 2) {
                        String q = CatalogLoader.plain(constraint.toString());
                        Map<String, Item> unique = new LinkedHashMap<>();
                        for (Obra o : db.obraDao().search(constraint.toString().trim())) {
                            unique.put(CatalogLoader.key(o.titulo, o.autor), new Item(o.titulo, o.autor, null));
                        }
                        for (LivroCatalogo l : db.catalogoDao().search(q)) {
                            String k = CatalogLoader.key(l.titulo, l.autor);
                            if (!unique.containsKey(k)) {
                                unique.put(k, new Item(l.titulo, l.autor, l.ano == null ? null : String.valueOf(l.ano)));
                            }
                        }
                        found.addAll(unique.values());
                        if (found.size() > 30) found = found.subList(0, 30);
                    }
                    r.values = found;
                    r.count = found.size();
                    return r;
                }

                @Override
                @SuppressWarnings("unchecked")
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    items.clear();
                    if (results.values != null) items.addAll((List<Item>) results.values);
                    notifyDataSetChanged();
                }

                @Override
                public CharSequence convertResultToString(Object resultValue) {
                    return ((Item) resultValue).titulo;
                }
            };
        }
    }
}
