// ──── CoreAPI ─────────────────────────────────────────────────────────▪
//     ▪ CoreAPI - Copyright © 2025 RhythmKnights [CoreAPI]
//     ▪ Original Work - Copyright © 2021 TriumphTeam [TriumphGUI]
//
//     ⏵ Licensed under the MIT License.
//         See LICENSE file in the project root for full license information.
// ────────────────────────────────────────────────────────────────────────────▪

package io.rhythmknights.coreapi.component.utility;

import io.rhythmknights.coreapi.component.module.ModalType;
import io.rhythmknights.coreapi.component.module.exception.ModalException;
import io.rhythmknights.coreapi.component.modal.BaseModal;
import io.rhythmknights.coreapi.component.modal.ModalItem;
import io.rhythmknights.coreapi.component.modal.PaginatedModal;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TODO fix comments
 */
public final class ModalFiller {

    private final BaseModal modal;

    public ModalFiller(final BaseModal modal) {
        this.modal = modal;
    }

    /**
     * Fills top portion of the modal
     *
     * @param modalItem ModalItem
     */
    public void fillTop(@NotNull final ModalItem modalItem) {
        fillTop(Collections.singletonList(modalItem));
    }

    /**
     * Fills top portion of the modal with alternation
     *
     * @param modalItems List of ModalItems
     */
    public void fillTop(@NotNull final List<ModalItem> modalItems) {
        final List<ModalItem> items = repeatList(modalItems);
        for (int i = 0; i < 9; i++) {
            if (!modal.getModalItems().containsKey(i)) modal.setItem(i, items.get(i));
        }
    }

    /**
     * Fills bottom portion of the modal
     *
     * @param modalItem ModalItem
     */
    public void fillBottom(@NotNull final ModalItem modalItem) {
        fillBottom(Collections.singletonList(modalItem));
    }

    /**
     * Fills bottom portion of the modal with alternation
     *
     * @param modalItems ModalItem
     */
    public void fillBottom(@NotNull final List<ModalItem> modalItems) {
        final int rows = modal.getRows();
        final List<ModalItem> items = repeatList(modalItems);
        for (int i = 9; i > 0; i--) {
            if (modal.getModalItems().get((rows * 9) - i) == null) {
                modal.setItem((rows * 9) - i, items.get(i));
            }
        }
    }

    /**
     * Fills the outside section of the modal with a ModalItem
     *
     * @param modalItem ModalItem
     */
    public void fillBorder(@NotNull final ModalItem modalItem) {
        fillBorder(Collections.singletonList(modalItem));
    }

    /**
     * Fill empty slots with Multiple ModalItems, goes through list and starts again
     *
     * @param modalItems ModalItem
     */
    public void fillBorder(@NotNull final List<ModalItem> modalItems) {
        final int rows = modal.getRows();
        if (rows <= 2) return;

        final List<ModalItem> items = repeatList(modalItems);

        for (int i = 0; i < rows * 9; i++) {
            if ((i <= 8)
                    || (i >= (rows * 9) - 8) && (i <= (rows * 9) - 2)
                    || i % 9 == 0
                    || i % 9 == 8)
                modal.setItem(i, items.get(i));

        }
    }

    /**
     * Fills rectangle from points within the modal
     *
     * @param rowFrom Row point 1
     * @param colFrom Col point 1
     * @param rowTo   Row point 2
     * @param colTo   Col point 2
     * @param modalItem Item to fill with
     * @author Harolds
     */
    public void fillBetweenPoints(final int rowFrom, final int colFrom, final int rowTo, final int colTo, @NotNull final ModalItem modalItem) {
        fillBetweenPoints(rowFrom, colFrom, rowTo, colTo, Collections.singletonList(modalItem));
    }

    /**
     * Fills rectangle from points within the modal
     *
     * @param rowFrom  Row point 1
     * @param colFrom  Col point 1
     * @param rowTo    Row point 2
     * @param colTo    Col point 2
     * @param modalItems Item to fill with
     * @author Harolds
     */
    public void fillBetweenPoints(final int rowFrom, final int colFrom, final int rowTo, final int colTo, @NotNull final List<ModalItem> modalItems) {
        final int minRow = Math.min(rowFrom, rowTo);
        final int maxRow = Math.max(rowFrom, rowTo);
        final int minCol = Math.min(colFrom, colTo);
        final int maxCol = Math.max(colFrom, colTo);

        final int rows = modal.getRows();
        final List<ModalItem> items = repeatList(modalItems);

        for (int row = 1; row <= rows; row++) {
            for (int col = 1; col <= 9; col++) {
                final int slot = getSlotFromRowCol(row, col);
                if (!((row >= minRow && row <= maxRow) && (col >= minCol && col <= maxCol)))
                    continue;

                modal.setItem(slot, items.get(slot));
            }
        }
    }

    /**
     * Sets an ModalItem to fill up the entire inventory where there is no other item
     *
     * @param modalItem The item to use as fill
     */
    public void fill(@NotNull final ModalItem modalItem) {
        fill(Collections.singletonList(modalItem));
    }

    /**
     * Fill empty slots with Multiple ModalItems, goes through list and starts again
     *
     * @param modalItems ModalItem
     */
    public void fill(@NotNull final List<ModalItem> modalItems) {
        if (modal instanceof PaginatedModal) {
            throw new ModalException("Full filling a modal is not supported in a Paginated modal!");
        }

        final ModalType type = modal.modalType();

        final int fill;
        if (type == ModalType.CHEST) {
            fill = modal.getRows() * type.getLimit();
        } else {
            fill = type.getFillSize();
        }

        final List<ModalItem> items = repeatList(modalItems);
        for (int i = 0; i < fill; i++) {
            if (modal.getModalItems().get(i) == null) modal.setItem(i, items.get(i));
        }
    }

    /**
     * Fills specified side of the modal with a ModalItem
     *
     * @param modalItems ModalItem
     */
    public void fillSide(@NotNull final Side side, @NotNull final List<ModalItem> modalItems) {
        switch (side) {
            case LEFT:
                this.fillBetweenPoints(1, 1, modal.getRows(), 1, modalItems);
                break;
            case RIGHT:
                this.fillBetweenPoints(1, 9, modal.getRows(), 9, modalItems);
                break;
            case BOTH:
                this.fillSide(Side.LEFT, modalItems);
                this.fillSide(Side.RIGHT, modalItems);
        }
    }

    /**
     * Repeats a list of items. Allows for alternating items
     * Stores references to existing objects -> Does not create new objects
     *
     * @param modalItems List of items to repeat
     * @return New list
     */
    private List<ModalItem> repeatList(@NotNull final List<ModalItem> modalItems) {
        final List<ModalItem> repeated = new ArrayList<>();
        Collections.nCopies(modal.getRows() * 9, modalItems).forEach(repeated::addAll);
        return repeated;
    }

    /**
     * Gets the slot from the row and col passed
     *
     * @param row The row
     * @param col The column
     * @return The new slot
     */
    private int getSlotFromRowCol(final int row, final int col) {
        return (col + (row - 1) * 9) - 1;
    }

    public enum Side {
        LEFT,
        RIGHT,
        BOTH
    }
}
