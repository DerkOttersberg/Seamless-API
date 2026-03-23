package com.derko.seamlessapi;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/**
 * SeamlessAPI mod.
 *
 * Other mods depend on this mod to register features across Seamless systems
 * (food buffs, deconstruction hooks, and future modules).
 * This mod contains no gameplay logic of its own — it only provides registration APIs.
 *
 * Example usage in another mod's constructor:
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
@Mod(SeamlessApiMod.MOD_ID)
public class SeamlessApiMod {
    public static final String MOD_ID = "seamlessapi";

    public SeamlessApiMod(IEventBus modEventBus) {
        // No logic — purely a registry API
    }
}
