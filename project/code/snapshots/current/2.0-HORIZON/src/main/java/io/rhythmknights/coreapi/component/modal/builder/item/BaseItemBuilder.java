// ──── CoreAPI ─────────────────────────────────────────────────────────▪
//     ▪ CoreAPI - Copyright © 2025 RhythmKnights [CoreAPI]
//     ▪ Original Work - Copyright © 2021 TriumphTeam [TriumphGUI]
//
//     ⏵ Licensed under the MIT License.
//         See LICENSE file in the project root for full license information.
// ────────────────────────────────────────────────────────────────────────────▪

package io.rhythmknights.coreapi.modal.builder.item;

import io.rhythmknights.coreapi.component.module.ModalAction;
import io.rhythmknights.coreapi.component.module.exception.ModalException;
import io.rhythmknights.coreapi.component.utility.ItemNBT;
import io.rhythmknights.coreapi.component.utility.VersionHelper;
import io.rhythmknights.coreapi.component.modal.ModalItem;

import com.google.common.base.Preconditions;
import net.kyori.adventure.platform.bukkit.MinecraftComponentSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Contains all the common methods for the future ItemBuilders
 * 
 * This class expects Components to be pre-parsed by CoreFramework's TextUtility.
 * CoreAPI handles ONLY GUI building - no text parsing.
 *
 * @param <B> The ItemBuilder type so the methods can cast to the subtype
 */
@SuppressWarnings("unchecked")
public abstract class BaseItemBuilder<B extends BaseItemBuilder<B>> {

    private static final EnumSet<Material> LEATHER_ARMOR = EnumSet.of(
        Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS
    );

    private static final Field DISPLAY_NAME_FIELD;
    private static final Field LORE_FIELD;

    // Add legacy serializer for fallback cases
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    static {
        try {
            final Class<?> metaClass = VersionHelper.craftClass("inventory.CraftMetaItem");

            DISPLAY_NAME_FIELD = metaClass.getDeclaredField("displayName");
            DISPLAY_NAME_FIELD.setAccessible(true);

            LORE_FIELD = metaClass.getDeclaredField("lore");
            LORE_FIELD.setAccessible(true);
        } catch (NoSuchFieldException | ClassNotFoundException exception) {
            exception.printStackTrace();
            throw new ModalException("Could not retrieve displayName nor lore field for ItemBuilder.");
        }
    }

    private ItemStack itemStack;
    private ItemMeta meta;

    protected BaseItemBuilder(@NotNull final ItemStack itemStack) {
        Preconditions.checkNotNull(itemStack, "Item can't be null!");

        this.itemStack = itemStack;
        meta = itemStack.hasItemMeta() ? itemStack.getItemMeta() : Bukkit.getItemFactory().getItemMeta(itemStack.getType());
    }

    /**
     * Serializes the component with the right ComponentSerializer for the current MC version
     * Expects Components to be pre-parsed by CoreFramework's TextUtility
     *
     * @param component component to serialize (already parsed)
     * @return the serialized representation of the component
     */
    protected @NotNull Object serializeComponent(@NotNull final Component component) {
        if (VersionHelper.IS_ITEM_NAME_COMPONENT) {
            //noinspection UnstableApiUsage
            return MinecraftComponentSerializer.get().serialize(component);
        } else {
            return GsonComponentSerializer.gson().serialize(component);
        }
    }

    /**
     * Deserializes the object with the right ComponentSerializer for the current MC version
     *
     * @param obj object to deserialize
     * @return the component
     */
    protected @NotNull Component deserializeComponent(@NotNull final Object obj) {
        if (VersionHelper.IS_ITEM_NAME_COMPONENT) {
            //noinspection UnstableApiUsage
            return MinecraftComponentSerializer.get().deserialize(obj);
        } else {
            return GsonComponentSerializer.gson().deserialize((String) obj);
        }
    }

