package io.iridium.vaultarhud.util;

import appeng.client.gui.me.common.MEStorageScreen;
import appeng.client.gui.me.items.CraftingTermScreen;
import com.refinedmods.refinedstorage.screen.grid.GridScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.*;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;
import org.cyclops.cyclopscore.client.gui.container.ContainerScreenExtended;
import com.lothrazar.storagenetwork.block.inventory.ScreenNetworkInventory;
import com.lothrazar.storagenetwork.block.request.ScreenNetworkTable;

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

    public static final List<Class<? extends Screen>> COLOSSALCHEST_SCREENS = Arrays.asList(
            ContainerScreenExtended.class
    );

    public static final List<Class<? extends Screen>> SSN_SCREENS = Arrays.asList(
            ScreenNetworkInventory.class,
            ScreenNetworkTable.class
    );


    public static boolean isValidScreen(Screen screen){
        return isScreenInList(screen, VANILLA_SCREENS) ||
                isScreenInList(screen, RS_SCREENS) ||
                isScreenInList(screen, SOPHISTICATED_BACKPACK_SCREENS) ||
                isScreenInList(screen, AE2_SCREENS) ||
                isScreenInList(screen, COLOSSALCHEST_SCREENS) ||
                isScreenInList(screen, SSN_SCREENS);
    }

    public static boolean isScreenInList(Screen screen, List<Class<? extends Screen>> screenList){
        return screenList.stream().anyMatch(cls -> cls.isInstance(screen));
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

        if (isScreenInList(screen, AE2_SCREENS) || isScreenInList(screen, COLOSSALCHEST_SCREENS) || isScreenInList(screen, SSN_SCREENS)){
            AbstractContainerScreen gs = (AbstractContainerScreen) screen;
            x = ((screenWidth - gs.getXSize()) / 2) - offset.getX();
            y = ((screenHeight + gs.getYSize()) / 2) - offset.getY();
        }

        return new Point(x, y);

    }


}
