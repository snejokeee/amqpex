package dev.alubenets.amqpex.logging;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Utility class for formatting message headers in a standardized way.
 * This class handles the proper string representation of different header value types,
 * including arrays, collections, maps, and other objects with special formatting for readability.
 *
 * <p>Features:
 * <ul>
 *   <li>Proper array formatting using Arrays.toString()</li>
 *   <li>Recursive map formatting for nested structures</li>
 *   <li>Collection formatting with proper element handling</li>
 *   <li>String quoting to distinguish text values</li>
 *   <li>Recursion depth limiting to prevent StackOverflowErrors</li>
 *   <li>Collection size limiting to prevent log flooding</li>
 *   <li>Thread-safe utility methods</li>
 * </ul>
 */
final class HeaderFormatter {

    private static final String NULL_STRING = "null";
    private static final String EMPTY_HEADERS = "{}";
    private static final String TRUNCATED_HEADERS = "{...}";
    private static final String EMPTY_COLLECTION = "[]";
    private static final String ENTRY_SEPARATOR = ", ";
    private static final String KEY_VALUE_SEPARATOR = "=";

    private static final int MAX_RECURSION_DEPTH = 5;
    private static final int MAX_COLLECTION_ELEMENTS = 100;

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private HeaderFormatter() {
        throw new AssertionError("HeaderFormatter is a utility class and cannot be instantiated");
    }

    /**
     * Formats the headers map into a string representation that properly handles
     * different value types including arrays, collections, and other objects.
     *
     * @param headers the headers map to format, may be null
     * @return a string representation of the headers with proper value formatting,
     * "{}" if headers is null or empty
     */
    public static String formatHeaders(Map<String, Object> headers) {
        return formatHeaders(headers, 0);
    }

    /**
     * Formats the headers map with recursion depth tracking to prevent infinite recursion.
     *
     * @param headers the headers map to format, may be null
     * @param depth   the current recursion depth
     * @return a string representation of the headers with proper value formatting
     */
    private static String formatHeaders(Map<String, Object> headers, int depth) {
        if (depth > MAX_RECURSION_DEPTH) {
            return TRUNCATED_HEADERS;
        }

        if (headers == null || headers.isEmpty()) {
            return EMPTY_HEADERS;
        }

        var joiner = new StringJoiner(ENTRY_SEPARATOR, "{", "}");

        for (var entry : headers.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            joiner.add(key + KEY_VALUE_SEPARATOR + formatHeaderValue(value, depth + 1));
        }

        return joiner.toString();
    }

    /**
     * Formats an individual header value with recursion depth tracking.
     *
     * @param value the header value to format, may be null
     * @param depth the current recursion depth
     * @return a properly formatted string representation of the value
     */
    private static String formatHeaderValue(Object value, int depth) {
        if (value == null) {
            return NULL_STRING;
        }

        var valueClass = value.getClass();

        if (valueClass.isArray()) {
            return formatArray(value);
        } else if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            var nestedMap = (Map<String, Object>) value;
            return formatHeaders(nestedMap, depth);
        } else if (value instanceof Collection) {
            return formatCollection((Collection<?>) value, depth);
        } else if (value instanceof CharSequence) {
            return "\"" + value + "\"";
        } else if (value instanceof Character ch) {
            return "'" + ch + "'";
        }

        return value.toString();
    }

    /**
     * Formats an array of any type into a readable string representation.
     *
     * @param array the array to format
     * @return formatted string representation of the array
     */
    private static String formatArray(Object array) {
        var componentType = array.getClass().getComponentType();

        if (componentType == boolean.class) {
            return Arrays.toString((boolean[]) array);
        } else if (componentType == byte.class) {
            return Arrays.toString((byte[]) array);
        } else if (componentType == char.class) {
            return Arrays.toString((char[]) array);
        } else if (componentType == short.class) {
            return Arrays.toString((short[]) array);
        } else if (componentType == int.class) {
            return Arrays.toString((int[]) array);
        } else if (componentType == long.class) {
            return Arrays.toString((long[]) array);
        } else if (componentType == float.class) {
            return Arrays.toString((float[]) array);
        } else if (componentType == double.class) {
            return Arrays.toString((double[]) array);
        } else {
            return Arrays.toString((Object[]) array);
        }
    }

    /**
     * Formats a collection into a readable string representation with recursion depth tracking.
     *
     * @param collection the collection to format
     * @param depth      the current recursion depth
     * @return formatted string representation of the collection
     */
    private static String formatCollection(Collection<?> collection, int depth) {
        if (collection.isEmpty()) {
            return EMPTY_COLLECTION;
        }

        var joiner = new StringJoiner(", ", "[", "]");
        int elementCount = 0;

        for (var element : collection) {
            if (elementCount >= MAX_COLLECTION_ELEMENTS) {
                joiner.add("...");
                break;
            }
            joiner.add(formatHeaderValue(element, depth + 1));
            elementCount++;
        }

        return joiner.toString();
    }

}