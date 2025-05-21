// ──── CoreAPI ─────────────────────────────────────────────────────────▪
//     ▪ CoreAPI - Copyright © 2025 RhythmKnights [CoreAPI]
//     ▪ Original Work - Copyright © 2021 TriumphTeam [TriumphGUI]
//
//     ⏵ Licensed under the MIT License.
//         See LICENSE file in the project root for full license information.
// ────────────────────────────────────────────────────────────────────────────▪

package io.rhythmknights.coreapi.component.module;

import io.rhythmknights.coreapi.component.modal.ModalItem;

import net.kyori.adventure.text.Component;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Interface for handling dynamic titles in modals
 * Allows for titles to be updated based on the current state of the modal
 */
public interface DynamicTitle {
    
    /**
     * Get the current dynamic title based on modal state
     * 
     * @return Current component title
     */
    @NotNull Component getDynamicTitle();
    
    /**
     * Update the dynamic title with a new state object
     * 
     * @param state Object containing state information for the update
     * @return Updated component title
     */
    @NotNull Component update(@NotNull Object state);
    
    /**
     * Simple implementation for creating dynamic titles with a function
     */
    class DynamicTitleImpl implements DynamicTitle {
        private final Function<Object, Component> titleFunction;
        private Object state;
        
        /**
         * Creates a dynamic title with a function that takes a state object
         * 
         * @param titleFunction Function to generate title based on state
         * @param state The state object to pass to the function
         */
        public DynamicTitleImpl(@NotNull Function<Object, Component> titleFunction, @NotNull Object state) {
            this.titleFunction = titleFunction;
            this.state = state;
        }
        
        @Override
        public @NotNull Component getDynamicTitle() {
            return titleFunction.apply(state);
        }
        
        @Override
        public @NotNull Component update(@NotNull Object state) {
            this.state = state;
            return getDynamicTitle();
        }
    }
    
    /**
     * Implementation for paginated modals that shows current page / total pages
     */
    class PaginatedDynamicTitle implements DynamicTitle {
        private final Function<PaginationState, Component> titleFunction;
        private final String baseTitle;
        private int currentPage;
        private int totalPages;
        
        /**
         * Creates a paginated dynamic title
         * 
         * @param baseTitle Base title text
         * @param currentPage Initial current page
         * @param totalPages Initial total pages
         */
        public PaginatedDynamicTitle(@NotNull String baseTitle, int currentPage, int totalPages) {
            this.baseTitle = baseTitle;
            this.currentPage = currentPage;
            this.totalPages = totalPages;
            this.titleFunction = state -> Component.text(
                state.baseTitle + " - Page " + state.currentPage + "/" + state.totalPages
            );
        }
        
        /**
         * Creates a paginated dynamic title with custom formatting
         * 
         * @param titleFunction Function to create title from pagination state
         * @param baseTitle Base title text
         * @param currentPage Initial current page
         * @param totalPages Initial total pages
         */
        public PaginatedDynamicTitle(
            @NotNull Function<PaginationState, Component> titleFunction,
            @NotNull String baseTitle, 
            int currentPage, 
            int totalPages
        ) {
            this.titleFunction = titleFunction;
            this.baseTitle = baseTitle;
            this.currentPage = currentPage;
            this.totalPages = totalPages;
        }
        
        @Override
        public @NotNull Component getDynamicTitle() {
            return titleFunction.apply(new PaginationState(baseTitle, currentPage, totalPages));
        }
        
        @Override
        public @NotNull Component update(@NotNull Object state) {
            if (state instanceof PaginationState) {
                PaginationState paginationState = (PaginationState) state;
                this.currentPage = paginationState.currentPage;
                this.totalPages = paginationState.totalPages;
                return getDynamicTitle();
            }
            return getDynamicTitle();
        }
        
        /**
         * Update pagination state and get new title
         * 
         * @param currentPage New current page
         * @param totalPages New total pages
         * @return Updated component title
         */
        public @NotNull Component update(int currentPage, int totalPages) {
            this.currentPage = currentPage;
            this.totalPages = totalPages;
            return getDynamicTitle();
        }
        
        /**
         * Get the current page
         * 
         * @return Current page number
         */
        public int getCurrentPage() {
            return currentPage;
        }
        
        /**
         * Get the total number of pages
         * 
         * @return Total pages
         */
        public int getTotalPages() {
            return totalPages;
        }
        
        /**
         * Get the base title
         * 
         * @return Base title text
         */
        public String getBaseTitle() {
            return baseTitle;
        }
    }
    
    /**
     * Implementation for updating titles based on item interactions
     */
    class InteractionDynamicTitle implements DynamicTitle {
        private final Function<InteractionState, Component> titleFunction;
        private final String baseTitle;
        private InteractionState currentState;
        
        /**
         * Creates a dynamic title that updates on item interactions
         * 
         * @param baseTitle Base title text
         */
        public InteractionDynamicTitle(@NotNull String baseTitle) {
            this.baseTitle = baseTitle;
            this.currentState = new InteractionState(baseTitle, null, -1, null, null);
            this.titleFunction = state -> Component.text(state.baseTitle);
        }

        /**
         * Creates a dynamic title with custom formatting
         * 
         * @param titleFunction Function to create title from interaction state
         * @param baseTitle Base title text
         */
        public InteractionDynamicTitle(
            @NotNull Function<InteractionState, Component> titleFunction,
            @NotNull String baseTitle
        ) {
            this.titleFunction = titleFunction;
            this.baseTitle = baseTitle;
            this.currentState = new InteractionState(baseTitle, null, -1, null, null);
        }

        @Override
        public @NotNull Component getDynamicTitle() {
            return titleFunction.apply(currentState);
        }

        @Override
        public @NotNull Component update(@NotNull Object state) {
            if (state instanceof InteractionState) {
                this.currentState = (InteractionState) state;
                return getDynamicTitle();
            }
            return Component.text(baseTitle);
        }

        /**
         * Update on item click
         * 
         * @param item The clicked item
         * @param slot The slot that was clicked
         * @param clickType The type of click
         * @param action The inventory action
         * @return Updated component title
         */
        public @NotNull Component updateOnClick(
            @Nullable ModalItem item, 
            int slot, 
            @Nullable ClickType clickType,
            @Nullable InventoryAction action
        ) {
            this.currentState = new InteractionState(baseTitle, item, slot, clickType, action);
            return getDynamicTitle();
        }

        /**
         * Get the base title
         * 
         * @return Base title text
         */
        public String getBaseTitle() {
            return baseTitle;
        }
    }
    
    /**
     * Data class for pagination state
     */
    class PaginationState {
        public final String baseTitle;
        public final int currentPage;
        public final int totalPages;
        
        public PaginationState(String baseTitle, int currentPage, int totalPages) {
            this.baseTitle = baseTitle;
            this.currentPage = currentPage;
            this.totalPages = totalPages;
        }
    }
    
    /**
     * Data class for interaction state
     */
    class InteractionState {
        public final String baseTitle;
        public final ModalItem clickedItem;
        public final int clickedSlot;
        public final ClickType clickType;
        public final InventoryAction action;
        
        public InteractionState(
            String baseTitle, 
            ModalItem clickedItem, 
            int clickedSlot, 
            ClickType clickType, 
            InventoryAction action
        ) {
            this.baseTitle = baseTitle;
            this.clickedItem = clickedItem;
            this.clickedSlot = clickedSlot;
            this.clickType = clickType;
            this.action = action;
        }
    }
}
