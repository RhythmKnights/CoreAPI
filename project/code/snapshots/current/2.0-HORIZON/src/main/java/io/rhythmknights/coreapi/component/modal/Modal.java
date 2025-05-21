// ──── CoreAPI ─────────────────────────────────────────────────────────▪
//     ▪ CoreAPI - Copyright © 2025 RhythmKnights [CoreAPI]
//     ▪ Original Work - Copyright © 2021 TriumphTeam [TriumphGUI]
//
//     ⏵ Licensed under the MIT License.
//         See LICENSE file in the project root for full license information.
// ────────────────────────────────────────────────────────────────────────────▪

package io.rhythmknights.coreapi.component.modal;

import io.rhythmknights.coreapi.component.modal.builder.modal.PaginatedBuilder;
import io.rhythmknights.coreapi.component.modal.builder.modal.ScrollingBuilder;
import io.rhythmknights.coreapi.component.modal.builder.modal.ChestModalBuilder;
import io.rhythmknights.coreapi.component.modal.builder.modal.TypedModalBuilder;
import io.rhythmknights.coreapi.component.module.ModalContainer;
import io.rhythmknights.coreapi.component.module.ModalType;
import io.rhythmknights.coreapi.component.module.InteractionModifier;
import io.rhythmknights.coreapi.component.module.ScrollType;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Standard modal implementation of {@link BaseModal}
 */
public class Modal extends BaseModal {

    public Modal(final @NotNull ModalContainer modalContainer, final @NotNull Set<InteractionModifier> interactionModifiers) {
        super(modalContainer, interactionModifiers);
    }

    /**
     * Creates a {@link TypedModalBuilder} to build a {@link io.rhythmknights.coreapi.component.modal.Modal}
     *
     * @param type The {@link ModalType} to be used
     * @return A {@link TypedModalBuilder}
     * @since 3.0.0
     */
    @Contract("_ -> new")
    public static @NotNull TypedModalBuilder modal(final @NotNull ModalType type) {
        return new TypedModalBuilder(type);
    }

    /**
     * Creates a {@link ChestModalBuilder} with CHEST as the {@link ModalType}
     *
     * @return A CHEST {@link ChestModalBuilder}
     * @since 3.0.0
     */
    @Contract(" -> new")
    public static @NotNull ChestModalBuilder modal() {
        return new ChestModalBuilder();
    }

    /**
     * Creates a {@link PaginatedBuilder} to build a {@link io.rhythmknights.coreapi.component.modal.PaginatedModal}
     *
     * @return A {@link PaginatedBuilder}
     * @since 3.0.0
     */
    @Contract(" -> new")
    public static @NotNull PaginatedBuilder paginated() {
        return new PaginatedBuilder();
    }

    /**
     * Creates a {@link ScrollingBuilder} to build a {@link io.rhythmknights.coreapi.component.modal.ScrollingModal}
     *
     * @param scrollType The {@link ScrollType} to be used by the modal
     * @return A {@link ScrollingBuilder}
     * @since 3.0.0
     */
    @Contract("_ -> new")
    public static @NotNull ScrollingBuilder scrolling(@NotNull final ScrollType scrollType) {
        return new ScrollingBuilder(scrollType);
    }

    /**
     * Creates a {@link ScrollingBuilder} with VERTICAL as the {@link ScrollType}
     *
     * @return A vertical {@link ChestModalBuilder}
     * @since 3.0.0
     */
    @Contract(" -> new")
    public static @NotNull ScrollingBuilder scrolling() {
        return scrolling(ScrollType.VERTICAL);
    }
}
