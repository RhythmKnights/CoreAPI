// ──── CoreAPI ─────────────────────────────────────────────────────────▪
//     ▪ CoreAPI - Copyright © 2025 RhythmKnights [CoreAPI]
//     ▪ Original Work - Copyright © 2021 TriumphTeam [TriumphGUI]
//
//     ⏵ Licensed under the MIT License.
//         See LICENSE file in the project root for full license information.
// ────────────────────────────────────────────────────────────────────────────▪

package io.rhythmknights.coreapi.component.modal.builder.modal;

import io.rhythmknights.coreapi.component.module.DynamicTitle;
import io.rhythmknights.coreapi.component.module.ModalContainer;
import io.rhythmknights.coreapi.component.module.PaginationRegion;
import io.rhythmknights.coreapi.component.modal.PaginatedModal;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Modal builder for creating a {@link PaginatedModal}
 */
public class PaginatedBuilder extends BaseChestModalBuilder<PaginatedModal, PaginatedBuilder> {

    private int pageSize = 0;
    private PaginationRegion paginationRegion = null;
    private boolean useDynamicTitle = false;
    private boolean useInteractionDynamicTitle = false;
    private String dynamicTitleBase = null;
    private Function<DynamicTitle.PaginationState, Component> titleFunction = null;
    private Function<DynamicTitle.InteractionState, Component> interactionTitleFunction = null;

    /**
     * Sets the desirable page size, most of the time this isn't needed
     *
     * @param pageSize The amount of free slots that page items should occupy
     * @return The current builder
     */
    @NotNull
    @Contract("_ -> this")
    public PaginatedBuilder pageSize(final int pageSize) {
        this.pageSize = pageSize;
        return this;
    }
    
    /**
     * Sets a specific region for pagination items
     * 
     * @param paginationRegion Region defining which slots to use for pagination
     * @return The current builder
     */
    @NotNull
    @Contract("_ -> this")
    public PaginatedBuilder paginationRegion(@NotNull final PaginationRegion paginationRegion) {
        this.paginationRegion = paginationRegion;
        return this;
    }
    
    /**
     * Enables a dynamic title that shows pagination information
     * 
     * @param baseTitle Base title text without pagination info
     * @return The current builder
     */
    @NotNull
    @Contract("_ -> this")
    public PaginatedBuilder dynamicTitle(@NotNull final String baseTitle) {
        this.useDynamicTitle = true;
        this.useInteractionDynamicTitle = false;
        this.dynamicTitleBase = baseTitle;
        this.titleFunction = null;
        return this;
    }
    
    /**
     * Enables a dynamic title with custom formatting function
     * 
     * @param baseTitle Base title text
     * @param titleFunction Function to generate title from pagination state
     * @return The current builder
     */
    @NotNull
    @Contract("_, _ -> this")
    public PaginatedBuilder dynamicTitle(
        @NotNull final String baseTitle,
        @NotNull final Function<DynamicTitle.PaginationState, Component> titleFunction
    ) {
        this.useDynamicTitle = true;
        this.useInteractionDynamicTitle = false;
        this.dynamicTitleBase = baseTitle;
        this.titleFunction = titleFunction;
        return this;
    }
    
    /**
     * Enables an interaction-based dynamic title
     * 
     * @param baseTitle Base title text
     * @return The current builder
     */
    @NotNull
    @Contract("_ -> this")
    public PaginatedBuilder interactionDynamicTitle(@NotNull final String baseTitle) {
        this.useDynamicTitle = true;
        this.useInteractionDynamicTitle = true;
        this.dynamicTitleBase = baseTitle;
        this.interactionTitleFunction = null;
        return this;
    }
    
    /**
     * Enables an interaction-based dynamic title with custom formatting function
     * 
     * @param baseTitle Base title text
     * @param titleFunction Function to generate title from interaction state
     * @return The current builder
     */
    @NotNull
    @Contract("_, _ -> this")
    public PaginatedBuilder interactionDynamicTitle(
        @NotNull final String baseTitle,
        @NotNull final Function<DynamicTitle.InteractionState, Component> titleFunction
    ) {
        this.useDynamicTitle = true;
        this.useInteractionDynamicTitle = true;
        this.dynamicTitleBase = baseTitle;
        this.interactionTitleFunction = titleFunction;
        return this;
    }

    /**
     * Creates a new {@link PaginatedModal}
     *
     * @return A new {@link PaginatedModal}
     */
    @NotNull
    @Override
    @Contract(" -> new")
    public PaginatedModal create() {
        // Create container with dynamic title if requested
        if (useDynamicTitle) {
            if (useInteractionDynamicTitle) {
                DynamicTitle.InteractionDynamicTitle dynamicTitle;
                if (interactionTitleFunction != null) {
                    dynamicTitle = new DynamicTitle.InteractionDynamicTitle(
                        interactionTitleFunction, dynamicTitleBase);
                } else {
                    dynamicTitle = new DynamicTitle.InteractionDynamicTitle(dynamicTitleBase);
                }
                
                ModalContainer.Chest container = new ModalContainer.Chest(
                    dynamicTitle, getInventoryProvider(), getRows());
                
                final PaginatedModal modal;
                if (paginationRegion != null) {
                    modal = new PaginatedModal(container, paginationRegion, getModifiers());
                } else {
                    modal = new PaginatedModal(container, pageSize, getModifiers());
                }
                
                // Set update title on click flag
                modal.setUpdateTitleOnItemClick(true);
                
                final Consumer<PaginatedModal> consumer = getConsumer();
                if (consumer != null) consumer.accept(modal);
                
                return modal;
            }
            
            DynamicTitle.PaginatedDynamicTitle dynamicTitle;
            if (titleFunction != null) {
                dynamicTitle = new DynamicTitle.PaginatedDynamicTitle(
                    titleFunction, dynamicTitleBase, 1, 1);
            } else {
                dynamicTitle = new DynamicTitle.PaginatedDynamicTitle(
                    dynamicTitleBase, 1, 1);
            }
            
            ModalContainer.Chest container = new ModalContainer.Chest(
                dynamicTitle, getInventoryProvider(), getRows());
            
            final PaginatedModal modal;
            if (paginationRegion != null) {
                modal = new PaginatedModal(container, paginationRegion, getModifiers());
            } else {
                modal = new PaginatedModal(container, pageSize, getModifiers());
            }
            
            // Set update title on item click if specified
            if (updateTitleOnItemClick) {
                modal.setUpdateTitleOnItemClick(true);
            }
            
            final Consumer<PaginatedModal> consumer = getConsumer();
            if (consumer != null) consumer.accept(modal);
            
            return modal;
        }
    
        // Create normal container
        final ModalContainer.Chest container = createContainer();
        final PaginatedModal modal;
        
        if (paginationRegion != null) {
            modal = new PaginatedModal(container, paginationRegion, getModifiers());
        } else {
            modal = new PaginatedModal(container, pageSize, getModifiers());
        }
        
        // Set update title on item click if specified
        if (updateTitleOnItemClick) {
            modal.setUpdateTitleOnItemClick(true);
        }
        
        final Consumer<PaginatedModal> consumer = getConsumer();
        if (consumer != null) consumer.accept(modal);
        
        return modal;
    }
}