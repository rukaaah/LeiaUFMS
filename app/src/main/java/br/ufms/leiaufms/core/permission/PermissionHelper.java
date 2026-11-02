package br.ufms.leiaufms.core.permission;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.Map;

/**
 * Pedido de permissão no momento do uso (docs/04-arquitetura.md §6). Trata concessão, negação e
 * negação permanente (shouldShowRequestPermissionRationale + registro de que já foi pedida).
 * Deve ser criado no onCreate do Fragment (registerForActivityResult).
 */
public class PermissionHelper {

    public interface Callback {
        void onGranted();

        /** @param permanently true quando o sistema não mostra mais o diálogo: oferecer Configurações. */
        void onDenied(boolean permanently);
    }

    private static final String PREFS = "leia_permissoes";

    private final Fragment fragment;
    private final ActivityResultLauncher<String[]> launcher;
    private Callback pending;
    private String[] pendingPermissions;

    public PermissionHelper(Fragment fragment) {
        this.fragment = fragment;
        launcher = fragment.registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(), this::onResult);
    }

    /** Concede se qualquer uma das permissões for dada (ex.: localização fina ou aproximada). */
    public void request(Callback callback, String... permissions) {
        Context context = fragment.requireContext();
        if (anyGranted(context, permissions)) {
            callback.onGranted();
            return;
        }
        pending = callback;
        pendingPermissions = permissions;
        SharedPreferences.Editor editor = prefs(context).edit();
        for (String p : permissions) editor.putBoolean(p, true);
        editor.apply();
        launcher.launch(permissions);
    }

    private void onResult(Map<String, Boolean> result) {
        Callback callback = pending;
        String[] permissions = pendingPermissions;
        pending = null;
        pendingPermissions = null;
        if (callback == null || permissions == null || !fragment.isAdded()) return;
        for (Boolean granted : result.values()) {
            if (Boolean.TRUE.equals(granted)) {
                callback.onGranted();
                return;
            }
        }
        callback.onDenied(isPermanentlyDenied(fragment.requireActivity(), permissions));
    }

    public static boolean isGranted(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean anyGranted(Context context, String... permissions) {
        for (String p : permissions) if (isGranted(context, p)) return true;
        return false;
    }

    /** Já pedida, ainda negada e o sistema não mostra mais justificativa: só pelas Configurações. */
    public static boolean isPermanentlyDenied(Activity activity, String... permissions) {
        for (String p : permissions) {
            if (isGranted(activity, p)) return false;
            if (!prefs(activity).getBoolean(p, false)) return false;
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, p)) return false;
        }
        return true;
    }

    public static void openAppSettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", context.getPackageName(), null));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
