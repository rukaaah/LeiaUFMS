package br.ufms.leiaufms.core.media;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Fotos no armazenamento interno do app (ADR-04, RF-09, RF-10). A imagem da câmera ou da galeria é
 * reduzida (lado maior até 1280 px), girada conforme o EXIF e gravada em filesDir/fotos.
 * O Room guarda só o caminho.
 */
public final class ImageStore {

    private static final String DIR = "fotos";
    private static final int MAX_SIDE = 1280;

    private ImageStore() {
    }

    private static File dir(Context context) {
        File dir = new File(context.getFilesDir(), DIR);
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    /** Arquivo temporário para a câmera escrever (TakePicture) e sua Uri via FileProvider. */
    public static Uri newCaptureUri(Context context) {
        File file = new File(dir(context), "captura_temp.jpg");
        return FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
    }

    public static File captureFile(Context context) {
        return new File(dir(context), "captura_temp.jpg");
    }

    /** Copia e reduz a imagem de origem. Devolve o caminho absoluto ou null se não for uma imagem válida. */
    @WorkerThread
    @Nullable
    public static String save(Context context, Uri source, String prefix) throws IOException {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream in = context.getContentResolver().openInputStream(source)) {
            if (in == null) return null;
            BitmapFactory.decodeStream(in, null, bounds);
        }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null;
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight, MAX_SIDE);
        Bitmap bitmap;
        try (InputStream in = context.getContentResolver().openInputStream(source)) {
            bitmap = BitmapFactory.decodeStream(in, null, opts);
        }
        if (bitmap == null) return null;
        bitmap = rotate(context, source, scaleDown(bitmap));
        File out = new File(dir(context), prefix + "_" + System.currentTimeMillis() + ".jpg");
        try (OutputStream os = new FileOutputStream(out)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, os);
        } finally {
            bitmap.recycle();
        }
        return out.getAbsolutePath();
    }

    /** Apaga uma foto antiga, só se ela estiver dentro da pasta do app. */
    @WorkerThread
    public static void delete(Context context, @Nullable String path) {
        if (path == null) return;
        File f = new File(path);
        if (f.getParentFile() != null && f.getParentFile().equals(dir(context))) f.delete();
    }

    /** Carrega uma miniatura para exibir (decodificação com inSampleSize). */
    @WorkerThread
    @Nullable
    public static Bitmap loadThumbnail(@Nullable String path, int targetPx) {
        if (path == null || !new File(path).exists()) return null;
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bounds);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight, Math.max(1, targetPx));
        return BitmapFactory.decodeFile(path, opts);
    }

    static int sampleSize(int width, int height, int target) {
        int sample = 1;
        while (Math.max(width, height) / (sample * 2) >= target) sample *= 2;
        return sample;
    }

    private static Bitmap scaleDown(Bitmap src) {
        int max = Math.max(src.getWidth(), src.getHeight());
        if (max <= MAX_SIDE) return src;
        float f = MAX_SIDE / (float) max;
        Bitmap scaled = Bitmap.createScaledBitmap(src, Math.round(src.getWidth() * f),
                Math.round(src.getHeight() * f), true);
        if (scaled != src) src.recycle();
        return scaled;
    }

    private static Bitmap rotate(Context context, Uri source, Bitmap bitmap) {
        int degrees = 0;
        try (InputStream in = context.getContentResolver().openInputStream(source)) {
            if (in != null) {
                int o = new ExifInterface(in).getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL);
                if (o == ExifInterface.ORIENTATION_ROTATE_90) degrees = 90;
                else if (o == ExifInterface.ORIENTATION_ROTATE_180) degrees = 180;
                else if (o == ExifInterface.ORIENTATION_ROTATE_270) degrees = 270;
            }
        } catch (IOException ignored) {
            // Sem EXIF: mantém a orientação
        }
        if (degrees == 0) return bitmap;
        Matrix m = new Matrix();
        m.postRotate(degrees);
        Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
        if (rotated != bitmap) bitmap.recycle();
        return rotated;
    }
}
