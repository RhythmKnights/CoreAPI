// ──── CoreAPI ─────────────────────────────────────────────────────────▪
//     ▪ CoreAPI - Copyright © 2025 RhythmKnights [CoreAPI]
//     ▪ Original Work - Copyright © 2021 TriumphTeam [TriumphGUI]
//
//     ⏵ Licensed under the MIT License.
//         See LICENSE file in the project root for full license information.
// ────────────────────────────────────────────────────────────────────────────▪

package io.rhythmknights.coreapi.component.module;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Used to control what kind of interaction can happen inside a modal
 *
 * @since 3.0.0
 * @author SecretX
 */
public enum InteractionModifier {
    PREVENT_ITEM_PLACE,
    PREVENT_ITEM_TAKE,
    PREVENT_ITEM_SWAP,
    PREVENT_ITEM_DROP,
    PREVENT_OTHER_ACTIONS;

    public static final Set<InteractionModifier> VALUES = Collections.unmodifiableSet(EnumSet.allOf(InteractionModifier.class));
}