package com.stratecide.potion_config.blocks;

import com.stratecide.potion_config.CustomPotion;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class FloorBlock extends Block {
    protected final ParticleEffect particle;
    protected final CustomPotion potion;


    public FloorBlock(ParticleEffect particle, CustomPotion potion) {
        super(FabricBlockSettings.copyOf(Blocks.WARPED_STEM));
        this.particle = particle;
        this.potion = potion;
    }

    public CustomPotion getPotion() {
        return potion;
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        double x = (double)pos.getX() + Math.random();
        double y = (double)pos.getY() + 1.0;
        double z = (double)pos.getZ() + Math.random();
        int color = potion.getColor(false);
        double r = (double)(color >> 16 & 0xFF) / 255.0;
        double g = (double)(color >> 8 & 0xFF) / 255.0;
        double b = (double)(color >> 0 & 0xFF) / 255.0;
        world.addParticle(this.particle, x, y, z, r, g, b);
    }
}
