package com.stratecide.potion_config;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Lifecycle;
import net.fabricmc.api.ModInitializer;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.event.GameEvent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class PotionConfigMod implements ModInitializer {

	public static final String MOD_ID = "potion-config";

	public static final Map<String, Integer> FUELS;
	public static final Map<String, Potion> WITCH_POTIONS = new HashMap<>();
	private static final Set<String> WITCH_POTION_IDS = ImmutableSet.of("normal-fire_resistance", "normal-water_breathing", "normal-healing", "normal-swiftness", "splash-harming", "splash-healing", "splash-poison", "splash-regeneration", "splash-slowness", "splash-weakness");
	public static final Potion WANDERING_TRADER_POTION;
	public static final Map<Potion, Identifier> MOD_COMPAT = new HashMap<>();

	public static int TOOLTIP_MILLISECONDS = 2000;

	public static final Potion WATER_POTION;
	public static final Set<Potion> NORMAL_POTIONS = new HashSet<>();
	public static final Set<Potion> SPLASH_POTIONS = new HashSet<>();
	public static final Set<Potion> LINGERING_POTIONS = new HashSet<>();
	public static final Set<Potion> ARROW_POTIONS = new HashSet<>();

	public static final JsonObject config;

	static  {
		config = loadConfig();

		for (Iterator<JsonElement> it = config.get("potions").getAsJsonArray().iterator(); it.hasNext(); ) {
			JsonObject entry = it.next().getAsJsonObject();
			String potionId = entry.get("id").getAsString();
			if (!entry.has("duration") && !entry.has("splash") && !entry.has("lingering") && !entry.has("arrow")) {
				throw new AssertionError("no duration was given for potion: " + potionId);
			}
			List<StatusEffect> effects = new ArrayList<>();
			List<Integer> amplifiers = new ArrayList<>();
			List<Double> normalDurations = new ArrayList<>();
			List<Double> splashDurations = new ArrayList<>();
			List<Double> lingeringDurations = new ArrayList<>();
			List<Double> arrowDurations = new ArrayList<>();
			for (Iterator<JsonElement> it2 = entry.getAsJsonArray("effects").iterator(); it2.hasNext(); ) {
				JsonObject effect = it2.next().getAsJsonObject();
				int amplifier = 0;
				if (effect.has("amplifier"))
					amplifier = Math.max(0, effect.get("amplifier").getAsInt());
				StatusEffect statusEffect = Registry.STATUS_EFFECT.get(new Identifier(effect.get("effect").getAsString()));
				if (statusEffect == null)
					throw new NullPointerException("Status Effect not found for ID '" + effect.get("effect").getAsString() + "'");
				effects.add(statusEffect);
				amplifiers.add(amplifier);
				if (entry.has("duration"))
					normalDurations.add(effect.has("duration") ? effect.get("duration").getAsDouble() : entry.get("duration").getAsDouble());
				if (entry.has("splash"))
					splashDurations.add(effect.has("splash") ? effect.get("splash").getAsDouble() : entry.get("splash").getAsDouble());
				if (entry.has("lingering"))
					lingeringDurations.add(effect.has("lingering") ? effect.get("lingering").getAsDouble() : entry.get("lingering").getAsDouble());
				if (entry.has("arrow"))
					arrowDurations.add(effect.has("arrow") ? effect.get("arrow").getAsDouble() : entry.get("arrow").getAsDouble());
			}
			if (entry.has("duration"))
				NORMAL_POTIONS.add(registerPotion("normal-" + potionId,  effects, amplifiers, normalDurations));
			if (entry.has("splash"))
				SPLASH_POTIONS.add(registerSplashPotion(potionId, effects, amplifiers, splashDurations));
			if (entry.has("lingering"))
				LINGERING_POTIONS.add(registerLingeringPotion(potionId, effects, amplifiers, lingeringDurations));
			if (entry.has("arrow"))
				ARROW_POTIONS.add(registerTippedArrow(potionId, effects, amplifiers, arrowDurations));
		}

		Potion waterPotion = Registry.POTION.get(new Identifier(MOD_ID, "normal-water"));
		if (waterPotion == Potions.EMPTY)
			NORMAL_POTIONS.add(waterPotion = Registry.POTION.add(RegistryKey.of(Registry.POTION_KEY, new Identifier(MOD_ID, "normal-water")), new Potion(), Lifecycle.stable()));
		WATER_POTION = waterPotion;

		if (config.has("fuel") && config.get("fuel").isJsonObject()) {
			FUELS = new HashMap<>();
		} else {
			FUELS = null;
		}
		JsonObject witch = config.get("witch").getAsJsonObject();
		for (String id : WITCH_POTION_IDS) {
			Potion potion = Registry.POTION.get(new Identifier(MOD_ID, witch.get(id).getAsString()));
			if (potion == Potions.EMPTY)
				throw new NullPointerException("Potion not found for witch potion '" + id + "'");
			WITCH_POTIONS.put(id, potion);
		}
		WANDERING_TRADER_POTION = Registry.POTION.get(new Identifier(MOD_ID, config.get("wandering_trader_night").getAsString()));
		if (WANDERING_TRADER_POTION == Potions.EMPTY)
			throw new NullPointerException("Potion not found for wandering trader");
	}

	private static StatusEffectInstance[] createStatusEffects(List<StatusEffect> effects, List<Integer> amplifiers, List<Double> durations) {
		assert effects.size() == amplifiers.size();
		StatusEffectInstance[] result = new StatusEffectInstance[effects.size()];
		for (int i = 0; i < result.length; i++) {
			StatusEffect effect = effects.get(i);
			result[i] = new StatusEffectInstance(effect, effect.isInstant() ? 1 : Math.max(1, (int) Math.round(durations.get(i) * 20)), amplifiers.get(i));
		}
		return result;
	}

	private static Potion registerPotion(String id, List<StatusEffect> effects, List<Integer> amplifiers, List<Double> durations) {
		Potion potion = new Potion(createStatusEffects(effects, amplifiers, durations));
		return Registry.POTION.add(RegistryKey.of(Registry.POTION_KEY, new Identifier(MOD_ID, id)), potion, Lifecycle.stable());
	}
	private static Potion registerSplashPotion(String id, List<StatusEffect> effects, List<Integer> amplifiers, List<Double> durations) {
		return registerPotion("splash-" + id, effects, amplifiers, durations);
	}
	private static Potion registerLingeringPotion(String id, List<StatusEffect> effects, List<Integer> amplifiers, List<Double> durations) {
		return registerPotion("lingering-" + id, effects, amplifiers, durations);
	}
	private static Potion registerTippedArrow(String id, List<StatusEffect> effects, List<Integer> amplifiers, List<Double> durations) {
		return registerPotion("arrow-" + id, effects, amplifiers, durations);
	}

	@Override
	public void onInitialize() {
		if (FUELS != null) {
			JsonObject fuel = config.get("fuel").getAsJsonObject();
			for (Entry<String, JsonElement> entry : fuel.entrySet()) {
				if (Registry.ITEM.get(new Identifier(entry.getKey())) == Items.AIR)
					throw new AssertionError("no Item found in registry for fuel type '" + entry.getKey() + "'");
				if (entry.getValue().getAsInt() > 0)
					FUELS.put(entry.getKey(), entry.getValue().getAsInt());
			}
		}

		if (!config.has("overwriteCauldron")) {
			CauldronBehavior emptyBehavior = CauldronBehavior.EMPTY_CAULDRON_BEHAVIOR.get(Items.POTION);
			CauldronBehavior.EMPTY_CAULDRON_BEHAVIOR.put(Items.POTION, (state, world, pos, player, hand, stack) -> {
				if (PotionUtil.getPotion(stack) != WATER_POTION) {
					return emptyBehavior.interact(state, world, pos, player, hand, stack);
				} else {
					if (!world.isClient) {
						Item item = stack.getItem();
						player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
						player.incrementStat(Stats.USE_CAULDRON);
						player.incrementStat(Stats.USED.getOrCreateStat(item));
						world.setBlockState(pos, Blocks.WATER_CAULDRON.getDefaultState());
						world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
						world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos);
					}

					return ActionResult.success(world.isClient);
				}
			});
			CauldronBehavior waterBehavior = CauldronBehavior.WATER_CAULDRON_BEHAVIOR.get(Items.POTION);
			CauldronBehavior.WATER_CAULDRON_BEHAVIOR.put(Items.POTION, (state, world, pos, player, hand, stack) -> {
				if (state.get(LeveledCauldronBlock.LEVEL) != 3 && PotionUtil.getPotion(stack) == WATER_POTION) {
					if (!world.isClient) {
						player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
						player.incrementStat(Stats.USE_CAULDRON);
						player.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
						world.setBlockState(pos, state.cycle(LeveledCauldronBlock.LEVEL));
						world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
						world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos);
					}

					return ActionResult.success(world.isClient);
				} else {
					return waterBehavior.interact(state, world, pos, player, hand, stack);
				}
			});
		}

		for (Identifier identifier : MOD_COMPAT.values()) {
			if (Registry.POTION.get(identifier) == Potions.EMPTY)
				throw new AssertionError("no Item found in registry for fuel type '" + identifier.getPath() + "'");
		}
	}

	public static void replaceModdedPotion(Identifier originalId, Potion moddedPotion) {
		if (!config.has("replace") || !config.get("replace").getAsJsonObject().has(originalId.toString())) {
			System.out.println("(Potion-Config) WARNING: no replacement configured for modded Potion: " + originalId);
			return;
		}
		String replacementId = config.get("replace").getAsJsonObject().get(originalId.toString()).getAsString();
		Identifier replacement = new Identifier(MOD_ID, replacementId);
		MOD_COMPAT.put(moddedPotion, replacement);
	}

	private static JsonObject loadConfig() {
		File file = new File(CONFIG_FILE);
		String data;
		if (!file.exists()) {
			data = DEFAULT_CONFIG;
			try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
				writer.write(data);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
			try (Scanner scanner = new Scanner(file)) {
				StringBuilder builder = new StringBuilder();
				while (scanner.hasNextLine())
					builder.append(scanner.nextLine());
				data = builder.toString();
			}
			catch (FileNotFoundException e) {
				e.printStackTrace();
				data = DEFAULT_CONFIG;
			}
		}
		return new JsonParser().parse(data).getAsJsonObject();
	}

	private static final String CONFIG_FILE = "config/potion-config.json";
	private static final String DEFAULT_CONFIG = """
{
	"potions": [
		{
			"id": "water",
			"duration": 0,
			"splash": 0,
			"lingering": 0,
			"arrow": 0,
			"effects": []
		},
		{
			"id": "mundane",
			"duration": 0,
			"splash": 0,
			"lingering": 0,
			"arrow": 0,
			"effects": []
		},
		{
			"id": "thick",
			"duration": 0,
			"splash": 0,
			"lingering": 0,
			"arrow": 0,
			"effects": []
		},
		{
			"id": "awkward",
			"duration": 0,
			"splash": 0,
			"lingering": 0,
			"arrow": 0,
			"effects": []
		},
		{
			"id": "night_vision",
			"duration": 180,
			"splash": 180,
			"lingering": 45,
			"arrow": 22.5,
			"effects": [
				{
					"effect": "minecraft:night_vision"
				}
			]
		},
		{
			"id": "long_night_vision",
			"duration": 480,
			"splash": 480,
			"lingering": 120,
			"arrow": 60,
			"effects": [
				{
					"effect": "minecraft:night_vision"
				}
			]
		},
		{
			"id": "invisibility",
			"duration": 180,
			"splash": 180,
			"lingering": 45,
			"arrow": 22.5,
			"effects": [
				{
					"effect": "minecraft:invisibility"
				}
			]
		},
		{
			"id": "long_invisibility",
			"duration": 480,
			"splash": 480,
			"lingering": 120,
			"arrow": 60,
			"effects": [
				{
					"effect": "minecraft:invisibility"
				}
			]
		},
		{
			"id": "leaping",
			"duration": 180,
			"splash": 180,
			"lingering": 45,
			"arrow": 22.5,
			"effects": [
				{
					"effect": "minecraft:jump_boost"
				}
			]
		},
		{
			"id": "long_leaping",
			"duration": 480,
			"splash": 480,
			"lingering": 120,
			"arrow": 60,
			"effects": [
				{
					"effect": "minecraft:jump_boost"
				}
			]
		},
		{
			"id": "strong_leaping",
			"duration": 90,
			"splash": 90,
			"lingering": 22.5,
			"arrow": 11.25,
			"effects": [
				{
					"effect": "minecraft:jump_boost",
					"amplifier": 1
				}
			]
		},
		{
			"id": "fire_resistance",
			"duration": 180,
			"splash": 180,
			"lingering": 45,
			"arrow": 22.5,
			"effects": [
				{
					"effect": "minecraft:fire_resistance"
				}
			]
		},
		{
			"id": "long_fire_resistance",
			"duration": 480,
			"splash": 480,
			"lingering": 120,
			"arrow": 60,
			"effects": [
				{
					"effect": "minecraft:fire_resistance"
				}
			]
		},
		{
			"id": "swiftness",
			"duration": 180,
			"splash": 180,
			"lingering": 45,
			"arrow": 22.5,
			"effects": [
				{
					"effect": "minecraft:speed"
				}
			]
		},
		{
			"id": "long_swiftness",
			"duration": 480,
			"splash": 480,
			"lingering": 120,
			"arrow": 60,
			"effects": [
				{
					"effect": "minecraft:speed"
				}
			]
		},
		{
			"id": "strong_swiftness",
			"duration": 90,
			"splash": 90,
			"lingering": 22.5,
			"arrow": 11.25,
			"effects": [
				{
					"effect": "minecraft:speed",
					"amplifier": 1
				}
			]
		},
		{
			"id": "slowness",
			"duration": 90,
			"splash": 90,
			"lingering": 22.5,
			"arrow": 11.25,
			"effects": [
				{
					"effect": "minecraft:slowness"
				}
			]
		},
		{
			"id": "slow_slowness",
			"duration": 240,
			"splash": 240,
			"lingering": 60,
			"arrow": 30,
			"effects": [
				{
					"effect": "minecraft:slowness"
				}
			]
		},
		{
			"id": "strong_slowness",
			"duration": 20,
			"splash": 20,
			"lingering": 5,
			"arrow": 2.5,
			"effects": [
				{
					"effect": "minecraft:slowness",
					"amplifier": 3
				}
			]
		},
		{
			"id": "turtle_master",
			"duration": 20,
			"splash": 20,
			"lingering": 5,
			"arrow": 2.5,
			"effects": [
				{
					"effect": "minecraft:slowness",
					"amplifier": 3
				},
				{
					"effect": "minecraft:resistance",
					"amplifier": 2
				}
			]
		},
		{
			"id": "long_turtle_master",
			"duration": 40,
			"splash": 40,
			"lingering": 10,
			"arrow": 5,
			"effects": [
				{
					"effect": "minecraft:slowness",
					"amplifier": 3
				},
				{
					"effect": "minecraft:resistance",
					"amplifier": 2
				}
			]
		},
		{
			"id": "strong_turtle_master",
			"duration": 20,
			"splash": 20,
			"lingering": 5,
			"arrow": 2.5,
			"effects": [
				{
					"effect": "minecraft:slowness",
					"amplifier": 5
				},
				{
					"effect": "minecraft:resistance",
					"amplifier": 3
				}
			]
		},
		{
			"id": "water_breathing",
			"duration": 180,
			"splash": 180,
			"lingering": 45,
			"arrow": 22.5,
			"effects": [
				{
					"effect": "minecraft:water_breathing"
				}
			]
		},
		{
			"id": "long_water_breathing",
			"duration": 480,
			"splash": 480,
			"lingering": 120,
			"arrow": 60,
			"effects": [
				{
					"effect": "minecraft:water_breathing"
				}
			]
		},
		{
			"id": "healing",
			"duration": 0,
			"splash": 0,
			"lingering": 0,
			"arrow": 0,
			"effects": [
				{
					"effect": "minecraft:instant_health"
				}
			]
		},
		{
			"id": "strong_healing",
			"duration": 0,
			"splash": 0,
			"lingering": 0,
			"arrow": 0,
			"effects": [
				{
					"effect": "minecraft:instant_health",
					"amplifier": 1
				}
			]
		},
		{
			"id": "harming",
			"duration": 0,
			"splash": 0,
			"lingering": 0,
			"arrow": 0,
			"effects": [
				{
					"effect": "minecraft:instant_damage"
				}
			]
		},
		{
			"id": "strong_harming",
			"duration": 0,
			"splash": 0,
			"lingering": 0,
			"arrow": 0,
			"effects": [
				{
					"effect": "minecraft:instant_damage",
					"amplifier": 1
				}
			]
		},
		{
			"id": "poison",
			"duration": 45,
			"splash": 45,
			"lingering": 11.25,
			"arrow": 5.6,
			"effects": [
				{
					"effect": "minecraft:poison"
				}
			]
		},
		{
			"id": "long_poison",
			"duration": 90,
			"splash": 90,
			"lingering": 22.5,
			"arrow": 11.25,
			"effects": [
				{
					"effect": "minecraft:poison"
				}
			]
		},
		{
			"id": "strong_poison",
			"duration": 21.6,
			"splash": 21.6,
			"lingering": 5.4,
			"arrow": 2.7,
			"effects": [
				{
					"effect": "minecraft:poison",
					"amplifier": 1
				}
			]
		},
		{
			"id": "regeneration",
			"duration": 45,
			"splash": 45,
			"lingering": 11.25,
			"arrow": 5.6,
			"effects": [
				{
					"effect": "minecraft:regeneration"
				}
			]
		},
		{
			"id": "long_regeneration",
			"duration": 90,
			"splash": 90,
			"lingering": 22.5,
			"arrow": 11.25,
			"effects": [
				{
					"effect": "minecraft:regeneration"
				}
			]
		},
		{
			"id": "strong_regeneration",
			"duration": 22.5,
			"splash": 22.5,
			"lingering": 5.6,
			"arrow": 2.8,
			"effects": [
				{
					"effect": "minecraft:regeneration",
					"amplifier": 1
				}
			]
		},
		{
			"id": "strength",
			"duration": 180,
			"splash": 180,
			"lingering": 45,
			"arrow": 22.5,
			"effects": [
				{
					"effect": "minecraft:strength"
				}
			]
		},
		{
			"id": "long_strength",
			"duration": 480,
			"splash": 480,
			"lingering": 120,
			"arrow": 60,
			"effects": [
				{
					"effect": "minecraft:strength"
				}
			]
		},
		{
			"id": "strong_strength",
			"duration": 90,
			"splash": 90,
			"lingering": 22.5,
			"arrow": 11.25,
			"effects": [
				{
					"effect": "minecraft:strength",
					"amplifier": 1
				}
			]
		},
		{
			"id": "weakness",
			"duration": 90,
			"splash": 90,
			"lingering": 22.5,
			"arrow": 11.25,
			"effects": [
				{
					"effect": "minecraft:weakness"
				}
			]
		},
		{
			"id": "long_weakness",
			"duration": 240,
			"splash": 240,
			"lingering": 60,
			"arrow": 30,
			"effects": [
				{
					"effect": "minecraft:weakness"
				}
			]
		},
		{
			"id": "slow_falling",
			"duration": 90,
			"splash": 90,
			"lingering": 22.5,
			"arrow": 11.25,
			"effects": [
				{
					"effect": "minecraft:slow_falling"
				}
			]
		},
		{
			"id": "long_slow_falling",
			"duration": 240,
			"splash": 240,
			"lingering": 60,
			"arrow": 30,
			"effects": [
				{
					"effect": "minecraft:slow_falling"
				}
			]
		}
	],
	"ingredient_groups": {
		"mundane": [
			"minecraft:glistering_melon_slice",
			"minecraft:ghast_tear",
			"minecraft:rabbit_foot",
			"minecraft:blaze_powder",
			"minecraft:spider_eye",
			"minecraft:sugar",
			"minecraft:magma_cream",
			"minecraft:redstone"
		]
	},
	"recipes": [
		{
			"input": "*-*",
			"ingredient": "minecraft:redstone",
			"output": "{1}-long_{2}"
		},
		{
			"input": "*-*",
			"ingredient": "minecraft:glowstone_dust",
			"output": "{1}-strong_{2}"
		},
		{
			"input": "normal-*",
			"ingredient": "minecraft:gunpowder",
			"output": "splash-{1}"
		},
		{
			"input": "normal-*",
			"ingredient": "minecraft:dragon_breath",
			"output": "lingering-{1}"
		},
		{
			"input": "*-water",
			"ingredient": "mundane",
			"output": "{1}-mundane"
		},
		{
			"input": "*-water",
			"ingredient": "minecraft:glowstone_dust",
			"output": "{1}-thick"
		},
		{
			"input": "*-water",
			"ingredient": "minecraft:nether_wart",
			"output": "{1}-awkward"
		},
		{
			"input": "*-awkward",
			"ingredient": "minecraft:golden_carrot",
			"output": "{1}-night_vision"
		},
		{
			"input": "*night_vision",
			"ingredient": "minecraft:fermented_spider_eye",
			"output": "{1}invisibility"
		},
		{
			"input": "*-awkward",
			"ingredient": "minecraft:magma_cream",
			"output": "{1}-fire_resistance"
		},
		{
			"input": "*-awkward",
			"ingredient": "minecraft:rabbit_foot",
			"output": "{1}-leaping"
		},
		{
			"input": "*leaping",
			"ingredient": "minecraft:fermented_spider_eye",
			"output": "{1}slowness"
		},
		{
			"input": "*-awkward",
			"ingredient": "minecraft:turtle_helmet",
			"output": "{1}-turtle_master"
		},
		{
			"input": "*-awkward",
			"ingredient": "minecraft:sugar",
			"output": "{1}-swiftness"
		},
		{
			"input": "*swiftness",
			"ingredient": "minecraft:fermented_spider_eye",
			"output": "{1}slowness"
		},
		{
			"input": "*-awkward",
			"ingredient": "minecraft:pufferfish",
			"output": "{1}-water_breathing"
		},
		{
			"input": "*-awkward",
			"ingredient": "minecraft:glistering_melon_slice",
			"output": "{1}-healing"
		},
		{
			"input": "*healing",
			"ingredient": "minecraft:fermented_spider_eye",
			"output": "{1}harming"
		},
		{
			"input": "*-awkward",
			"ingredient": "minecraft:spider_eye",
			"output": "{1}-poison"
		},
		{
			"input": "*poison",
			"ingredient": "minecraft:fermented_spider_eye",
			"output": "{1}harming"
		},
		{
			"input": "*-awkward",
			"ingredient": "minecraft:ghast_tear",
			"output": "{1}-regeneration"
		},
		{
			"input": "*-awkward",
			"ingredient": "minecraft:blaze_powder",
			"output": "{1}-strength"
		},
		{
			"input": "*-water",
			"ingredient": "minecraft:fermented_spider_eye",
			"output": "{1}-weakness"
		},
		{
			"input": "*-awkward",
			"ingredient": "minecraft:phantom_membrane",
			"output": "{1}-slow_falling"
		}
	],
	"arrow_recipes": [
		{
			"input": "splash-*",
			"output": "{1}"
		}
	],
	"fuel": {
		"minecraft:blaze_powder": 20
	},
	"witch": {
		"normal-fire_resistance": "normal-fire_resistance",
		"normal-water_breathing": "normal-water_breathing",
		"normal-healing": "normal-healing",
		"normal-swiftness": "normal-swiftness",
		"splash-harming": "splash-harming",
		"splash-healing": "splash-healing",
		"splash-poison": "splash-poison",
		"splash-regeneration": "splash-regeneration",
		"splash-slowness": "splash-slowness",
		"splash-weakness": "splash-weakness"
	},
	"wandering_trader_night": "normal-invisibility"
}
""";
}
