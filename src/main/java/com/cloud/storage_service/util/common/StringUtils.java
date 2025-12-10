package com.cloud.storage_service.util.common;

import java.util.Arrays;
import java.util.stream.Collectors;

import static com.cloud.storage_service.constants.GeneralConstant.SINGLE_SPACE;

@SuppressWarnings({
        "PMD.UseUtilityClass",
        "PMD.UseUnderscoresInNumericLiterals"})
public class StringUtils {

    /**
     * Capitalizes each word in a string, splitting by spaces, underscores, or slashes.
     */
    public static String capitalizeWords(String input) {
        return Arrays.stream(input.split("[\\s_/]+")) // Split by space, underscore, or slash
                .filter(word -> !word.isEmpty()) // Avoid empty substrings
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                .collect(Collectors.joining(SINGLE_SPACE));
    }

    /**
     * Extracts the base name and extension from a filename.
     */
    public static String[] extractBaseNameAndExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return new String[] {
                    fileName.substring(0, lastDotIndex), // Base name
                    fileName.substring(lastDotIndex + 1) // Extension
            };
        }
        return new String[] { fileName, null }; // No extension
    }

    public static String generateReferenceNumber() {
        return "Q" + (int) (Math.random() * 100_000_0000);
    }

    public static String generateReferenceNumber(String code) {
        return code + System.currentTimeMillis();
    }

    /**
     * Ensures the given prefix is non-null and ends with a forward slash.
     *
     * @param prefix the input string
     * @return a normalized prefix that is never null and always ends with "/"
     */
    public static String normalizePrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return "";
        }

        prefix = prefix.replaceAll("^/+", "");
        prefix = prefix.replaceAll("/+$", "");
        return prefix;
    }
}