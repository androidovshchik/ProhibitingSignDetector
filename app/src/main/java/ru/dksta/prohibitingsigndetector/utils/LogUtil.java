package ru.dksta.prohibitingsigndetector.utils;

import java.util.Collections;

import timber.log.Timber;

public final class LogUtil {

    private static int STYLED_LOG_LENGTH = 48;

    public static Timber.Tree tag(String tag) {
        return Timber.tag(tag);
    }

    public static void logCentered(String character, String tag, String text) {
        int length = text.length() + 2;
        String edge;
        if (length >= STYLED_LOG_LENGTH) {
            edge = "";
            text = character + "%s" + text.substring(0, STYLED_LOG_LENGTH - 5)
                    + "%s" + "..." + character;
        } else {
            edge = repeat(" ", (STYLED_LOG_LENGTH - length) / 2);
            text = character + "%s" + text + "%s" + (length % 2 == 0 ? "" : " ") + character;
        }
        log(tag, text, edge);
    }

    private static void log(String tag, String text, String edge) {
        tag(tag).i(text, edge, edge);
    }

    public static void logDivider(String tag, String character) {
        tag(tag).i(repeat(character, STYLED_LOG_LENGTH));
    }

    private static String repeat(String toRepeat, int times) {
        return Collections.nCopies(times, toRepeat)
                .toString()
                .replace("[", "")
                .replace("]", "")
                .replaceAll(", ", "");
    }
}
