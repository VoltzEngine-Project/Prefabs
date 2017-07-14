package com.builtbroken.mc.prefab.tile.listeners;

import com.builtbroken.mc.api.tile.listeners.IBlockListener;
import com.builtbroken.mc.api.tile.listeners.IRenderBoundsListener;
import com.builtbroken.mc.api.tile.listeners.ITileEventListener;
import com.builtbroken.mc.api.tile.listeners.ITileEventListenerBuilder;
import com.builtbroken.mc.imp.transform.region.Cube;
import com.builtbroken.mc.lib.json.loading.JsonProcessorData;
import net.minecraft.block.Block;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles unloading the inventory onto the group when a tile is broken
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/3/2017.
 */
public class RenderBoundsListener extends TileListener implements IBlockListener, IRenderBoundsListener
{
    @JsonProcessorData(value = "box", type = "cube")
    protected Cube renderBounds;

    @Override
    public Cube getRenderBounds()
    {
        return renderBounds;
    }

    @Override
    public List<String> getListenerKeys()
    {
        List<String> list = new ArrayList();
        list.add("renderBounds");
        return list;
    }

    public static class Builder implements ITileEventListenerBuilder
    {
        @Override
        public ITileEventListener createListener(Block block)
        {
            return new RenderBoundsListener();
        }

        @Override
        public String getListenerKey()
        {
            return "renderBounds";
        }
    }
}
