// ──── CoreAPI ─────────────────────────────────────────────────────────▪
//     ▪ CoreAPI - Copyright © 2025 RhythmKnights [CoreAPI]
//     ▪ Original Work - Copyright © 2021 TriumphTeam [TriumphGUI]
//
//     ⏵ Licensed under the MIT License.
//         See LICENSE file in the project root for full license information.
// ────────────────────────────────────────────────────────────────────────────▪

package io.rhythmknights.coreapi.component.modal;

import io.rhythmknights.coreapi.component.module.DynamicTitle;
import io.rhythmknights.coreapi.component.module.ModalContainer;
import io.rhythmknights.coreapi.component.module.InteractionModifier;
import io.rhythmknights.coreapi.component.module.PaginationRegion;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Modal that allows you to have multiple pages
 */
@SuppressWarnings("unused")
public class PaginatedModal extends BaseModal {

    // List with all the page items
    private final List<ModalItem> pageItems = new ArrayList<>();
    // Saves the current page items and it's slot
    private final Map<Integer, ModalItem> currentPage;
    // Optional pagination region
    private PaginationRegion paginationRegion;

    private int pageSize;
    private int pageNum = 1;

    public PaginatedModal(final @NotNull ModalContainer modalContainer, final int pageSize, final @NotNull Set<InteractionModifier> interactionModifiers) {
        super(modalContainer, interactionModifiers);
        this.pageSize = pageSize;
        this.currentPage = new LinkedHashMap<>(modalContainer.inventorySize());
        this.paginationRegion = null;
    }
    
    /**
     * Constructor with pagination region
     * 
     * @param modalContainer Modal container
     * @param paginationRegion Region for pagination items
     * @param interactionModifiers Interaction modifiers
     */
    public PaginatedModal(
        final @NotNull ModalContainer modalContainer, 
        final @NotNull PaginationRegion paginationRegion,
        final @NotNull Set<InteractionModifier> interactionModifiers
    ) {
        super(modalContainer, interactionModifiers);
        this.paginationRegion = paginationRegion;
        this.pageSize = paginationRegion.size();
        this.currentPage = new LinkedHashMap<>(modalContainer.inventorySize());
    }

    /**
     * Sets the page size
     *
     * @param pageSize The new page size
     * @return The modal for easier use when declaring, works like a builder
     */
    public BaseModal setPageSize(final int pageSize) {
        this.pageSize = pageSize;
        return this;
    }
    
    /**
     * Sets the pagination region to use specific slots
     * 
     * @param paginationRegion Region defining which slots to use for pagination
     * @return The modal
     */
    public PaginatedModal setPaginationRegion(@NotNull PaginationRegion paginationRegion) {
        this.paginationRegion = paginationRegion;
        this.pageSize = paginationRegion.size();
        return this;
    }
    
    /**
     * Gets the pagination region if one is set
     * 
     * @return Optional containing the pagination region, or empty if none set
     */
    public Optional<PaginationRegion> getPaginationRegion() {
        return Optional.ofNullable(paginationRegion);
    }
    
    /**
     * Sets a dynamic title that updates with pagination
     * 
     * @param baseTitle Base title text
     * @return The modal
     */
    public PaginatedModal setDynamicTitle(@NotNull String baseTitle) {
        if (modalContainer() instanceof ModalContainer.Chest) {
            DynamicTitle.PaginatedDynamicTitle dynamicTitle = 
                new DynamicTitle.PaginatedDynamicTitle(baseTitle, pageNum, getPagesNum());
            ((ModalContainer.Chest) modalContainer()).setDynamicTitle(dynamicTitle);
        } else if (modalContainer() instanceof ModalContainer.Typed) {
            DynamicTitle.PaginatedDynamicTitle dynamicTitle = 
                new DynamicTitle.PaginatedDynamicTitle(baseTitle, pageNum, getPagesNum());
            ((ModalContainer.Typed) modalContainer()).setDynamicTitle(dynamicTitle);
        }
        return this;
    }
    
    /**
     * Update dynamic title with current pagination info
     */
    private void updateDynamicTitle() {
        ModalContainer container = modalContainer();
        if (container.hasDynamicTitle() && container.getDynamicTitle() instanceof DynamicTitle.PaginatedDynamicTitle) {
            DynamicTitle.PaginatedDynamicTitle dynamicTitle = 
                (DynamicTitle.PaginatedDynamicTitle) container.getDynamicTitle();
            
            dynamicTitle.update(pageNum, getPagesNum());
        }
    }

    /**
     * Adds an {@link ModalItem} to the next available slot in the page area
     *
     * @param item The {@link ModalItem} to add to the page
     */
    public void addItem(@NotNull final ModalItem item) {
        pageItems.add(item);
    }

    /**
     * Overridden {@link BaseModal#addItem(ModalItem...)} to add the items to the page instead
     *
     * @param items Varargs for specifying the {@link ModalItem}s
     */
    @Override
    public void addItem(@NotNull final ModalItem... items) {
        pageItems.addAll(Arrays.asList(items));
    }

    /**
     * Overridden {@link BaseModal#update()} to use the paginated open
     */
    @Override
    public void update() {
        getInventory().clear();
        populateModal();

        updatePage();
    }

