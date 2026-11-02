package br.ufms.leiaufms.ui.common;

import android.graphics.Bitmap;
import android.util.LruCache;
import android.widget.ImageView;

import br.ufms.leiaufms.core.media.ImageStore;
import br.ufms.leiaufms.core.util.AppExecutors;

/** Carrega fotos locais fora da main thread, com cache em memória pequeno. */
public final class ImageLoader {

    private static final LruCache<String, Bitmap> CACHE = new LruCache<String, Bitmap>(16 * 1024 * 1024) {
        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getByteCount();
        }
    };

    private ImageLoader() {
    }

    /** @return false se não há caminho (quem chama mostra o substituto, como iniciais ou capa genérica). */
    public static boolean load(ImageView view, String path, int targetPx) {
        view.setTag(path);
        if (path == null) {
            view.setImageDrawable(null);
            return false;
        }
        String key = path + "@" + targetPx;
        Bitmap cached = CACHE.get(key);
        if (cached != null) {
            view.setImageBitmap(cached);
            return true;
        }
        view.setImageDrawable(null);
        AppExecutors.io().execute(() -> {
            Bitmap bmp = ImageStore.loadThumbnail(path, targetPx);
            if (bmp != null) CACHE.put(key, bmp);
            AppExecutors.main(() -> {
                if (path.equals(view.getTag()) && bmp != null) view.setImageBitmap(bmp);
            });
        });
        return true;
    }

    public static String initials(String name) {
        if (name == null) return "";
        String[] parts = name.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (!p.isEmpty() && Character.isLetter(p.charAt(0))) sb.append(Character.toUpperCase(p.charAt(0)));
            if (sb.length() == 2) break;
        }
        return sb.toString();
    }
}
