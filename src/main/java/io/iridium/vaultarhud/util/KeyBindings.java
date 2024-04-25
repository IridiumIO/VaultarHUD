package io.iridium.vaultarhud.util;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {

    public static final String KEY_CATEGORY = "key.categories.vaultarhud";
    public static final String KEY_BINDING = "key.vaultarhud.toggle";
    public static final String KEY_ENABLE = "key.vaultarhud.enable";
    public static final KeyMapping TOGGLE_HUD = new KeyMapping(KEY_BINDING, KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_TAB, KEY_CATEGORY);
    public static final KeyMapping ENABLE_HUD = new KeyMapping(KEY_ENABLE, KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_0, KEY_CATEGORY);

    public static void init(){
        ClientRegistry.registerKeyBinding(TOGGLE_HUD);
        ClientRegistry.registerKeyBinding(ENABLE_HUD);
    }

}
