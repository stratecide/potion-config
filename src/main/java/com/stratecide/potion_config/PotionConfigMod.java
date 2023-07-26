package com.stratecide.potion_config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stratecide.potion_config.effects.AfterEffect;
import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public class PotionConfigMod implements ModInitializer {

	public static final String MOD_ID = "potion-config";
	public static final String AFTER_EFFECT_PREFIX = "after_effect_";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static final Map<String, CustomPotion> CUSTOM_POTIONS = new HashMap<>();
	public static boolean hasCustomPotion(String id, Optional<PotionType> type) {
		if (type.isEmpty())
			return CUSTOM_POTIONS.containsKey(id);
		return switch (type.get()) {
			case Normal -> NORMAL_POTIONS.containsValue(id);
			case Splash -> SPLASH_POTIONS.containsValue(id);
			case Lingering -> LINGERING_POTIONS.containsValue(id);
		};
		// why is this needed?
	}
	public static CustomPotion getCustomPotion(String id) {
		if (!CUSTOM_POTIONS.containsKey(id)) {
			LOGGER.warn("Attempted to get custom potion '" + id + "' but it doesn't exist!");
			return CustomPotion.empty();
		}
		return CUSTOM_POTIONS.get(id);
	}
	public static final Set<Item> VALID_INGREDIENTS = new HashSet<>();
	public static final List<CustomRecipe> CUSTOM_RECIPES = new ArrayList<>();
	public static final List<ArrowRecipe> ARROW_RECIPES = new ArrayList<>();
	public static final Map<Identifier, Integer> FUELS = new HashMap<>();
	public static final Map<Identifier, Potion> WITCH_POTIONS_NORMAL = new HashMap<>();
	public static final Map<Identifier, Potion> WITCH_POTIONS_SPLASH = new HashMap<>();
	public static final Map<Identifier, Potion> WITCH_POTIONS_LINGERING = new HashMap<>();
	public static Potion WANDERING_TRADER_POTION;

	public static int TOOLTIP_MILLISECONDS = 2000;
	public static int STACK_SIZE = 1;
	public static int STACK_SIZE_SPLASH = 1;
	public static int STACK_SIZE_LINGERING = 1;
	public static boolean GLINT = false;
	public static double HIDE_EFFECTS_BELOW_CHANCE = 0.;
	public static boolean HIDE_AFTER_EFFECTS = false;
	public static String MYSTERY_NORMAL_POTION = null;
	public static String MYSTERY_SPLASH_POTION = null;
	public static String MYSTERY_LINGERING_POTION = null;
	public static String MYSTERY_ARROW = null;
	public static int DURATION_DEFAULT = 3600;
	public static String MILK_POTION = null;
	public static final TrackedDataHandler<PotionColorList> POTION_PARTICLE_COLORS = TrackedDataHandler.of(PotionColorList::writePotionColors, PotionColorList::readPotionColors);
	static {
		loadConfigMain();
		TrackedDataHandlerRegistry.register(POTION_PARTICLE_COLORS);
	}

	private static final Map<Identifier, String> NORMAL_POTIONS = new HashMap<>();
	public static final Map<String, List<PotionInput>> NORMAL_INPUTS = new HashMap<>();
	private static final Map<Identifier, String> SPLASH_POTIONS = new HashMap<>();
	public static final Map<String, List<PotionInput>> SPLASH_INPUTS = new HashMap<>();
	private static final Map<Identifier, String> LINGERING_POTIONS = new HashMap<>();
	public static final Map<String, List<PotionInput>> LINGERING_INPUTS = new HashMap<>();
	private static final Map<Identifier, String> ARROW_POTIONS = new HashMap<>();
	public static final Map<String, List<ArrowInput>> ARROW_INPUTS = new HashMap<>();

	public static boolean hasNormalPotion(Potion potion) {
		return NORMAL_POTIONS.containsKey(Registry.POTION.getId(potion));
	}
	public static CustomPotion getNormalPotion(Potion replaced) {
		return getPotion(NORMAL_POTIONS, replaced, MYSTERY_NORMAL_POTION, "normal");
	}
	public static boolean hasSplashPotion(Potion potion) {
		return SPLASH_POTIONS.containsKey(Registry.POTION.getId(potion));
	}
	public static CustomPotion getSplashPotion(Potion replaced) {
		return getPotion(SPLASH_POTIONS, replaced, MYSTERY_SPLASH_POTION, "splash");
	}
	public static boolean hasLingeringPotion(Potion potion) {
		return LINGERING_POTIONS.containsKey(Registry.POTION.getId(potion));
	}
	public static CustomPotion getLingeringPotion(Potion replaced) {
		return getPotion(LINGERING_POTIONS, replaced, MYSTERY_LINGERING_POTION, "lingering");
	}
	public static boolean hasCustomArrowPotion(String id) {
		return ARROW_POTIONS.containsValue(id);
	}
	public static boolean hasArrowPotion(Potion potion) {
		return ARROW_POTIONS.containsKey(Registry.POTION.getId(potion));
	}
	public static CustomPotion getArrowPotion(Potion replaced) {
		return getPotion(ARROW_POTIONS, replaced, MYSTERY_ARROW, "arrow");
	}

	private static CustomPotion getPotion(Map<Identifier, String> map, Potion replaced, String mystery, String loggingKey) {
		Identifier identifier = Registry.POTION.getId(replaced);
		String customId = map.get(identifier);
		if (customId != null) {
			return getCustomPotion(customId);
		} else if (mystery != null && map.containsValue(mystery)) {
			return getCustomPotion(mystery);
		}
		return CustomPotion.empty();
	}

	public static String getCustomPotionId(PotionType type, Potion potion) {
		Map<Identifier, String> map = switch (type) {
			case Splash -> SPLASH_POTIONS;
			case Lingering -> LINGERING_POTIONS;
			default -> NORMAL_POTIONS;
		};
		Identifier identifier = Registry.POTION.getId(potion);
		return map.get(identifier);
	}
	public static String getCustomArrowPotionId(Potion potion) {
		Identifier identifier = Registry.POTION.getId(potion);
		return ARROW_POTIONS.get(identifier);
	}

	public static Potion getOriginalPotion(String customId, PotionType type) {
		Map<Identifier, String> map = switch (type) {
			case Splash -> SPLASH_POTIONS;
			case Lingering -> LINGERING_POTIONS;
			default -> NORMAL_POTIONS;
		};
		for (Map.Entry<Identifier, String> entry : map.entrySet()) {
			if (entry.getValue().equals(customId)) {
				return Registry.POTION.get(entry.getKey());
			}
		}
		return Potions.EMPTY;
	}
	public static Potion getOriginalArrowPotion(String customId) {
		for (Map.Entry<Identifier, String> entry : ARROW_POTIONS.entrySet()) {
			if (entry.getValue().equals(customId)) {
				return Registry.POTION.get(entry.getKey());
			}
		}
		return Potions.EMPTY;
	}

	@Override
	public void onInitialize() {
		loadConfigEffects();
		loadConfigNormal();
		loadConfigSplash();
		loadConfigLingering();
		loadConfigArrows();
		Map<String, Ingredient> itemGroups = loadConfigIngredientGroups();
		loadConfigRecipes(itemGroups);
		loadConfigArrowRecipes();
		loadConfigFuel();
		loadConfigOther();
		buildRecipeInputs();
	}

	private static JsonElement loadConfig(String filename, String defaultContent) {
		File file = new File(filename);
		String data;
		if (!file.exists() || true) {
			file.getParentFile().mkdirs();
			data = defaultContent;
			try (FileWriter writer = new FileWriter(filename)) {
				writer.write(data);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try (Scanner scanner = new Scanner(file)) {
				StringBuilder builder = new StringBuilder();
				while (scanner.hasNextLine())
					builder.append(scanner.nextLine());
				data = builder.toString();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				data = defaultContent;
			}
		}
		LOGGER.info("parsing " + filename);
		return JsonParser.parseString(data);
	}

	/**
	 * STATUS EFFECT
	 * 		unchanged from vanilla
	 *
	 * POTION
	 * 		duration is the same for all effects
	 * 		optional aftereffects POTION
	 * 			(doesn't go into effect if no effects are applied from the current stage)
	 * 		has a list of status effects, each with the following attributes
	 * 			strength (== 1 + amplifier)
	 * 			chance (chance of being applied when affected by the potion)
	 *
	 * NORMAL, SPLASH, LINGERING, ARROW
	 * 		optional vanilla id
	 * 		custom potion id
	 *
	 * SPLASH, LINGERING
	 * 		radius
	 */

	private static final String CONFIG_DIR = "config/potion-config/";


	private static void loadConfigMain() {
		JsonObject json = loadConfig(CONFIG_FILE_MAIN, DEFAULT_MAIN).getAsJsonObject();
		if (json.has("stack_size")) {
			STACK_SIZE = json.get("stack_size").getAsInt();
		}
		if (json.has("stack_size_splash")) {
			STACK_SIZE_SPLASH = json.get("stack_size_splash").getAsInt();
		}
		if (json.has("stack_size_lingering")) {
			STACK_SIZE_LINGERING = json.get("stack_size_lingering").getAsInt();
		}
		if (json.has("glint")) {
			GLINT = json.get("glint").getAsBoolean();
		}
		if (json.has("hide_effects_below_chance")) {
			HIDE_EFFECTS_BELOW_CHANCE = json.get("hide_effects_below_chance").getAsDouble();
		}
		if (json.has("hide_after_effects")) {
			HIDE_AFTER_EFFECTS = json.get("hide_after_effects").getAsBoolean();
		}
		if (json.has("default_duration")) {
			DURATION_DEFAULT = json.get("default_duration").getAsInt();
		}
		if (json.has("milk")) {
			MILK_POTION = json.get("milk").getAsString();
		}
		if (json.has("mystery_normal")) {
			MYSTERY_NORMAL_POTION = json.get("mystery_normal").getAsString();
		}
		if (json.has("mystery_splash")) {
			MYSTERY_SPLASH_POTION = json.get("mystery_splash").getAsString();
		}
		if (json.has("mystery_lingering")) {
			MYSTERY_LINGERING_POTION = json.get("mystery_lingering").getAsString();
		}
		if (json.has("mystery_arrow")) {
			MYSTERY_ARROW = json.get("mystery_arrow").getAsString();
		}
	}
	private static final String CONFIG_FILE_MAIN = CONFIG_DIR + "general.json";
	private static final String DEFAULT_MAIN = """
{
	"stack_size": 16,
	"stack_size_splash": 4,
	"stack_size_lingering": 3,
	"glint": false,
	"hide_effects_below_chance": 0.3,
	"hide_after_effects": false,
	"default_duration": 3600,
	"mystery_normal": "awkward",
	"mystery_splash": "thick",
	"mystery_lingering": "rainbow_gradient",
	"mystery_arrow": "floating",
	"milk": "milk"
}""";

	private void loadConfigEffects() {
		JsonObject jsonObject = loadConfig(CONFIG_FILE_EFFECTS, DEFAULT_EFFECTS).getAsJsonObject();
		for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			String id = entry.getKey();
			Identifier afterEffectId = new Identifier(MOD_ID, AFTER_EFFECT_PREFIX + id);
			Registry.register(Registry.STATUS_EFFECT, afterEffectId, new AfterEffect(id));
			CustomPotion potion = CustomPotion.parse(entry.getValue().getAsJsonObject());
			CUSTOM_POTIONS.put(id, potion);
		}
	}
	private static final String CONFIG_FILE_EFFECTS = CONFIG_DIR + "effects.json";
	private static final String DEFAULT_EFFECTS = """
{
	"water": { "color": "0000ff" },
	"mundane": {
		"color": "6633ff"
	},
	"thick": {
		"color": "cc66ff",
		"duration": 200,
		"minecraft:wither": { "chance": 0.2 },
		"minecraft:regeneration": { "chance": 0.2 },
		"minecraft:nausea": { "chance": 0.4 }
	},
	"awkward": {
		"color": "3388ff",
		"duration": 200,
		"minecraft:poison": { "chance": 0.2 },
		"minecraft:regeneration": { "chance": 0.2 },
		"minecraft:glowing": { "chance": 0.4 }
	},
	
	"milk": {
		"color": "ffffff",
		"potion-config:milk": {}
	},
	"lemonade": {
		"color": "ddff00",
		"duration": 1200,
		"minecraft:speed": { "amplifier": 1 },
		"minecraft:jump_boost": {},
		"after": "aftereffect_slow"
	},
	"aftereffect_slow": {
		"duration": 100,
		"minecraft:slowness": { "chance": 0.75 },
		"potion-config:jump_drop": { "chance": 0.5 }
	},
	"beer": {
		"color": "d8c068",
		"duration": 600,
		"potion-config:drunk": { "chance": 0.5 },
		"minecraft:nausea": {}
	},
	"unfiltered_cider": {
		"color": "b9812f",
		"duration": 600,
		"potion-config:drunk": {},
		"minecraft:nausea": { "amplifier": 1 },
		"minecraft:hunger": { "chance": 0.6 },
		"minecraft:darkness": { "chance": 0.6 },
		"after": "beer"
	},
	"cider": {
		"color": "b9812f",
		"duration": 600,
		"potion-config:drunk": {},
		"minecraft:nausea": { "amplifier": 1 },
		"minecraft:hunger": { "chance": 0.2 },
		"after": "beer"
	},
	"vodka": {
		"color": "bbbbbb",
		"duration": 600,
		"potion-config:drunk": { "amplifier": 3 },
		"minecraft:nausea": { "amplifier": 3 },
		"minecraft:hunger": { "chance": 0.3 },
		"after": "cider"
	},
	"health_gamble": {
		"color": "F87D23",
		"potion-config:random_choice": {
			"options": [
				{
					"key": "potion-config:all_or_none",
					"chance": 1,
					"children": [
						{ "key": "potion-config:health_boost", "amplifier": 4 },
						{ "key": "potion-config:particles", "color": "f87d23" }
					]
				},
				{
					"key": "potion-config:all_or_none",
					"chance": 1,
					"children": [
						{ "key": "potion-config:health_drop", "amplifier": 4 },
						{ "key": "potion-config:particles", "color": "5a3740" }
					]
				}
			]
		}
	},
	"slow_falling": {
		"color": "FFEFD1",
		"duration": 6000,
		"minecraft:slow_falling": {},
		"potion-config:particles": { "color": "FFEFD1" }
	},
	"floating": {
		"color": "CEFFFF",
		"duration": 400,
		"minecraft:levitation": { "amplifier": 2 },
		"minecraft:speed": {},
		"potion-config:particles": { "color": "CEFFFF" },
		"after": "aftereffect_slow_falling"
	},
	"aftereffect_slow_falling": {
		"duration": 240,
		"minecraft:slow_falling": {}
	},
	"creative_flight": {
		"color": "66bbff",
		"potion-config:creative_flight": {},
		"after": "aftereffect_slow_falling"
	},
	"turtle_master": {
		"color": "666999",
		"potion-config:knockback_resistance": { "amplifier": 2 },
		"minecraft:slowness": { "amplifier": 2 },
		"minecraft:resistance": { "amplifier": 2 },
		"potion-config:jump_drop": {},
		"potion-config:particles": { "color": "666999" }
	},
	"invisibility": {
		"color": "7F8392",
		"minecraft:invisibility": {},
		"minecraft:darkness": {}
	},
	"fire_resistance_witch": {
		"color": "E49A3A",
		"duration": 2400,
		"potion-config:particles": { "color": "E49A3A" },
		"minecraft:fire_resistance": {}
	},
	"water_breathing_witch": {
		"color": "2E5299",
		"duration": 2400,
		"potion-config:particles": { "color": "2E5299" },
		"minecraft:water_breathing": {}
	},
	"regeneration_witch": {
		"color": "CD5CAB",
		"duration": 2400,
		"potion-config:particles": { "color": "CD5CAB" },
		"minecraft:regeneration": {}
	},
	
	"swiftness": {
		"color": "7CAFC6",
		"minecraft:speed": { "amplifier": 1 },
		"minecraft:jump_boost": {},
		"potion-config:particles": { "color": "7CAFC6" },
		"after": "aftereffect_slow"
	},
	"long_swiftness": {
		"color": "7CAFC6",
		"duration": 12000,
		"minecraft:speed": { "amplifier": 1 },
		"minecraft:jump_boost": {},
		"potion-config:particles": { "color": "7CAFC6" },
		"after": "aftereffect_slow"
	},
	"strength": {
		"color": "932423",
		"minecraft:strength": {},
		"minecraft:haste": { "amplifier": 1 },
		"potion-config:particles": { "color": "932423" },
		"after": "aftereffect_weakness"
	},
	"aftereffect_weakness": {
		"duration": 100,
		"minecraft:mining_fatigue": { "chance": 0.7 },
		"minecraft:slowness": { "chance": 0.7 },
		"minecraft:weakness": { "chance": 0.7 }
	},
	"night_vision": {
		"color": "1F1FA1",
		"minecraft:night_vision": {},
		"potion-config:particles": {
			"color": [0, "000000", "88ff88"]
		},
		"after": "aftereffect_blindness"
	},
	"aftereffect_blindness": {
		"duration": 80,
		"minecraft:blindness": { "chance": 0.7 }
	},
	"water_breathing": {
		"color": "2E5299",
		"potion-config:finesse": { "amplifier": 2 },
		"minecraft:water_breathing": {}
	},
	"fire_resistance": {
		"color": "E49A3A",
		"potion-config:particles": { "color": "E49A3A" },
		"minecraft:fire_resistance": {}
	},
	"flames": {
		"color": "ff6600",
		"potion-config:flames": { "amplifier": 9 }
	},
	"wither": {
		"color": "352A27",
		"duration": 80,
		"minecraft:wither": {},
		"potion-config:particles": { "color": "352A27" }
	},
	
	"poison": {
		"color": "4E9331",
		"duration": 100,
		"minecraft:poison": {},
		"potion-config:particles": { "color": "4E9331" }
	},
	"short_slowness": {
		"color": "5A6C81",
		"duration": 200,
		"minecraft:slowness": {},
		"potion-config:particles": { "color": "5A6C81" }
	},
	"healing": {
		"color": "CD5CAB",
		"minecraft:instant_health": { "amplifier": 1 }
	},
	"harming": {
		"color": "430A09",
		"minecraft:instant_damage": { "amplifier": 1 }
	},
	
	"black": {
		"color": "1D1D21",
		"potion-config:particles": {
			"amplifier": 50,
			"color": "1D1D21"
		}
	},
	"red": {
		"color": "B02E26",
		"potion-config:particles": {
			"amplifier": 50,
			"color": "B02E26"
		}
	},
	"green": {
		"color": "5E7C16",
		"potion-config:particles": {
			"amplifier": 50,
			"color": "5E7C16"
		}
	},
	"brown": {
		"color": "835432",
		"potion-config:particles": {
			"amplifier": 50,
			"color": "835432"
		}
	},
	"blue": {
		"color": "3C44AA",
		"potion-config:particles": {
			"amplifier": 50,
			"color": "3C44AA"
		}
	},
	"purple": {
		"color": "8932B8",
		"potion-config:particles": {
			"amplifier": 50,
			"color": "8932B8"
		}
	},
	"cyan": {
		"color": "169C9C",
		"potion-config:particles": {
			"amplifier": 50,
			"color": "169C9C"
		}
	},
	"light_gray": {
		"color": "9D9D97",
		"potion-config:particles": {
			"amplifier": 50,
			"color": "9D9D97"
		}
	},
	"gray": {
		"color": "474F52",
		"potion-config:particles": {
			"amplifier": 50,
			"color": "474F52"
		}
	},
	"pink": {
		"color": "F38BAA",
		"potion-config:particles": {
			"amplifier": 50,
			"color": "F38BAA"
		}
	},
	"lime": {
		"color": "80C71F",
		"potion-config:particles": {
			"amplifier": 50,
			"color": "80C71F"
		}
	},
	"yellow": {
		"color": "FED83D",
		"potion-config:particles": {
			"amplifier": 50,
			"color": "FED83D"
		}
	},
	"light_blue": {
		"color": "3AB3DA",
		"potion-config:particles": {
			"amplifier": 50,
			"color": "3AB3DA"
		}
	},
	"magenta": {
		"color": "C74EBD",
		"potion-config:particles": {
			"amplifier": 50,
			"color": "C74EBD"
		}
	},
	"orange": {
		"color": "F9801D",
		"potion-config:particles": {
			"amplifier": 50,
			"color": "F9801D"
		}
	},
	"white": {
		"color": "F9FFFE",
		"potion-config:particles": {
			"amplifier": 50,
			"color": "F9FFFE"
		}
	},
	
	"fire_gradient": {
		"color": [20, "ff0000", "ffff00"],
		"potion-config:particles": {
			"amplifier": 50,
			"color": [20, "ff0000", "ffff00"]
		}
	},
	"black_white_gradient": {
		"color": [40, "000000", "000000", "ffffff", "ffffff"],
		"potion-config:particles": {
			"amplifier": 50,
			"color": [40, "000000", "000000", "ffffff", "ffffff"]
		}
	},
	"cloud_gradient": {
		"color": [0, "F9FFFE", "F9FFFE", "3AB3DA"],
		"potion-config:particles": {
			"amplifier": 50,
			"color": [0, "F9FFFE", "F9FFFE", "3AB3DA"]
		}
	},
	"rainbow_gradient": {
		"color": [20, "ff0000", "ffff00", "00ff00", "00ffff", "0000ff", "ff00ff"],
		"potion-config:particles": {
			"amplifier": 50,
			"color": [20, "ff0000", "ffff00", "00ff00", "00ffff", "0000ff", "ff00ff"]
		}
	}
 }""";

	private static Identifier addPotionTranslation(String replaces, String id) {
		if (replaces != null) {
			return new Identifier(replaces);
		}
		Identifier identifier = new Identifier(MOD_ID, id);
		if (!Registry.POTION.containsId(identifier)) {
			Registry.register(Registry.POTION, identifier, new Potion());
		}
		return identifier;
	}

	private void loadConfigNormal() {
		JsonObject jsonObject = loadConfig(CONFIG_FILE_NORMAL, DEFAULT_NORMAL).getAsJsonObject();
		for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			String id = entry.getKey();
			JsonObject json = entry.getValue().getAsJsonObject();
			String replaced = null;
			if (json.has("replaces")) {
				replaced = json.get("replaces").getAsString();
			}
			Identifier identifier = addPotionTranslation(replaced, id);
			NORMAL_POTIONS.put(identifier, id);
		}
	}
	private static final String CONFIG_FILE_NORMAL = CONFIG_DIR + "normal_potions.json";
	private static final String DEFAULT_NORMAL = """
{
	"water": { "replaces": "minecraft:water" },
	"mundane": { "replaces": "minecraft:mundane" },
	"thick": { "replaces": "minecraft:thick" },
	"awkward": { "replaces": "minecraft:awkward" },
	
	"milk": { "replaces": "minecraft:leaping" },
	"lemonade": { "replaces": "minecraft:swiftness" },
	"beer": { "replaces": "minecraft:poison" },
	"unfiltered_cider": {},
	"cider": { "replaces": "minecraft:harming" },
	"vodka": { "replaces": "minecraft:strong_harming" },
	"health_gamble": { "replaces": "minecraft:strong_healing" },
	"slow_falling": { "replaces": "minecraft:long_slow_falling" },
	"floating": { "replaces": "minecraft:slow_falling" },
	"creative_flight": { "replaces": "minecraft:strong_leaping" },
	"turtle_master": { "replaces": "minecraft:turtle_master" },
	"invisibility": { "replaces": "minecraft:invisibility" },
	
	"fire_resistance_witch": { "replaces": "minecraft:fire_resistance" },
	"water_breathing_witch": { "replaces": "minecraft:water_breathing" },
	"regeneration_witch": { "replaces": "minecraft:regeneration" }
}""";

	private void loadConfigSplash() {
		JsonObject jsonObject = loadConfig(CONFIG_FILE_SPLASH, DEFAULT_SPLASH).getAsJsonObject();
		for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			String id = entry.getKey();
			JsonObject json = entry.getValue().getAsJsonObject();
			String replaced = null;
			if (json.has("replaces")) {
				replaced = json.get("replaces").getAsString();
			}
			Identifier identifier = addPotionTranslation(replaced, id);
			SPLASH_POTIONS.put(identifier, id);
		}
	}
	private static final String CONFIG_FILE_SPLASH = CONFIG_DIR + "splash_potions.json";
	private static final String DEFAULT_SPLASH = """
{
	"water": { "replaces": "minecraft:water" },
	"mundane": { "replaces": "minecraft:mundane" },
	"thick": { "replaces": "minecraft:thick" },
	"awkward": { "replaces": "minecraft:awkward" },
	
	"swiftness": { "replaces": "minecraft:swiftness" },
	"long_swiftness": { "replaces": "minecraft:long_swiftness" },
	"strength": { "replaces": "minecraft:strength" },
	"night_vision": { "replaces": "minecraft:night_vision" },
	"water_breathing": { "replaces": "minecraft:water_breathing" },
	"fire_resistance": { "replaces": "minecraft:fire_resistance" },
	"flames": { "replaces": "minecraft:harming" },
	"wither": { "replaces": "minecraft:strong_harming" }
}""";

	private void loadConfigLingering() {
		JsonObject jsonObject = loadConfig(CONFIG_FILE_LINGERING, DEFAULT_LINGERING).getAsJsonObject();
		for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			String id = entry.getKey();
			JsonObject json = entry.getValue().getAsJsonObject();
			String replaced = null;
			if (json.has("replaces")) {
				replaced = json.get("replaces").getAsString();
			}
			Identifier identifier = addPotionTranslation(replaced, id);
			LINGERING_POTIONS.put(identifier, id);
		}
	}
	private static final String CONFIG_FILE_LINGERING = CONFIG_DIR + "lingering_potions.json";
	private static final String DEFAULT_LINGERING = """
{
	"water": { "replaces": "minecraft:water" },
	"mundane": { "replaces": "minecraft:mundane" },
	"thick": { "replaces": "minecraft:thick" },
	"awkward": { "replaces": "minecraft:awkward" },
	
	"healing": { "replaces": "minecraft:regeneration" },
	"poison": { "replaces": "minecraft:poison" },
	"harming": { "replaces": "minecraft:strong_harming" },
	"short_slowness": { "replaces": "minecraft:slowness" },
	
	"black": {},
	"red": {},
	"green": {},
	"brown": {},
	"blue": {},
	"purple": {},
	"cyan": {},
	"light_gray": {},
	"gray": {},
	"pink": {},
	"lime": {},
	"yellow": {},
	"light_blue": {},
	"magenta": {},
	"orange": {},
	"white": {},
	
	"fire_gradient": {},
	"black_white_gradient": {},
	"cloud_gradient": {},
	"rainbow_gradient": {}
}""";
	private void loadConfigArrows() {
		JsonObject jsonObject = loadConfig(CONFIG_FILE_ARROWS, DEFAULT_ARROWS).getAsJsonObject();
		for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			String id = entry.getKey();
			JsonObject json = entry.getValue().getAsJsonObject();
			String replaced = null;
			if (json.has("replaces")) {
				replaced = json.get("replaces").getAsString();
			}
			Identifier identifier = addPotionTranslation(replaced, id);
			ARROW_POTIONS.put(identifier, id);
		}
	}
	private static final String CONFIG_FILE_ARROWS = CONFIG_DIR + "arrows.json";
	private static final String DEFAULT_ARROWS = """
{
	"floating": { "replaces": "minecraft:slow_falling" },
	"short_slowness": { "replaces": "minecraft:slowness" },
	
	"black": {},
	"red": {},
	"green": {},
	"brown": {},
	"blue": {},
	"purple": {},
	"cyan": {},
	"light_gray": {},
	"gray": {},
	"pink": {},
	"lime": {},
	"yellow": {},
	"light_blue": {},
	"magenta": {},
	"orange": {},
	"white": {},
	
	"fire_gradient": {},
	"black_white_gradient": {},
	"cloud_gradient": {},
	"rainbow_gradient": {}
}""";

	private Map<String, Ingredient> loadConfigIngredientGroups() {
		JsonObject jsonObject = loadConfig(CONFIG_FILE_INGREDIENT_GROUPS, DEFAULT_INGREDIENT_GROUPS).getAsJsonObject();
		Map<String, Ingredient> itemGroups = new HashMap<>();
		for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			String groupId = entry.getKey();
			Set<Item> items = new HashSet<>();
			for (JsonElement element : entry.getValue().getAsJsonArray()) {
				Identifier identifier = new Identifier(element.getAsString());
				Item item = Registry.ITEM.get(identifier);
				if (item == Items.AIR) {
					LOGGER.warn("Missing item in ingredient group '" + groupId + "': " + identifier);
				} else {
					items.add(item);
				}
			}
			if (items.size() > 0) {
				Ingredient ingredient = Ingredient.ofStacks(items.stream().map(ItemStack::new));
				itemGroups.put(groupId, ingredient);
			}
		}
		return itemGroups;
	}
	private static final String CONFIG_FILE_INGREDIENT_GROUPS = CONFIG_DIR + "ingredient_groups.json";
	private static final String DEFAULT_INGREDIENT_GROUPS = """
{
	"mundane_items": [
		"minecraft:rotten_flesh",
		"minecraft:rabbit_foot",
		"minecraft:glistering_melon_slice",
		"minecraft:spider_eye"
	]
}""";

	/**
	 * recipe has
	 * 		input pattern
	 * 		input item type
	 * 		ingredient (item or itemGroup)
	 * 		list of
	 * 			output pattern
	 * 			output item type
	 * 			weight (relative chance of being selected)
	 *
	 */
	private void loadConfigRecipes(Map<String, Ingredient> itemGroups) {
		JsonArray jsonArray = loadConfig(CONFIG_FILE_RECIPES, DEFAULT_RECIPES).getAsJsonArray();
		for (JsonElement jsonElement : jsonArray) {
			JsonObject json = jsonElement.getAsJsonObject();

			JsonArray input = json.get("input").getAsJsonArray();
			String ingredientId = input.get(0).getAsString();
			Ingredient ingredient = itemGroups.get(ingredientId);
			if (ingredient == null) {
				Item item = Registry.ITEM.get(new Identifier(ingredientId));
				if (item == Items.AIR)
					throw new AssertionError("Invalid ingredient identifier : " + ingredientId);
				ingredient = Ingredient.ofItems(item);
			}
			Pattern inputPattern = CustomRecipe.patternFromString(input.get(1).getAsString(), true);
			Optional<PotionType> inputType = Optional.empty();
			if (input.size() > 2) {
				inputType = PotionType.parse(input.get(2).getAsString());
			}

			List<CustomRecipe.Output> outputs = new ArrayList<>();
			if (json.has("output")) {
				JsonArray output = json.get("output").getAsJsonArray();
				Pattern outputPattern = CustomRecipe.patternFromString(output.get(0).getAsString(), false);
				Optional<PotionType> outputType = Optional.empty();
				if (output.size() > 1) {
					outputType = PotionType.parse(output.get(1).getAsString());
				}
				outputs.add(new CustomRecipe.Output(1, outputPattern.toString(), outputType));
			} else {
				for (JsonElement element : json.get("outputs").getAsJsonArray()) {
					JsonArray output = element.getAsJsonArray();
					int weight = output.get(0).getAsInt();
					Pattern outputPattern = CustomRecipe.patternFromString(output.get(1).getAsString(), false);
					Optional<PotionType> outputType = Optional.empty();
					if (output.size() > 2) {
						outputType = PotionType.parse(output.get(2).getAsString());
					}
					outputs.add(new CustomRecipe.Output(Math.max(1, weight), outputPattern.toString(), outputType));
				}
			}

			CustomRecipe recipe = new CustomRecipe(inputPattern, inputType, ingredient, outputs);
			for (ItemStack stack : ingredient.getMatchingStacks()) {
				VALID_INGREDIENTS.add(stack.getItem());
			}
			CUSTOM_RECIPES.add(recipe);
		}
	}
	private static void buildRecipeInputs() {
		for (PotionType type : PotionType.values()) {
			Collection<String> customIds = switch (type) {
				case Normal -> NORMAL_POTIONS.values();
				case Splash -> SPLASH_POTIONS.values();
				case Lingering -> LINGERING_POTIONS.values();
			};
			for (String customId : customIds) {
				for (Item item : VALID_INGREDIENTS) {
					ItemStack ingredient = new ItemStack(item);
					for (CustomRecipe recipe : CUSTOM_RECIPES) {
						if (recipe.matches(type, customId, ingredient)) {
							Potion potion = getOriginalPotion(customId, type);
							for (CustomRecipe.Result result : recipe.craftOptions(type, potion)) {
								Map<String, List<PotionInput>> inputs = switch (result.outputType()) {
									case Normal -> NORMAL_INPUTS;
									case Splash -> SPLASH_INPUTS;
									case Lingering -> LINGERING_INPUTS;
								};
								PotionInput input = new PotionInput(potion, type, item);
								if (!inputs.containsKey(result.customId())) {
									inputs.put(result.customId(), new ArrayList<>());
								}
								inputs.get(result.customId()).add(input);
							}
							break;
						}
					}
				}
				for (ArrowRecipe recipe : ARROW_RECIPES) {
					if (recipe.matches(type, customId)) {
						Potion potion = getOriginalPotion(customId, type);
						ItemStack result = recipe.craft(type, potion);
						String resultId = getCustomArrowPotionId(PotionUtil.getPotion(result));
						ArrowInput input = new ArrowInput(potion, type);
						if (!ARROW_INPUTS.containsKey(resultId)) {
							ARROW_INPUTS.put(resultId, new ArrayList<>());
						}
						ARROW_INPUTS.get(resultId).add(input);
						break;
					}
				}
			}
		}
	}
	private static final String CONFIG_FILE_RECIPES = CONFIG_DIR + "recipes.json";
	private static final String DEFAULT_RECIPES = """
[
	{
		"input": ["mundane_items", "water"],
		"output": ["mundane"]
	},
	{
		"input": ["minecraft:nether_wart", "water"],
		"output": ["awkward"]
	},
	{
		"input": ["minecraft:gunpowder", "*", "normal"],
		"output": ["{1}", "splash"]
	},
	{
		"input": ["minecraft:gunpowder", "*", "splash"],
		"output": ["{1}", "lingering"]
	},
	
	{
		"input": ["minecraft:sugar", "water"],
		"output": ["lemonade"]
	},
	{
		"input": ["minecraft:wheat", "water"],
		"output": ["beer"]
	},
	{
		"input": ["minecraft:apple", "lemonade"],
		"output": ["unfiltered_cider"]
	},
	{
		"input": ["minecraft:paper", "unfiltered_cider"],
		"output": ["cider"]
	},
	{
		"input": ["minecraft:poisonous_potato", "lemonade"],
		"output": ["vodka"]
	},
	{
		"input": ["minecraft:nether_wart", "awkward"],
		"output": ["health_gamble"]
	},
	{
		"input": ["minecraft:phantom_membrane", "awkward"],
		"outputs": [
			[3, "slow_falling"],
			[2, "floating"],
			[1, "creative_flight"]
		]
	},
	{
		"input": ["minecraft:scute", "awkward"],
		"output": ["turtle_master"]
	},
	{
		"input": ["minecraft:poisonous_potato", "awkward"],
		"output": ["invisibility"]
	},
	
	{
		"input": ["minecraft:sugar", "awkward"],
		"outputs": [
			[4, "swiftness"],
			[1, "thick"]
		]
	},
	{
		"input": ["minecraft:glistering_melon_slice", "swiftness"],
		"output": ["long_swiftness"]
	},
	{
		"input": ["minecraft:tropical_fish", "awkward"],
		"outputs": [
			[4, "strength"],
			[1, "thick"]
		]
	},
	{
		"input": ["minecraft:carrot", "awkward"],
		"outputs": [
			[4, "night_vision"],
			[1, "thick"]
		]
	},
	{
		"input": ["minecraft:pufferfish", "awkward"],
		"output": ["water_breathing"]
	},
	{
		"input": ["minecraft:magma_cream", "awkward"],
		"output": ["fire_resistance"]
	},
	{
		"input": ["minecraft:blaze_powder", "awkward"],
		"output": ["flames"]
	},
	{
		"input": ["minecraft:fermented_spider_eye", "awkward"],
		"output": ["wither"]
	},
	
	{
		"input": ["minecraft:glistering_melon_slice", "awkward"],
		"output": ["healing"]
	},
	{
		"input": ["minecraft:spider_eye", "awkward"],
		"output": ["poison"]
	},
	{
		"input": ["minecraft:fermented_spider_eye", "awkward"],
		"output": ["harming"]
	},
	{
		"input": ["minecraft:slime_ball", "awkward"],
		"output": ["short_slowness"]
	},

	{
		"input": ["minecraft:black_dye", "mundane"],
		"output": ["black"]
	},
	{
		"input": ["minecraft:red_dye", "mundane"],
		"output": ["red"]
	},
	{
		"input": ["minecraft:green_dye", "mundane"],
		"output": ["green"]
	},
	{
		"input": ["minecraft:brown_dye", "mundane"],
		"output": ["brown"]
	},
	{
		"input": ["minecraft:blue_dye", "mundane"],
		"output": ["blue"]
	},
	{
		"input": ["minecraft:purple_dye", "mundane"],
		"output": ["purple"]
	},
	{
		"input": ["minecraft:cyan_dye", "mundane"],
		"output": ["cyan"]
	},
	{
		"input": ["minecraft:light_gray_dye", "mundane"],
		"output": ["light_gray"]
	},
	{
		"input": ["minecraft:gray_dye", "mundane"],
		"output": ["gray"]
	},
	{
		"input": ["minecraft:pink_dye", "mundane"],
		"output": ["pink"]
	},
	{
		"input": ["minecraft:lime_dye", "mundane"],
		"output": ["lime"]
	},
	{
		"input": ["minecraft:yellow_dye", "mundane"],
		"output": ["yellow"]
	},
	{
		"input": ["minecraft:light_blue_dye", "mundane"],
		"output": ["light_blue"]
	},
	{
		"input": ["minecraft:magenta_dye", "mundane"],
		"output": ["magenta"]
	},
	{
		"input": ["minecraft:orange_dye", "mundane"],
		"output": ["orange"]
	},
	{
		"input": ["minecraft:white_dye", "mundane"],
		"output": ["white"]
	},

	{
		"input": ["minecraft:yellow_dye", "red"],
		"output": ["fire_gradient"]
	},
	{
		"input": ["minecraft:red_dye", "yellow"],
		"output": ["fire_gradient"]
	},
	{
		"input": ["minecraft:black_dye", "white"],
		"output": ["black_white_gradient"]
	},
	{
		"input": ["minecraft:white_dye", "black"],
		"output": ["black_white_gradient"]
	},
	{
		"input": ["minecraft:white_dye", "light_blue"],
		"output": ["cloud_gradient"]
	},
	{
		"input": ["minecraft:light_blue_dye", "white"],
		"output": ["cloud_gradient"]
	},
	{
		"input": ["minecraft:glow_ink_sac", "mundane"],
		"output": ["rainbow_gradient"]
	}
]""";

	private void loadConfigArrowRecipes() {
		JsonArray jsonArray = loadConfig(CONFIG_FILE_ARROW_RECIPES, DEFAULT_ARROW_RECIPES).getAsJsonArray();
		for (JsonElement element : jsonArray) {
			JsonObject json = element.getAsJsonObject();
			JsonArray input = json.get("input").getAsJsonArray();
			Pattern inputPattern = CustomRecipe.patternFromString(input.get(0).getAsString(), true);
			Optional<PotionType> inputType = Optional.empty();
			if (input.size() > 1) {
				inputType = PotionType.parse(input.get(1).getAsString());
			}
			Pattern outputPattern = CustomRecipe.patternFromString(json.get("output").getAsString(), false);
			ARROW_RECIPES.add(new ArrowRecipe(inputPattern, inputType, outputPattern.toString()));
		}
	}
	private static final String CONFIG_FILE_ARROW_RECIPES = CONFIG_DIR + "arrow_recipes.json";
	private static final String DEFAULT_ARROW_RECIPES = """
[
	{
		"input": ["*"],
		"output": "{1}"
	}
]""";

	private void loadConfigFuel() {
		JsonObject jsonObject = loadConfig(CONFIG_FILE_FUEL, DEFAULT_FUEL).getAsJsonObject();
		for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			Identifier identifier = new Identifier(entry.getKey());
			if (!Registry.ITEM.containsId(identifier)) {
				LOGGER.warn("Unknown fuel item " + identifier);
				continue;
			}
			FUELS.put(identifier, entry.getValue().getAsInt());
		}
	}
	private static final String CONFIG_FILE_FUEL = CONFIG_DIR + "fuel.json";
	private static final String DEFAULT_FUEL = """
{
	"minecraft:blaze_powder": 20,
	"lapis_lazuli": 10
}""";

	private static void putReplacementPotion(Map<Identifier, Potion> map, String key, String customId, PotionType type) {
		Identifier identifier = new Identifier(key);
		if (!Registry.POTION.containsId(identifier)) {
			LOGGER.warn("Unknown key for potion replacement: " + key);
			return;
		}
		if (hasCustomPotion(customId, Optional.of(type))) {
			map.put(identifier, getOriginalPotion(customId, type));
		} else {
			LOGGER.warn("Unknown custom-potion-id for " + type + " potion replacement: " + customId);
		}
	}
	private void loadConfigOther() {
		JsonObject jsonObject = loadConfig(CONFIG_FILE_OTHER, DEFAULT_OTHER).getAsJsonObject();
		if (jsonObject.has("witch_normal")) {
			for (Entry<String, JsonElement> entry : jsonObject.get("witch_normal").getAsJsonObject().entrySet()) {
				putReplacementPotion(WITCH_POTIONS_NORMAL, entry.getKey(), entry.getValue().getAsString(), PotionType.Normal);
			}
		}
		if (jsonObject.has("witch_splash")) {
			for (Entry<String, JsonElement> entry : jsonObject.get("witch_splash").getAsJsonObject().entrySet()) {
				putReplacementPotion(WITCH_POTIONS_SPLASH, entry.getKey(), entry.getValue().getAsString(), PotionType.Splash);
			}
		}
		if (jsonObject.has("witch_lingering")) {
			for (Entry<String, JsonElement> entry : jsonObject.get("witch_lingering").getAsJsonObject().entrySet()) {
				putReplacementPotion(WITCH_POTIONS_LINGERING, entry.getKey(), entry.getValue().getAsString(), PotionType.Lingering);
			}
		}
		if (jsonObject.has("wandering_trader_night")) {
			WANDERING_TRADER_POTION = getOriginalPotion(jsonObject.get("wandering_trader_night").getAsString(), PotionType.Normal);
		}
	}
	private static final String CONFIG_FILE_OTHER = CONFIG_DIR + "other.json";
	private static final String DEFAULT_OTHER = """
{
	"witch_normal": {
		"fire_resistance": "fire_resistance_witch",
		"water_breathing": "water_breathing",
		"healing": "regeneration",
		"swiftness": "swiftness"
	},
	"witch_splash": {
		"weakness": "flames"
	},
	"witch_lingering": {
		"slowness": "short_slowness",
		"harming": "harming",
		"poison": "poison",
		"healing": "healing",
		"regeneration": "healing"
	},
	"wandering_trader_night": "invisibility"
}
""";
}
