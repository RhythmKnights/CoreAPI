// ──── CoreAPI ─────────────────────────────────────────────────────────▪
//     ▪ CoreAPI - Copyright © 2025 RhythmKnights [CoreAPI]
//     ▪ Original Work - Copyright © 2021 TriumphTeam [TriumphGUI]
//
//     ⏵ Licensed under the MIT License.
//         See LICENSE file in the project root for full license information.
// ────────────────────────────────────────────────────────────────────────────▪

package io.rhythmknights.coreapi.component.modal;

import io.rhythmknights.coreapi.CoreAPI;
import io.rhythmknights.coreapi.component.module.ModalContainer;
import io.rhythmknights.coreapi.component.module.ModalAction;
import io.rhythmknights.coreapi.component.module.ModalType;
import io.rhythmknights.coreapi.component.module.InteractionModifier;
import io.rhythmknights.coreapi.component.module.exception.ModalException;
import io.rhythmknights.coreapi.component.module.DynamicTitle;
import io.rhythmknights.coreapi.component.utility.ModalFiller;
import io.rhythmknights.coreapi.component.utility.VersionHelper;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Base class that every modal extends.
 * Contains all the basics for the modal to work.
 * Main and simplest implementation of this is {@link Modal}.
 */
@SuppressWarnings("unused")
public abstract class BaseModal implements InventoryHolder {

    // The plugin instance for registering the event and for the close delay.
    private static final Plugin plugin = CoreAPI.getPlugin();

    private static Method GET_SCHEDULER_METHOD = null;
    private static Method EXECUTE_METHOD = null;

    // Registering the listener class.
    static {
        try {
            GET_SCHEDULER_METHOD = Entity.class.getMethod("getScheduler");
            final Class<?> entityScheduler = Class.forName("io.papermc.paper.threadedregions.scheduler.EntityScheduler");
            EXECUTE_METHOD = entityScheduler.getMethod("execute", Plugin.class, Runnable.class, Runnable.class, long.class);
        } catch (NoSuchMethodException | ClassNotFoundException ignored) {
        }

        Bukkit.getPluginManager().registerEvents(new ModalListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new InteractionModifierListener(), plugin);
    }

    // Modal filler.
    private final ModalFiller filler = new ModalFiller(this);
    // Contains all items the modal will have.
    private final Map<Integer, ModalItem> modalItems;
    // Actions for specific slots.
    private final Map<Integer, ModalAction<InventoryClickEvent>> slotActions;
    // Interaction modifiers.
    private final Set<InteractionModifier> interactionModifiers;

    // Modal control
    private final ModalContainer modalContainer;

    // Main inventory.
    private Inventory inventory;

    // Action to execute when clicking on any item.
    private ModalAction<InventoryClickEvent> defaultClickAction;
    // Action to execute when clicking on the top part of the modal only.
    private ModalAction<InventoryClickEvent> defaultTopClickAction;
    // Action to execute when clicking on the player Inventory.
    private ModalAction<InventoryClickEvent> playerInventoryAction;
    // Action to execute when dragging the item on the modal.
    private ModalAction<InventoryDragEvent> dragAction;
    // Action to execute when modal closes.
    private ModalAction<InventoryCloseEvent> closeModalAction;
    // Action to execute when modal opens.
    private ModalAction<InventoryOpenEvent> openModalAction;
    // Action to execute when clicked outside the modal.
    private ModalAction<InventoryClickEvent> outsideClickAction;

    // Whether the modal is updating.
    private boolean updating;

    // Whether should run the actions from the close and open methods.
    private boolean runCloseAction = true;
    private boolean runOpenAction = true;
    
    // Whether should update title on item clicks
    private boolean updateTitleOnItemClick = false;

    public BaseModal(final @NotNull ModalContainer modalContainer, @NotNull final Set<InteractionModifier> interactionModifiers) {
        this.interactionModifiers = safeCopyOf(interactionModifiers);
        this.modalContainer = modalContainer;
        this.inventory = modalContainer.createInventory(this);
        this.slotActions = new LinkedHashMap<>(modalContainer.inventorySize());
        this.modalItems = new LinkedHashMap<>(modalContainer.inventorySize());
    }

