package br.ufms.leiaufms.ui.common;

import android.content.Context;

import androidx.annotation.StringRes;

import com.google.android.material.textfield.TextInputLayout;

import br.ufms.leiaufms.R;
import br.ufms.leiaufms.core.util.Validators;

/** Traduz o resultado das validações para a microcopy do design system §7. */
public final class FieldErrors {

    private FieldErrors() {
    }

    /** Mostra o erro no campo (ou limpa) e devolve true se o campo está válido. */
    public static boolean show(TextInputLayout layout, Validators.Result result, @StringRes int emptyMessage,
                               int maxLength) {
        Context c = layout.getContext();
        String message = message(c, result, emptyMessage, maxLength);
        layout.setError(message);
        return message == null;
    }

    public static String message(Context c, Validators.Result result, @StringRes int emptyMessage, int maxLength) {
        switch (result) {
            case OK:
                return null;
            case EMPTY:
                return c.getString(emptyMessage);
            case TOO_SHORT:
                return c.getString(R.string.error_name_short);
            case TOO_LONG:
                return c.getString(R.string.error_too_long, maxLength);
            case WRONG_DOMAIN:
                return c.getString(R.string.error_email_domain);
            case WEAK:
                return c.getString(R.string.error_password_weak);
            case MISMATCH:
                return c.getString(R.string.error_password_mismatch);
            case OUT_OF_RANGE:
                return c.getString(R.string.error_semester_range);
            case INVALID:
            default:
                return c.getString(R.string.error_email_invalid);
        }
    }

    public static char[] chars(android.widget.EditText editText) {
        android.text.Editable e = editText.getText();
        if (e == null) return new char[0];
        char[] out = new char[e.length()];
        e.getChars(0, e.length(), out, 0);
        return out;
    }

    public static String text(android.widget.EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString();
    }
}
