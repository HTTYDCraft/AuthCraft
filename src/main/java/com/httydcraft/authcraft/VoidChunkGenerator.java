package com.httydcraft.authcraft;

import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

import java.util.Random;

/**
 * Custom chunk generator that creates an empty void world for the limbo environment.
 *
 * @author HttyDCraft
 * @version 1.0.2
 */
public class VoidChunkGenerator extends ChunkGenerator {
    /**
     * Generates an empty chunk for the limbo world.
     *
     * @param world The world being generated.
     * @param random The random instance.
     * @param x The chunk X coordinate.
     * @param z The chunk Z coordinate.
     * @param biome The biome grid.
     * @return The generated chunk data.
     */
    @Override
    public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) {
        return createChunkData(world);
    }
}