    /**
     * Copy a set into an EnumSet, required because {@link EnumSet#copyOf(EnumSet)} throws an exception if the collection passed as argument is empty.
     *
     * @param set The set to be copied.
     * @return An EnumSet with the provided elements from the original set.
     */
    @NotNull
    private Set<InteractionModifier> safeCopyOf(@NotNull final Set<InteractionModifier> set) {
        if (set.isEmpty()) return EnumSet.noneOf(InteractionModifier.class);
        else return EnumSet.copyOf(set);
    }

    /**
     * Gets the modal title as a {@link Component}.
     *
     * @return The modal title {@link Component}.
     */
    @NotNull
    public Component title() {
        return modalContainer.title();
    }

    /**
     * Sets the {@link ModalItem} to a specific slot on the modal.
     *
     * @param slot      The modal slot.
     * @param modalItem The {@link ModalItem} to add to the slot.
     */
    public void setItem(final int slot, @NotNull final ModalItem modalItem) {
        validateSlot(slot);
        modalItems.put(slot, modalItem);
    }

    /**
     * Removes the given {@link ModalItem} from the modal.
     *
     * @param item The item to remove.
     */
    public void removeItem(@NotNull final ModalItem item) {
        modalItems.entrySet()
            .stream()
            .filter(it -> it.getValue().equals(item))
            .findFirst()
            .ifPresent(it -> {
                modalItems.remove(it.getKey());
                inventory.remove(it.getValue().getItemStack());
            });
    }

    /**
     * Removes the given {@link ItemStack} from the modal.
     *
     * @param item The item to remove.
     */
    public void removeItem(@NotNull final ItemStack item) {
        modalItems.entrySet()
            .stream()
            .filter(it -> it.getValue().getItemStack().equals(item))
            .findFirst()
            .ifPresent(it -> {
                modalItems.remove(it.getKey());
                inventory.remove(item);
            });
    }

    /**
     * Removes the {@link ModalItem} in the specific slot.
     *
     * @param slot The modal slot.
     */
    public void removeItem(final int slot) {
        validateSlot(slot);
        modalItems.remove(slot);
        inventory.setItem(slot, null);
    }

    /**
     * Alternative {@link #removeItem(int)} with cols and rows.
     *
     * @param row The row.
     * @param col The column.
     */
    public void removeItem(final int row, final int col) {
        removeItem(getSlotFromRowCol(row, col));
    }

    /**
     * Alternative {@link #setItem(int, ModalItem)} to set item that takes a {@link List} of slots instead.
     *
     * @param slots     The slots in which the item should go.
     * @param modalItem The {@link ModalItem} to add to the slots.
     */
    public void setItem(@NotNull final List<Integer> slots, @NotNull final ModalItem modalItem) {
        for (final int slot : slots) {
            setItem(slot, modalItem);
        }
    }

    /**
     * Alternative {@link #setItem(int, ModalItem)} to set item that uses <i>ROWS</i> and <i>COLUMNS</i> instead of slots.
     *
     * @param row       The modal row number.
     * @param col       The modal column number.
     * @param modalItem The {@link ModalItem} to add to the slot.
     */
    public void setItem(final int row, final int col, @NotNull final ModalItem modalItem) {
        setItem(getSlotFromRowCol(row, col), modalItem);
    }

    /**
     * Adds {@link ModalItem}s to the modal without specific slot.
     * It'll set the item to the next empty slot available.
     *
     * @param items Varargs for specifying the {@link ModalItem}s.
     */
    public void addItem(@NotNull final ModalItem... items) {
        this.addItem(false, items);
    }

    /**
     * Adds {@link ModalItem}s to the modal without specific slot.
     * It'll set the item to the next empty slot available.
     *
     * @param items        Varargs for specifying the {@link ModalItem}s.
     * @param expandIfFull If true, expands the modal if it is full
     *                     and there are more items to be added
     */
    public void addItem(final boolean expandIfFull, @NotNull final ModalItem... items) {
        final List<ModalItem> notAddedItems = new ArrayList<>();
        final int rows = modalContainer.rows();
        final ModalType modalType = modalContainer.modalType();

        for (final ModalItem modalItem : items) {
            for (int slot = 0; slot < rows * 9; slot++) {
                if (modalItems.get(slot) != null) {
                    if (slot == rows * 9 - 1) {
                        notAddedItems.add(modalItem);
                    }
                    continue;
                }

                modalItems.put(slot, modalItem);
                break;
            }
        }

        if (!expandIfFull || rows >= 6 || notAddedItems.isEmpty() || modalType != ModalType.CHEST) {
            return;
        }

        if (!(modalContainer instanceof ModalContainer.Chest)) return;
        ((ModalContainer.Chest) modalContainer).rows(modalContainer.rows() + 1);
        this.inventory = modalContainer.createInventory(this);
        this.update();
        this.addItem(true, notAddedItems.toArray(new ModalItem[0]));
    }

