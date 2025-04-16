package com.httydcraft.authcraft.core.util;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// #region Class Documentation
/**
 * Utility class for parsing time durations.
 * Converts human-readable time strings into millisecond durations.
 */
public final class TimeUtils {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private static final String YEAR_PATTERN = createTimeKeyPattern("y|г");
    private static final String MONTH_PATTERN = createTimeKeyPattern("mo|мес");
    private static final String WEEK_PATTERN = createTimeKeyPattern("w|н");
    private static final String DAY_PATTERN = createTimeKeyPattern("d|д");
    private static final String HOUR_PATTERN = createTimeKeyPattern("h|ч");
    private static final String MINUTE_PATTERN = createTimeKeyPattern("m|м");
    private static final String SECOND_PATTERN = createTimeKeyPattern("s|с");
    private static final String MILLISECOND_PATTERN = createTimeKeyPattern("ms|мс");
    private static final Pattern TIME_PATTERN = Pattern.compile(
            YEAR_PATTERN + MONTH_PATTERN + WEEK_PATTERN + DAY_PATTERN +
                    HOUR_PATTERN + MINUTE_PATTERN + SECOND_PATTERN + MILLISECOND_PATTERN,
            Pattern.CASE_INSENSITIVE);
    private static final int[] CALENDAR_TYPE = {
            Calendar.YEAR, Calendar.MONTH, Calendar.WEEK_OF_YEAR, Calendar.DAY_OF_MONTH,
            Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND
    };

    private TimeUtils() {
        throw new AssertionError("TimeUtils cannot be instantiated");
    }
    // #endregion

    // #region Time Parsing
    /**
     * Checks if a string can be parsed as a time duration.
     *
     * @param input The input string to check. May be null.
     * @return {@code true} if the string contains a valid time pattern, {@code false} otherwise.
     */
    public static boolean canParseDate(String input) {
        if (input == null) {
            LOGGER.atFine().log("Input is null, cannot parse date");
            return false;
        }
        boolean result = TIME_PATTERN.matcher(input).find();
        LOGGER.atFine().log("Can parse date '%s': %b", input, result);
        return result;
    }

    /**
     * Parses a time duration string into milliseconds.
     *
     * @param input The time duration string. May be null.
     * @return The duration in milliseconds, or 0 if input is null or invalid.
     */
    public static long parseDuration(String input) {
        if (input == null) {
            LOGGER.atFine().log("Input is null, returning 0");
            return 0;
        }
        LOGGER.atFine().log("Parsing duration: %s", input);

        Matcher dateMatcher = TIME_PATTERN.matcher(input);
        long currentTimeMillis = System.currentTimeMillis();
        Calendar calendar = new GregorianCalendar();
        while (dateMatcher.find()) {
            for (int i = 0; i < CALENDAR_TYPE.length; i++) {
                int value = getMatcherGroupInt(dateMatcher, i + 1);
                if (value > 0) {
                    calendar.add(CALENDAR_TYPE[i], value);
                    LOGGER.atFine().log("Added %d to calendar type %d", value, CALENDAR_TYPE[i]);
                }
            }
        }
        long result = calendar.getTimeInMillis() - currentTimeMillis;
        LOGGER.atFine().log("Parsed duration: %d ms", result);
        return result;
    }
    // #endregion

    // #region Helper Methods
    /**
     * Creates a regex pattern for a time key.
     *
     * @param timeKeyRegex The regex for the time unit. Must not be null.
     * @return The formatted regex pattern.
     */
    private static String createTimeKeyPattern(String timeKeyRegex) {
        Preconditions.checkNotNull(timeKeyRegex, "timeKeyRegex must not be null");
        String pattern = "(?:([0-9]+)\\s*(?:" + timeKeyRegex + ")[a-z]*[,\\s]*)?";
        LOGGER.atFine().log("Created time key pattern: %s", pattern);
        return pattern;
    }

    /**
     * Extracts an integer value from a matcher group.
     *
     * @param matcher The matcher. Must not be null.
     * @param groupIndex The group index.
     * @return The parsed integer, or 0 if the group is empty or invalid.
     */
    private static int getMatcherGroupInt(Matcher matcher, int groupIndex) {
        Preconditions.checkNotNull(matcher, "matcher must not be null");
        String group = matcher.group(groupIndex);
        if (group != null && !group.isEmpty()) {
            try {
                int result = Integer.parseInt(group);
                LOGGER.atFine().log("Parsed group %d: %d", groupIndex, result);
                return result;
            } catch (NumberFormatException e) {
                LOGGER.atWarning().withCause(e).log("Invalid number in group %d: %s", groupIndex, group);
            }
        }
        return 0;
    }
    // #endregion
}