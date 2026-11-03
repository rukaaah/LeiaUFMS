package br.ufms.leiaufms.ui.explore;

import android.app.Application;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import br.ufms.leiaufms.core.location.WalkEstimate;
import br.ufms.leiaufms.data.local.AppDatabase;
import br.ufms.leiaufms.data.local.model.ListingRow;

/** Explorar (RF-14) com "Perto de mim" (RF-27): distância calculada só quando há posição. */
public class ExploreViewModel extends AndroidViewModel {

    private final MutableLiveData<String[]> filter = new MutableLiveData<>(new String[]{"", "", ""});
    private final MutableLiveData<Location> location = new MutableLiveData<>();
    private final MutableLiveData<Integer> radius = new MutableLiveData<>(null);
    private final MediatorLiveData<List<ListingRow>> result = new MediatorLiveData<>();
    private List<ListingRow> raw;

    public ExploreViewModel(@NonNull Application app) {
        super(app);
        LiveData<List<ListingRow>> source = Transformations.switchMap(filter,
                f -> AppDatabase.get(app).anuncioDao().searchByGenre(f[0], f[2]));
        result.addSource(source, rows -> {
            raw = rows;
            recompute();
        });
        result.addSource(location, l -> recompute());
        result.addSource(radius, r -> recompute());
    }

    public LiveData<List<ListingRow>> listings() {
        return result;
    }

    public void setQuery(String q, String modalidade, String genero) {
        String[] current = filter.getValue();
        String query = q == null ? "" : q.trim();
        String g = genero == null ? "" : genero;
        if (current != null && current[0].equals(query) && current[1].equals(modalidade) && current[2].equals(g)) return;
        filter.setValue(new String[]{query, modalidade, g});
    }

    public String genre() {
        String[] f = filter.getValue();
        return f == null ? "" : f[2];
    }

    public String query() {
        String[] f = filter.getValue();
        return f == null ? "" : f[0];
    }

    public void setLocation(Location l) {
        location.setValue(l);
    }

    public Location location() {
        return location.getValue();
    }

    /** null desliga o filtro de raio. */
    public void setRadius(Integer meters) {
        radius.setValue(meters);
    }

    public Integer radius() {
        return radius.getValue();
    }

    private void recompute() {
        if (raw == null) return;
        Location l = location.getValue();
        Integer r = radius.getValue();
        String[] f = filter.getValue();
        String modalidade = f == null ? "" : f[1];
        List<ListingRow> out = new ArrayList<>();
        for (ListingRow original : raw) {
            if (!modalidade.isEmpty() && !modalidade.equals(original.modalidade)
                    && !br.ufms.leiaufms.data.local.entity.Anuncio.MODALIDADE_AMBAS.equals(original.modalidade)) continue;
            ListingRow row = original.copy();
            Double lat = row.latitude();
            Double lng = row.longitude();
            if (l != null && lat != null && lng != null) {
                row.distancia = WalkEstimate.distance(l.getLatitude(), l.getLongitude(), lat, lng);
            }
            if (r != null && (row.distancia == null || row.distancia > r)) continue;
            out.add(row);
        }
        if (l != null) {
            Collections.sort(out, Comparator.comparing(
                    (ListingRow x) -> x.distancia == null ? Float.MAX_VALUE : x.distancia));
        }
        result.setValue(out);
    }
}
