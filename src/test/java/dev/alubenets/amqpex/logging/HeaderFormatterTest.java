package dev.alubenets.amqpex.logging;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link HeaderFormatter}.
 * <p>
 * Tests the core logic of header formatting with various data types.
 * All tests ensure that header formatting functionality works as expected
 * while maintaining proper string representations for different value types.
 */
class HeaderFormatterTest {

    /**
     * Tests basic header formatting functionality.
     */
    @Nested
    class BasicFormatting {

        /**
         * Tests that null headers are formatted as an empty map representation.
         */
        @Test
        void shouldReturnEmptyMapRepresentationForNullHeaders() {
            String result = HeaderFormatter.formatHeaders(null);
            assertThat(result).isEqualTo("{}");
        }

        /**
         * Tests that empty headers are formatted as an empty map representation.
         */
        @Test
        void shouldReturnEmptyMapRepresentationForEmptyHeaders() {
            String result = HeaderFormatter.formatHeaders(Map.of());
            assertThat(result).isEqualTo("{}");
        }

        /**
         * Tests that string headers are formatted correctly.
         */
        @Test
        void shouldFormatStringHeadersCorrectly() {
            java.util.Map<String, Object> headers = Map.of("key1", "value1", "key2", "value2");
            String result = HeaderFormatter.formatHeaders(headers);
            assertThat(result)
                .contains("key1=\"value1\"")
                .contains("key2=\"value2\"");
        }
    }

    /**
     * Tests different value type handling functionality.
     */
    @Nested
    class ValueTypes {

        /**
         * Tests that different types of header values are properly handled and formatted.
         * <p>
         * Verifies that headers with various value types (String, Integer, Boolean, etc.) are formatted correctly.
         */
        @Test
        void shouldFormatDifferentValueTypesCorrectly() {
            java.util.Map<String, Object> headers = new java.util.HashMap<>();
            headers.put("string-header", "string-value");
            headers.put("integer-header", 42);
            headers.put("boolean-header", true);
            headers.put("double-header", 3.14);
            headers.put("null-header", null);

            String result = HeaderFormatter.formatHeaders(headers);
            assertThat(result)
                .contains("string-header=\"string-value\"")
                .contains("integer-header=42")
                .contains("boolean-header=true")
                .contains("double-header=3.14")
                .contains("null-header=null");
        }

        /**
         * Tests that list headers are formatted correctly.
         */
        @Test
        void shouldFormatListHeadersCorrectly() {
            java.util.Map<String, Object> headers = Map.of("list-header", List.of("item1", "item2", "item3"));
            String result = HeaderFormatter.formatHeaders(headers);
            assertThat(result).contains("list-header=[\"item1\", \"item2\", \"item3\"]");
        }

        /**
         * Tests that array headers are formatted correctly.
         */
        @Test
        void shouldFormatArrayHeadersCorrectly() {
            java.util.Map<String, Object> headers = Map.of("array-header", new String[]{"arr1", "arr2", "arr3"});
            String result = HeaderFormatter.formatHeaders(headers);
            assertThat(result).contains("array-header=[arr1, arr2, arr3]");
        }
    }

    /**
     * Tests complex header formatting functionality.
     */
    @Nested
    class ComplexFormatting {

        /**
         * Tests that complex object header values are properly handled and formatted.
         * <p>
         * Verifies that headers with complex object types (List, Map, nested structures) are formatted correctly.
         */
        @Test
        void shouldFormatComplexHeadersCorrectly() {
            java.util.Map<String, Object> headers = Map.of(
                "list-header", List.of("item1", "item2", "item3"),
                "array-header", new String[]{"arr1", "arr2", "arr3"},
                "nested-map", Map.of("key1", "value1", "key2", List.of("nested1", "nested2"))
            );

            String result = HeaderFormatter.formatHeaders(headers);

            // Check that all expected elements are present in the result, regardless of order
            assertThat(result)
                .contains("list-header=[\"item1\", \"item2\", \"item3\"]")
                .contains("array-header=[arr1, arr2, arr3]")
                .contains("key1=\"value1\"")
                .contains("key2=[\"nested1\", \"nested2\"]");
        }
    }

}