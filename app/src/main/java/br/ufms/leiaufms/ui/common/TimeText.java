package br.ufms.leiaufms.ui.common;

import android.content.Context;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import br.ufms.leiaufms.R;

/** Tempo relativo sempre em português, independente do idioma do aparelho ("há 5 horas"). */
public final class TimeText {

    private static final long MINUTE = 60_000L;
    private static final long HOUR = 60 * MINUTE;
    private static final long DAY = 24 * HOUR;
    private static final Locale PT_BR = new Locale("pt", "BR");

    private TimeText() {
    }

    public static String ago(Context c, long when) {
        long diff = Math.max(0, System.currentTimeMillis() - when);
        if (diff < MINUTE) return c.getString(R.string.time_now);
        if (diff < HOUR) {
            int m = (int) (diff / MINUTE);
            return c.getResources().getQuantityString(R.plurals.time_minutes, m, m);
        }
        if (diff < DAY) {
            int h = (int) (diff / HOUR);
            return c.getResources().getQuantityString(R.plurals.time_hours, h, h);
        }
        if (diff < 7 * DAY) {
            int d = (int) (diff / DAY);
            return c.getResources().getQuantityString(R.plurals.time_days, d, d);
        }
        return date(when);
    }

    public static String date(long when) {
        return DateFormat.getDateInstance(DateFormat.SHORT, PT_BR).format(new Date(when));
    }

    public static String hour(long when) {
        return DateFormat.getTimeInstance(DateFormat.SHORT, PT_BR).format(new Date(when));
    }

    /** "Hoje 23:35 · em 18 min", "Amanhã 12:00" ou "05/11 12:00": data em palavra curta, hora em destaque. */
    public static String meeting(Context c, long when) {
        long now = System.currentTimeMillis();
        String day;
        if (android.text.format.DateUtils.isToday(when)) day = c.getString(R.string.today);
        else if (android.text.format.DateUtils.isToday(when - DAY)) day = c.getString(R.string.tomorrow);
        else day = new java.text.SimpleDateFormat("dd/MM", PT_BR).format(new Date(when));
        String base = day + " " + hour(when);
        long diff = when - now;
        if (diff > 0 && diff < 2 * HOUR) {
            return c.getString(R.string.meet_time_rel, base, c.getString(R.string.time_in_min, (int) Math.ceil(diff / (double) MINUTE)));
        }
        return base;
    }

    public static String dateTime(long when) {
        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, PT_BR).format(new Date(when));
    }
}
