package net.lawliet.infinitefluidplugin;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.util.Config;
import net.lawliet.infinitefluidplugin.config.InfiniteFluidConfig;

public class InfiniteFluidPlugin extends JavaPlugin {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private final Config<InfiniteFluidConfig> config;

    public InfiniteFluidPlugin(JavaPluginInit init) {
        super(init);
        LOGGER.atInfo().log("Hello from %s version %s", this.getName(), this.getManifest().getVersion().toString());
        config = this.withConfig("InfiniteFluidConfig", InfiniteFluidConfig.CODEC);
    }

    @Override
    protected void setup() {
        this.config.save();
        this.getCommandRegistry().registerCommand(new ExampleCommand(this.getName(), this.getManifest().getVersion().toString()));
        this.getChunkStoreRegistry().registerSystem(new InfiniteFluidSystem(config));
    }
}
