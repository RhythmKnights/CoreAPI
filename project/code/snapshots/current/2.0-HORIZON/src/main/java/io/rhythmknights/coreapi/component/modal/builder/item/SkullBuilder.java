// ──── CoreAPI ─────────────────────────────────────────────────────────▪
//     ▪ CoreAPI - Copyright © 2025 RhythmKnights [CoreAPI]
//     ▪ Original Work - Copyright © 2021 TriumphTeam [TriumphGUI]
//
//     ⏵ Licensed under the MIT License.
//         See LICENSE file in the project root for full license information.
// ────────────────────────────────────────────────────────────────────────────▪

package io.rhythmknights.coreapi.modal.builder.item;

import io.rhythmknights.coreapi.component.module.exception.ModalException;
import io.rhythmknights.coreapi.component.utility.SkullUtil;
import io.rhythmknights.coreapi.component.utility.VersionHelper;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

/**
 * New builder for skull only, created to separate the specific features for skulls
 * Soon I'll add more useful features to this builder
 */
public final class SkullBuilder extends BaseItemBuilder<SkullBuilder> {

    private static final Field PROFILE_FIELD;

    static {
        Field field;

        try {
            final SkullMeta skullMeta = (SkullMeta) SkullUtil.skull().getItemMeta();
            field = skullMeta.getClass().getDeclaredField("profile");
            field.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            field = null;
        }

        PROFILE_FIELD = field;
    }

    SkullBuilder() {
        super(SkullUtil.skull());
    }

    SkullBuilder(final @NotNull ItemStack itemStack) {
        super(itemStack);
        if (!SkullUtil.isPlayerSkull(itemStack)) {
            throw new ModalException("SkullBuilder requires the material to be a PLAYER_HEAD/SKULL_ITEM!");
        }
    }

    /**
     * Sets the skull texture using a BASE64 string
     *
     * @param texture The base64 texture
     * @param profileId The unique id of the profile
     * @return {@link SkullBuilder}
     */
    @NotNull
    @Contract("_, _ -> this")
    public SkullBuilder texture(@NotNull final String texture, @NotNull final UUID profileId) {
        if (!SkullUtil.isPlayerSkull(getItemStack())) return this;

        if (VersionHelper.IS_PLAYER_PROFILE_API) {
            final String textureUrl = SkullUtil.getSkinUrl(texture);

            if (textureUrl == null) {
                return this;
            }

            final SkullMeta skullMeta = (SkullMeta) getMeta();
            final PlayerProfile profile = Bukkit.createPlayerProfile(profileId, "");
            final PlayerTextures textures = profile.getTextures();

            try {
                textures.setSkin(new URL(textureUrl));
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return this;
            }

            profile.setTextures(textures);
            skullMeta.setOwnerProfile(profile);
            setMeta(skullMeta);
            return this;
        }

        if (PROFILE_FIELD == null) {
            return this;
        }

        final SkullMeta skullMeta = (SkullMeta) getMeta();
        
        try {
            // Create GameProfile using reflection to avoid direct dependency on com.mojang.authlib
            Class<?> gameProfileClass = Class.forName("com.mojang.authlib.GameProfile");
            Object profile = gameProfileClass.getConstructor(UUID.class, String.class).newInstance(profileId, "");
            
            // Get properties and put texture
            Method getPropertiesMethod = gameProfileClass.getMethod("getProperties");
            Object properties = getPropertiesMethod.invoke(profile);
            
            // Create Property for texture
            Class<?> propertyClass = Class.forName("com.mojang.authlib.properties.Property");
            Object textureProperty = propertyClass.getConstructor(String.class, String.class)
                .newInstance("textures", texture);
            
            // Add property to properties
            Method putMethod = properties.getClass().getMethod("put", Object.class, Object.class);
            putMethod.invoke(properties, "textures", textureProperty);
            
            // Set the profile field
            PROFILE_FIELD.set(skullMeta, profile);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setMeta(skullMeta);
        return this;
    }

    /**
     * Sets the skull texture using a BASE64 string
     *
     * @param texture The base64 texture
     * @return {@link SkullBuilder}
     */
    @NotNull
    @Contract("_ -> this")
    public SkullBuilder texture(@NotNull final String texture) {
        return texture(texture, UUID.randomUUID());
    }

    /**
     * Sets skull owner via bukkit methods
     *
     * @param player {@link OfflinePlayer} to set skull of
     * @return {@link SkullBuilder}
     */
    @NotNull
    @Contract("_ -> this")
    public SkullBuilder owner(@NotNull final OfflinePlayer player) {
        if (!SkullUtil.isPlayerSkull(getItemStack())) return this;

        final SkullMeta skullMeta = (SkullMeta) getMeta();

        if (VersionHelper.IS_SKULL_OWNER_LEGACY) {
            skullMeta.setOwner(player.getName());
        } else {
            skullMeta.setOwningPlayer(player);
        }

        setMeta(skullMeta);
        return this;
    }
}
