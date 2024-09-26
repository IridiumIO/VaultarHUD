package io.iridium.vaultarhud.util;

import io.iridium.vaultarhud.VaultarHud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScreenValidator {

    public static final List<Class<? extends Screen>> VANILLA_SCREENS = Arrays.asList(
            InventoryScreen.class,
            ContainerScreen.class,
            CraftingScreen.class,
            FurnaceScreen.class
    );

    public static final List<Class<? extends Screen>> RS_SCREENS = VaultarHud.ISDEBUG ? null : getModScreens(
            "com.refinedmods.refinedstorage.screen.grid.GridScreen"
    );

    public static final List<Class<? extends Screen>> SOPHISTICATED_BACKPACK_SCREENS = VaultarHud.ISDEBUG ? null : getModScreens(
            "net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase"
    );

    public static final List<Class<? extends Screen>> AE2_SCREENS = VaultarHud.ISDEBUG ? null : getModScreens(
            "appeng.client.gui.me.common.MEStorageScreen",
            "appeng.client.gui.me.items.CraftingTermScreen"
    );

    public static final List<Class<? extends Screen>> COLOSSALCHEST_SCREENS = VaultarHud.ISDEBUG ? null : getModScreens(
            "org.cyclops.cyclopscore.client.gui.container.ContainerScreenExtended"
    );

    public static final List<Class<? extends Screen>> SSN_SCREENS = VaultarHud.ISDEBUG ? null : getModScreens(
            "com.lothrazar.storagenetwork.block.inventory.ScreenNetworkInventory",
            "com.lothrazar.storagenetwork.block.request.ScreenNetworkTable"
    );

    public static final List<Class<? extends Screen>> THERMAL_SCREENS = VaultarHud.ISDEBUG ? null : getModScreens(
            "cofh.core.client.gui.ContainerScreenCoFH"
    );

    private static List<Class<? extends Screen>> getModScreens(String... classNames) {
        List<Class<? extends Screen>> screens = new ArrayList<>();
        for (String className : classNames) {
            try {
                Class<?> cls = Class.forName(className);
                if (Screen.class.isAssignableFrom(cls)) {
                    screens.add(cls.asSubclass(Screen.class));
                }
            } catch (ClassNotFoundException e) {
                VaultarHud.LOGGER.warn("Class not found: " + className);
            }
        }
        return screens;
    }


    public static boolean isValidScreen(Screen screen){
        return isScreenInList(screen, VANILLA_SCREENS) ||
                isScreenInList(screen, RS_SCREENS) ||
                isScreenInList(screen, SOPHISTICATED_BACKPACK_SCREENS) ||
                isScreenInList(screen, AE2_SCREENS) ||
                isScreenInList(screen, COLOSSALCHEST_SCREENS) ||
                isScreenInList(screen, SSN_SCREENS) ||
                isScreenInList(screen, THERMAL_SCREENS);
    }

    public static boolean isScreenInList(Screen screen, List<Class<? extends Screen>> screenList){
        return screenList != null && screenList.stream().anyMatch(cls -> cls.isInstance(screen));
    }

    public static Point getScreenHUDCoordinates(Screen screen, Point offset){
        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        double x = 0;
        double y = 0;

        if (isScreenInList(screen, VANILLA_SCREENS)){
            x = ((screenWidth - 176) / 2) - offset.getX();
            y = ((screenHeight + 166) / 2) - offset.getY();
        }

        if (isScreenInList(screen, SOPHISTICATED_BACKPACK_SCREENS)){
            AbstractContainerScreen gs = (AbstractContainerScreen) screen;
            x = ((screenWidth - 176) / 2) - offset.getX(); //Need to use fixed width here due to different sizes of backpacks
            y = ((screenHeight + gs.getYSize()) / 2) - offset.getY();
        }

        if (isScreenInList(screen, RS_SCREENS)){
            AbstractContainerScreen gs = (AbstractContainerScreen) screen;
            x = ((screenWidth - gs.getXSize()) / 2) - offset.getX();
            y = ((screenHeight + gs.getYSize()) / 2) - offset.getY() -1;
        }

        if (isScreenInList(screen, AE2_SCREENS) || isScreenInList(screen, COLOSSALCHEST_SCREENS) || isScreenInList(screen, SSN_SCREENS) || isScreenInList(screen, THERMAL_SCREENS)){
            AbstractContainerScreen gs = (AbstractContainerScreen) screen;
            x = ((screenWidth - gs.getXSize()) / 2) - offset.getX();
            y = ((screenHeight + gs.getYSize()) / 2) - offset.getY();
        }

        return new Point(x, y);

    }


}