    /**
     * Adds a {@link ModalAction} for when clicking on a specific slot.
     * See {@link InventoryClickEvent}.
     *
     * @param slot       The slot that will trigger the {@link ModalAction}.
     * @param slotAction {@link ModalAction} to resolve when clicking on specific slots.
     */
    public void addSlotAction(final int slot, @Nullable final ModalAction<@NotNull InventoryClickEvent> slotAction) {
        validateSlot(slot);
        slotActions.put(slot, slotAction);
    }

    /**
     * Alternative method for {@link #addSlotAction(int, ModalAction)} to add a {@link ModalAction} to a specific slot using <i>ROWS</i> and <i>COLUMNS</i> instead of slots.
     * See {@link InventoryClickEvent}.
     *
     * @param row        The row of the slot.
     * @param col        The column of the slot.
     * @param slotAction {@link ModalAction} to resolve when clicking on the slot.
     */
    public void addSlotAction(final int row, final int col, @Nullable final ModalAction<@NotNull InventoryClickEvent> slotAction) {
        addSlotAction(getSlotFromRowCol(row, col), slotAction);
    }

    /**
     * Gets a specific {@link ModalItem} on the slot.
     *
     * @param slot The slot of the item.
     * @return The {@link ModalItem} on the introduced slot or {@code null} if doesn't exist.
     */
    @Nullable
    public ModalItem getModalItem(final int slot) {
        return modalItems.get(slot);
    }

    /**
     * Checks whether or not the modal is updating.
     *
     * @return Whether the modal is updating or not.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isUpdating() {
        return updating;
    }

    /**
     * Sets the updating status of the modal.
     *
     * @param updating Sets the modal to the updating status.
     */
    public void setUpdating(final boolean updating) {
        this.updating = updating;
    }

    /**
     * Opens the modal for a {@link HumanEntity}.
     *
     * @param player The {@link HumanEntity} to open the modal to.
     */
    public void open(@NotNull final HumanEntity player) {
        if (player.isSleeping()) return;

        inventory.clear();
        populateModal();
        player.openInventory(inventory);
    }

    /**
     * Closes the modal with a {@code 2 tick} delay (to prevent items from being taken from the {@link Inventory}).
     *
     * @param player The {@link HumanEntity} to close the modal to.
     */
    public void close(@NotNull final HumanEntity player) {
        close(player, true);
    }

    /**
     * Closes the modal with a {@code 2 tick} delay (to prevent items from being taken from the {@link Inventory}).
     *
     * @param player         The {@link HumanEntity} to close the modal to.
     * @param runCloseAction If should or not run the close action.
     */
    public void close(@NotNull final HumanEntity player, final boolean runCloseAction) {
        final Runnable task = () -> {
            this.runCloseAction = runCloseAction;
            player.closeInventory();
            this.runCloseAction = true;
        };

        if (VersionHelper.IS_FOLIA) {
            if (GET_SCHEDULER_METHOD == null || EXECUTE_METHOD == null) {
                throw new ModalException("Could not find Folia Scheduler methods.");
            }

            try {
                EXECUTE_METHOD.invoke(GET_SCHEDULER_METHOD.invoke(player), plugin, task, null, 2L);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new ModalException("Could not invoke Folia task.", e);
            }
            return;
        }

        Bukkit.getScheduler().runTaskLater(plugin, task, 2L);
    }

    /**
     * Updates the modal for all the {@link Inventory} views.
     */
    public void update() {
        inventory.clear();
        populateModal();
        // for (HumanEntity viewer : new ArrayList<>(inventory.getViewers())) ((Player) viewer).updateInventory();
    }

