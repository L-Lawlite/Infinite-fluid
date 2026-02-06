package net.lawliet.infinitefluidplugin;

import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.builtin.blocktick.system.ChunkBlockTickSystem;
import com.hypixel.hytale.builtin.fluid.FluidSystems;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.asset.type.blocktick.BlockTickStrategy;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.asset.type.fluid.FluidTicker;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.ChunkSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.FluidSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.util.Config;
import net.lawliet.infinitefluidplugin.config.FluidConfig;
import net.lawliet.infinitefluidplugin.config.InfiniteFluidConfig;
import net.lawliet.infinitefluidplugin.data.FluidData;
import net.lawliet.infinitefluidplugin.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class InfiniteFluidSystem extends EntityTickingSystem<ChunkStore> {
    private static final Query<ChunkStore> QUERY = Query.and(FluidSection.getComponentType(), ChunkSection.getComponentType(), BlockSection.getComponentType());
    private static final Set<Dependency<ChunkStore>> DEPENDENCIES = Set.of(
            new SystemDependency<>(Order.AFTER, FluidSystems.Ticking.class),
            new SystemDependency<>(Order.BEFORE, ChunkBlockTickSystem.Ticking.class)
    );
    private static final int[][] OFFSET = new int[][]{{-1,0},{1,0},{0,-1},{0,1}};

    private static Config<InfiniteFluidConfig> fluidConfig;

    public InfiniteFluidSystem(Config<InfiniteFluidConfig> config) {
        fluidConfig = config;
    }

    @Override
    public void tick(float v, int i, @NotNull ArchetypeChunk<ChunkStore> archetypeChunk, @NotNull Store<ChunkStore> store, @NotNull CommandBuffer<ChunkStore> commandBuffer) {
        FluidSection fluidSection = archetypeChunk.getComponent(i, FluidSection.getComponentType());
        if (fluidSection == null || fluidSection.isEmpty()) return;
        BlockSection blockSection = archetypeChunk.getComponent(i, BlockSection.getComponentType());
        if(blockSection == null || blockSection.getTickingBlocksCount() == 0) return;
        // config setup
        getAllFluidKeys().forEach((k, _) -> {
            if (!k.endsWith("_Source")) return;
            String fluidName = StringUtil.removeSuffixIfExists(k, "_Source");
            FluidConfig currentFluidConfig = fluidConfig.get().getFluidConfig(fluidName);
            if (!currentFluidConfig.isEnabled()) return;
            Optional<FluidData> fluidData = initFluidData(fluidName);
            if (fluidData.isEmpty()) return;
            FluidTicker.CachedAccessor accessor = FluidTicker.CachedAccessor.of(commandBuffer, fluidSection, blockSection, 1);
            int sectionBlockX = fluidSection.getX() * 32;
            int sectionBlockZ = fluidSection.getZ() * 32;
            blockSection.forEachTicking(accessor, fluidSection, fluidSection.getY(), (acc, fs, localX, worldY, localZ, blockId) -> this.processFluid(acc, fs, blockSection, sectionBlockX + localX, worldY, sectionBlockZ + localZ, fluidData.get()));
        });

    }

    private BlockTickStrategy processFluid(FluidTicker.CachedAccessor accessor, FluidSection fluidSection, BlockSection blockSection, int x, int y, int z, FluidData fluidData) {
        int fluidId = fluidSection.getFluidId(x,y,z);
        if(fluidId == fluidData.sourceId) {
            this.processFluidSource(accessor, fluidSection, blockSection, x, y, z, fluidData);
            return BlockTickStrategy.IGNORED;
        }
        if (fluidId != fluidData.flowingId) {
            return BlockTickStrategy.IGNORED;
        }
        int neighbourCount = this.countFluidNeighbours(accessor, fluidSection, x, y, z, fluidData);
        if (neighbourCount < 2) return  BlockTickStrategy.IGNORED;
        fluidSection.setFluid(x, y, z, fluidData.sourceId, fluidData.sourceLevel);
        FluidTicker.setTickingSurrounding(accessor, blockSection, x, y, z);
        return BlockTickStrategy.SLEEP;
    }

    private int countFluidNeighbours(FluidTicker.CachedAccessor accessor, FluidSection currentSection, int x, int y, int z, FluidData fluidData) {
        int     count = 0;
        for (int[] offset : OFFSET) {
            int neighbourX = x + offset[0];
            int neighbourZ = z + offset[1];
            boolean inSameSection = ChunkUtil.isSameChunkSection(x, y, z, neighbourX, y, neighbourZ);
            FluidSection neighbourFluidSection = inSameSection ? currentSection : accessor.getFluidSectionByBlock(neighbourX, y, neighbourZ);
            if (neighbourFluidSection == null) continue;
            int fluidId = neighbourFluidSection.getFluidId(neighbourX, y, neighbourZ);
            if (fluidId == fluidData.sourceId) count++;
        }
        return count;
    }

    private void processFluidSource(FluidTicker.CachedAccessor accessor, FluidSection currentSection, BlockSection blockSection, int x, int y, int z, FluidData fluidData) {
        for (int[] offset : OFFSET) {
            int neighbourX = x + offset[0];
            int neighbourZ = z + offset[1];
            boolean inSameSection = ChunkUtil.isSameChunkSection(x, y, z, neighbourX, y, neighbourZ);
            FluidSection neighbourFluidSection = inSameSection ? currentSection : accessor.getFluidSectionByBlock(neighbourX, y, neighbourZ);
            if (neighbourFluidSection == null) continue;
            int fluidId = neighbourFluidSection.getFluidId(neighbourX, y, neighbourZ);
            if (fluidId != fluidData.flowingId) continue;
            int sourceCount = this.countFluidNeighbours(accessor, neighbourFluidSection, neighbourX, y, neighbourZ, fluidData);
            if (sourceCount < 2) continue;
            neighbourFluidSection.setFluid(neighbourX, y, neighbourZ, fluidData.sourceId, fluidData.sourceLevel);
            BlockSection neighbourBlockSection = inSameSection ? blockSection : accessor.getBlockSection(neighbourX, y, neighbourZ);
            if (neighbourBlockSection == null) continue;
            FluidTicker.setTickingSurrounding(accessor, neighbourBlockSection, neighbourX, y, neighbourZ);
        }
    }

    private Optional<FluidData> initFluidData(String fluidKey) {
        String sourceKey = fluidKey + "_Source";
        FluidData fluidData = new FluidData(sourceKey, fluidKey);
        if(fluidData.isValid()) return Optional.of(fluidData);
        return Optional.empty();
    }

    private static Map<String, Fluid> getAllFluidKeys() {
        IndexedLookupTableAssetMap<String, Fluid> fluidMap = Fluid.getAssetMap();
        return fluidMap.getAssetMap();
    }

    @Override
    public @Nullable Query<ChunkStore> getQuery() {
        return QUERY;
    }


    public static Set<Dependency<ChunkStore>> getDEPENDENCIES() {
        return DEPENDENCIES;
    }
}
