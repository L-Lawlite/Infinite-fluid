package net.lawliet.infinitefluidplugin.data;

import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;

public class FluidData {
    public String sourceKey;
    public String flowingKey;
    public byte sourceLevel;
    public int sourceId;
    public int flowingId;
    private boolean isValid;

    public FluidData(String sourceKey, String flowingKey) {
        this.sourceKey = sourceKey;
        this.flowingKey = flowingKey;
        IndexedLookupTableAssetMap<String, Fluid> fluidMap = Fluid.getAssetMap();
        this.sourceId = fluidMap.getIndex(sourceKey);
        this.flowingId = fluidMap.getIndex(flowingKey);
        if (sourceId == Integer.MIN_VALUE || flowingId == Integer.MIN_VALUE) {
            isValid = false;
            return;
        }
        Fluid source = fluidMap.getAsset(sourceId);
        this.sourceLevel =  source != null ? (byte) source.getMaxFluidLevel() : 0;
    }

    public boolean isValid() {
        return this.isValid;
    }
}
