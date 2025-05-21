// ──── CoreAPI ─────────────────────────────────────────────────────────▪
//     ▪ CoreAPI - Copyright © 2025 RhythmKnights [CoreAPI]
//     ▪ Original Work - Copyright © 2021 TriumphTeam [TriumphGUI]
//
//     ⏵ Licensed under the MIT License.
//         See LICENSE file in the project root for full license information.
// ────────────────────────────────────────────────────────────────────────────▪

package io.rhythmknights.coreapi.modal.builder.item;

import io.rhythmknights.coreapi.component.module.exception.ModalException;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Item builder for {@link Material#MAP} only
 *
 * @author GabyTM <a href="https://github.com/iGabyTM">https://github.com/iGabyTM</a>
 * @since 3.0.1
 */
public class MapBuilder extends BaseItemBuilder<MapBuilder> {

    private static final Material MAP = Material.MAP;

    MapBuilder() {
        super(new ItemStack(MAP));
    }

    MapBuilder(@NotNull ItemStack itemStack) {
        super(itemStack);
        if (itemStack.getType() != MAP) {
            throw new ModalException("MapBuilder requires the material to be a MAP!");
        }
    }

    /**
     * Sets the map color. A custom map color will alter the display of the map
     * in an inventory slot.
     *
     * @param color the color to set
     * @return {@link MapBuilder}
     * @since 3.0.1
     */
    @NotNull
    @Override
    @Contract("_ -> this")
    public MapBuilder color(@Nullable final Color color) {
        final MapMeta mapMeta = (MapMeta) getMeta();

        mapMeta.setColor(color);
        setMeta(mapMeta);
        return this;
    }

    /**
     * Sets the location name. A custom map color will alter the display of the
     * map in an inventory slot.
     *
     * @param name the name to set
     * @return {@link MapMeta}
     * @since 3.0.1
     */
    @NotNull
    @Contract("_ -> this")
    public MapBuilder locationName(@Nullable final String name) {
        final MapMeta mapMeta = (MapMeta) getMeta();

        mapMeta.setLocationName(name);
        setMeta(mapMeta);
        return this;
    }

    /**
     * Sets if this map is scaling or not.
     *
     * @param scaling true to scale
     * @return {@link MapMeta}
     * @since 3.0.1
     */
    @NotNull
    @Contract("_ -> this")
    public MapBuilder scaling(final boolean scaling) {
        final MapMeta mapMeta = (MapMeta) getMeta();

        mapMeta.setScaling(scaling);
        setMeta(mapMeta);
        return this;
    }

    /**
     * Sets the associated map. This is used to determine what map is displayed.
     *
     * <p>
     * The implementation <b>may</b> allow null to clear the associated map, but
     * this is not required and is liable to generate a new (undefined) map when
     * the item is first used.
     *
     * @param view the map to set
     * @return {@link MapBuilder}
     * @since 3.0.1
     */
    @NotNull
    @Contract("_ -> this")
    public MapBuilder view(@NotNull final MapView view) {
        final MapMeta mapMeta = (MapMeta) getMeta();

        mapMeta.setMapView(view);
        setMeta(mapMeta);
        return this;
    }

}
