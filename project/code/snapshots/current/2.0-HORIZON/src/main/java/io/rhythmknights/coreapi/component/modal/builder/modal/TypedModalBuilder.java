// ──── CoreAPI ─────────────────────────────────────────────────────────▪
//     ▪ CoreAPI - Copyright © 2025 RhythmKnights [CoreAPI]
//     ▪ Original Work - Copyright © 2021 TriumphTeam [TriumphGUI]
//
//     ⏵ Licensed under the MIT License.
//         See LICENSE file in the project root for full license information.
// ────────────────────────────────────────────────────────────────────────────▪

package io.rhythmknights.coreapi.component.modal.builder.modal;

import io.rhythmknights.coreapi.component.module.ModalContainer;
import io.rhythmknights.coreapi.component.module.ModalType;
import io.rhythmknights.coreapi.component.module.InventoryProvider;
import io.rhythmknights.coreapi.component.utility.Legacy;
import io.rhythmknights.coreapi.component.modal.Modal;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * The simple modal builder is used for creating a {@link Modal}
 */
public final class TypedModalBuilder extends BaseModalBuilder<Modal, TypedModalBuilder> {

    private ModalType modalType;
    private InventoryProvider.Typed inventoryProvider =
        (title, owner, type) -> Bukkit.createInventory(owner, type, Legacy.SERIALIZER.serialize(title));

    /**
     * Main constructor
     *
     * @param modalType The {@link ModalType} to default to
     */
    public TypedModalBuilder(final @NotNull ModalType modalType) {
        this.modalType = modalType;
    }

    public TypedModalBuilder(final @NotNull ModalType modalType, final @NotNull ChestModalBuilder builder) {
        this.modalType = modalType;
        consumeBuilder(builder);
    }

    /**
     * Sets the {@link ModalType} to use on the modal
     * This method is unique to the simple modal
     *
     * @param modalType The {@link ModalType}
     * @return The current builder
     */
    @NotNull
    @Contract("_ -> this")
    public TypedModalBuilder type(final @NotNull ModalType modalType) {
        this.modalType = modalType;
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public TypedModalBuilder inventory(@NotNull final InventoryProvider.Typed inventoryProvider) {
        this.inventoryProvider = inventoryProvider;
        return this;
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
        final Modal modal = new Modal(new ModalContainer.Typed(getTitle(), inventoryProvider, modalType), getModifiers());

        // Set update title on item click if specified
        if (updateTitleOnItemClick) {
            modal.setUpdateTitleOnItemClick(true);
        }

        final Consumer<Modal> consumer = getConsumer();
        if (consumer != null) consumer.accept(modal);
        return modal;
    }

}
