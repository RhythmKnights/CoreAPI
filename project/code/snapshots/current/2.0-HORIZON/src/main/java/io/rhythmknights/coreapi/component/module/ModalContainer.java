// ──── CoreAPI ─────────────────────────────────────────────────────────▪
//     ▪ CoreAPI - Copyright © 2025 RhythmKnights [CoreAPI]
//     ▪ Original Work - Copyright © 2021 TriumphTeam [TriumphGUI]
//
//     ⏵ Licensed under the MIT License.
//         See LICENSE file in the project root for full license information.
// ────────────────────────────────────────────────────────────────────────────▪

package io.rhythmknights.coreapi.component.module;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ModalContainer {

    @NotNull Component title();
    
    /**
     * Check if this container uses a dynamic title
     * 
     * @return true if this container has a dynamic title
     */
    default boolean hasDynamicTitle() {
        return false;
    }

    /**
     * Get the dynamic title handler if one exists
     * 
     * @return DynamicTitle handler or null if not using dynamic titles
     */
    @Nullable
    default DynamicTitle getDynamicTitle() {
        return null;
    }

    void title(final @NotNull Component title);

    @NotNull Inventory createInventory(final @NotNull InventoryHolder inventoryHolder);

    @NotNull ModalType modalType();

    int inventorySize();

    int rows();

    class Chest implements ModalContainer {

        private final InventoryProvider.Chest inventoryProvider;

        private int rows;
        private Component title;
        private DynamicTitle dynamicTitle;

        public Chest(
            final @NotNull Component title,
            final @NotNull InventoryProvider.Chest inventoryProvider,
            final int rows
        ) {
            this.inventoryProvider = inventoryProvider;
            this.title = title;
            this.rows = rows;
            this.dynamicTitle = null;
        }

        /**
         * Constructor with dynamic title
         * 
         * @param dynamicTitle Dynamic title handler
         * @param inventoryProvider Inventory provider
         * @param rows Number of rows
         */
        public Chest(
            final @NotNull DynamicTitle dynamicTitle,
            final @NotNull InventoryProvider.Chest inventoryProvider,
            final int rows
        ) {
            this.inventoryProvider = inventoryProvider;
            this.dynamicTitle = dynamicTitle;
            this.title = dynamicTitle.getDynamicTitle();
            this.rows = rows;
        }

        @Override
        public @NotNull Component title() {
            return hasDynamicTitle() ? dynamicTitle.getDynamicTitle() : title;
        }
        
        @Override
        public boolean hasDynamicTitle() {
            return dynamicTitle != null;
        }
        
        @Override
        public @Nullable DynamicTitle getDynamicTitle() {
            return dynamicTitle;
        }
        
        /**
         * Set a dynamic title handler
         * 
         * @param dynamicTitle Dynamic title handler
         */
        public void setDynamicTitle(@NotNull DynamicTitle dynamicTitle) {
            this.dynamicTitle = dynamicTitle;
            this.title = dynamicTitle.getDynamicTitle();
        }

        @Override
        public void title(final @NotNull Component title) {
            this.title = title;
            // Clear dynamic title if explicitly setting a static title
            this.dynamicTitle = null;
        }

        @Override
        public int inventorySize() {
            return rows * 9;
        }

        @Override
        public @NotNull ModalType modalType() {
            return ModalType.CHEST;
        }

        @Override
        public int rows() {
            return rows;
        }

        public void rows(final int rows) {
            this.rows = rows;
        }

        @Override
        public @NotNull Inventory createInventory(final @NotNull InventoryHolder inventoryHolder) {
            return inventoryProvider.getInventory(title(), inventoryHolder, inventorySize());
        }
    }

    class Typed implements ModalContainer {

        private final InventoryProvider.Typed inventoryProvider;
        private final ModalType modalType;
        private Component title;
        private DynamicTitle dynamicTitle;

        public Typed(
            final @NotNull Component title,
            final @NotNull InventoryProvider.Typed inventoryProvider,
            final @NotNull ModalType modalType
        ) {
            this.inventoryProvider = inventoryProvider;
            this.title = title;
            this.modalType = modalType;
            this.dynamicTitle = null;
        }
        
        /**
         * Constructor with dynamic title
         * 
         * @param dynamicTitle Dynamic title handler
         * @param inventoryProvider Inventory provider
         * @param modalType Modal type
         */
        public Typed(
            final @NotNull DynamicTitle dynamicTitle,
            final @NotNull InventoryProvider.Typed inventoryProvider,
            final @NotNull ModalType modalType
        ) {
            this.inventoryProvider = inventoryProvider;
            this.dynamicTitle = dynamicTitle;
            this.title = dynamicTitle.getDynamicTitle();
            this.modalType = modalType;
        }

        @Override
        public @NotNull Component title() {
            return hasDynamicTitle() ? dynamicTitle.getDynamicTitle() : title;
        }
        
        @Override
        public boolean hasDynamicTitle() {
            return dynamicTitle != null;
        }
        
        @Override
        public @Nullable DynamicTitle getDynamicTitle() {
            return dynamicTitle;
        }
        
        /**
         * Set a dynamic title handler
         * 
         * @param dynamicTitle Dynamic title handler
         */
        public void setDynamicTitle(@NotNull DynamicTitle dynamicTitle) {
            this.dynamicTitle = dynamicTitle;
            this.title = dynamicTitle.getDynamicTitle();
        }

        @Override
        public void title(@NotNull Component title) {
            this.title = title;
            // Clear dynamic title if explicitly setting a static title
            this.dynamicTitle = null;
        }

        @Override
        public int inventorySize() {
            return modalType.getLimit();
        }

        @Override
        public @NotNull ModalType modalType() {
            return modalType;
        }

        @Override
        public int rows() {
            return 1;
        }

        @Override
        public @NotNull Inventory createInventory(@NotNull InventoryHolder inventoryHolder) {
            return inventoryProvider.getInventory(title(), inventoryHolder, modalType.getInventoryType());
        }
    }
}
