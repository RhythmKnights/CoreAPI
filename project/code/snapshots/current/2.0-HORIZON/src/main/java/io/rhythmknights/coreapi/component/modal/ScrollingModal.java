// ──── CoreAPI ─────────────────────────────────────────────────────────▪
//     ▪ CoreAPI - Copyright © 2025 RhythmKnights [CoreAPI]
//     ▪ Original Work - Copyright © 2021 TriumphTeam [TriumphGUI]
//
//     ⏵ Licensed under the MIT License.
//         See LICENSE file in the project root for full license information.
// ────────────────────────────────────────────────────────────────────────────▪

package io.rhythmknights.coreapi.component.modal;

import io.rhythmknights.coreapi.component.module.ModalContainer;
import io.rhythmknights.coreapi.component.module.DynamicTitle;
import io.rhythmknights.coreapi.component.module.InteractionModifier;
import io.rhythmknights.coreapi.component.module.PaginationRegion;
import io.rhythmknights.coreapi.component.module.ScrollType;

import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Modal that allows you to scroll through items
 */
@SuppressWarnings("unused")
public class ScrollingModal extends PaginatedModal {

    private final ScrollType scrollType;
    private int scrollSize = 0;

    public ScrollingModal(final @NotNull ModalContainer modalContainer, final int pageSize, @NotNull final ScrollType scrollType, @NotNull final Set<InteractionModifier> interactionModifiers) {
        super(modalContainer, pageSize, interactionModifiers);
        this.scrollType = scrollType;
    }
    
    /**
     * Constructor with pagination region
     * 
     * @param modalContainer Modal container
     * @param paginationRegion Region for pagination items
     * @param scrollType Scrolling direction
     * @param interactionModifiers Interaction modifiers
     */
    public ScrollingModal(
        final @NotNull ModalContainer modalContainer, 
        final @NotNull PaginationRegion paginationRegion,
        @NotNull final ScrollType scrollType,
        @NotNull final Set<InteractionModifier> interactionModifiers
    ) {
        super(modalContainer, paginationRegion, interactionModifiers);
        this.scrollType = scrollType;
    }
    
    /**
     * Sets a dynamic title that updates with scrolling position
     * 
     * @param baseTitle Base title text
     * @return The modal
     */
    @Override
    public ScrollingModal setDynamicTitle(@NotNull String baseTitle) {
        super.setDynamicTitle(baseTitle);
        return this;
    }

    /**
     * Overrides {@link PaginatedModal#next()} to make it work with the specific scrolls
     */
    @Override
    public boolean next() {
        if (getPageNum() * scrollSize + getPageSize() >= getPageItems().size() + scrollSize) return false;

        setPageNum(getPageNum() + 1);
        updatePage();
        return true;
    }

    /**
     * Overrides {@link PaginatedModal#previous()} to make it work with the specific scrolls
     */
    @Override
    public boolean previous() {
        if (getPageNum() - 1 == 0) return false;

        setPageNum(getPageNum() - 1);
        updatePage();
        return true;
    }

    /**
     * Overrides {@link PaginatedModal#open(HumanEntity)} to make it work with the specific scrolls
     *
     * @param player The {@link HumanEntity} to open the modal to
     */
    @Override
    public void open(@NotNull final HumanEntity player) {
        open(player, 1);
    }

    /**
     * Overrides {@link PaginatedModal#open(HumanEntity, int)} to make it work with the specific scrolls
     *
     * @param player   The {@link HumanEntity} to open the modal to
     * @param openPage The page to open on
     */
    @Override
    public void open(@NotNull final HumanEntity player, final int openPage) {
        if (player.isSleeping()) return;
        getInventory().clear();
        getMutableCurrentPageItems().clear();

        populateModal();

        if (getPageSize() == 0) setPageSize(calculatePageSize());
        if (scrollSize == 0) scrollSize = calculateScrollSize();
        if (openPage > 0 && (openPage * scrollSize + getPageSize() <= getPageItems().size() + scrollSize)) {
            setPageNum(openPage);
        }

        populatePage();
        
        // Update dynamic title if used
        ModalContainer container = modalContainer();
        if (container.hasDynamicTitle() && container.getDynamicTitle() instanceof DynamicTitle.PaginatedDynamicTitle) {
            DynamicTitle.PaginatedDynamicTitle dynamicTitle = 
                (DynamicTitle.PaginatedDynamicTitle) container.getDynamicTitle();
            
            dynamicTitle.update(getPageNum(), getPagesNum());
        }

        player.openInventory(getInventory());
    }

