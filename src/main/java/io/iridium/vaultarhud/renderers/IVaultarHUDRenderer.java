package io.iridium.vaultarhud.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import io.iridium.vaultarhud.util.Point;

public interface IVaultarHUDRenderer {

    void render(PoseStack poseStack, Point renderOrigin);

}
