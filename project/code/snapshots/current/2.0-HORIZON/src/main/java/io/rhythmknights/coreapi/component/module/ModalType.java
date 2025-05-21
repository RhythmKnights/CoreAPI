// ──── CoreAPI ─────────────────────────────────────────────────────────▪
//     ▪ CoreAPI - Copyright © 2025 RhythmKnights [CoreAPI]
//     ▪ Original Work - Copyright © 2021 TriumphTeam [TriumphGUI]
//
//     ⏵ Licensed under the MIT License.
//         See LICENSE file in the project root for full license information.
// ────────────────────────────────────────────────────────────────────────────▪

package io.rhythmknights.coreapi.component.module;

import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

public enum ModalType {

    CHEST(InventoryType.CHEST, 9, 9),
    WORKBENCH(InventoryType.WORKBENCH, 9, 10),
    HOPPER(InventoryType.HOPPER, 5, 5),
    DISPENSER(InventoryType.DISPENSER, 8, 9),
    BREWING(InventoryType.BREWING, 4, 5);

    @NotNull
    private final InventoryType inventoryType;
    private final int limit;
    private final int fillSize;

    ModalType(@NotNull final InventoryType inventoryType, final int limit, final int fillSize) {
        this.inventoryType = inventoryType;
        this.limit = limit;
        this.fillSize = fillSize;
    }

    @NotNull
    public InventoryType getInventoryType() {
        return inventoryType;
    }

    public int getLimit() {
        return limit;
    }

    public int getFillSize() {
        return fillSize;
    }
}
