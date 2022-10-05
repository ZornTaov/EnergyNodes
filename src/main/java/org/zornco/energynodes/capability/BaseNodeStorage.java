package org.zornco.energynodes.capability;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;
import org.zornco.energynodes.block.IControllerNode;
import org.zornco.energynodes.tile.BaseNodeTile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class BaseNodeStorage implements INBTSerializable<CompoundTag> {
    @Nonnull
    protected final BaseNodeTile nodeTile;
    @Nullable
    protected IControllerNode controllerTile;

    public BaseNodeStorage(@Nonnull BaseNodeTile tile) {
        this.nodeTile = tile;
    }

    @Nonnull
    public BaseNodeTile getNodeTile() {
        return nodeTile;
    }

    public BlockPos getLocation() {
        return this.nodeTile.getBlockPos();
    }

    public void setController(IControllerNode controllerTile) {
        this.controllerTile = controllerTile;
    }

    @Nullable
    public IControllerNode getControllerTile() {
        return controllerTile;
    }
}
