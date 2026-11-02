package br.ufms.leiaufms.ui.common;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/** Capa do livro: foto do exemplar ou as iniciais do título. */
public final class Covers {

    private Covers() {
    }

    public static void bind(ImageView image, TextView text, String fotoPath, String titulo) {
        int size = Math.max(image.getLayoutParams().width, image.getLayoutParams().height);
        if (size <= 0) size = image.getResources().getDisplayMetrics().widthPixels / 3;
        boolean hasPhoto = ImageLoader.load(image, fotoPath, size);
        text.setVisibility(hasPhoto ? View.GONE : View.VISIBLE);
        text.setText(ImageLoader.initials(titulo));
    }
}