    /**
     * Updates the page {@link ModalItem} on the slot in the page
     * Can get the slot from {@link InventoryClickEvent#getSlot()}
     *
     * @param slot      The slot of the item to update
     * @param itemStack The new {@link ItemStack}
     */
    public void updatePageItem(final int slot, @NotNull final ItemStack itemStack) {
        if (!currentPage.containsKey(slot)) return;
        final ModalItem modalItem = currentPage.get(slot);
        modalItem.setItemStack(itemStack);
        getInventory().setItem(slot, modalItem.getItemStack());
    }

    /**
     * Alternative {@link #updatePageItem(int, ItemStack)} that uses <i>ROWS</i> and <i>COLUMNS</i> instead
     *
     * @param row       The row of the slot
     * @param col       The columns of the slot
     * @param itemStack The new {@link ItemStack}
     */
    public void updatePageItem(final int row, final int col, @NotNull final ItemStack itemStack) {
        updateItem(getSlotFromRowCol(row, col), itemStack);
    }

    /**
     * Alternative {@link #updatePageItem(int, ItemStack)} that uses {@link ModalItem} instead
     *
     * @param slot The slot of the item to update
     * @param item The new ItemStack
     */
    public void updatePageItem(final int slot, @NotNull final ModalItem item) {
        if (!currentPage.containsKey(slot)) return;
        // Gets the old item and its index on the main items list
        final ModalItem oldItem = currentPage.get(slot);
        final int index = pageItems.indexOf(currentPage.get(slot));

        // Updates both lists and inventory
        currentPage.put(slot, item);
        pageItems.set(index, item);
        getInventory().setItem(slot, item.getItemStack());
    }

    /**
     * Alternative {@link #updatePageItem(int, ModalItem)} that uses <i>ROWS</i> and <i>COLUMNS</i> instead
     *
     * @param row  The row of the slot
     * @param col  The columns of the slot
     * @param item The new {@link ModalItem}
     */
    public void updatePageItem(final int row, final int col, @NotNull final ModalItem item) {
        updateItem(getSlotFromRowCol(row, col), item);
    }

    /**
     * Removes a given {@link ModalItem} from the page.
     *
     * @param item The {@link ModalItem} to remove.
     */
    public void removePageItem(@NotNull final ModalItem item) {
        pageItems.remove(item);
        updatePage();
    }

    /**
     * Removes a given {@link ItemStack} from the page.
     *
     * @param item The {@link ItemStack} to remove.
     */
    public void removePageItem(@NotNull final ItemStack item) {
        final Optional<ModalItem> modalItem = pageItems.stream().filter(it -> it.getItemStack().equals(item)).findFirst();
        modalItem.ifPresent(this::removePageItem);
    }

    /**
     * Overrides {@link BaseModal#open(HumanEntity)} to use the paginated populator instead
     *
     * @param player The {@link HumanEntity} to open the modal to
     */
    @Override
    public void open(@NotNull final HumanEntity player) {
        open(player, 1);
    }

    /**
     * Specific open method for the Paginated modal
     * Uses {@link #populatePage()}
     *
     * @param player   The {@link HumanEntity} to open it to
     * @param openPage The specific page to open at
     */
    public void open(@NotNull final HumanEntity player, final int openPage) {
        if (player.isSleeping()) return;
        if (openPage <= getPagesNum() || openPage > 0) pageNum = openPage;

        getInventory().clear();
        currentPage.clear();

        populateModal();

        if (pageSize == 0) pageSize = calculatePageSize();

        populatePage();
        updateDynamicTitle();

        player.openInventory(getInventory());
    }

    /**
     * Overrides {@link BaseModal#updateTitle(Component)} to use the paginated populator instead
     * Updates the title of the modal
     * <i>This method may cause LAG if used on a loop</i>
     *
     * @param title The title to set
     * @return The modal for easier use when declaring, works like a builder
     */
    @Override
    public @NotNull BaseModal updateTitle(@NotNull final Component title) {
        setUpdating(true);

        final List<HumanEntity> viewers = new ArrayList<>(getInventory().getViewers());
        final ModalContainer modalContainer = modalContainer();

        modalContainer.title(title);
        setInventory(modalContainer.createInventory(this));

        for (final HumanEntity player : viewers) {
            open(player, getPageNum());
        }

        setUpdating(false);
        return this;
    }

    /**
     * Gets an immutable {@link Map} with all the current pages items
     *
     * @return The {@link Map} with all the {@link #currentPage}
     */
    @NotNull
    public Map<@NotNull Integer, @NotNull ModalItem> getCurrentPageItems() {
        return Collections.unmodifiableMap(currentPage);
    }

    /**
     * Gets an immutable {@link List} with all the page items added to the modal
     *
     * @return The  {@link List} with all the {@link #pageItems}
     */
    @NotNull
    public List<@NotNull ModalItem> getPageItems() {
        return Collections.unmodifiableList(pageItems);
    }


    /**
     * Gets the current page number
     *
     * @return The current page number
     */
    public int getCurrentPageNum() {
        return pageNum;
    }