    /**
     * Sets the display name of the item using {@link Component}
     * 
     * Component should be pre-parsed by CoreFramework's TextUtility
     *
     * @param name The {@link Component} name (pre-parsed)
     * @return {@link ItemBuilder}
     * @since 3.0.0
     */
    @NotNull
    @Contract("_ -> this")
    public B name(@NotNull final Component name) {
        if (meta == null) return (B) this;

        if (VersionHelper.IS_COMPONENT_LEGACY) {
            // For legacy versions, use the setDisplayName method directly
            meta.setDisplayName(LEGACY_SERIALIZER.serialize(name));
            return (B) this;
        }

        try {
            DISPLAY_NAME_FIELD.set(meta, this.serializeComponent(name));
        } catch (IllegalAccessException exception) {
            exception.printStackTrace();
        }

        return (B) this;
    }

    /**
     * Sets the amount of items
     *
     * @param amount the amount of items
     * @return {@link ItemBuilder}
     * @since 3.0.0
     */
    @NotNull
    @Contract("_ -> this")
    public B amount(final int amount) {
        itemStack.setAmount(amount);
        return (B) this;
    }

    /**
     * Set the lore lines of an item
     * 
     * Components should be pre-parsed by CoreFramework's TextUtility
     *
     * @param lore Lore lines as varargs (pre-parsed)
     * @return {@link ItemBuilder}
     * @since 3.0.0
     */
    @NotNull
    @Contract("_ -> this")
    public B lore(@Nullable final Component @NotNull ... lore) {
        return lore(Arrays.asList(lore));
    }

    /**
     * Set the lore lines of an item
     * 
     * Components should be pre-parsed by CoreFramework's TextUtility
     *
     * @param lore A {@link List} with the lore lines (pre-parsed)
     * @return {@link ItemBuilder}
     * @since 3.0.0
     */
    @NotNull
    @Contract("_ -> this")
    public B lore(@NotNull final List<@Nullable Component> lore) {
        if (meta == null) return (B) this;

        if (VersionHelper.IS_COMPONENT_LEGACY) {
            // For legacy versions, use the setLore method directly
            meta.setLore(lore.stream()
                .filter(Objects::nonNull)
                .map(LEGACY_SERIALIZER::serialize)
                .collect(Collectors.toList()));
            return (B) this;
        }

        final List<Object> jsonLore = lore.stream()
            .filter(Objects::nonNull)
            .map(this::serializeComponent)
            .collect(Collectors.toList());

        try {
            LORE_FIELD.set(meta, jsonLore);
        } catch (IllegalAccessException exception) {
            exception.printStackTrace();
        }

        return (B) this;
    }

    /**
     * Consumer for freely adding to the lore
     * 
     * Components should be pre-parsed by CoreFramework's TextUtility
     *
     * @param lore A {@link Consumer} with the {@link List} of lore {@link Component}
     * @return {@link ItemBuilder}
     * @since 3.0.0
     */
    @NotNull
    @Contract("_ -> this")
    public B lore(@NotNull final Consumer<List<@Nullable Component>> lore) {
        if (meta == null) return (B) this;

        List<Component> components;
        if (VersionHelper.IS_COMPONENT_LEGACY) {
            final List<String> stringLore = meta.getLore();
            components = (stringLore == null) ? new ArrayList<>() : 
                stringLore.stream().map(LEGACY_SERIALIZER::deserialize).collect(Collectors.toList());
        } else {
            try {
                final List<Object> jsonLore = (List<Object>) LORE_FIELD.get(meta);
                // The field is null by default ._.
                components = (jsonLore == null) ? new ArrayList<>() : 
                    jsonLore.stream().map(this::deserializeComponent).collect(Collectors.toList());
            } catch (IllegalAccessException exception) {
                components = new ArrayList<>();
                exception.printStackTrace();
            }
        }

        lore.accept(components);
        return lore(components);
    }

    /**
     * Enchants the {@link ItemStack}
     *
     * @param enchantment            The {@link Enchantment} to add
     * @param level                  The level of the {@link Enchantment}
     * @param ignoreLevelRestriction If should or not ignore it
     * @return {@link ItemBuilder}
     * @since 3.0.0
     */
    @NotNull
    @Contract("_, _, _ -> this")
    public B enchant(@NotNull final Enchantment enchantment, final int level, final boolean ignoreLevelRestriction) {
        meta.addEnchant(enchantment, level, ignoreLevelRestriction);
        return (B) this;
    }

