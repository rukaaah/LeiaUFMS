package br.ufms.leiaufms.ui.common;

import android.content.Context;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import br.ufms.leiaufms.R;

/** Lista oficial de cursos de graduação da UFMS (res/values/cursos_ufms.xml, Dados Abertos UFMS). */
public final class Courses {

    private Courses() {
    }

    public static void attach(MaterialAutoCompleteTextView field) {
        field.setSimpleItems(field.getContext().getResources().getStringArray(R.array.cursos_ufms));
    }

    /** Nome oficial do curso digitado (ignora maiúsculas e espaços), ou null se não estiver na lista. */
    public static String official(Context c, String typed) {
        if (typed == null) return null;
        String t = typed.trim();
        for (String s : c.getResources().getStringArray(R.array.cursos_ufms)) {
            if (s.equalsIgnoreCase(t)) return s;
        }
        return null;
    }
}