    /**
     * Gets the next page number
     *
     * @return The next page number or {@link #pageNum} if no next is present
     */
    public int getNextPageNum() {
        if (pageNum + 1 > getPagesNum()) return pageNum;
        return pageNum + 1;
    }

    /**
     * Gets the previous page number
     *
     * @return The previous page number or {@link #pageNum} if no previous is present
     */
    public int getPrevPageNum() {
        if (pageNum - 1 == 0) return pageNum;
        return pageNum - 1;
    }

    /**
     * Goes to the next page
     *
     * @return False if there is no next page.
     */
    public boolean next() {
        if (pageNum + 1 > getPagesNum()) return false;

        pageNum++;
        updatePage();
        updateDynamicTitle();
        return true;
    }

    /**
     * Goes to the previous page if possible
     *
     * @return False if there is no previous page.
     */
    public boolean previous() {
        if (pageNum - 1 == 0) return false;

        pageNum--;
        updatePage();
        updateDynamicTitle();
        return true;
    }

    /**
     * Gets the page item for the modal listener
     *
     * @param slot The slot to get
     * @return The ModalItem on that slot
     */
    ModalItem getPageItem(final int slot) {
        return currentPage.get(slot);
    }

    /**
     * Gets the items in the page
     *
     * @param givenPage The page to get
     * @return A list with all the page items
     */
    private List<ModalItem> getPageNum(final int givenPage) {
        final int page = givenPage - 1;

        final List<ModalItem> modalPage = new ArrayList<>();

        int max = ((page * pageSize) + pageSize);
        if (max > pageItems.size()) max = pageItems.size();

        for (int i = page * pageSize; i < max; i++) {
            modalPage.add(pageItems.get(i));
        }

        return modalPage;
    }

    /**
     * Gets the number of pages the modal has
     *
     * @return The pages number
     */
    public int getPagesNum() {
        if (pageSize == 0) pageSize = calculatePageSize();
        return (int) Math.ceil((double) pageItems.size() / pageSize);
    }

    /**
     * Populates the inventory with the page items
     */
    private void populatePage() {
        final List<ModalItem> pageContent = getPageNum(pageNum);
        
        // If we have a pagination region, only use those slots
        if (paginationRegion != null) {
            List<Integer> regionSlots = paginationRegion.getSlots();
            int itemIndex = 0;
            
            for (Integer slot : regionSlots) {
                if (itemIndex >= pageContent.size()) break;
                
                // Skip occupied slots
                if (getModalItem(slot) != null || getInventory().getItem(slot) != null) {
                    continue;
                }
                
                ModalItem modalItem = pageContent.get(itemIndex++);
                currentPage.put(slot, modalItem);
                getInventory().setItem(slot, modalItem.getItemStack());
            }
        } else {
            // Traditional pagination - fill from slot 0 upwards
            int slot = 0;
            final int inventorySize = getInventory().getSize();
            final Iterator<ModalItem> iterator = pageContent.iterator();
            
            while (iterator.hasNext()) {
                if (slot >= inventorySize) {
                    break; // Exit the loop if slot exceeds inventory size
                }

                if (getModalItem(slot) != null || getInventory().getItem(slot) != null) {
                    slot++;
                    continue;
                }

                final ModalItem modalItem = iterator.next();

                currentPage.put(slot, modalItem);
                getInventory().setItem(slot, modalItem.getItemStack());
                slot++;
            }
        }
    }

    /**
     * Gets the current page items to be used on other modal types
     *
     * @return The {@link Map} with all the {@link #currentPage}
     */
    Map<Integer, ModalItem> getMutableCurrentPageItems() {
        return currentPage;
    }

    /**
     * Clears the page content
     */
    void clearPage() {
        for (Map.Entry<Integer, ModalItem> entry : currentPage.entrySet()) {
            getInventory().setItem(entry.getKey(), null);
        }
        
        currentPage.clear();
    }

    /**
     * Clears all previously added page items
     */
    public void clearPageItems(final boolean update) {
        pageItems.clear();
        if (update) update();
    }

    public void clearPageItems() {
        clearPageItems(false);
    }


    /**
     * Gets the page size
     *
     * @return The page size
     */
    int getPageSize() {
        return pageSize;
    }

    /**
     * Gets the page number
     *
     * @return The current page number
     */
    int getPageNum() {
        return pageNum;
    }

    /**
     * Sets the page number
     *
     * @param pageNum Sets the current page to be the specified number
     */
    public void setPageNum(final int pageNum) {
        this.pageNum = pageNum;
        updateDynamicTitle();
    }

    /**
     * Updates the page content
     */
    void updatePage() {
        clearPage();
        populatePage();
    }

    /**
     * Calculates the size of the give page
     *
     * @return The page size
     */
    int calculatePageSize() {
        // If we have a pagination region, use its size
        if (paginationRegion != null) {
            return paginationRegion.size();
        }
        
        // Otherwise calculate based on available slots
        int counter = 0;

        for (int slot = 0; slot < getRows() * 9; slot++) {
            if (getModalItem(slot) == null) counter++;
        }

        if (counter == 0) return 1;
        return counter;
    }
}
