package com.stratecide.potion_config.blocks.portal;

import com.stratecide.potion_config.CustomPotion;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class PortalBlock extends PillarBlock {

    protected static final VoxelShape X_SHAPE = Block.createCuboidShape(6.0, 0.0, 0.0, 10.0, 16.0, 16.0);
    protected static final VoxelShape Y_SHAPE = Block.createCuboidShape(0.0, 6.0, 0.0, 16.0, 10.0, 16.0);
    protected static final VoxelShape Z_SHAPE = Block.createCuboidShape(0.0, 0.0, 6.0, 16.0, 16.0, 10.0);

    protected final ParticleEffect particle;
    protected final CustomPotion potion;

    public PortalBlock(ParticleEffect particle, CustomPotion potion) {
        super(FabricBlockSettings.of(Material.GLASS).nonOpaque().noCollision().sounds(BlockSoundGroup.GLASS));
        this.particle = particle;
        this.potion = potion;
    }

    public CustomPotion getPotion() {
        return potion;
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        double x = (double)pos.getX() + 0.5;
        double y = (double)pos.getY() + 0.5;
        double z = (double)pos.getZ() + 0.5;
        if (state.get(AXIS) != Direction.Axis.X)
            x += Math.random() - 0.5;
        if (state.get(AXIS) != Direction.Axis.Y)
            y += Math.random() - 0.5;
        if (state.get(AXIS) != Direction.Axis.Z)
            z += Math.random() - 0.5;
        int color = potion.getColor(false);
        double r = (double)(color >> 16 & 0xFF) / 255.0;
        double g = (double)(color >> 8 & 0xFF) / 255.0;
        double b = (double)(color >> 0 & 0xFF) / 255.0;
        world.addParticle(this.particle, x, y, z, r, g, b);
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = ((LivingEntity) entity);
            for (StatusEffectInstance statusEffectInstance : getPotion().generateEffectInstances()) {
                livingEntity.addStatusEffect(statusEffectInstance);
            }
        }
    }

    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(AXIS)) {
            case X -> X_SHAPE;
            case Y -> Y_SHAPE;
            case Z -> Z_SHAPE;
        };
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(AXIS, ctx.getPlayerLookDirection().getAxis());
    }
}
