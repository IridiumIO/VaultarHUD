package io.iridium.vaultarhud.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class VaultarHudClientConfigs {

    public static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec CLIENT_CONFIG;

    public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_INVENTORY_HUD;
    public static final ForgeConfigSpec.ConfigValue<Boolean> USE_SMALL_INVENTORY_HUD;


    static {
        CLIENT_BUILDER.push("Configs for VaultarHud");

        ENABLE_INVENTORY_HUD = CLIENT_BUILDER.comment("Enable Inventory HUD").define("enableInventoryHUD", true);

        USE_SMALL_INVENTORY_HUD = CLIENT_BUILDER.comment("Use Small Inventory HUD").define("useSmallInventoryHUD", false);

        CLIENT_BUILDER.pop();

        CLIENT_CONFIG = CLIENT_BUILDER.build();
    }

}
