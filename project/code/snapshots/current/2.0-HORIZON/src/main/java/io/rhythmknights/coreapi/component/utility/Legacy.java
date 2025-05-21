// ──── CoreAPI ─────────────────────────────────────────────────────────▪
//     ▪ CoreAPI - Copyright © 2025 RhythmKnights [CoreAPI]
//     ▪ Original Work - Copyright © 2021 TriumphTeam [TriumphGUI]
//
//     ⏵ Licensed under the MIT License.
//         See LICENSE file in the project root for full license information.
// ────────────────────────────────────────────────────────────────────────────▪

package io.rhythmknights.coreapi.component.utility;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class for converting between different text formatting styles
 */
public final class Legacy {

    /**
     * Legacy serializer that handles &f and &#FFFFFF style color codes
     */
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder()
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .character('&')
            .extractUrls()
            .build();

    /**
     * MiniMessage parser that handles <white> and <#FFFFFF> style formatting
     */
    private static final MiniMessage MINI_MESSAGE = MiniMessage.builder()
            .strict(false)
            .build();

    /**
     * Creates a single serializer that supports multiple formats:
     * 1. Legacy format (&f and &#FFFFFF)
     * 2. MiniMessage format (<white> and <#FFFFFF>)
     * 
     * Usage:
     * - To convert a string with any supported format to a Component: SERIALIZER.deserialize(text)
     * - To convert a Component to a legacy string: SERIALIZER.serialize(component)
     */
    public static final TextSerializer SERIALIZER = new TextSerializer();

    private Legacy() {
        throw new UnsupportedOperationException("Class should not be instantiated!");
    }

    /**
     * Custom serializer that combines both legacy and MiniMessage formats
     */
    public static class TextSerializer {
        /**
         * Converts a string with any combination of formatting styles to a Component
         * Supports:
         * - Legacy format (&f and &#FFFFFF)
         * - MiniMessage format (<white> and <#FFFFFF>)
         *
         * @param text Text with any supported formatting
         * @return Adventure Component with applied formatting
         */
        public @NotNull Component deserialize(@NotNull String text) {
            // First handle legacy formatting (&f and &#FFFFFF)
            Component component = LEGACY_SERIALIZER.deserialize(text);
            
            // Then parse the text for MiniMessage formatting (<white> and <#FFFFFF>)
            String serialized = LEGACY_SERIALIZER.serialize(component);
            
            // Check if there might be MiniMessage formatting present
            if (serialized.contains("<") && serialized.contains(">")) {
                try {
                    return MINI_MESSAGE.deserialize(serialized);
                } catch (Exception ignored) {
                    // If MiniMessage parsing fails, return the component with just legacy formatting
                }
            }
            
            return component;
        }

        /**
         * Serializes a Component to a string with legacy formatting
         *
         * @param component Component to serialize
         * @return String with legacy formatting
         */
        public @NotNull String serialize(@NotNull Component component) {
            return LEGACY_SERIALIZER.serialize(component);
        }
    }
}