    /**
     * Updates the title of the modal.
     * <i>This method may cause LAG if used on a loop</i>.
     *
     * @param title The title to set.
     * @return The modal for easier use when declaring, works like a builder.
     */
    @NotNull
    @Contract("_ -> this")
    public BaseModal updateTitle(@NotNull final Component title) {
        updating = true;

        final List<HumanEntity> viewers = new ArrayList<>(inventory.getViewers());

        modalContainer.title(title); // Update the title.
        inventory = modalContainer.createInventory(this);

        for (final HumanEntity player : viewers) {
            open(player);
        }

        updating = false;
        return this;
    }

    /**
     * Updates the specified item in the modal at runtime, without creating a new {@link ModalItem}.
     *
     * @param slot      The slot of the item to update.
     * @param itemStack The {@link ItemStack} to replace in the original one in the {@link ModalItem}.
     */
    public void updateItem(final int slot, @NotNull final ItemStack itemStack) {
        final ModalItem modalItem = modalItems.get(slot);

        if (modalItem == null) {
            updateItem(slot, new ModalItem(itemStack));
            return;
        }

        modalItem.setItemStack(itemStack);
        updateItem(slot, modalItem);
    }

    /**
     * Alternative {@link #updateItem(int, ItemStack)} that takes <i>ROWS</i> and <i>COLUMNS</i> instead of slots.
     *
     * @param row       The row of the slot.
     * @param col       The columns of the slot.
     * @param itemStack The {@link ItemStack} to replace in the original one in the {@link ModalItem}.
     */
    public void updateItem(final int row, final int col, @NotNull final ItemStack itemStack) {
        updateItem(getSlotFromRowCol(row, col), itemStack);
    }

    /**
     * Alternative {@link #updateItem(int, ItemStack)} but creates a new {@link ModalItem}.
     *
     * @param slot The slot of the item to update.
     * @param item The {@link ModalItem} to replace in the original.
     */
    public void updateItem(final int slot, @NotNull final ModalItem item) {
        modalItems.put(slot, item);
        inventory.setItem(slot, item.getItemStack());
    }

    /**
     * Alternative {@link #updateItem(int, ModalItem)} that takes <i>ROWS</i> and <i>COLUMNS</i> instead of slots.
     *
     * @param row  The row of the slot.
     * @param col  The columns of the slot.
     * @param item The {@link ModalItem} to replace in the original.
     */
    public void updateItem(final int row, final int col, @NotNull final ModalItem item) {
        updateItem(getSlotFromRowCol(row, col), item);
    }

    /**
     * Disable item placement inside the modal.
     *
     * @return The BaseModal.
     * @author SecretX.
     * @since 3.0.0.
     */
    @NotNull
    @Contract(" -> this")
    public BaseModal disableItemPlace() {
        interactionModifiers.add(InteractionModifier.PREVENT_ITEM_PLACE);
        return this;
    }

    /**
     * Disable item retrieval inside the modal.
     *
     * @return The BaseModal.
     * @author SecretX.
     * @since 3.0.0.
     */
    @NotNull
    @Contract(" -> this")
    public BaseModal disableItemTake() {
        interactionModifiers.add(InteractionModifier.PREVENT_ITEM_TAKE);
        return this;
    }

    /**
     * Disable item swap inside the modal.
     *
     * @return The BaseModal.
     * @author SecretX.
     * @since 3.0.0.
     */
    @NotNull
    @Contract(" -> this")
    public BaseModal disableItemSwap() {
        interactionModifiers.add(InteractionModifier.PREVENT_ITEM_SWAP);
        return this;
    }

    /**
     * Disable item drop inside the modal
     *
     * @return The BaseModal
     * @since 3.0.3.
     */
    @NotNull
    @Contract(" -> this")
    public BaseModal disableItemDrop() {
        interactionModifiers.add(InteractionModifier.PREVENT_ITEM_DROP);
        return this;
    }

    /**
     * Disable other modal actions
     * This option pretty much disables creating a clone stack of the item
     *
     * @return The BaseModal
     * @since 3.0.4
     */
    @NotNull
    @Contract(" -> this")
    public BaseModal disableOtherActions() {
        interactionModifiers.add(InteractionModifier.PREVENT_OTHER_ACTIONS);
        return this;
    }

