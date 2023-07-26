package com.stratecide.potion_config.effects;

import com.stratecide.potion_config.PotionColor;
import com.stratecide.potion_config.PotionConfigMod;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Particles extends CustomStatusEffect {
    private static int ID = 0;
    public final PotionColor color;
    public final int id;

    public static Identifier generateIdentifier(long index) {
        return new Identifier(PotionConfigMod.MOD_ID, "particles" + index);
    }

    protected Particles(StatusEffectCategory statusEffectCategory, int id, PotionColor color) {
        super(statusEffectCategory, 0);
        this.id = id;
        this.color = color;
    }

    public Particles withColor(PotionColor color) {
        int id = ++ID;
        return Registry.register(Registry.STATUS_EFFECT, generateIdentifier(id), new Particles(this.getCategory(), id, color));
    }

    public void tick(LivingEntity entity, int intensity) {
        double particlesPerSecond = 2 + intensity;
        if (entity.isInvisible())
            particlesPerSecond /= 4.0;
        while (particlesPerSecond >= 20.0 || particlesPerSecond > 0 && entity.getRandom().nextDouble() < particlesPerSecond / 20.0) {
            particlesPerSecond -= 20.0;
            int color = this.color.getColor(false);
            double red = (double) (color >> 16 & 0xFF) / 255.0;
            double green = (double) (color >> 8 & 0xFF) / 255.0;
            double blue = (double) (color & 0xFF) / 255.0;
            entity.getWorld().addParticle(ParticleTypes.ENTITY_EFFECT, entity.getParticleX(0.5), entity.getRandomBodyY(), entity.getParticleZ(0.5), red, green, blue);
        }
    }
}
