package com.derko.seamlessapi;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SeamlessAPI mod.
 *
 * Other mods depend on this mod to register features across Seamless systems
 * (food buffs, deconstruction hooks, and future modules).
 * This mod contains no gameplay logic of its own — it only provides registration APIs.
 *
 * Example usage in another mod's onInitialize():
 * <pre>
 *   SatiationAPI.registerFood("mymod:my_food",
 *       FoodBuffRegistration.builder()
 *           .buff("walk_speed")
 *           .duration(1200)
 *           .magnitude(0.20)
 *           .hearts(0.5)
 *           .build());
 * </pre>
 */
public class SeamlessApiMod implements ModInitializer {
    public static final String MOD_ID = "seamlessapi";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // No gameplay logic — purely a registry API
        LOGGER.info("SeamlessAPI initialized.");
    }
}