    /**
     * Disable all the modifications of the modal, making it immutable by player interaction.
     *
     * @return The BaseModal.
     * @author SecretX.
     * @since 3.0.0.
     */
    @NotNull
    @Contract(" -> this")
    public BaseModal disableAllInteractions() {
        interactionModifiers.addAll(InteractionModifier.VALUES);
        return this;
    }

    /**
     * Allows item placement inside the modal.
     *
     * @return The BaseModal.
     * @author SecretX.
     * @since 3.0.0.
     */
    @NotNull
    @Contract(" -> this")
    public BaseModal enableItemPlace() {
        interactionModifiers.remove(InteractionModifier.PREVENT_ITEM_PLACE);
        return this;
    }

    /**
     * Allow items to be taken from the modal.
     *
     * @return The BaseModal.
     * @author SecretX.
     * @since 3.0.0.
     */
    @NotNull
    @Contract(" -> this")
    public BaseModal enableItemTake() {
        interactionModifiers.remove(InteractionModifier.PREVENT_ITEM_TAKE);
        return this;
    }

    /**
     * Allows item swap inside the modal.
     *
     * @return The BaseModal.
     * @author SecretX.
     * @since 3.0.0.
     */
    @NotNull
    @Contract(" -> this")
    public BaseModal enableItemSwap() {
        interactionModifiers.remove(InteractionModifier.PREVENT_ITEM_SWAP);
        return this;
    }

    /**
     * Allows item drop inside the modal
     *
     * @return The BaseModal
     * @since 3.0.3
     */
    @NotNull
    @Contract(" -> this")
    public BaseModal enableItemDrop() {
        interactionModifiers.remove(InteractionModifier.PREVENT_ITEM_DROP);
        return this;
    }

    /**
     * Enable other modal actions
     * This option pretty much enables creating a clone stack of the item
     *
     * @return The BaseModal
     * @since 3.0.4
     */
    @NotNull
    @Contract(" -> this")
    public BaseModal enableOtherActions() {
        interactionModifiers.remove(InteractionModifier.PREVENT_OTHER_ACTIONS);
        return this;
    }

    /**
     * Enable all modifications of the modal, making it completely mutable by player interaction.
     *
     * @return The BaseModal.
     * @author SecretX.
     * @since 3.0.0.
     */
    @NotNull
    @Contract(" -> this")
    public BaseModal enableAllInteractions() {
        interactionModifiers.clear();
        return this;
    }

    public boolean allInteractionsDisabled() {
        return interactionModifiers.size() == InteractionModifier.VALUES.size();
    }

    /**
     * Check if item placement is allowed inside this modal.
     *
     * @return True if item placement is allowed for this modal.
     * @author SecretX.
     * @since 3.0.0.
     */
    public boolean canPlaceItems() {
        return !interactionModifiers.contains(InteractionModifier.PREVENT_ITEM_PLACE);
    }

    /**
     * Check if item retrieval is allowed inside this modal.
     *
     * @return True if item retrieval is allowed inside this modal.
     * @author SecretX.
     * @since 3.0.0.
     */
    public boolean canTakeItems() {
        return !interactionModifiers.contains(InteractionModifier.PREVENT_ITEM_TAKE);
    }

    /**
     * Check if item swap is allowed inside this modal.
     *
     * @return True if item swap is allowed for this modal.
     * @author SecretX.
     * @since 3.0.0.
     */
    public boolean canSwapItems() {
        return !interactionModifiers.contains(InteractionModifier.PREVENT_ITEM_SWAP);
    }

    /**
     * Check if item drop is allowed inside this modal
     *
     * @return True if item drop is allowed for this modal
     * @since 3.0.3
     */
    public boolean canDropItems() {
        return !interactionModifiers.contains(InteractionModifier.PREVENT_ITEM_DROP);
    }

    /**
     * Check if any other actions are allowed in this modal
     *
     * @return True if other actions are allowed
     * @since 3.0.4
     */
    public boolean allowsOtherActions() {
        return !interactionModifiers.contains(InteractionModifier.PREVENT_OTHER_ACTIONS);
    }

    /**
     * Gets the {@link ModalFiller} that it's used for filling up the modal in specific ways.
     *
     * @return The {@link ModalFiller}.
     */
    @NotNull
    public ModalFiller getFiller() {
        return filler;
    }

