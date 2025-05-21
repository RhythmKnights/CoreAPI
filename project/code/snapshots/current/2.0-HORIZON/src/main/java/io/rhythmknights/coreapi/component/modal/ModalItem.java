// ──── CoreAPI ─────────────────────────────────────────────────────────▪
//     ▪ CoreAPI - Copyright © 2025 RhythmKnights [CoreAPI]
//     ▪ Original Work - Copyright © 2021 TriumphTeam [TriumphGUI]
//
//     ⏵ Licensed under the MIT License.
//         See LICENSE file in the project root for full license information.
// ────────────────────────────────────────────────────────────────────────────▪

package io.rhythmknights.coreapi.component.modal;

import io.rhythmknights.coreapi.component.module.ModalAction;
import io.rhythmknights.coreapi.component.utility.ItemNBT;

import com.google.common.base.Preconditions;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * ModalItem represents the {@link ItemStack} on the {@link Inventory}
 */
@SuppressWarnings("unused")
public class ModalItem {

    // Random UUID to identify the item when clicking
    private final UUID uuid = UUID.randomUUID();
    // Action to do when clicking on the item
    private ModalAction<InventoryClickEvent> action;
    // The ItemStack of the ModalItem
    private ItemStack itemStack;

    /**
     * Main constructor of the ModalItem
     *
     * @param itemStack The {@link ItemStack} to be used
     * @param action    The {@link ModalAction} to run when clicking on the Item
     */
    public ModalItem(@NotNull final ItemStack itemStack, @Nullable final ModalAction<@NotNull InventoryClickEvent> action) {
        Preconditions.checkNotNull(itemStack, "The ItemStack for the Modal Item cannot be null!");

        this.action = action;

        // Sets the UUID to an NBT tag to be identifiable later
        setItemStack(itemStack);
    }

    /**
     * Secondary constructor with no action
     *
     * @param itemStack The ItemStack to be used
     */
    public ModalItem(@NotNull final ItemStack itemStack) {
        this(itemStack, null);
    }

    /**
     * Alternate constructor that takes {@link Material} instead of an {@link ItemStack} but without a {@link ModalAction}
     *
     * @param material The {@link Material} to be used when invoking class
     */
    public ModalItem(@NotNull final Material material) {
        this(new ItemStack(material), null);
    }

    /**
     * Alternate constructor that takes {@link Material} instead of an {@link ItemStack}
     *
     * @param material The {@code Material} to be used when invoking class
     * @param action   The {@link ModalAction} should be passed on {@link InventoryClickEvent}
     */
    public ModalItem(@NotNull final Material material, @Nullable final ModalAction<@NotNull InventoryClickEvent> action) {
        this(new ItemStack(material), action);
    }

    /**
     * Gets the ModalItem's {@link ItemStack}
     *
     * @return The {@link ItemStack}
     */
    @NotNull
    public ItemStack getItemStack() {
        return itemStack;
    }

    /**
     * Replaces the {@link ItemStack} of the Modal Item
     *
     * @param itemStack The new {@link ItemStack}
     */
    public void setItemStack(@NotNull final ItemStack itemStack) {
        Preconditions.checkNotNull(itemStack, "The ItemStack for the Modal Item cannot be null!");
        if (itemStack.getType() != Material.AIR) {
            this.itemStack = ItemNBT.setString(itemStack.clone(), "cf-modal", uuid.toString());
        } else {
            this.itemStack = itemStack.clone();
        }
    }

    /**
     * Gets the {@link ModalAction} to do when the player clicks on it
     */
    public @Nullable ModalAction<InventoryClickEvent> getAction() {
        return action;
    }

    /**
     * Replaces the {@link ModalAction} of the current Modal Item
     *
     * @param action The new {@link ModalAction} to set
     */
    public void setAction(@Nullable final ModalAction<@NotNull InventoryClickEvent> action) {
        this.action = action;
    }

    /**
     * Gets the random {@link UUID} that was generated when the ModalItem was made
     */
    @NotNull UUID getUuid() {
        return uuid;
    }
}
