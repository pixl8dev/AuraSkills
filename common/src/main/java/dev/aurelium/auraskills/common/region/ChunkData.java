package dev.aurelium.auraskills.common.region;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkData {

    private final Region region;
    private final byte x;
    private final byte z;
    private final Set<BlockPosition> placedBlocks;

    public ChunkData(Region region, byte x, byte z) {
        this.region = region;
        this.x = x;
        this.z = z;
        this.placedBlocks = ConcurrentHashMap.newKeySet();
    }

    public Region getRegion() {
        return region;
    }

    public byte getX() {
        return x;
    }

    public byte getZ() {
        return z;
    }

    public boolean isPlacedBlock(BlockPosition blockPosition) {
        return placedBlocks.contains(blockPosition);
    }

    public Set<BlockPosition> getPlacedBlocks() {
        return placedBlocks;
    }

    public void addPlacedBlock(BlockPosition blockPosition) {
        this.placedBlocks.add(blockPosition);
    }

    public void removePlacedBlock(BlockPosition blockPosition) {
        this.placedBlocks.remove(blockPosition);
    }

}
