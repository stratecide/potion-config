package com.stratecide.potion_config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stratecide.potion_config.effects.CustomStatusEffect;
import com.stratecide.potion_config.mixin.BrewingRecipeRegistryAccessor;
import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.recipe.Ingredient;
import net.minecraft.tag.TagKey;
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

public class PotionConfigMod implements ModInitializer {

	public static final String MOD_ID = "potion-config";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final Map<String, Identifier> CUSTOM_IDS = new HashMap<>();
	public static final Map<Identifier, String> CUSTOM_IDS_REVERSE = new HashMap<>();
	public static Identifier getPotionIdentifier(String key) {
		Identifier identifier = CUSTOM_IDS.get(key);
		if (identifier == null)
			identifier = new Identifier(key);
		if (!Registry.POTION.containsId(identifier) && !key.contains(":"))
			identifier = new Identifier(MOD_ID, key);
		return identifier;
	}

	public static String getPotionKey(Potion potion) {
		Identifier identifier = Registry.POTION.getId(potion);
		for (Map.Entry<String, Identifier> entry : CUSTOM_IDS.entrySet()) {
			if (entry.getValue().equals(identifier))
				return entry.getKey();
		}
		return identifier.getPath();
	}

	private static int NEXT_UNSTABLE_ID = 0;
	public static final Map<Potion, Map<Potion, Integer>> UNSTABLE_POTIONS = new HashMap<>();

	public static final Map<Potion, CustomPotion> CUSTOM_POTIONS = new HashMap<>();
	public static CustomPotion getCustomPotion(Potion vanillaPotion) {
		CustomPotion result = CUSTOM_POTIONS.get(vanillaPotion);
		if (result != null)
			return result;
		Identifier identifier = Registry.POTION.getId(vanillaPotion);
		//LOGGER.warn("Attempted to get custom potion '" + identifier + "' but it doesn't exist!");
		return CustomPotion.empty(identifier);
	}

	public static final CraftingPotion CRAFTING_POTION = Registry.register(Registry.ITEM, new Identifier(MOD_ID, "crafting_potion"), new CraftingPotion());
	public static final List<CustomPotion> ARROW_POTIONS = new ArrayList<>();

	public static final Map<Identifier, Integer> FUELS = new HashMap<>();
	public static final Map<String, Potion> WITCH_POTIONS = new HashMap<>();
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

	@Override
	public void onInitialize() {
		StatusEffect test = CustomStatusEffect.MILK;
		loadConfigPotions();
		loadConfigArrows();
		loadConfigRecipes();
		loadConfigFuel();
		loadConfigOther();
	}