    /**
     * Overrides {@link PaginatedModal#updatePage()} to make it work with the specific scrolls
     */
    @Override
    void updatePage() {
        clearPage();
        populatePage();
    }

    /**
     * Fills the page with the items
     */
    private void populatePage() {
        // Adds the paginated items to the page
        final List<ModalItem> pageItems = getPage(getPageNum());
        
        // If we have a pagination region, use it
        if (getPaginationRegion().isPresent()) {
            List<Integer> regionSlots = getPaginationRegion().get().getSlots();
            int itemIndex = 0;
            
            for (Integer slot : regionSlots) {
                if (itemIndex >= pageItems.size()) break;
                
                // Skip occupied slots
                if (getModalItem(slot) != null || getInventory().getItem(slot) != null) {
                    continue;
                }
                
                ModalItem modalItem = pageItems.get(itemIndex++);
                getMutableCurrentPageItems().put(slot, modalItem);
                getInventory().setItem(slot, modalItem.getItemStack());
            }
        } else {
            // Traditional approach using scrolling direction
            for (final ModalItem modalItem : pageItems) {
                if (scrollType == ScrollType.HORIZONTAL) {
                    putItemHorizontally(modalItem);
                    continue;
                }

                putItemVertically(modalItem);
            }
        }
    }

    /**
     * Calculates the size of each scroll
     *
     * @return The size of he scroll
     */
    private int calculateScrollSize() {
        // If we have a pagination region, use one row/column of it
        if (getPaginationRegion().isPresent()) {
            return scrollType == ScrollType.VERTICAL ? 9 : getRows(); // One column or one row
        }
        
        int counter = 0;

        if (scrollType == ScrollType.VERTICAL) {
            boolean foundCol = false;

            for (int row = 1; row <= getRows(); row++) {
                for (int col = 1; col <= 9; col++) {
                    final int slot = getSlotFromRowCol(row, col);
                    if (getInventory().getItem(slot) == null) {
                        if (!foundCol) foundCol = true;
                        counter++;
                    }
                }

                if (foundCol) return counter;
            }

            return counter;
        }

        boolean foundRow = false;

        for (int col = 1; col <= 9; col++) {
            for (int row = 1; row <= getRows(); row++) {
                final int slot = getSlotFromRowCol(row, col);
                if (getInventory().getItem(slot) == null) {
                    if (!foundRow) foundRow = true;
                    counter++;
                }
            }

            if (foundRow) return counter;
        }

        return counter;
    }

    /**
     * Puts the item in the modal for horizontal scrolling
     *
     * @param modalItem The modal item
     */
    private void putItemVertically(final ModalItem modalItem) {
        for (int slot = 0; slot < getRows() * 9; slot++) {
            if (getModalItem(slot) != null || getInventory().getItem(slot) != null) continue;
            getMutableCurrentPageItems().put(slot, modalItem);
            getInventory().setItem(slot, modalItem.getItemStack());
            break;
        }
    }

    /**
     * Puts item into the modal for vertical scrolling
     *
     * @param modalItem The modal item
     */
    private void putItemHorizontally(final ModalItem modalItem) {
        for (int col = 1; col < 10; col++) {
            for (int row = 1; row <= getRows(); row++) {
                final int slot = getSlotFromRowCol(row, col);
                if (getModalItem(slot) != null || getInventory().getItem(slot) != null) continue;
                getMutableCurrentPageItems().put(slot, modalItem);
                getInventory().setItem(slot, modalItem.getItemStack());
                return;
            }
        }
    }

    /**
     * Gets the items from the current page
     *
     * @param givenPage The page number
     * @return A list with all the items
     */
    private List<ModalItem> getPage(final int givenPage) {
        final int page = givenPage - 1;
        final int pageItemsSize = getPageItems().size();

        final List<ModalItem> modalPage = new ArrayList<>();

        int max = page * scrollSize + getPageSize();
        if (max > pageItemsSize) max = pageItemsSize;

        for (int i = page * scrollSize; i < max; i++) {
            modalPage.add(getPageItems().get(i));
        }

        return modalPage;
    }
}