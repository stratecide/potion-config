package com.stratecide.potion_config.mixin;

import com.stratecide.potion_config.PotionConfigMod;
import com.stratecide.potion_config.effects.CustomStatusEffect;
import net.minecraft.client.Mouse;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.GlfwUtil;
import net.minecraft.util.math.Vec2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(Mouse.class)
public class MouseMixin {
    @Shadow private double lastMouseUpdateTime;
    @Unique
    private double previousMouseUpdateTime = Double.MIN_VALUE;
    @Unique
    private double strength = 0;
    @Unique
    private double progress = 0;
    @Unique
    private List<Vec2f> points = new ArrayList<>();
    @Unique
    private double speed = 0;
    @Unique
    private double targetSpeed = 0;

    @Inject(method = "updateMouse", at = @At("HEAD"))
    private void updateTime(CallbackInfo ci) {
        previousMouseUpdateTime = lastMouseUpdateTime;
    }

    @Redirect(method = "updateMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"))
    private void moveScreenWhenDrunk(ClientPlayerEntity player, double cursorDeltaX, double cursorDeltaY) {
        double time_delta = Math.min(0.5, this.lastMouseUpdateTime - previousMouseUpdateTime);
        if (player.hasStatusEffect(CustomStatusEffect.DRUNK) || speed > 0) {
            if (player.hasStatusEffect(CustomStatusEffect.DRUNK)) {
                strength = player.getStatusEffect(CustomStatusEffect.DRUNK).getAmplifier() + 2;
            } else {
                targetSpeed = 0;
            }
            addPoints(points);
            double acceleration = time_delta * 3;
            if (Math.abs(targetSpeed - speed) < acceleration) {
                speed = targetSpeed;
            } else {
                speed += acceleration * Math.signum(targetSpeed - speed);
            }
            Vec2f previous = bezierFromZero(points, progress);
            progress += time_delta * speed;
            if (progress >= 1) {
                progress -= 1;
                Vec2f removed = points.remove(0);
                Vec2f retained = points.remove(0);
                previous = previous.add(retained.negate());
                points.add(retained.add(removed.negate()));
                addPoints(points);
            }
            Vec2f delta = bezierFromZero(points, progress).add(previous.negate());
            cursorDeltaX += delta.x;
            cursorDeltaY += delta.y;
        } else while (points.size() > 0) {
            progress = 0;
            points.remove(0);
        }
        player.changeLookDirection(cursorDeltaX, cursorDeltaY);
    }

    private void addPoints(List<Vec2f> points) {
        while (points.size() < 2) {
            double dx = (Math.random() - 0.5) * 400;
            double dy = (Math.random() - 0.5) * 100;
            if (points.size() > 0) {
                dx += points.get(points.size() - 1).x;
                dy += points.get(points.size() - 1).y;
            }
            points.add(new Vec2f((float) dx, (float) dy));
            targetSpeed = strength * (Math.random() + 1) / 5.0;
        }
    }

    private static Vec2f bezierFromZero(List<Vec2f> points, double progress) {
        return points.get(0).multiply((float) (2 * (1 - progress) * progress))
            .add(points.get(1).multiply((float) (progress * progress)));
    }
}
