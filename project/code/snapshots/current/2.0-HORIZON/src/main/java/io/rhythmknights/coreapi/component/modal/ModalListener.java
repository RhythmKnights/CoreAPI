// ──── CoreAPI ─────────────────────────────────────────────────────────▪
//     ▪ CoreAPI - Copyright © 2025 RhythmKnights [CoreAPI]
//     ▪ Original Work - Copyright © 2021 TriumphTeam [TriumphGUI]
//
//     ⏵ Licensed under the MIT License.
//         See LICENSE file in the project root for full license information.
// ────────────────────────────────────────────────────────────────────────────▪

package io.rhythmknights.coreapi.component.modal;

import io.rhythmknights.coreapi.component.module.ModalAction;
import io.rhythmknights.coreapi.component.utility.ItemNBT;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class ModalListener implements Listener {

    /**
     * Handles what happens when a player clicks on the modal
     *
     * @param event The InventoryClickEvent
     */
    @EventHandler
    public void onModalClick(final InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof BaseModal)) return;

        // Modal
        final BaseModal modal = (BaseModal) event.getInventory().getHolder();

        // Executes the outside click action
        final ModalAction<InventoryClickEvent> outsideClickAction = modal.getOutsideClickAction();
        if (outsideClickAction != null && event.getClickedInventory() == null) {
            outsideClickAction.execute(event);
            return;
        }

        if (event.getClickedInventory() == null) return;

        // Default click action and checks weather or not there is a default action and executes it
        final ModalAction<InventoryClickEvent> defaultTopClick = modal.getDefaultTopClickAction();
        if (defaultTopClick != null && event.getClickedInventory().getType() != InventoryType.PLAYER) {
            defaultTopClick.execute(event);
        }

        // Default click action and checks weather or not there is a default action and executes it
        final ModalAction<InventoryClickEvent> playerInventoryClick = modal.getPlayerInventoryAction();
        if (playerInventoryClick != null && event.getClickedInventory().getType() == InventoryType.PLAYER) {
            playerInventoryClick.execute(event);
        }

        // Default click action and checks weather or not there is a default action and executes it
        final ModalAction<InventoryClickEvent> defaultClick = modal.getDefaultClickAction();
        if (defaultClick != null) defaultClick.execute(event);

        // Slot action and checks weather or not there is a slot action and executes it
        final ModalAction<InventoryClickEvent> slotAction = modal.getSlotAction(event.getSlot());
        if (slotAction != null && event.getClickedInventory().getType() != InventoryType.PLAYER) {
            slotAction.execute(event);
        }

        ModalItem modalItem;

        // Checks whether it's a paginated modal or not
        if (modal instanceof PaginatedModal) {
            final PaginatedModal paginatedModal = (PaginatedModal) modal;

            // Gets the modal item from the added items or the page items
            modalItem = paginatedModal.getModalItem(event.getSlot());
            if (modalItem == null) modalItem = paginatedModal.getPageItem(event.getSlot());

        } else {
            // The clicked Modal Item
            modalItem = modal.getModalItem(event.getSlot());
        }

        if (!isModalItem(event.getCurrentItem(), modalItem)) return;

        // Executes the action of the item
        final ModalAction<InventoryClickEvent> itemAction = modalItem.getAction();
        if (itemAction != null) {
            itemAction.execute(event);

            // Add this for title updates on item clicks
            if (modal.shouldUpdateTitleOnItemClick()) {
                modal.updateTitleOnItemClick(modalItem, event.getSlot(), event);
            }
        } else if (modal.shouldUpdateTitleOnItemClick()) {
            // Even if no action is set, still update the title if needed
            modal.updateTitleOnItemClick(modalItem, event.getSlot(), event);
        }
    }

    /**
     * Handles what happens when a player clicks on the modal
     *
     * @param event The InventoryClickEvent
     */
    @EventHandler
    public void onModalDrag(final InventoryDragEvent event) {
        if (!(event.getInventory().getHolder() instanceof BaseModal)) return;

        // Modal
        final BaseModal modal = (BaseModal) event.getInventory().getHolder();

        // Default click action and checks weather or not there is a default action and executes it
        final ModalAction<InventoryDragEvent> dragAction = modal.getDragAction();
        if (dragAction != null) dragAction.execute(event);
    }

    /**
     * Handles what happens when the modal is closed
     *
     * @param event The InventoryCloseEvent
     */
    @EventHandler
    public void onModalClose(final InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof BaseModal)) return;

        // Modal
        final BaseModal modal = (BaseModal) event.getInventory().getHolder();

        // The Modal action for closing
        final ModalAction<InventoryCloseEvent> closeAction = modal.getCloseModalAction();

        // Checks if there is or not an action set and executes it
        if (closeAction != null && !modal.isUpdating() && modal.shouldRunCloseAction()) closeAction.execute(event);
    }

    /**
     * Handles what happens when the modal is opened
     *
     * @param event The InventoryOpenEvent
     */
    @EventHandler
    public void onModalOpen(final InventoryOpenEvent event) {
        if (!(event.getInventory().getHolder() instanceof BaseModal)) return;

        // Modal
        final BaseModal modal = (BaseModal) event.getInventory().getHolder();

        // The Modal action for opening
        final ModalAction<InventoryOpenEvent> openAction = modal.getOpenModalAction();

        // Checks if there is or not an action set and executes it
        if (openAction != null && !modal.isUpdating()) openAction.execute(event);
    }

    /**
     * Checks if the item is or not a Modal item
     *
     * @param currentItem The current item clicked
     * @param modalItem   The Modal item in the slot
     * @return Whether it is or not a Modal item
     */
    private boolean isModalItem(@Nullable final ItemStack currentItem, @Nullable final ModalItem modalItem) {
        if (currentItem == null || modalItem == null) return false;
        // Checks whether the Item is truly a Modal Item
        final String nbt = ItemNBT.getString(currentItem, "cf-modal");
        if (nbt == null) return false;
        return nbt.equals(modalItem.getUuid().toString());
    }
}
