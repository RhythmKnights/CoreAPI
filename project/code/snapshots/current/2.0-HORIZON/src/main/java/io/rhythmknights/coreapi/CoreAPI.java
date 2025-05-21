// ──── CoreAPI ─────────────────────────────────────────────────────────▪
//     ▪ CoreAPI - Copyright © 2025 RhythmKnights [CoreAPI]
//     ▪ Original Work - Copyright © 2021 TriumphTeam [TriumphGUI]
//
//     ⏵ Licensed under the MIT License.
//         See LICENSE file in the project root for full license information.
// ────────────────────────────────────────────────────────────────────────────▪

package io.rhythmknights.coreapi;

import io.rhythmknights.coreapi.component.modal.BaseModal;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class CoreAPI {

    // The plugin instance for registering the event and for the close delay.
    private static Plugin PLUGIN = null;

    private CoreAPI() {}

    public static void init(final @NotNull Plugin plugin) {
        PLUGIN = plugin;
    }

    public static @NotNull Plugin getPlugin() {
        if (PLUGIN == null) init(JavaPlugin.getProvidingPlugin(BaseModal.class));
        return PLUGIN;
    }
}
