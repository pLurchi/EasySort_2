package me.lucacicada.spigot.vanillasort;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public class WorldAreaBlockIterator implements Iterator<Block> {

    @Nonnull
    private final World world;
    private final int minX, maxX, minY, maxY, minZ, maxZ;
    private final Predicate<Block> predicate;

    private int currentX, currentY, currentZ;
    private Block nextBlock;

    public WorldAreaBlockIterator(@Nonnull World world, @Nonnull Location location, int radius) {
        this(
                world,
                location,
                radius,
                radius,
                radius,
                null
        );
    }

    public WorldAreaBlockIterator(@Nonnull World world, @Nonnull Location location, int radius, Predicate<Block> predicate) {
        this(
                world,
                location,
                radius,
                radius,
                radius,
                predicate
        );
    }

    public WorldAreaBlockIterator(@Nonnull World world, @Nonnull Location location, int radiusX, int radiusY, int radiusZ) {
        this(
                world,
                location,
                radiusX,
                radiusY,
                radiusZ,
                null
        );
    }

    public WorldAreaBlockIterator(@Nonnull World world, @Nonnull Location location, int radiusX, int radiusY, int radiusZ, Predicate<Block> predicate) {
        this(
                world,
                location.getBlockX() - radiusX,
                location.getBlockY() - radiusY,
                location.getBlockZ() - radiusZ,
                location.getBlockX() + radiusX,
                location.getBlockY() + radiusY,
                location.getBlockZ() + radiusZ,
                predicate
        );
    }

    public WorldAreaBlockIterator(@Nonnull World world, @Nonnull Location start, @Nonnull Location end) {
        this(world, start, end, null);
    }

    public WorldAreaBlockIterator(@Nonnull World world, @Nonnull Location start, @Nonnull Location end, Predicate<Block> predicate) {
        this(
                world,
                start.getBlockX(),
                start.getBlockY(),
                start.getBlockZ(),
                end.getBlockX(),
                end.getBlockY(),
                end.getBlockZ(),
                predicate
        );
    }

    public WorldAreaBlockIterator(@Nonnull World world, int x1, int y1, int z1, int x2, int y2, int z2) {
        this(world, x1, y1, z1, x2, y2, z2, null);
    }

    public WorldAreaBlockIterator(@Nonnull World world, int x1, int y1, int z1, int x2, int y2, int z2, Predicate<Block> predicate) {
        this.world = world;

        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.minZ = Math.min(z1, z2);
        this.maxX = Math.max(x1, x2);
        this.maxY = Math.max(y1, y2);
        this.maxZ = Math.max(z1, z2);
        this.currentX = minX;
        this.currentY = minY;
        this.currentZ = minZ;
        this.predicate = predicate;

        findNext();
    }

    private void findNext() {
        nextBlock = null;
        while (currentX <= maxX) {
            while (currentY <= maxY) {
                while (currentZ <= maxZ) {
                    Block block = world.getBlockAt(currentX, currentY, currentZ);
                    currentZ++;
                    if (predicate == null || predicate.test(block)) {
                        nextBlock = block;
                        return;
                    }
                }
                currentZ = minZ;
                currentY++;
            }
            currentY = minY;
            currentX++;
        }
    }

    @Override
    public boolean hasNext() {
        return nextBlock != null;
    }

    @Override
    @Nonnull
    public Block next() {
        if (nextBlock == null) {
            throw new NoSuchElementException();
        }
        Block result = nextBlock;
        findNext();
        return result;
    }
}
