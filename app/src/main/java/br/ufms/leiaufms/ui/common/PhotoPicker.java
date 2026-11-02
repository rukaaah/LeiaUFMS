package br.ufms.leiaufms.ui.common;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;

import br.ufms.leiaufms.R;
import br.ufms.leiaufms.core.media.ImageStore;
import br.ufms.leiaufms.core.permission.PermissionHelper;
import br.ufms.leiaufms.databinding.SheetPhotoBinding;

/**
 * Folha "Tirar foto / Escolher da galeria" (RF-09, RF-10, RF-12). Câmera com permissão tratada
 * (negada e negada de vez); galeria pelo Photo Picker, que não precisa de permissão.
 * Criar no onCreate do Fragment.
 */
public class PhotoPicker {

    public interface Callback {
        void onPicked(Uri uri);
    }

    private final Fragment fragment;
    private final PermissionHelper permissions;
    private final ActivityResultLauncher<Uri> takePicture;
    private final ActivityResultLauncher<PickVisualMediaRequest> pickImage;

    public PhotoPicker(Fragment fragment, Callback callback) {
        this.fragment = fragment;
        permissions = new PermissionHelper(fragment);
        takePicture = fragment.registerForActivityResult(new ActivityResultContracts.TakePicture(), saved -> {
            // Cancelar a câmera não muda nada
            if (Boolean.TRUE.equals(saved)) callback.onPicked(ImageStore.newCaptureUri(fragment.requireContext()));
        });
        pickImage = fragment.registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) callback.onPicked(uri);
        });
    }

    public void show(@StringRes int title) {
        BottomSheetDialog sheet = new BottomSheetDialog(fragment.requireContext());
        SheetPhotoBinding sb = SheetPhotoBinding.inflate(fragment.getLayoutInflater());
        sb.sheetTitle.setText(title);
        sb.optionCamera.setOnClickListener(v -> {
            sheet.dismiss();
            openCamera();
        });
        sb.optionGallery.setOnClickListener(v -> {
            sheet.dismiss();
            pickImage.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });
        sheet.setContentView(sb.getRoot());
        sheet.show();
    }

    private void openCamera() {
        View root = fragment.requireView();
        if (!fragment.requireContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            snack(root, R.string.camera_unavailable);
            return;
        }
        permissions.request(new PermissionHelper.Callback() {
            @Override
            public void onGranted() {
                try {
                    takePicture.launch(ImageStore.newCaptureUri(fragment.requireContext()));
                } catch (ActivityNotFoundException e) {
                    snack(root, R.string.camera_unavailable);
                }
            }

            @Override
            public void onDenied(boolean permanently) {
                if (fragment.getView() == null) return;
                if (permanently) {
                    Snackbar.make(fragment.requireView(), R.string.camera_denied, Snackbar.LENGTH_LONG)
                            .setAction(R.string.action_open_settings,
                                    v -> PermissionHelper.openAppSettings(fragment.requireContext()))
                            .show();
                } else {
                    snack(fragment.requireView(), R.string.camera_rationale);
                }
            }
        }, Manifest.permission.CAMERA);
    }

    private static void snack(View root, int text) {
        Snackbar.make(root, text, Snackbar.LENGTH_LONG).show();
    }
}
