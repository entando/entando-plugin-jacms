package com.agiletec.plugins.jacms.aps.system.services;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * This class was introduced to accommodate inconsistencies between Java 8 and Java 11 in the way they implemented
 * the medium date style formatting in the Italian locale. Java 8 uses dashes to separate words, Java 11 uses spaces
 */
public class Jdk11CompatibleDateFormatter {
    
    public static String formatMediumDate(String input) {
        return formatDate(input, DateFormat.MEDIUM);
    }

    public static String formatLongDate(String input) {
        return formatDate(input, DateFormat.LONG);
    }

    public static String formatDate(String input, int format) {
        try {
            Date date = new SimpleDateFormat("dd-MMM-yyyy", Locale.forLanguageTag("it")).parse(input);
            return DateFormat.getDateInstance(format, Locale.forLanguageTag("it")).format(date);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static void main(String[] args) {
        System.out.println(formatMediumDate("28-mar-2009"));
    }
}