    /**
     * Gets an immutable {@link Map} with all the modal items.
     *
     * @return The {@link Map} with all the {@link #modalItems}.
     */
    @NotNull
    public Map<@NotNull Integer, @NotNull ModalItem> getModalItems() {
        return Collections.unmodifiableMap(modalItems);
    }

    /**
     * Gets the main {@link Inventory} of this modal.
     *
     * @return Gets the {@link Inventory} from the holder.
     */
    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Sets the new inventory of the modal.
     *
     * @param inventory The new inventory.
     */
    public void setInventory(@NotNull final Inventory inventory) {
        this.inventory = inventory;
    }

    /**
     * Gets the amount of {@link #modalContainer} rows.
     *
     * @return The {@link #modalContainer }'s rows of the modal.
     */
    public int getRows() {
        return modalContainer.rows();
    }

    /**
     * Gets the {@link ModalType} in use.
     *
     * @return The {@link ModalType}.
     */
    @NotNull
    public ModalType modalType() {
        return modalContainer.modalType();
    }

    /**
     * Gets the default click resolver.
     */
    @Nullable
    ModalAction<InventoryClickEvent> getDefaultClickAction() {
        return defaultClickAction;
    }

    /**
     * Sets the {@link ModalAction} of a default click on any item.
     * See {@link InventoryClickEvent}.
     *
     * @param defaultClickAction {@link ModalAction} to resolve when any item is clicked.
     */
    public void setDefaultClickAction(@Nullable final ModalAction<@NotNull InventoryClickEvent> defaultClickAction) {
        this.defaultClickAction = defaultClickAction;
    }

    /**
     * Gets the default top click resolver.
     */
    @Nullable
    ModalAction<InventoryClickEvent> getDefaultTopClickAction() {
        return defaultTopClickAction;
    }

    /**
     * Sets the {@link ModalAction} of a default click on any item on the top part of the modal.
     * Top inventory being for example chests etc, instead of the {@link Player} inventory.
     * See {@link InventoryClickEvent}.
     *
     * @param defaultTopClickAction {@link ModalAction} to resolve when clicking on the top inventory.
     */
    public void setDefaultTopClickAction(@Nullable final ModalAction<@NotNull InventoryClickEvent> defaultTopClickAction) {
        this.defaultTopClickAction = defaultTopClickAction;
    }

    /**
     * Gets the player inventory action.
     */
    @Nullable
    ModalAction<InventoryClickEvent> getPlayerInventoryAction() {
        return playerInventoryAction;
    }

    public void setPlayerInventoryAction(@Nullable final ModalAction<@NotNull InventoryClickEvent> playerInventoryAction) {
        this.playerInventoryAction = playerInventoryAction;
    }

    /**
     * Gets the default drag resolver.
     */
    @Nullable
    ModalAction<InventoryDragEvent> getDragAction() {
        return dragAction;
    }

    /**
     * Sets the {@link ModalAction} of a default drag action.
     * See {@link InventoryDragEvent}.
     *
     * @param dragAction {@link ModalAction} to resolve.
     */
    public void setDragAction(@Nullable final ModalAction<@NotNull InventoryDragEvent> dragAction) {
        this.dragAction = dragAction;
    }

    /**
     * Gets the close modal resolver.
     */
    @Nullable
    ModalAction<InventoryCloseEvent> getCloseModalAction() {
        return closeModalAction;
    }

    /**
     * Sets the {@link ModalAction} to run once the inventory is closed.
     * See {@link InventoryCloseEvent}.
     *
     * @param closeModalAction {@link ModalAction} to resolve when the inventory is closed.
     */
    public void setCloseModalAction(@Nullable final ModalAction<@NotNull InventoryCloseEvent> closeModalAction) {
        this.closeModalAction = closeModalAction;
    }

    /**
     * Gets the open modal resolver.
     */
    @Nullable
    ModalAction<InventoryOpenEvent> getOpenModalAction() {
        return openModalAction;
    }

    /**
     * Sets the {@link ModalAction} to run when the modal opens.
     * See {@link InventoryOpenEvent}.
     *
     * @param openModalAction {@link ModalAction} to resolve when opening the inventory.
     */
    public void setOpenModalAction(@Nullable final ModalAction<@NotNull InventoryOpenEvent> openModalAction) {
        this.openModalAction = openModalAction;
    }

