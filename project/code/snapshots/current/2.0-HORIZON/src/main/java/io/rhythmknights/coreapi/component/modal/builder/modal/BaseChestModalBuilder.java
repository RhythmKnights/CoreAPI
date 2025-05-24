// ──── CoreAPI ─────────────────────────────────────────────────────────▪
//     ▪ CoreAPI - Copyright © 2025 RhythmKnights [CoreAPI]
//     ▪ Original Work - Copyright © 2021 TriumphTeam [TriumphGUI]
//
//     ⏵ Licensed under the MIT License.
//         See LICENSE file in the project root for full license information.
// ────────────────────────────────────────────────────────────────────────────▪

package io.rhythmknights.coreapi.component.modal.builder.modal;

import io.rhythmknights.coreapi.component.module.ModalContainer;
import io.rhythmknights.coreapi.component.module.InventoryProvider;
import io.rhythmknights.coreapi.component.modal.BaseModal;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unchecked")
public abstract class BaseChestModalBuilder<M extends BaseModal, B extends BaseChestModalBuilder<M, B>> extends BaseModalBuilder<M, B> {

    private int rows = 1;
    
    // Create a proper legacy serializer that handles color codes correctly
    private static final LegacyComponentSerializer INVENTORY_SERIALIZER = LegacyComponentSerializer.builder()
            .character('§') // Use section symbol for Bukkit inventory titles
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();
    
    private InventoryProvider.Chest inventoryProvider =
            (title, owner, rows) -> {
                // Convert Component to legacy string for Bukkit inventory creation
                // This preserves the formatting by using section symbols (§) instead of ampersands (&)
                String titleString = INVENTORY_SERIALIZER.serialize(title);
                return Bukkit.createInventory(owner, rows, titleString);
            };

    /**
     * Sets the rows for the modal
     * This will only work on CHEST {@link io.rhythmknights.coreapi.component.module.ModalType}
     *
     * @param rows The number of rows
     * @return The builder
     */
    @NotNull
    @Contract("_ -> this")
    public B rows(final int rows) {
        this.rows = rows;
        return (B) this;
    }

    public B inventory(@NotNull final InventoryProvider.Chest inventoryProvider) {
        this.inventoryProvider = inventoryProvider;
        return (B) this;
    }

    /**
     * Getter for the rows
     *
     * @return The amount of rows
     */
    protected int getRows() {
        return rows;
    }

    protected @NotNull InventoryProvider.Chest getInventoryProvider() {
        return inventoryProvider;
    }

    protected @NotNull ModalContainer.Chest createContainer() {
        return new ModalContainer.Chest(getTitle(), inventoryProvider, getRows());
    }
}