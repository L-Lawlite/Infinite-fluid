package net.lawliet.infinitefluidplugin.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class FluidConfig {
    private static final String ENABLED_KEY = "Enabled";
//    private static final String SOURCE_KEY = "Source";
//    private static final String FLOWING_KEY = "Flowing";



    private boolean enabled;
//    private String sourceKey;
//    private String flowingKey;

    public static final BuilderCodec<FluidConfig> CODEC = BuilderCodec.builder(FluidConfig.class, FluidConfig::new)
            .append(
                    new KeyedCodec<>(ENABLED_KEY, Codec.BOOLEAN),
                    FluidConfig::setEnabled,
                    FluidConfig::isEnabled
                    ).add()
//            .append(
//                    new KeyedCodec<>(SOURCE_KEY, Codec.STRING),
//                    FluidConfig::setSourceKey,
//                    FluidConfig::getSourceKey
//            ).add()
//            .append(
//                    new KeyedCodec<>(FLOWING_KEY, Codec.STRING),
//                    FluidConfig::setFlowingKey,
//                    FluidConfig::getFlowingKey
//            ).add()
            .build();


    public FluidConfig() {
        this.enabled = false;
    }

//    public FluidConfig(boolean enabled, String sourceKey, String flowingKey) {
//        this.enabled = enabled;
//        this.sourceKey = sourceKey;
//        this.flowingKey = flowingKey;
//    }

    public FluidConfig(boolean enabled) {
        this.enabled = enabled;
    }

//    public String getSourceKey() {
//        return sourceKey;
//    }
//
//    public void setSourceKey(String sourceKey) {
//        this.sourceKey = sourceKey;
//    }
//
//    public String getFlowingKey() {
//        return flowingKey;
//    }
//
//    public void setFlowingKey(String flowingKey) {
//        this.flowingKey = flowingKey;
//    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
