// ──── CoreAPI ─────────────────────────────────────────────────────────▪
//     ▪ CoreAPI - Copyright © 2025 RhythmKnights [CoreAPI]
//     ▪ Original Work - Copyright © 2021 TriumphTeam [TriumphGUI]
//
//     ⏵ Licensed under the MIT License.
//         See LICENSE file in the project root for full license information.
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
     * Uses & for input parsing but § for output (Bukkit compatibility)
     */
    private static final LegacyComponentSerializer LEGACY_INPUT_SERIALIZER = LegacyComponentSerializer.builder()
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .character('&')
            .extractUrls()
            .build();

    /**
     * Legacy serializer for output that uses § symbols (what Bukkit expects)
     */
    private static final LegacyComponentSerializer LEGACY_OUTPUT_SERIALIZER = LegacyComponentSerializer.builder()
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .character('§')
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
     * 1. Legacy format ({@code &f} and {@code &#FFFFFF})
     * 2. MiniMessage format ({@code <white>} and {@code <#FFFFFF>})
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
         * - Legacy format ({@code &f} and {@code &#FFFFFF})
         * - MiniMessage format ({@code <white>} and {@code <#FFFFFF>})
         *
         * @param text Text with any supported formatting
         * @return Adventure Component with applied formatting
         */
        public @NotNull Component deserialize(@NotNull String text) {
            // Check if this looks like MiniMessage format
            if (text.contains("<") && text.contains(">")) {
                try {
                    // Try MiniMessage first for strings that look like they contain MiniMessage tags
                    return MINI_MESSAGE.deserialize(text);
                } catch (Exception ignored) {
                    // If MiniMessage parsing fails, fall back to legacy
                }
            }
            
            // Handle legacy formatting (&f and &#FFFFFF)
            return LEGACY_INPUT_SERIALIZER.deserialize(text);
        }

        /**
         * Serializes a Component to a string with legacy formatting using § symbols
         * This is what Bukkit inventories expect for proper color display
         *
         * @param component Component to serialize
         * @return String with legacy formatting using § symbols
         */
        public @NotNull String serialize(@NotNull Component component) {
            return LEGACY_OUTPUT_SERIALIZER.serialize(component);
        }
    }
}