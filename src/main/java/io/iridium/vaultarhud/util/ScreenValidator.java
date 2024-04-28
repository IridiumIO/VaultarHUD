package io.iridium.vaultarhud.util;

import appeng.client.gui.me.common.MEStorageScreen;
import appeng.client.gui.me.items.CraftingTermScreen;
import com.refinedmods.refinedstorage.screen.grid.GridScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.FurnaceScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;

import java.util.Arrays;
import java.util.List;

public class ScreenValidator {

    public static final List<Class<? extends Screen>> VANILLA_SCREENS = Arrays.asList(
            InventoryScreen.class,
            ContainerScreen.class,
            CraftingScreen.class,
            FurnaceScreen.class
    );

    public static final List<Class<? extends Screen>> RS_SCREENS = Arrays.asList(
            GridScreen.class
    );

    public static final List<Class<? extends Screen>> SOPHISTICATED_BACKPACK_SCREENS = Arrays.asList(
            StorageScreenBase.class
    );

    public static final List<Class<? extends Screen>> AE2_SCREENS = Arrays.asList(
            MEStorageScreen.class,
            CraftingTermScreen.class
    );


    public static boolean isValidScreen(Screen screen){
        return isScreenInList(screen, VANILLA_SCREENS) ||
                isScreenInList(screen, RS_SCREENS) ||
                isScreenInList(screen, SOPHISTICATED_BACKPACK_SCREENS) ||
                isScreenInList(screen, AE2_SCREENS);
    }

    public static boolean isScreenInList(Screen screen, List<Class<? extends Screen>> screenList){
        return screenList.stream().anyMatch(cls -> cls.isInstance(screen));
    }

    public static Point getScreenHUDCoordinates(Screen screen){
        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int x = 0;
        int y = 0;
        int offset = 0;

        String screenName = screen.getClass().getName();

        if (isScreenInList(screen, VANILLA_SCREENS)){
            x = ((screenWidth - 176) / 2) - 27;
            y = ((screenHeight + 166) / 2) - 87;

        }

        if (isScreenInList(screen, RS_SCREENS)){

            GridScreen gs = (GridScreen) screen;

            x = ((screenWidth - 227) / 2) - 27;
            y = ((screenHeight + gs.getYSize()) / 2) - 88;
        }


        if (isScreenInList(screen, SOPHISTICATED_BACKPACK_SCREENS)){

            StorageScreenBase gs = (StorageScreenBase) screen;

            x = ((screenWidth - 176) / 2) - 27;
            y = ((screenHeight + gs.getYSize()) / 2) - 87;
        }

        if (isScreenInList(screen, AE2_SCREENS)){

            MEStorageScreen gs = (MEStorageScreen) screen;
            gs = (gs == null) ? (CraftingTermScreen) screen : gs;

            x = ((screenWidth - 195) / 2) - 27;
            y = ((screenHeight + gs.getYSize()) / 2) - 88;
        }

        return new Point(x, y);

    }


}