	private static JsonElement loadConfig(String filename, String defaultContent) {
		File file = new File(filename);
		String data;
		if (!file.exists()) {
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

	private void loadConfigPotions() {
		JsonObject jsonObject = loadConfig(CONFIG_FILE_EFFECTS, DEFAULT_EFFECTS).getAsJsonObject();
		for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			String id = entry.getKey();
			JsonObject json = entry.getValue().getAsJsonObject();
			Identifier identifier;
			if (json.has("replaces")) {
				identifier = new Identifier(json.get("replaces").getAsString());
				if (!Registry.POTION.containsId(identifier)) {
					LOGGER.warn("Missing potion " + identifier + ": can't be replaced by " + id);
					continue;
				}
				CUSTOM_IDS.put(id, identifier);
				CUSTOM_IDS_REVERSE.put(identifier, id);
			} else {
				identifier = new Identifier(id);
				if (!Registry.POTION.containsId(identifier)) {
					if (!id.contains(":")) {
						identifier = new Identifier(MOD_ID, id);
						Registry.register(Registry.POTION, identifier, new Potion());
					} else {
						LOGGER.warn("Missing potion " + id + ": If you want to create custom potions, they should have no namespace");
						continue;
					}
				}
			}
			Potion vanillaPotion = Registry.POTION.get(identifier);
			CustomPotion customPotion = CustomPotion.parse(identifier, json);
			CUSTOM_POTIONS.put(vanillaPotion, customPotion);
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
		"type": "craft",
		"color": "cc66ff",
		"duration": 200,
		"minecraft:wither": { "chance": 0.2 },
		"minecraft:regeneration": { "chance": 0.2 },
		"minecraft:nausea": { "chance": 0.4 }
	},
	"awkward": {
		"type": "splash",
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
		"after": {
			"duration": 100,
			"minecraft:slowness": { "chance": 0.75 },
			"potion-config:jump_drop": { "chance": 0.5 }
		}
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
		"minecraft:darkness": { "chance": 0.6 }
	},
	"cider": {
		"color": "b9812f",
		"duration": 600,
		"potion-config:drunk": {},
		"minecraft:nausea": { "amplifier": 1 },
		"minecraft:hunger": { "chance": 0.2 }
	},
	"vodka": {
		"color": "bbbbbb",
		"duration": 600,
		"potion-config:drunk": { "amplifier": 3 },
		"minecraft:nausea": { "amplifier": 3 },
		"minecraft:hunger": { "chance": 0.3 }
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
		"after": {
			"duration": 240,
			"minecraft:slow_falling": {}
		}
	},
	"creative_flight": {
		"color": "66bbff",
		"potion-config:creative_flight": {},
		"after": {
			"duration": 240,
			"minecraft:slow_falling": {}
		}
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
		"after": {
			"duration": 100,
			"minecraft:slowness": { "chance": 0.75 },
			"potion-config:jump_drop": { "chance": 0.5 }
		}
	},
	"long_swiftness": {
		"color": "7CAFC6",
		"duration": 12000,
		"minecraft:speed": { "amplifier": 1 },
		"minecraft:jump_boost": {},
		"potion-config:particles": { "color": "7CAFC6" },
		"after": {
			"duration": 100,
			"minecraft:slowness": { "chance": 0.75 },
			"potion-config:jump_drop": { "chance": 0.5 }
		}
	},
	"strength": {
		"color": "932423",
		"minecraft:strength": {},
		"minecraft:haste": { "amplifier": 1 },
		"potion-config:particles": { "color": "932423" },
		"after": {
			"duration": 100,
			"minecraft:mining_fatigue": { "chance": 0.7 },
			"minecraft:slowness": { "chance": 0.7 },
			"minecraft:weakness": { "chance": 0.7 }
		}
	},
	"night_vision": {
		"color": "1F1FA1",
		"minecraft:night_vision": {},
		"potion-config:particles": {
			"color": [0, "000000", "88ff88"]
		},
		"after": {
			"duration": 80,
			"minecraft:blindness": { "chance": 0.7 }
		}
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
		"type": "splash",
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
		"type": "linger",
		"color": "4E9331",
		"duration": 100,
		"minecraft:poison": {},
		"potion-config:particles": { "color": "4E9331" }
	},
	"short_slowness": {
		"type": "splash",
		"color": "5A6C81",
		"duration": 200,
		"minecraft:slowness": {},
		"potion-config:particles": { "color": "5A6C81" }
	},
	"healing": {
		"type": "linger",
		"color": "CD5CAB",
		"minecraft:instant_health": { "amplifier": 1 }
	},
	"harming": {
		"type": "linger",
		"color": "430A09",
		"minecraft:instant_damage": { "amplifier": 1 }
	},
	
	"black": {
		"type": "linger",
		"color": "1D1D21",
		"potion-config:particles": {
			"amplifier": 50,
			"color": "1D1D21"
		}
	},
	"red": {
		"type": "linger",
		"color": "B02E26",
		"potion-config:particles": {
			"amplifier": 50,
			"color": "B02E26"
		}
	},
	"green": {
		"type": "linger",
		"color": "5E7C16",
		"potion-config:particles": {
			"amplifier": 50,
			"color": "5E7C16"
		}
	},
	"brown": {
		"type": "linger",
		"color": "835432",
		"potion-config:particles": {
			"amplifier": 50,
			"color": "835432"
		}
	},
	"blue": {
		"type": "linger",
		"color": "3C44AA",
		"potion-config:particles": {
			"amplifier": 50,
			"color": "3C44AA"
		}
	},
	"purple": {
		"type": "linger",
		"color": "8932B8",
		"potion-config:particles": {
			"amplifier": 50,
			"color": "8932B8"
		}
	},
	"cyan": {
		"type": "linger",
		"color": "169C9C",
		"potion-config:particles": {
			"amplifier": 50,
			"color": "169C9C"
		}
	},
	"light_gray": {
		"type": "linger",
		"color": "9D9D97",
		"potion-config:particles": {
			"amplifier": 50,
			"color": "9D9D97"
		}
	},
	"gray": {
		"type": "linger",
		"color": "474F52",
		"potion-config:particles": {
			"amplifier": 50,
			"color": "474F52"
		}
	},
	"pink": {
		"type": "linger",
		"color": "F38BAA",
		"potion-config:particles": {
			"amplifier": 50,
			"color": "F38BAA"
		}
	},
	"lime": {
		"type": "linger",
		"color": "80C71F",
		"potion-config:particles": {
			"amplifier": 50,
			"color": "80C71F"
		}
	},
	"yellow": {
		"type": "linger",
		"color": "FED83D",
		"potion-config:particles": {
			"amplifier": 50,
			"color": "FED83D"
		}
	},
	"light_blue": {
		"type": "linger",
		"color": "3AB3DA",
		"potion-config:particles": {
			"amplifier": 50,
			"color": "3AB3DA"
		}
	},
	"magenta": {
		"type": "linger",
		"color": "C74EBD",
		"potion-config:particles": {
			"amplifier": 50,
			"color": "C74EBD"
		}
	},
	"orange": {
		"type": "linger",
		"color": "F9801D",
		"potion-config:particles": {
			"amplifier": 50,
			"color": "F9801D"
		}
	},
	"white": {
		"type": "linger",
		"color": "F9FFFE",
		"potion-config:particles": {
			"amplifier": 50,
			"color": "F9FFFE"
		}
	},
	
	"fire_gradient": {
		"type": "linger",
		"color": [20, "ff0000", "ffff00"],
		"potion-config:particles": {
			"amplifier": 50,
			"color": [20, "ff0000", "ffff00"]
		}
	},
	"black_white_gradient": {
		"type": "linger",
		"color": [40, "000000", "000000", "ffffff", "ffffff"],
		"potion-config:particles": {
			"amplifier": 50,
			"color": [40, "000000", "000000", "ffffff", "ffffff"]
		}
	},
	"cloud_gradient": {
		"type": "linger",
		"color": [0, "F9FFFE", "F9FFFE", "3AB3DA"],
		"potion-config:particles": {
			"amplifier": 50,
			"color": [0, "F9FFFE", "F9FFFE", "3AB3DA"]
		}
	},
	"rainbow_gradient": {
		"type": "linger",
		"color": [20, "ff0000", "ffff00", "00ff00", "00ffff", "0000ff", "ff00ff"],
		"potion-config:particles": {
			"amplifier": 50,
			"color": [20, "ff0000", "ffff00", "00ff00", "00ffff", "0000ff", "ff00ff"]
		}
	}
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
	private void loadConfigRecipes() {
		JsonObject jsonObject = loadConfig(CONFIG_FILE_RECIPES, DEFAULT_RECIPES).getAsJsonObject();
		for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			Potion input = Registry.POTION.get(getPotionIdentifier(entry.getKey()));
			if (input == Potions.EMPTY) {
				LOGGER.warn("skipped recipe for unknown potion " + entry.getKey());
				continue;
			}
			JsonObject ingredients = entry.getValue().getAsJsonObject();
			for (Map.Entry<String, JsonElement> ingredientEntry : ingredients.entrySet()) {
				Ingredient ingredient = ingredientFromString(ingredientEntry.getKey());
				Map<Potion, Integer> outputs = new HashMap<>();
				if (ingredientEntry.getValue().isJsonObject()) {
					for (Map.Entry<String, JsonElement> outputEntry : ingredientEntry.getValue().getAsJsonObject().entrySet()) {
						int chance = outputEntry.getValue().getAsInt();
						if (chance > 0) {
							outputs.put(Registry.POTION.get(getPotionIdentifier(outputEntry.getKey())), chance);
						}
					}
				} else {
					outputs.put(Registry.POTION.get(getPotionIdentifier(ingredientEntry.getValue().getAsString())), 1);
				}
				if (outputs.size() == 1) {
					for (Potion output : outputs.keySet())
						BrewingRecipeRegistryAccessor.getRecipes().add(new BrewingRecipeRegistry.Recipe(input, ingredient, output));
				} else if (outputs.size() > 1) {
					Potion unstable = Registry.register(Registry.POTION, new Identifier(MOD_ID, "unstable_" + NEXT_UNSTABLE_ID), new Potion());
					NEXT_UNSTABLE_ID += 1;
					UNSTABLE_POTIONS.put(unstable, outputs);
					BrewingRecipeRegistryAccessor.getRecipes().add(new BrewingRecipeRegistry.Recipe(input, ingredient, unstable));
				}
			}
		}
	}

	private static Ingredient ingredientFromString(String ingredientId) {
		if (ingredientId.startsWith("#")) {
			return Ingredient.fromTag(TagKey.of(Registry.ITEM_KEY, new Identifier(ingredientId.substring(1))));
		} else {
			Item item = Registry.ITEM.get(new Identifier(ingredientId));
			if (item == Items.AIR)
				throw new AssertionError("Invalid ingredient identifier : " + ingredientId);
			return Ingredient.ofItems(item);
		}
	}

	private static final String CONFIG_FILE_RECIPES = CONFIG_DIR + "recipes.json";
	private static final String DEFAULT_RECIPES = """
{
	"water": {
		"#minecraft:leaves": "mundane",
		"minecraft:nether_wart": "awkward",
		"minecraft:sugar": "lemonade",
		"minecraft:wheat": "beer"
	},
	"awkward": {
		"minecraft:phantom_membrane": {
			"slow_falling": 3,
			"floating": 2,
			"creative_flight": 1
		}
	}
}""";

	private void loadConfigArrows() {
		JsonArray jsonArray = loadConfig(CONFIG_FILE_ARROWS, DEFAULT_ARROWS).getAsJsonArray();
		for (JsonElement element : jsonArray) {
			Identifier identifier = getPotionIdentifier(element.getAsString());
			Potion potion = Registry.POTION.get(identifier);
			CustomPotion customPotion = CUSTOM_POTIONS.get(potion);
			if (customPotion == null || customPotion.getType() != PotionType.CraftIngredient) {
				LOGGER.warn("unable to add arrow type for identifier: missing crafting potion");
				continue;
			}
			ARROW_POTIONS.add(customPotion);
		}
	}
	private static final String CONFIG_FILE_ARROWS = CONFIG_DIR + "arrows.json";
	private static final String DEFAULT_ARROWS = """
[
	"thick"
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

	private void loadConfigOther() {
		JsonObject jsonObject = loadConfig(CONFIG_FILE_OTHER, DEFAULT_OTHER).getAsJsonObject();
		if (jsonObject.has("witch")) {
			for (Entry<String, JsonElement> entry : jsonObject.get("witch").getAsJsonObject().entrySet()) {
				WITCH_POTIONS.put(entry.getKey(), Registry.POTION.get(getPotionIdentifier(entry.getValue().getAsString())));
			}
		}
		if (jsonObject.has("wandering_trader_night")) {
			WANDERING_TRADER_POTION = Registry.POTION.get(getPotionIdentifier(jsonObject.get("wandering_trader_night").getAsString()));
		}
	}
	private static final String CONFIG_FILE_OTHER = CONFIG_DIR + "other.json";
	private static final String DEFAULT_OTHER = """
{
	"witch": {
		"water_breathing": "water_breathing_witch",
		"fire_resistance": "fire_resistance_witch",
		"healing": "regeneration_witch",
		"swiftness": "swiftness",
		"splash_harming": "harming",
		"splash_healing": "healing",
		"splash_regeneration": "healing",
		"splash_slowness": "short_slowness",
		"splash_poison": "poison",
		"splash_weakness": "flames"
	},
	"wandering_trader_night": "invisibility"
}
""";
}
