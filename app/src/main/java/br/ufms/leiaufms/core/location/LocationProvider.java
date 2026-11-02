package br.ufms.leiaufms.core.location;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import androidx.core.location.LocationManagerCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

import br.ufms.leiaufms.core.permission.PermissionHelper;

/**
 * Localização sob demanda (RF-27, RNF-07, ADR-03): uma leitura com prioridade balanceada, sem
 * atualização contínua. Sem Google Play Services, usa a última posição do LocationManager.
 */
public class LocationProvider {

    public enum Failure { NO_PERMISSION, DISABLED, UNAVAILABLE }

    public interface Callback {
        void onLocation(Location location);

        void onFailure(Failure failure);
    }

    public static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private static final long MAX_AGE_MS = 2 * 60 * 1000;
    private static final long TIMEOUT_MS = 15 * 1000;

    private final Context context;
    private CancellationTokenSource cancellation;

    public LocationProvider(Context context) {
        this.context = context.getApplicationContext();
    }

    public static boolean isEnabled(Context context) {
        LocationManager lm = context.getSystemService(LocationManager.class);
        return lm != null && LocationManagerCompat.isLocationEnabled(lm);
    }

    @SuppressLint("MissingPermission")
    public void current(Callback callback) {
        if (!PermissionHelper.anyGranted(context, PERMISSIONS)) {
            callback.onFailure(Failure.NO_PERMISSION);
            return;
        }
        if (!isEnabled(context)) {
            callback.onFailure(Failure.DISABLED);
            return;
        }
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) != ConnectionResult.SUCCESS) {
            fallback(callback);
            return;
        }
        cancel();
        cancellation = new CancellationTokenSource();
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(context);
        CurrentLocationRequest request = new CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                .setMaxUpdateAgeMillis(MAX_AGE_MS)
                .setDurationMillis(TIMEOUT_MS)
                .build();
        client.getCurrentLocation(request, cancellation.getToken())
                .addOnSuccessListener(location -> {
                    if (location != null) callback.onLocation(location);
                    else fallback(callback);
                })
                .addOnFailureListener(e -> fallback(callback));
    }

    /** Sem resposta do Fused: usa a última posição conhecida dos provedores do aparelho. */
    @SuppressLint("MissingPermission")
    private void fallback(Callback callback) {
        LocationManager lm = context.getSystemService(LocationManager.class);
        Location best = null;
        if (lm != null) {
            for (String provider : lm.getProviders(true)) {
                Location l = lm.getLastKnownLocation(provider);
                if (l != null && (best == null || l.getTime() > best.getTime())) best = l;
            }
        }
        if (best != null) callback.onLocation(best);
        else callback.onFailure(Failure.UNAVAILABLE);
    }

    public void cancel() {
        if (cancellation != null) cancellation.cancel();
        cancellation = null;
    }
}
