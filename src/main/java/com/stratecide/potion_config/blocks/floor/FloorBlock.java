package com.stratecide.potion_config.blocks.floor;

import com.stratecide.potion_config.CustomPotion;
import com.stratecide.potion_config.PotionConfigMod;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.registry.Registries;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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

    @Override
    public List<ItemStack> getDroppedStacks(BlockState state, LootContextParameterSet.Builder builder) {
        if (PotionConfigMod.BLOCKS_DROP_SELF)
            return List.of(new ItemStack(Registries.ITEM.get(Registries.BLOCK.getId(this))));
        return super.getDroppedStacks(state, builder);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        Potion potion = Registries.POTION.get(getPotion().potionId);
        ItemStack itemStack = new ItemStack(PotionConfigMod.CRAFTING_POTION);
        PotionUtil.setPotion(itemStack, potion);
        PotionUtil.buildTooltip(itemStack, tooltip, 1.0f);
    }
}
