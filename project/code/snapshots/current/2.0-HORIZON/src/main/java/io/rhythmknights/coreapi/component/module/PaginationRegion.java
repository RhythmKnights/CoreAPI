// ──── CoreAPI ─────────────────────────────────────────────────────────▪
//     ▪ CoreAPI - Copyright © 2025 RhythmKnights [CoreAPI]
//     ▪ Original Work - Copyright © 2021 TriumphTeam [TriumphGUI]
//
//     ⏵ Licensed under the MIT License.
//         See LICENSE file in the project root for full license information.
// ────────────────────────────────────────────────────────────────────────────▪

package io.rhythmknights.coreapi.component.module;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Defines a region within a modal where pagination items can be placed
 */
public class PaginationRegion {
    private final List<Integer> slots;
    
    /**
     * Create a pagination region with specific slots
     * 
     * @param slots Array of specific slots to use for pagination
     */
    public PaginationRegion(int... slots) {
        this.slots = Arrays.stream(slots).boxed().collect(Collectors.toList());
    }
    
    /**
     * Create a pagination region from a list of slots
     * 
     * @param slots List of specific slots to use for pagination
     */
    public PaginationRegion(@NotNull List<Integer> slots) {
        this.slots = new ArrayList<>(slots);
    }
    
    /**
     * Create a pagination region from a rectangular area
     * 
     * @param startRow Starting row (1-based)
     * @param startCol Starting column (1-based)
     * @param endRow Ending row (1-based)
     * @param endCol Ending column (1-based)
     * @return PaginationRegion containing all slots in the defined area
     */
    public static PaginationRegion rectangle(int startRow, int startCol, int endRow, int endCol) {
        List<Integer> slots = new ArrayList<>();
        
        // Make sure we have start ≤ end
        int minRow = Math.min(startRow, endRow);
        int maxRow = Math.max(startRow, endRow);
        int minCol = Math.min(startCol, endCol);
        int maxCol = Math.max(startCol, endCol);
        
        for (int row = minRow; row <= maxRow; row++) {
            for (int col = minCol; col <= maxCol; col++) {
                slots.add(getSlotFromRowCol(row, col));
            }
        }
        
        return new PaginationRegion(slots);
    }
    
    /**
     * Create a pagination region for specific rows
     * 
     * @param rows Array of rows to include in pagination (1-based)
     * @return PaginationRegion containing all slots in the specified rows
     */
    public static PaginationRegion rows(int... rows) {
        List<Integer> slots = new ArrayList<>();
        
        for (int row : rows) {
            for (int col = 1; col <= 9; col++) {
                slots.add(getSlotFromRowCol(row, col));
            }
        }
        
        return new PaginationRegion(slots);
    }
    
    /**
     * Create a pagination region for specific columns
     * 
     * @param maxRows Maximum number of rows in the modal
     * @param cols Array of columns to include in pagination (1-based)
     * @return PaginationRegion containing all slots in the specified columns
     */
    public static PaginationRegion columns(int maxRows, int... cols) {
        List<Integer> slots = new ArrayList<>();
        
        for (int col : cols) {
            for (int row = 1; row <= maxRows; row++) {
                slots.add(getSlotFromRowCol(row, col));
            }
        }
        
        return new PaginationRegion(slots);
    }
    
    /**
     * Create a pagination region covering all slots in the modal
     * 
     * @param rows Number of rows in the modal
     * @return PaginationRegion containing all slots 
     */
    public static PaginationRegion all(int rows) {
        return new PaginationRegion(IntStream.range(0, rows * 9).boxed().collect(Collectors.toList()));
    }
    
    /**
     * Get the list of slots in this region
     * 
     * @return List of slot indices
     */
    public List<Integer> getSlots() {
        return slots;
    }
    
    /**
     * Get the size of this region (number of slots)
     * 
     * @return Number of slots in region
     */
    public int size() {
        return slots.size();
    }
    
    /**
     * Check if a slot is within this region
     * 
     * @param slot Slot to check
     * @return true if the slot is in this region
     */
    public boolean contains(int slot) {
        return slots.contains(slot);
    }
    
    /**
     * Convert row and column to slot index
     * 
     * @param row Row (1-based)
     * @param col Column (1-based)
     * @return Slot index
     */
    private static int getSlotFromRowCol(int row, int col) {
        return (col + (row - 1) * 9) - 1;
    }
}