    /**
     * Enchants the {@link ItemStack}
     *
     * @param enchantment The {@link Enchantment} to add
     * @param level       The level of the {@link Enchantment}
     * @return {@link ItemBuilder}
     * @since 3.0.0
     */
    @NotNull
    @Contract("_, _ -> this")
    public B enchant(@NotNull final Enchantment enchantment, final int level) {
        return enchant(enchantment, level, true);
    }

    /**
     * Enchants the {@link ItemStack}
     *
     * @param enchantment The {@link Enchantment} to add
     * @return {@link ItemBuilder}
     * @since 3.0.0
     */
    @NotNull
    @Contract("_ -> this")
    public B enchant(@NotNull final Enchantment enchantment) {
        return enchant(enchantment, 1, true);
    }

    /**
     * Enchants the {@link ItemStack} with the specified map where the value
     * is the level of the key's enchantment
     *
     * @param enchantments           Enchantments to add
     * @param ignoreLevelRestriction If level restriction should be ignored
     * @return {@link ItemBuilder}
     * @since 3.1.2
     */
    @NotNull
    @Contract("_, _ -> this")
    public B enchant(@NotNull final Map<Enchantment, Integer> enchantments, final boolean ignoreLevelRestriction) {
        enchantments.forEach((enchantment, level) -> this.enchant(enchantment, level, ignoreLevelRestriction));
        return (B) this;
    }

    /**
     * Enchants the {@link ItemStack} with the specified map where the value
     * is the level of the key's enchantment
     *
     * @param enchantments Enchantments to add
     * @return {@link ItemBuilder}
     * @since 3.1.2
     */
    @NotNull
    @Contract("_ -> this")
    public B enchant(@NotNull final Map<Enchantment, Integer> enchantments) {
        return enchant(enchantments, true);
    }

    /**
     * Disenchants a certain {@link Enchantment} from the {@link ItemStack}
     *
     * @param enchantment The {@link Enchantment} to remove
     * @return {@link ItemBuilder}
     * @since 3.0.0
     */
    @NotNull
    @Contract("_ -> this")
    public B disenchant(@NotNull final Enchantment enchantment) {
        itemStack.removeEnchantment(enchantment);
        return (B) this;
    }

    /**
     * Add an {@link ItemFlag} to the item
     *
     * @param flags The {@link ItemFlag} to add
     * @return {@link ItemBuilder}
     * @since 3.0.0
     */
    @NotNull
    @Contract("_ -> this")
    public B flags(@NotNull final ItemFlag... flags) {
        meta.addItemFlags(flags);
        return (B) this;
    }

    /**
     * Makes the {@link ItemStack} unbreakable
     *
     * @return {@link ItemBuilder}
     * @since 3.0.0
     */
    @NotNull
    @Contract(" -> this")
    public B unbreakable() {
        return unbreakable(true);
    }

    /**
     * Sets the item as unbreakable
     *
     * @param unbreakable If should or not be unbreakable
     * @return {@link ItemBuilder}
     */
    @NotNull
    @Contract("_ -> this")
    public B unbreakable(boolean unbreakable) {
        if (VersionHelper.IS_UNBREAKABLE_LEGACY) {
            return setNBT("Unbreakable", unbreakable);
        }

        meta.setUnbreakable(unbreakable);
        return (B) this;
    }

    /**
     * Makes the {@link ItemStack} glow
     *
     * @return {@link ItemBuilder}
     * @since 3.0.0
     */
    @NotNull
    @Contract(" -> this")
    public B glow() {
        return glow(true);
    }

    /**
     * Adds or removes the {@link ItemStack} glow
     *
     * @param glow Should the item glow
     * @return {@link ItemBuilder}
     */
    @NotNull
    @Contract("_ -> this")
    public B glow(boolean glow) {
        if (glow) {
            meta.addEnchant(Enchantment.LURE, 1, false);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            return (B) this;
        }

        for (final Enchantment enchantment : meta.getEnchants().keySet()) {
            meta.removeEnchant(enchantment);
        }

        return (B) this;
    }

    /**
     * Consumer for applying {@link PersistentDataContainer} to the item
     * This method will only work on versions above 1.14
     *
     * @param consumer The {@link Consumer} with the PDC
     * @return {@link ItemBuilder}
     * @since 3.0.0
     */
    @NotNull
    @Contract("_ -> this")
    public B pdc(@NotNull final Consumer<PersistentDataContainer> consumer) {
        consumer.accept(meta.getPersistentDataContainer());
        return (B) this;
    }

