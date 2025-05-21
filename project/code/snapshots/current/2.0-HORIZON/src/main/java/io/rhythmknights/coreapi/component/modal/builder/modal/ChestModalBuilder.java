// ──── CoreAPI ─────────────────────────────────────────────────────────▪
//     ▪ CoreAPI - Copyright © 2025 RhythmKnights [CoreAPI]
//     ▪ Original Work - Copyright © 2021 TriumphTeam [TriumphGUI]
//
//     ⏵ Licensed under the MIT License.
//         See LICENSE file in the project root for full license information.
// ────────────────────────────────────────────────────────────────────────────▪

package io.rhythmknights.coreapi.component.modal.builder.modal;

import io.rhythmknights.coreapi.component.module.ModalType;
import io.rhythmknights.coreapi.component.modal.Modal;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * The simple modal builder is used for creating a {@link Modal}
 */
public final class ChestModalBuilder extends BaseChestModalBuilder<Modal, ChestModalBuilder> {

    /**
     * Sets the {@link ModalType} to use on the modal
     * This method is unique to the simple modal
     *
     * @param modalType The {@link ModalType}
     * @return The current builder
     */
    @NotNull
    @Contract("_ -> new")
    public TypedModalBuilder type(@NotNull final ModalType modalType) {
        return new TypedModalBuilder(modalType, this);
    }

    /**
     * Creates a new {@link Modal}
     *
     * @return A new {@link Modal}
     */
    @NotNull
    @Override
    @Contract(" -> new")
    public Modal create() {
        final Modal modal = new Modal(createContainer(), getModifiers());

        // Set update title on item click if specified
        if (updateTitleOnItemClick) {
            modal.setUpdateTitleOnItemClick(true);
        }

        final Consumer<Modal> consumer = getConsumer();
        if (consumer != null) consumer.accept(modal);
        return modal;
    }
}
