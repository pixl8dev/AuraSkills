package dev.aurelium.auraskills.bukkit.region;

import dev.aurelium.auraskills.api.source.SkillSource;
import dev.aurelium.auraskills.api.source.type.BlockXpSource;
import dev.aurelium.auraskills.bukkit.AuraSkills;
import dev.aurelium.auraskills.bukkit.source.BlockLeveler;
import dev.aurelium.auraskills.common.region.*;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BukkitRegionManager extends RegionManager {

    private final AuraSkills plugin;
    private final Set<RegionCoordinate> pendingRegionLoads;
    @Nullable
    private BlockLeveler blockLeveler; // Lazy initialized in handleBlockPlace

    public BukkitRegionManager(AuraSkills plugin) {
        super(plugin);
        this.plugin = plugin;
        this.pendingRegionLoads = ConcurrentHashMap.newKeySet();
    }

    public boolean isPlacedBlock(Block block) {
        int chunkX = block.getChunk().getX();
        int chunkZ = block.getChunk().getZ();

        int regionX = (int) Math.floor((double) chunkX / 32.0);
        int regionZ = (int) Math.floor((double) chunkZ / 32.0);

        Region region = regions.get(new RegionCoordinate(block.getWorld().getName(), regionX, regionZ));
        if (region != null) {
            byte regionChunkX = (byte) (chunkX - regionX * 32);
            byte regionChunkZ = (byte) (chunkZ - regionZ * 32);
            ChunkData chunkData = region.getChunkData(new ChunkCoordinate(regionChunkX, regionChunkZ));
            if (chunkData != null) {
                BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
                return chunkData.isPlacedBlock(blockPosition);
            }
        }
        return false;
    }

    public void handleBlockPlace(Block block) {
        // Lazy initialize BlockLeveler
        if (blockLeveler == null) {
            blockLeveler = plugin.getLevelManager().getLeveler(BlockLeveler.class);
        }

        SkillSource<BlockXpSource> skillSource = blockLeveler.getSource(block, BlockXpSource.BlockTriggers.BREAK);

        if (skillSource == null) { // Not a source
            return;
        }

        BlockXpSource source = skillSource.source();

        if (!source.checkReplace()) { // Check source option
            return;
        }

        addPlacedBlock(block);
    }

    public void addPlacedBlock(Block block) {
        RegionCoordinate regionCoordinate = getRegionCoordinate(block);
        Region region = regions.computeIfAbsent(regionCoordinate, ignored ->
                new Region(block.getWorld().getName(), regionCoordinate.getX(), regionCoordinate.getZ()));

        addToRegion(block, region);

        if (!region.isLoaded() || region.shouldReload()) {
            loadRegionAsync(regionCoordinate, region);
        }
    }

    public void loadRegionAsync(String worldName, int regionX, int regionZ) {
        RegionCoordinate regionCoordinate = new RegionCoordinate(worldName, regionX, regionZ);
        Region region = regions.computeIfAbsent(regionCoordinate, ignored -> new Region(worldName, regionX, regionZ));
        loadRegionAsync(regionCoordinate, region);
    }

    private void loadRegionAsync(RegionCoordinate regionCoordinate, Region region) {
        if (!pendingRegionLoads.add(regionCoordinate)) {
            return;
        }
        plugin.getScheduler().executeAsync(() -> {
            try {
                loadRegion(region);
            } finally {
                pendingRegionLoads.remove(regionCoordinate);
            }
        });
    }

    private void addToRegion(Block block, Region region) {
        byte regionChunkX = (byte) (block.getChunk().getX() - region.getX() * 32);
        byte regionChunkZ = (byte) (block.getChunk().getZ() - region.getZ() * 32);
        ChunkData chunkData = region.getChunkData(new ChunkCoordinate(regionChunkX, regionChunkZ));
        // Create chunk data if it does not exist
        if (chunkData == null) {
            chunkData = new ChunkData(region, regionChunkX, regionChunkZ);
            region.setChunkData(new ChunkCoordinate(regionChunkX, regionChunkZ), chunkData);
        }
        chunkData.addPlacedBlock(new BlockPosition(block.getX(), block.getY(), block.getZ()));
    }

    public void removePlacedBlock(Block block) {
        Region region = getRegionFromBlock(block);
        if (region != null) {
            byte regionChunkX = (byte) (block.getChunk().getX() - region.getX() * 32);
            byte regionChunkZ = (byte) (block.getChunk().getZ() - region.getZ() * 32);
            ChunkCoordinate chunkCoordinate = new ChunkCoordinate(regionChunkX, regionChunkZ);
            ChunkData chunkData = region.getChunkData(chunkCoordinate);
            if (chunkData != null) {
                chunkData.removePlacedBlock(new BlockPosition(block.getX(), block.getY(), block.getZ()));
                if (chunkData.getPlacedBlocks().isEmpty()) {
                    region.removeChunkData(chunkCoordinate, chunkData);
                }
            }
        }
    }

    @Nullable
    private Region getRegionFromBlock(Block block) {
        return regions.get(getRegionCoordinate(block));
    }

    private RegionCoordinate getRegionCoordinate(Block block) {
        int chunkX = block.getChunk().getX();
        int chunkZ = block.getChunk().getZ();

        int regionX = (int) Math.floor((double) chunkX / 32.0);
        int regionZ = (int) Math.floor((double) chunkZ / 32.0);

        return new RegionCoordinate(block.getWorld().getName(), regionX, regionZ);
    }

    @Override
    public boolean isChunkLoaded(String worldName, int chunkX, int chunkZ) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return false;
        }
        return world.isChunkLoaded(chunkX, chunkZ);
    }

}
