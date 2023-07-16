package com.stratecide.potion_config.effects;

import com.stratecide.potion_config.CustomPotion;
import com.stratecide.potion_config.PotionConfigMod;
import net.minecraft.entity.effect.StatusEffectCategory;

public class AfterEffect extends CustomStatusEffect {

    private String potionId;
    //private Entity source;

    public AfterEffect(String potionId) {
        super(StatusEffectCategory.NEUTRAL, 0x00000000);
        this.potionId = potionId;
    }

    public CustomPotion getPotion() {
        return PotionConfigMod.getCustomPotion(potionId);
    }

}
