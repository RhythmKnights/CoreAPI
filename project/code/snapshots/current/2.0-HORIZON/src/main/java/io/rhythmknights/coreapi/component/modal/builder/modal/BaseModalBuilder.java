// ──── CoreAPI ─────────────────────────────────────────────────────────▪
//     ▪ CoreAPI - Copyright © 2025 RhythmKnights [CoreAPI]
//     ▪ Original Work - Copyright © 2021 TriumphTeam [TriumphGUI]
//
//     ⏵ Licensed under the MIT License.
//         See LICENSE file in the project root for full license information.
// ────────────────────────────────────────────────────────────────────────────▪

package io.rhythmknights.coreapi.component.modal.builder.modal;

import io.rhythmknights.coreapi.component.module.InteractionModifier;
import io.rhythmknights.coreapi.component.module.exception.ModalException;
import io.rhythmknights.coreapi.component.modal.BaseModal;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * The base for all the modal builders this is due to some limitations
 * where some builders will have unique features based on the modal type
 *
 * @param <M> The Type of {@link BaseModal}
 */
@SuppressWarnings("unchecked")
public abstract class BaseModalBuilder<M extends BaseModal, B extends BaseModalBuilder<M, B>> {

    private Component title = null;
    private final EnumSet<InteractionModifier> interactionModifiers = EnumSet.noneOf(InteractionModifier.class);

    private Consumer<M> consumer;
    
    // Changed from private to protected to allow access from subclasses
    protected boolean updateTitleOnItemClick = false;

    /**
     * Sets the title for the modal
     * This will be either a Component or a String
     *
     * @param title The modal title
     * @return The builder
     */
    @NotNull
    @Contract("_ -> this")
    public B title(@NotNull final Component title) {
        this.title = title;
        return (B) this;
    }

    /**
     * Disable item placement inside the modal
     *
     * @return The builder
     * @author SecretX
     * @since 3.0.0
     */
    @NotNull
    @Contract(" -> this")
    public B disableItemPlace() {
        interactionModifiers.add(InteractionModifier.PREVENT_ITEM_PLACE);
        return (B) this;
    }

    /**
     * Disable item retrieval inside the modal
     *
     * @return The builder
     * @author SecretX
     * @since 3.0.0
     */
    @NotNull
    @Contract(" -> this")
    public B disableItemTake() {
        interactionModifiers.add(InteractionModifier.PREVENT_ITEM_TAKE);
        return (B) this;
    }

    /**
     * Disable item swap inside the modal
     *
     * @return The builder
     * @author SecretX
     * @since 3.0.0
     */
    @NotNull
    @Contract(" -> this")
    public B disableItemSwap() {
        interactionModifiers.add(InteractionModifier.PREVENT_ITEM_SWAP);
        return (B) this;
    }

    /**
     * Disable item drop inside the modal
     *
     * @return The builder
     * @since 3.0.3
     */
    @NotNull
    @Contract(" -> this")
    public B disableItemDrop() {
        interactionModifiers.add(InteractionModifier.PREVENT_ITEM_DROP);
        return (B) this;
    }

    /**
     * Disable other modal actions
     * This option pretty much disables creating a clone stack of the item
     *
     * @return The builder
     * @since 3.0.4
     */
    @NotNull
    @Contract(" -> this")
    public B disableOtherActions() {
        interactionModifiers.add(InteractionModifier.PREVENT_OTHER_ACTIONS);
        return (B) this;
    }

    /**
     * Disable all the modifications of the modal, making it immutable by player interaction
     *
     * @return The builder
     * @author SecretX
     * @since 3.0.0
     */
    @NotNull
    @Contract(" -> this")
    public B disableAllInteractions() {
        interactionModifiers.addAll(InteractionModifier.VALUES);
        return (B) this;
    }

    /**
     * Allows item placement inside the modal
     *
     * @return The builder
     * @author SecretX
     * @since 3.0.0
     */
    @NotNull
    @Contract(" -> this")
    public B enableItemPlace() {
        interactionModifiers.remove(InteractionModifier.PREVENT_ITEM_PLACE);
        return (B) this;
    }

    /**
     * Allow items to be taken from the modal
     *
     * @return The builder
     * @author SecretX
     * @since 3.0.0
     */
    @NotNull
    @Contract(" -> this")
    public B enableItemTake() {
        interactionModifiers.remove(InteractionModifier.PREVENT_ITEM_TAKE);
        return (B) this;
    }

    /**
     * Allows item swap inside the modal
     *
     * @return The builder
     * @author SecretX
     * @since 3.0.0
     */
    @NotNull
    @Contract(" -> this")
    public B enableItemSwap() {
        interactionModifiers.remove(InteractionModifier.PREVENT_ITEM_SWAP);
        return (B) this;
    }

    /**
     * Allows item drop inside the modal
     *
     * @return The builder
     * @since 3.0.3
     */
    @NotNull
    @Contract(" -> this")
    public B enableItemDrop() {
        interactionModifiers.remove(InteractionModifier.PREVENT_ITEM_DROP);
        return (B) this;
    }

    /**
     * Enables or disables title updates when items are clicked
     * 
     * @param enable Whether to update title on item clicks
     * @return The builder
     */
    @NotNull
    @Contract("_ -> this")
    public B updateTitleOnItemClick(boolean enable) {
        this.updateTitleOnItemClick = enable;
        return (B) this;
    }
    
    /**
     * Enable other modal actions
     * This option pretty much enables creating a clone stack of the item
     *
     * @return The builder
     * @since 3.0.4
     */
    @NotNull
    @Contract(" -> this")
    public B enableOtherActions() {
        interactionModifiers.remove(InteractionModifier.PREVENT_OTHER_ACTIONS);
        return (B) this;
    }

    /**
     * Enable all modifications of the modal, making it completely mutable by player interaction
     *
     * @return The builder
     * @author SecretX
     * @since 3.0.0
     */
    @NotNull
    @Contract(" -> this")
    public B enableAllInteractions() {
        interactionModifiers.clear();
        return (B) this;
    }

    /**
     * Applies anything to the modal once it's created
     * Can be pretty useful for setting up small things like default actions
     *
     * @param consumer A {@link Consumer} that passes the built modal
     * @return The builder
     */
    @NotNull
    @Contract("_ -> this")
    public B apply(@NotNull final Consumer<M> consumer) {
        this.consumer = consumer;
        return (B) this;
    }

    /**
     * Creates the given ModalBase
     * Has to be abstract because each modal are different
     *
     * @return The new {@link BaseModal}
     */
    @NotNull
    @Contract(" -> new")
    public abstract M create();

    /**
     * Getter for the title
     *
     * @return The current title
     */
    @NotNull
    protected Component getTitle() {
        if (title == null) {
            throw new ModalException("Modal title is missing!");
        }

        return title;
    }

    /**
     * Getter for the consumer
     *
     * @return The consumer
     */
    @Nullable
    protected Consumer<M> getConsumer() {
        return consumer;
    }


    /**
     * Getter for the set of interaction modifiers
     *
     * @return The set of {@link InteractionModifier}
     * @author SecretX
     * @since 3.0.0
     */
    @NotNull
    protected Set<InteractionModifier> getModifiers() {
        return interactionModifiers;
    }

    protected void consumeBuilder(final @NotNull BaseModalBuilder<?, ?> builder) {
        this.title = builder.title;
        this.interactionModifiers.addAll(builder.interactionModifiers);
        this.updateTitleOnItemClick = builder.updateTitleOnItemClick; // Add this line
    }
}