package net.lawliet.infinitefluidplugin.config;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InfiniteFluidConfig {
    private static final String FLUID_KEY = "Fluids";
    public static final BuilderCodec<InfiniteFluidConfig> CODEC = BuilderCodec.builder(InfiniteFluidConfig.class, InfiniteFluidConfig::new)
            .append( new KeyedCodec<>(FLUID_KEY, new MapCodec<>(FluidConfig.CODEC, ConcurrentHashMap::new)),
                    InfiniteFluidConfig::setFluids,
                    InfiniteFluidConfig::getFluids
            ).add()
            .build();

    private Map<String, FluidConfig> fluids = new ConcurrentHashMap<>();


    public InfiniteFluidConfig() {
        String water_key = "Water";
//        this.fluids.put(water_key, new FluidConfig(true, water_key + "_Source", water_key));
        this.fluids.put(water_key, new FluidConfig(true));
    }

    public FluidConfig getFluidConfig(String key) {
        return this.fluids.computeIfAbsent(key, _ -> new FluidConfig());
    }

    public Map<String, FluidConfig> getFluids() {
        return fluids;
    }

    public void setFluids(Map<String, FluidConfig> fluids) {
        this.fluids = fluids;
    }
}