    /**
     * Sets the custom model data of the item
     * Added in 1.13
     *
     * @param modelData The custom model data from the resource pack
     * @return {@link ItemBuilder}
     * @since 3.0.0
     */
    @NotNull
    @Contract("_ -> this")
    public B model(final int modelData) {
        if (VersionHelper.IS_CUSTOM_MODEL_DATA) {
            meta.setCustomModelData(modelData);
        }

        return (B) this;
    }

    /**
     * Color an {@link org.bukkit.inventory.ItemStack}
     *
     * @param color color
     * @return {@link B}
     * @see org.bukkit.inventory.meta.LeatherArmorMeta#setColor(Color)
     * @see org.bukkit.inventory.meta.MapMeta#setColor(Color)
     * @since 3.0.3
     */
    @NotNull
    @Contract("_ -> this")
    public B color(@NotNull final Color color) {
        if (LEATHER_ARMOR.contains(itemStack.getType())) {
            final LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) getMeta();

            leatherArmorMeta.setColor(color);
            setMeta(leatherArmorMeta);
        }

        return (B) this;
    }

    /**
     * Sets NBT tag to the {@link ItemStack}
     *
     * @param key   The NBT key
     * @param value The NBT value
     * @return {@link ItemBuilder}
     */
    @NotNull
    @Contract("_, _ -> this")
    public B setNBT(@NotNull final String key, @NotNull final String value) {
        itemStack.setItemMeta(meta);
        itemStack = ItemNBT.setString(itemStack, key, value);
        meta = itemStack.getItemMeta();
        return (B) this;
    }

    /**
     * Sets NBT tag to the {@link ItemStack}
     *
     * @param key   The NBT key
     * @param value The NBT value
     * @return {@link ItemBuilder}
     */
    @NotNull
    @Contract("_, _ -> this")
    public B setNBT(@NotNull final String key, final boolean value) {
        itemStack.setItemMeta(meta);
        itemStack = ItemNBT.setBoolean(itemStack, key, value);
        meta = itemStack.getItemMeta();
        return (B) this;
    }

    /**
     * Removes NBT tag from the {@link ItemStack}
     *
     * @param key The NBT key
     * @return {@link ItemBuilder}
     */
    @NotNull
    @Contract("_ -> this")
    public B removeNBT(@NotNull final String key) {
        itemStack.setItemMeta(meta);
        itemStack = ItemNBT.removeTag(itemStack, key);
        meta = itemStack.getItemMeta();
        return (B) this;
    }

    /**
     * Builds the item into {@link ItemStack}
     *
     * @return The fully built {@link ItemStack}
     */
    @NotNull
    public ItemStack build() {
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    /**
     * Creates a {@link ModalItem} instead of an {@link ItemStack}
     *
     * @return A {@link ModalItem} with no {@link ModalAction}
     */
    @NotNull
    @Contract(" -> new")
    public ModalItem asModalItem() {
        return new ModalItem(build());
    }

    /**
     * Creates a {@link ModalItem} instead of an {@link ItemStack}
     *
     * @param action The {@link ModalAction} to apply to the item
     * @return A {@link ModalItem} with {@link ModalAction}
     */
    @NotNull
    @Contract("_ -> new")
    public ModalItem asModalItem(@NotNull final ModalAction<InventoryClickEvent> action) {
        return new ModalItem(build(), action);
    }

    /**
     * Package private getter for extended builders
     *
     * @return The ItemStack
     */
    @NotNull
    protected ItemStack getItemStack() {
        return itemStack;
    }

    /**
     * Package private setter for the extended builders
     *
     * @param itemStack The ItemStack
     */
    protected void setItemStack(@NotNull final ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    /**
     * Package private getter for extended builders
     *
     * @return The ItemMeta
     */
    @NotNull
    protected ItemMeta getMeta() {
        return meta;
    }

    /**
     * Package private setter for the extended builders
     *
     * @param meta The ItemMeta
     */
    protected void setMeta(@NotNull final ItemMeta meta) {
        this.meta = meta;
    }
}