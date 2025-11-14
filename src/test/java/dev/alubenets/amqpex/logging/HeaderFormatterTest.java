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
            var result = HeaderFormatter.formatHeaders(null);
            assertThat(result).isEqualTo("{}");
        }

        /**
         * Tests that empty headers are formatted as an empty map representation.
         */
        @Test
        void shouldReturnEmptyMapRepresentationForEmptyHeaders() {
            var result = HeaderFormatter.formatHeaders(Map.of());
            assertThat(result).isEqualTo("{}");
        }

        /**
         * Tests that string headers are formatted correctly.
         */
        @Test
        void shouldFormatStringHeadersCorrectly() {
            Map<String, Object> headers = Map.of("key1", "value1", "key2", "value2");
            var result = HeaderFormatter.formatHeaders(headers);
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
            Map<String, Object> headers = new java.util.HashMap<>();
            headers.put("string-header", "string-value");
            headers.put("integer-header", 42);
            headers.put("boolean-header", true);
            headers.put("double-header", 3.14);
            headers.put("null-header", null);

            var result = HeaderFormatter.formatHeaders(headers);
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
            Map<String, Object> headers = Map.of("list-header", List.of("item1", "item2", "item3"));
            var result = HeaderFormatter.formatHeaders(headers);
            assertThat(result).contains("list-header=[\"item1\", \"item2\", \"item3\"]");
        }

        /**
         * Tests that string array headers are formatted correctly.
         */
        @Test
        void shouldFormatStringArrayHeadersCorrectly() {
            Map<String, Object> headers = Map.of("array-header", new String[]{"arr1", "arr2", "arr3"});
            var result = HeaderFormatter.formatHeaders(headers);
            assertThat(result).contains("array-header=[arr1, arr2, arr3]");
        }

        /**
         * Tests that primitive boolean array headers are formatted correctly.
         */
        @Test
        void shouldFormatBooleanArrayHeadersCorrectly() {
            Map<String, Object> headers = Map.of("array-header", new boolean[]{true, false, true});
            var result = HeaderFormatter.formatHeaders(headers);
            assertThat(result).contains("array-header=[true, false, true]");
        }

        /**
         * Tests that primitive byte array headers are formatted correctly.
         */
        @Test
        void shouldFormatByteArrayHeadersCorrectly() {
            Map<String, Object> headers = Map.of("array-header", new byte[]{1, 2, 3});
            var result = HeaderFormatter.formatHeaders(headers);
            assertThat(result).contains("array-header=[1, 2, 3]");
        }

        /**
         * Tests that primitive char array headers are formatted correctly.
         */
        @Test
        void shouldFormatCharArrayHeadersCorrectly() {
            Map<String, Object> headers = Map.of("array-header", new char[]{'a', 'b', 'c'});
            var result = HeaderFormatter.formatHeaders(headers);
            assertThat(result).contains("array-header=[a, b, c]");
        }

        /**
         * Tests that primitive short array headers are formatted correctly.
         */
        @Test
        void shouldFormatShortArrayHeadersCorrectly() {
            Map<String, Object> headers = Map.of("array-header", new short[]{1, 2, 3});
            var result = HeaderFormatter.formatHeaders(headers);
            assertThat(result).contains("array-header=[1, 2, 3]");
        }

        /**
         * Tests that primitive int array headers are formatted correctly.
         */
        @Test
        void shouldFormatIntArrayHeadersCorrectly() {
            Map<String, Object> headers = Map.of("array-header", new int[]{1, 2, 3});
            var result = HeaderFormatter.formatHeaders(headers);
            assertThat(result).contains("array-header=[1, 2, 3]");
        }

        /**
         * Tests that primitive long array headers are formatted correctly.
         */
        @Test
        void shouldFormatLongArrayHeadersCorrectly() {
            Map<String, Object> headers = Map.of("array-header", new long[]{1L, 2L, 3L});
            var result = HeaderFormatter.formatHeaders(headers);
            assertThat(result).contains("array-header=[1, 2, 3]");
        }

        /**
         * Tests that primitive float array headers are formatted correctly.
         */
        @Test
        void shouldFormatFloatArrayHeadersCorrectly() {
            Map<String, Object> headers = Map.of("array-header", new float[]{1.1f, 2.2f, 3.3f});
            var result = HeaderFormatter.formatHeaders(headers);
            assertThat(result).contains("array-header=[1.1, 2.2, 3.3]");
        }

        /**
         * Tests that primitive double array headers are formatted correctly.
         */
        @Test
        void shouldFormatDoubleArrayHeadersCorrectly() {
            Map<String, Object> headers = Map.of("array-header", new double[]{1.1, 2.2, 3.3});
            var result = HeaderFormatter.formatHeaders(headers);
            assertThat(result).contains("array-header=[1.1, 2.2, 3.3]");
        }

        /**
         * Tests that object array headers are formatted correctly.
         */
        @Test
        void shouldFormatObjectArrayHeadersCorrectly() {
            Map<String, Object> headers = Map.of("array-header", new Object[]{"obj1", 42, true});
            var result = HeaderFormatter.formatHeaders(headers);
            assertThat(result).contains("array-header=[obj1, 42, true]");
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
            var headers = Map.of(
                "list-header", List.of("item1", "item2", "item3"),
                "array-header", new String[]{"arr1", "arr2", "arr3"},
                "nested-map", Map.of("key1", "value1", "key2", List.of("nested1", "nested2"))
            );

            var result = HeaderFormatter.formatHeaders(headers);

            assertThat(result)
                .contains("list-header=[\"item1\", \"item2\", \"item3\"]")
                .contains("array-header=[arr1, arr2, arr3]")
                .contains("key1=\"value1\"")
                .contains("key2=[\"nested1\", \"nested2\"]");
        }
    }

    /**
     * Tests deep nesting header formatting functionality.
     */
    @Nested
    class DeepNesting {

        /**
         * Creates a deeply nested map structure for testing.
         *
         * @param depth the depth of nesting to create
         * @return a nested map with the specified depth
         */
        private Map<String, Object> createDeeplyNestedMap(int depth) {
            if (depth <= 0) {
                return Map.of("value", "deep-value");
            }
            return Map.of("nested", createDeeplyNestedMap(depth - 1));
        }

        /**
         * Tests that deeply nested maps are truncated when exceeding max recursion depth.
         */
        @Test
        void shouldTruncateDeeplyNestedMapsWhenExceedingMaxDepth() {
            var deeplyNestedHeaders = createDeeplyNestedMap(10);
            var result = HeaderFormatter.formatHeaders(deeplyNestedHeaders);

            assertThat(result).contains("{...}");
        }

        /**
         * Tests that shallow nested maps are formatted normally without truncation.
         */
        @Test
        void shouldFormatShallowNestedMapsNormallyWithoutTruncation() {
            var shallowlyNestedHeaders = createDeeplyNestedMap(3);
            var result = HeaderFormatter.formatHeaders(shallowlyNestedHeaders);

            assertThat(result)
                .doesNotContain("{...}")
                .contains("value=\"deep-value\"");
        }

        /**
         * Tests that deeply nested collections are also handled properly.
         */
        @Test
        void shouldHandleDeeplyNestedCollectionsProperly() {
            Map<String, Object> headers = Map.of(
                "nested-list", List.of(
                    List.of(
                        List.of(
                            List.of(
                                List.of("deep-value")
                            )
                        )
                    )
                )
            );

            var result = HeaderFormatter.formatHeaders(headers);

            assertThat(result).isNotNull();
        }
    }

}