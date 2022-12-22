package org.zornco.energynodes.capability;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;
import org.zornco.energynodes.tile.IControllerTile;
import org.zornco.energynodes.tile.BaseNodeTile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class BaseNodeStorage implements INBTSerializable<CompoundTag> {
    @Nonnull
    protected final BaseNodeTile nodeTile;
    @Nullable
    protected IControllerTile controllerTile;

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

    public void setController(IControllerTile controllerTile) {
        this.controllerTile = controllerTile;
    }

    @Nullable
    public IControllerTile getControllerTile() {
        if (getNodeTile().getController()!=null) {
            setController(getNodeTile().getController());
        }
        return controllerTile;
    }
}
