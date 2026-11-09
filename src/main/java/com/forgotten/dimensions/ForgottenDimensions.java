package com.forgotten.dimensions;

import com.forgotten.dimensions.command.FdCommand;
import com.forgotten.dimensions.event.CombatEvents;
import com.forgotten.dimensions.event.ServerTickHandler;
import com.forgotten.dimensions.event.WorldEvents;
import com.forgotten.dimensions.network.ModNetworking;
import com.forgotten.dimensions.registry.ModAttachments;
import com.forgotten.dimensions.registry.ModBlocks;
import com.forgotten.dimensions.registry.ModEntities;
import com.forgotten.dimensions.registry.ModItemGroups;
import com.forgotten.dimensions.registry.ModItems;
import com.forgotten.dimensions.util.config.FdConfig;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ForgottenDimensions implements ModInitializer {
    public static final String MOD_ID = "forgotten_dimensions";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Les Dimensions Oubliées : initialisation");
        FdConfig.load();
        ModBlocks.register();
        ModItems.register();
        ModEntities.register();
        ModAttachments.register();
        ModItemGroups.register();
        ModNetworking.register();
        ServerTickHandler.register();
        CombatEvents.register();
        WorldEvents.register();
        FdCommand.register();
        com.forgotten.dimensions.world.generation.ModWorldGen.register();
    }
}
