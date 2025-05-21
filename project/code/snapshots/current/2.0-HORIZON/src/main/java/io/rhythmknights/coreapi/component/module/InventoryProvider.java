// ──── CoreAPI ─────────────────────────────────────────────────────────▪
//     ▪ CoreAPI - Copyright © 2025 RhythmKnights [CoreAPI]
//     ▪ Original Work - Copyright © 2021 TriumphTeam [TriumphGUI]
//
//     ⏵ Licensed under the MIT License.
//         See LICENSE file in the project root for full license information.
// ────────────────────────────────────────────────────────────────────────────▪

package io.rhythmknights.coreapi.component.module;

import net.kyori.adventure.text.Component;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public final class InventoryProvider {

    @FunctionalInterface
    public interface Chest {

        @NotNull Inventory getInventory(
                final @NotNull Component title,
                final @NotNull InventoryHolder owner,
                final int rows
        );
    }

    @FunctionalInterface
    public interface Typed {

        @NotNull Inventory getInventory(
                final @NotNull Component title,
                final @NotNull InventoryHolder owner,
                final @NotNull InventoryType inventoryType
        );
    }
}
