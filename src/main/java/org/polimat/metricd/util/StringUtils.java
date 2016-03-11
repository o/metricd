package org.polimat.metricd.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    public static String getFirstMatchFromString(final Pattern pattern, final String string) {
        Matcher matcher = pattern.matcher(string);
        if (matcher.find()) {
            final String firstMatch = matcher.group(1);
            if (firstMatch != null && firstMatch.length() > 0) {
                return firstMatch;
            }
        }

        return null;
    }

    public static List<String> getAllMatchesFromString(final Pattern pattern, final String string) {
        List<String> matches = new ArrayList<>();

        Matcher matcher = pattern.matcher(string);
        while (matcher.find()) {
            matches.add(matcher.group());
        }

        return matches;
    }

}
