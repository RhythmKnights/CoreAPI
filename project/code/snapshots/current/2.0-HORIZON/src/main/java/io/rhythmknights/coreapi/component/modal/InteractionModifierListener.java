// ──── CoreAPI ─────────────────────────────────────────────────────────▪
//     ▪ CoreAPI - Copyright © 2025 RhythmKnights [CoreAPI]
//     ▪ Original Work - Copyright © 2021 TriumphTeam [TriumphGUI]
//
//     ⏵ Licensed under the MIT License.
//         See LICENSE file in the project root for full license information.
// ────────────────────────────────────────────────────────────────────────────▪

package io.rhythmknights.coreapi.component.modal;

import com.google.common.base.Preconditions;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Listener that apply default modal {@link io.rhythmknights.coreapi.component.module.InteractionModifier InteractionModifier}s to all modals
 *
 * @author SecretX
 * @since 3.0.0
 */
public final class InteractionModifierListener implements Listener {

    /**
     * Handles any click on modals, applying all {@link io.rhythmknights.coreapi.component.module.InteractionModifier InteractionModifier} as required
     *
     * @param event The InventoryClickEvent
     * @author SecretX
     * @since 3.0.0
     */
    @EventHandler
    public void onModalClick(final InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof BaseModal)) return;

        // Modal
        final BaseModal modal = (BaseModal) event.getInventory().getHolder();

        if (modal.allInteractionsDisabled()) {
            event.setCancelled(true);
            event.setResult(Event.Result.DENY);
            return;
        }

        // if player is trying to do a disabled action, cancel it
        if ((!modal.canPlaceItems() && isPlaceItemEvent(event)) || (!modal.canTakeItems() && isTakeItemEvent(event)) || (!modal.canSwapItems() && isSwapItemEvent(event)) || (!modal.canDropItems() && isDropItemEvent(event)) || (!modal.allowsOtherActions() && isOtherEvent(event))) {
            event.setCancelled(true);
            event.setResult(Event.Result.DENY);
        }
    }

    /**
     * Handles any item drag on modals, applying all {@link io.rhythmknights.coreapi.component.module.InteractionModifier InteractionModifier} as required
     *
     * @param event The InventoryDragEvent
     * @author SecretX
     * @since 3.0.0
     */
    @EventHandler
    public void onModalDrag(final InventoryDragEvent event) {
        if (!(event.getInventory().getHolder() instanceof BaseModal)) return;

        // Modal
        final BaseModal modal = (BaseModal) event.getInventory().getHolder();

        if (modal.allInteractionsDisabled()) {
            event.setCancelled(true);
            event.setResult(Event.Result.DENY);
            return;
        }

        // if players are allowed to place items on the modal, or player is not dragging on modal, return
        if (modal.canPlaceItems() || !isDraggingOnModal(event)) return;

        // cancel the interaction
        event.setCancelled(true);
        event.setResult(Event.Result.DENY);
    }

    /**
     * Checks if what is happening on the {@link InventoryClickEvent} is take an item from the modal
     *
     * @param event The InventoryClickEvent
     * @return True if the {@link InventoryClickEvent} is for taking an item from the modal
     * @author SecretX
     * @since 3.0.0
     */
    private boolean isTakeItemEvent(final InventoryClickEvent event) {
        Preconditions.checkNotNull(event, "event cannot be null");

        final Inventory inventory = event.getInventory();
        final Inventory clickedInventory = event.getClickedInventory();
        final InventoryAction action = event.getAction();

        // magic logic, simplified version of https://paste.helpch.at/tizivomeco.cpp
        if (clickedInventory != null && clickedInventory.getType() == InventoryType.PLAYER || inventory.getType() == InventoryType.PLAYER) {
            return false;
        }

        return action == InventoryAction.MOVE_TO_OTHER_INVENTORY || isTakeAction(action);
    }

    private boolean isOtherEvent(final InventoryClickEvent event) {
        Preconditions.checkNotNull(event, "event cannot be null");

        final Inventory inventory = event.getInventory();
        final Inventory clickedInventory = event.getClickedInventory();
        final InventoryAction action = event.getAction();

        return isOtherAction(action)
                && (clickedInventory != null || inventory.getType() != InventoryType.PLAYER);
    }

    /**
     * Checks if any item is being dragged on the modal
     *
     * @param event The InventoryDragEvent
     * @return True if the {@link InventoryDragEvent} is for dragging an item inside the modal
     * @author SecretX
     * @since 3.0.0
     */
    private boolean isDraggingOnModal(final InventoryDragEvent event) {
        Preconditions.checkNotNull(event, "event cannot be null");
        final int topSlots = event.getView().getTopInventory().getSize();
        // is dragging on any top inventory slot
        return event.getRawSlots().stream().anyMatch(slot -> slot < topSlots);
    }

    private boolean isTakeAction(final InventoryAction action) {
        Preconditions.checkNotNull(action, "action cannot be null");
        return ITEM_TAKE_ACTIONS.contains(action);
    }

    private boolean isPlaceAction(final InventoryAction action) {
        Preconditions.checkNotNull(action, "action cannot be null");
        return ITEM_PLACE_ACTIONS.contains(action);
    }

    private boolean isSwapAction(final InventoryAction action) {
        Preconditions.checkNotNull(action, "action cannot be null");
        return ITEM_SWAP_ACTIONS.contains(action);
    }

    private boolean isDropAction(final InventoryAction action) {
        Preconditions.checkNotNull(action, "action cannot be null");
        return ITEM_DROP_ACTIONS.contains(action);
    }

    private boolean isOtherAction(final InventoryAction action) {
        Preconditions.checkNotNull(action, "action cannot be null");
        return action == InventoryAction.CLONE_STACK || action == InventoryAction.UNKNOWN;
    }

    /**
     * Checks if what is happening on the {@link InventoryClickEvent} is place an item on the modal
     *
     * @param event The InventoryClickEvent
     * @return True if the {@link InventoryClickEvent} is for placing an item from the modal
     * @author SecretX
     * @since 3.0.0
     */
    private boolean isPlaceItemEvent(final InventoryClickEvent event) {
        Preconditions.checkNotNull(event, "event cannot be null");

        final Inventory inventory = event.getInventory();
        final Inventory clickedInventory = event.getClickedInventory();
        final InventoryAction action = event.getAction();

        // shift click on item in player inventory
        if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY
                && clickedInventory != null && clickedInventory.getType() == InventoryType.PLAYER
                && inventory.getType() != clickedInventory.getType()) {
            return true;
        }

        // normal click on modal empty slot with item on cursor
        return isPlaceAction(action)
                && (clickedInventory == null || clickedInventory.getType() != InventoryType.PLAYER)
                && inventory.getType() != InventoryType.PLAYER;
    }

    /**
     * Checks if what is happening on the {@link InventoryClickEvent} is swap any item with an item from the modal
     *
     * @param event The InventoryClickEvent
     * @return True if the {@link InventoryClickEvent} is for swapping any item with an item from the modal
     * @author SecretX
     * @since 3.0.0
     */
    private boolean isSwapItemEvent(final InventoryClickEvent event) {
        Preconditions.checkNotNull(event, "event cannot be null");

        final Inventory inventory = event.getInventory();
        final Inventory clickedInventory = event.getClickedInventory();
        final InventoryAction action = event.getAction();

        return isSwapAction(action)
                && (clickedInventory == null || clickedInventory.getType() != InventoryType.PLAYER)
                && inventory.getType() != InventoryType.PLAYER;
    }

    /**
     * Checks if what is happening on the {@link InventoryClickEvent} is drop an item
     * 
     * @param event The InventoryClickEvent
     * @return True if the event is for dropping an item
     */
    private boolean isDropItemEvent(final InventoryClickEvent event) {
        Preconditions.checkNotNull(event, "event cannot be null");

        final Inventory inventory = event.getInventory();
        final Inventory clickedInventory = event.getClickedInventory();
        final InventoryAction action = event.getAction();

        return isDropAction(action)
                && (clickedInventory != null || inventory.getType() != InventoryType.PLAYER);
    }

    /**
     * Holds all the actions that should be considered "take" actions
     */
    private static final Set<InventoryAction> ITEM_TAKE_ACTIONS = Collections.unmodifiableSet(EnumSet.of(InventoryAction.PICKUP_ONE, InventoryAction.PICKUP_SOME, InventoryAction.PICKUP_HALF, InventoryAction.PICKUP_ALL, InventoryAction.COLLECT_TO_CURSOR, InventoryAction.HOTBAR_SWAP, InventoryAction.MOVE_TO_OTHER_INVENTORY));

    /**
     * Holds all the actions that should be considered "place" actions
     */
    private static final Set<InventoryAction> ITEM_PLACE_ACTIONS = Collections.unmodifiableSet(EnumSet.of(InventoryAction.PLACE_ONE, InventoryAction.PLACE_SOME, InventoryAction.PLACE_ALL));

    /**
     * Holds all actions relating to swapping items
     */
    private static final Set<InventoryAction> ITEM_SWAP_ACTIONS = Collections.unmodifiableSet(EnumSet.of(InventoryAction.HOTBAR_SWAP, InventoryAction.SWAP_WITH_CURSOR, InventoryAction.HOTBAR_MOVE_AND_READD));

    /**
     * Holds all actions relating to dropping items
     */
    private static final Set<InventoryAction> ITEM_DROP_ACTIONS = Collections.unmodifiableSet(EnumSet.of(InventoryAction.DROP_ONE_SLOT, InventoryAction.DROP_ALL_SLOT, InventoryAction.DROP_ONE_CURSOR, InventoryAction.DROP_ALL_CURSOR));
}
