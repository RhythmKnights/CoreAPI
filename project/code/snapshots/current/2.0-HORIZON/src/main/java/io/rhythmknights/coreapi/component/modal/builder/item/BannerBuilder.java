// ──── CoreAPI ─────────────────────────────────────────────────────────▪
//     ▪ CoreAPI - Copyright © 2025 RhythmKnights [CoreAPI]
//     ▪ Original Work - Copyright © 2021 TriumphTeam [TriumphGUI]
//
//     ⏵ Licensed under the MIT License.
//         See LICENSE file in the project root for full license information.
// ────────────────────────────────────────────────────────────────────────────▪

package io.rhythmknights.coreapi.modal.builder.item;

import io.rhythmknights.coreapi.component.module.exception.ModalException;
import io.rhythmknights.coreapi.component.utility.VersionHelper;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * Item builder for banners only
 *
 * @author GabyTM <a href="https://github.com/iGabyTM">https://github.com/iGabyTM</a>
 * @since 3.0.1
 */
@SuppressWarnings("unused")
public final class BannerBuilder extends BaseItemBuilder<BannerBuilder> {

    private static final Material DEFAULT_BANNER;
    private static final EnumSet<Material> BANNERS;

    static {
        if (VersionHelper.IS_ITEM_LEGACY) {
            DEFAULT_BANNER = Material.valueOf("BANNER");
            BANNERS = EnumSet.of(Material.valueOf("BANNER"));
        } else {
            DEFAULT_BANNER = Material.WHITE_BANNER;
            BANNERS = EnumSet.copyOf(Tag.BANNERS.getValues());
        }
    }

    BannerBuilder() {
        super(new ItemStack(DEFAULT_BANNER));
    }

    BannerBuilder(@NotNull ItemStack itemStack) {
        super(itemStack);
        if (!BANNERS.contains(itemStack.getType())) {
            throw new ModalException("BannerBuilder requires the material to be a banner!");
        }
    }

    /**
     * Sets the base color for this banner
     * Note: This method is only available in older versions of Bukkit.
     * In newer versions, use different banner materials for different colors.
     *
     * @param color the base color
     * @return {@link BannerBuilder}
     * @since 3.0.1
     */
    @NotNull
    @Contract("_ -> this")
    public BannerBuilder baseColor(@NotNull final DyeColor color) {
        final BannerMeta bannerMeta = (BannerMeta) getMeta();
        
        // Only try to set base color if we're on a legacy version that supports it
        if (VersionHelper.IS_ITEM_LEGACY) {
            try {
                // Use reflection to safely call the method if it exists
                java.lang.reflect.Method setBaseColorMethod = bannerMeta.getClass().getMethod("setBaseColor", DyeColor.class);
                setBaseColorMethod.invoke(bannerMeta, color);
            } catch (Exception e) {
                // Method doesn't exist or other error, log a warning
                System.out.println("Warning: setBaseColor method not available in this Minecraft version");
            }
        } else {
            // For newer versions, suggest using specific banner material
            System.out.println("Info: In this Minecraft version, use " + color.toString() + "_BANNER material instead of setBaseColor");
        }
        
        setMeta(bannerMeta);
        return this;
    }

    /**
     * Adds a new pattern on top of the existing patterns
     *
     * @param color   the pattern color
     * @param pattern the pattern type
     * @return {@link BannerBuilder}
     * @since 3.0.1
     */
    @NotNull
    @Contract("_, _ -> this")
    public BannerBuilder pattern(@NotNull final DyeColor color, @NotNull final PatternType pattern) {
        final BannerMeta bannerMeta = (BannerMeta) getMeta();

        bannerMeta.addPattern(new Pattern(color, pattern));
        setMeta(bannerMeta);
        return this;
    }

    /**
     * Adds new patterns on top of the existing patterns
     *
     * @param pattern the patterns
     * @return {@link BannerBuilder}
     * @since 3.0.1
     */
    @NotNull
    @Contract("_ -> this")
    public BannerBuilder pattern(@NotNull final Pattern... pattern) {
        return pattern(Arrays.asList(pattern));
    }

    /**
     * Adds new patterns on top of the existing patterns
     *
     * @param patterns the patterns
     * @return {@link BannerBuilder}
     * @since 3.0.1
     */
    @NotNull
    @Contract("_ -> this")
    public BannerBuilder pattern(@NotNull final List<Pattern> patterns) {
        final BannerMeta bannerMeta = (BannerMeta) getMeta();

        for (final Pattern it : patterns) {
            bannerMeta.addPattern(it);
        }

        setMeta(bannerMeta);
        return this;
    }

    /**
     * Sets the pattern at the specified index
     *
     * @param index   the index
     * @param color   the pattern color
     * @param pattern the pattern type
     * @return {@link BannerBuilder}
     * @throws IndexOutOfBoundsException when index is not in [0, {@link BannerMeta#numberOfPatterns()}) range
     * @since 3.0.1
     */
    @NotNull
    @Contract("_, _, _ -> this")
    public BannerBuilder pattern(final int index, @NotNull final DyeColor color, @NotNull final PatternType pattern) {
        return pattern(index, new Pattern(color, pattern));
    }

    /**
     * Sets the pattern at the specified index
     *
     * @param index   the index
     * @param pattern the new pattern
     * @return {@link BannerBuilder}
     * @throws IndexOutOfBoundsException when index is not in [0, {@link BannerMeta#numberOfPatterns()}) range
     * @since 3.0.1
     */
    @NotNull
    @Contract("_, _ -> this")
    public BannerBuilder pattern(final int index, @NotNull final Pattern pattern) {
        final BannerMeta bannerMeta = (BannerMeta) getMeta();

        bannerMeta.setPattern(index, pattern);
        setMeta(bannerMeta);
        return this;
    }

    /**
     * Sets the patterns used on this banner
     *
     * @param patterns the new list of patterns
     * @return {@link BannerBuilder}
     * @since 3.0.1
     */
    @NotNull
    @Contract("_ -> this")
    public BannerBuilder setPatterns(@NotNull List<@NotNull Pattern> patterns) {
        final BannerMeta bannerMeta = (BannerMeta) getMeta();

        bannerMeta.setPatterns(patterns);
        setMeta(bannerMeta);
        return this;
    }

    // TODO add shield()

}