    /**
     * Gets the resolver for the outside click.
     */
    @Nullable
    ModalAction<InventoryClickEvent> getOutsideClickAction() {
        return outsideClickAction;
    }

    /**
     * Sets the {@link ModalAction} to run when clicking on the outside of the inventory.
     * See {@link InventoryClickEvent}.
     *
     * @param outsideClickAction {@link ModalAction} to resolve when clicking outside of the inventory.
     */
    public void setOutsideClickAction(@Nullable final ModalAction<@NotNull InventoryClickEvent> outsideClickAction) {
        this.outsideClickAction = outsideClickAction;
    }

    /**
     * Gets the action for the specified slot.
     *
     * @param slot The slot clicked.
     */
    @Nullable
    ModalAction<InventoryClickEvent> getSlotAction(final int slot) {
        return slotActions.get(slot);
    }

    /**
     * Populates the modal with it's items.
     */
    void populateModal() {
        for (final Map.Entry<Integer, ModalItem> entry : modalItems.entrySet()) {
            inventory.setItem(entry.getKey(), entry.getValue().getItemStack());
        }
    }

    boolean shouldRunCloseAction() {
        return runCloseAction;
    }

    boolean shouldRunOpenAction() {
        return runOpenAction;
    }

    /**
     * Gets the slot from the row and column passed.
     *
     * @param row The row.
     * @param col The column.
     * @return The slot needed.
     */
    int getSlotFromRowCol(final int row, final int col) {
        return (col + (row - 1) * 9) - 1;
    }

    /**
     * Checks if the slot introduces is a valid slot.
     *
     * @param slot The slot to check.
     */
    private void validateSlot(final int slot) {
        final ModalType modalType = modalContainer.modalType();
        final int limit = modalType.getLimit();

        if (modalType == ModalType.CHEST) {
            if (slot < 0 || slot >= modalContainer.rows() * limit) throwInvalidSlot(slot);
            return;
        }

        if (slot < 0 || slot > limit) throwInvalidSlot(slot);
    }

    /**
     * Throws an exception if the slot is invalid.
     *
     * @param slot The specific slot to display in the error message.
     */
    private void throwInvalidSlot(final int slot) {
        if (modalContainer.modalType() == ModalType.CHEST) {
            throw new ModalException("Slot " + slot + " is not valid for the modal type - " + modalContainer.modalType().name() + " and rows - " + modalContainer.rows() + "!");
        }

        throw new ModalException("Slot " + slot + " is not valid for the modal type - " + modalContainer.modalType().name() + "!");
    }

    /**
     * Enables or disables title updates when items are clicked
     * 
     * @param enable Whether to update title on item clicks
     * @return The modal for easier use when declaring
     */
    @NotNull
    @Contract("_ -> this")
    public BaseModal setUpdateTitleOnItemClick(boolean enable) {
        this.updateTitleOnItemClick = enable;
        return this;
    }

    /**
     * Checks if the modal should update its title on item clicks
     * 
     * @return True if title should update on item clicks
     */
    public boolean shouldUpdateTitleOnItemClick() {
        return updateTitleOnItemClick;
    }

    /**
     * Updates the title based on item interaction
     * 
     * @param item The clicked modal item
     * @param slot The slot that was clicked
     * @param event The inventory click event
     */
    public void updateTitleOnItemClick(@Nullable ModalItem item, int slot, InventoryClickEvent event) {
        if (!updateTitleOnItemClick || !modalContainer.hasDynamicTitle()) {
            return;
        }

        DynamicTitle dynamicTitle = modalContainer.getDynamicTitle();
        if (dynamicTitle instanceof DynamicTitle.InteractionDynamicTitle) {
            DynamicTitle.InteractionState state = new DynamicTitle.InteractionState(
                ((DynamicTitle.InteractionDynamicTitle) dynamicTitle).getBaseTitle(),
                item,
                slot,
                event.getClick(),
                event.getAction()
            );
            Component newTitle = ((DynamicTitle.InteractionDynamicTitle) dynamicTitle).update(state);
            // No need to call full title update as the component is updated internally
        }
    }
    
    
    protected @NotNull ModalContainer modalContainer() {
        return modalContainer;
    }
}


