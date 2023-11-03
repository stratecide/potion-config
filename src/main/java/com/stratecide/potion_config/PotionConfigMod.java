package com.stratecide.potion_config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stratecide.potion_config.blocks.floor.FloorBlock;
import com.stratecide.potion_config.blocks.floor.FloorBlockRecipe;
import com.stratecide.potion_config.blocks.floor.FloorBlockRecipeContainer;
import com.stratecide.potion_config.blocks.portal.PortalBlock;
import com.stratecide.potion_config.blocks.portal.PortalBlockRecipe;
import com.stratecide.potion_config.blocks.portal.PortalBlockRecipeContainer;
import com.stratecide.potion_config.effects.CustomStatusEffect;
import com.stratecide.potion_config.mixin.BrewingRecipeRegistryAccessor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.*;
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

	public static final Map<Potion, FloorBlock> FLOOR_BLOCKS = new HashMap<>();
	public static final SpecialRecipeSerializer<FloorBlockRecipe> FLOOR_BLOCK_RECIPE = RecipeSerializer.register(MOD_ID + ":crafting_special_floor", new SpecialRecipeSerializer<>(FloorBlockRecipe::new));
	public static final Map<Potion, FloorBlockRecipeContainer> FLOOR_BLOCK_RECIPES = new HashMap<>();

	public static final Map<Potion, PortalBlock> PORTAL_BLOCKS = new HashMap<>();
	public static final SpecialRecipeSerializer<PortalBlockRecipe> PORTAL_BLOCK_RECIPE = RecipeSerializer.register(MOD_ID + ":crafting_special_portal", new SpecialRecipeSerializer<>(PortalBlockRecipe::new));
	public static final Map<Potion, PortalBlockRecipeContainer> PORTAL_BLOCK_RECIPES = new HashMap<>();


	public static final Map<Identifier, Integer> FUELS = new HashMap<>();
	public static final Map<String, Potion> WITCH_POTIONS = new HashMap<>();
	public static Potion WANDERING_TRADER_POTION;
	public static final Map<EntityType, Potion> MILK_POTIONS = new HashMap<>();

	public static int TOOLTIP_MILLISECONDS = 2000;
	public static boolean HIDE_AFTER_EFFECTS_DISPLAY = false;
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
	public static boolean BLOCKS_DROP_SELF = true;
	public static Potion MILK_BUCKET_POTION = Potions.EMPTY;
	public static Potion HONEY_BOTTLE_POTION = Potions.EMPTY;
	public static final TrackedDataHandler<PotionColorList> POTION_PARTICLE_COLORS = TrackedDataHandler.of(PotionColorList::writePotionColors, PotionColorList::readPotionColors);

	static {
		loadConfigMain();
		TrackedDataHandlerRegistry.register(POTION_PARTICLE_COLORS);
	}

	@Override
	public void onInitialize() {
		StatusEffect test = CustomStatusEffect.MILK;
		loadConfigPotions();
		loadConfigRecipes();
		loadConfigOther();
	}

	private static JsonElement loadConfig(String filename, String defaultContent) {
		File file = new File(filename);
		String data;
		if (true || !file.exists()) {
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
		if (json.has("blocks_drop_self")) {
			BLOCKS_DROP_SELF = json.get("blocks_drop_self").getAsBoolean();
		}
	}
	private static final String CONFIG_FILE_MAIN = CONFIG_DIR + "general.json";
	private static final String DEFAULT_MAIN = """
{
	"stack_size": 16,
	"stack_size_splash": 4,
	"stack_size_lingering": 4,
	"glint": false,
	"hide_effects_below_chance": 0.0,
	"hide_after_effects": false,
	"mystery_normal": "awkward",
	"mystery_splash": "thick",
	"mystery_lingering": "rainbow_gradient",
	"mystery_arrow": "floating",
	"blocks_drop_self": true
}""";

	private void loadConfigPotions() {
		JsonObject jsonObject = loadConfig(CONFIG_FILE_EFFECTS, DEFAULT_EFFECTS).getAsJsonObject();
		for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			String id = entry.getKey();
			JsonObject json = entry.getValue().getAsJsonObject();
			Identifier potionId;
			if (json.has("replaces")) {
				potionId = new Identifier(json.get("replaces").getAsString());
				if (!Registry.POTION.containsId(potionId)) {
					LOGGER.warn("Missing potion " + potionId + ": can't be replaced by " + id);
					continue;
				}
				CUSTOM_IDS.put(id, potionId);
				CUSTOM_IDS_REVERSE.put(potionId, id);
			} else {
				potionId = new Identifier(id);
				if (!Registry.POTION.containsId(potionId)) {
					if (!id.contains(":")) {
						potionId = new Identifier(MOD_ID, id);
						Registry.register(Registry.POTION, potionId, new Potion());
					} else {
						LOGGER.warn("Missing potion " + id + ": If you want to create custom potions, they should have no namespace");
						continue;
					}
				}
			}
			Potion vanillaPotion = Registry.POTION.get(potionId);
			CustomPotion customPotion = CustomPotion.parse(potionId, json);
			CUSTOM_POTIONS.put(vanillaPotion, customPotion);
			if (customPotion.type == PotionType.CraftIngredient) {
				for (JsonElement purpose : json.get("type").getAsJsonArray()) {
					if (purpose.isJsonObject()) {
						JsonObject obj = purpose.getAsJsonObject();
						switch (obj.get("craft").getAsString()) {
							case "arrow" -> ARROW_POTIONS.add(customPotion);
							case "floor" -> {
								String ingredient = null;
								if (obj.has("ingredient"))
									ingredient = obj.get("ingredient").getAsString();
								int outputCount = 1;
								if (obj.has("count"))
									outputCount = obj.get("count").getAsInt();
								registerFloorBlock(id, vanillaPotion, customPotion, ingredient, outputCount);
							}
							case "portal" -> {
								String ingredient = null;
								if (obj.has("ingredient"))
									ingredient = obj.get("ingredient").getAsString();
								int outputCount = 1;
								if (obj.has("count"))
									outputCount = obj.get("count").getAsInt();
								registerPortalBlock(id, vanillaPotion, customPotion, ingredient, outputCount);
							}
							default -> LOGGER.warn("Unknown potion type " + purpose.getAsString());
						}
					} else switch (purpose.getAsString()) {
						case "arrow" -> ARROW_POTIONS.add(customPotion);
						case "floor" -> registerFloorBlock(id, vanillaPotion, customPotion, "#logs", 1);
						case "portal" -> registerPortalBlock(id, vanillaPotion, customPotion, "#c:glass_panes", 1);
						default -> LOGGER.warn("Unknown potion type " + purpose.getAsString());
					}
				}
			}
		}
	}
	private static void registerFloorBlock(String id, Potion vanillaPotion, CustomPotion customPotion, String recipeIngredient, int recipeOutput) {
		Identifier blockId = new Identifier(MOD_ID, "floor_" + id);
		FloorBlock block = new FloorBlock(ParticleTypes.AMBIENT_ENTITY_EFFECT, customPotion);
		FLOOR_BLOCKS.put(vanillaPotion, block);
		Registry.register(Registry.BLOCK, blockId, block);
		Registry.register(Registry.ITEM, blockId, new BlockItem(block, new FabricItemSettings().group(ItemGroup.BREWING)));
		if (recipeIngredient != null && recipeOutput > 0) {
			FLOOR_BLOCK_RECIPES.put(vanillaPotion, new FloorBlockRecipeContainer(ingredientFromString(recipeIngredient), recipeOutput));
		}
	}
	private static void registerPortalBlock(String id, Potion vanillaPotion, CustomPotion customPotion, String recipeIngredient, int recipeOutput) {
		Identifier blockId = new Identifier(MOD_ID, "portal_" + id);
		PortalBlock block = new PortalBlock(ParticleTypes.AMBIENT_ENTITY_EFFECT, customPotion);
		PORTAL_BLOCKS.put(vanillaPotion, block);
		Registry.register(Registry.BLOCK, blockId, block);
		Registry.register(Registry.ITEM, blockId, new BlockItem(block, new FabricItemSettings().group(ItemGroup.BREWING)));
		if (recipeIngredient != null && recipeOutput > 0) {
			PORTAL_BLOCK_RECIPES.put(vanillaPotion, new PortalBlockRecipeContainer(ingredientFromString(recipeIngredient), recipeOutput));
		}
	}
	private static final String CONFIG_FILE_EFFECTS = CONFIG_DIR + "effects.json";
	private static final String DEFAULT_EFFECTS = """
{
	"water": {
		"color": "0000ff",
		"duration": 1
	},
	"milk": {
		"color": "ffffff",
		"duration": 1,
		"potion-config:milk": {}
	},
	"honey": {
		"color": "ff9116",
		"duration": 1,
		"potion-config:remove_effect": { "effect": "poison" }
	},

	"weakness": {
		"type": "splash",
		"color": "484D48",
		"duration": 1200,
		"minecraft:weakness": {},
		"potion-config:particles": { "color": "484D48" }
	},
	"strong_healing": {
		"color": "F82423",
		"duration": 10,
		"minecraft:instant_health": { "amplifier": 1 },
		"potion-config:particles": { "color": "F82423", "amplifier": 400 }
	},
	"slowness": {
		"type": ["arrow"],
		"color": "5A6C81",
		"duration": 600,
		"minecraft:slowness": { "amplifier": 1 },
		"potion-config:particles": { "color": "5A6C81" }
	},
	"paralysis": {
		"type": "splash",
		"replaces": "minecraft:strong_slowness",
		"color": "5A6C81",
		"duration": 80,
		"minecraft:slowness": { "amplifier": 5 },
			"potion-config:jump_drop": { "amplifier": 1 },
		"potion-config:particles": { "color": "5A6C81", "amplifier": 50 }
	},
	"strong_regeneration": {
		"type": "linger",
		"color": "F82423",
		"duration": 10,
		"minecraft:instant_health": {},
		"potion-config:particles": { "color": "F82423", "amplifier": 200 }
	},
	"water_breathing": {
		"color": "2E5299",
		"duration": 3600,
		"minecraft:water_breathing": {},
		"potion-config:particles": { "color": "2E5299" }
	},
	"fire_resistance": {
		"color": "E49A3A",
		"duration": 3600,
		"minecraft:fire_resistance": {},
		"potion-config:particles": { "color": "E49A3A" }
	},

	"lemonade": {
		"color": "ddff00",
		"duration": 1200,
		"minecraft:speed": { "amplifier": 1 },
		"minecraft:jump_boost": {},
		"potion-config:particles": { "color": "ddff00" },
		"after": {
			"duration": 100,
			"minecraft:slowness": {},
			"potion-config:particles": { "color": "ddff00" }
		}
	},
	"beer": {
		"color": "d8c068",
		"duration": 600,
		"potion-config:drunk": { "chance": 0.5 },
		"minecraft:nausea": {},
		"minecraft:absorption": {},
		"potion-config:particles": { "color": "d8c068" },
		"after": {
			"duration": 50,
			"potion-config:drunk": { "chance": 0.5 }
		}
	},
	"cider": {
		"color": "b9812f",
		"duration": 600,
		"potion-config:drunk": {},
		"minecraft:nausea": { "amplifier": 1 },
		"minecraft:absorption": { "amplifier": 1 },
		"minecraft:hunger": { "chance": 0.2 },
		"potion-config:particles": { "color": "b9812f" },
		"after": {
			"duration": 50,
			"potion-config:drunk": { "chance": 0.5 },
			"minecraft:nausea": { "chance": 0.5 }
		}
	},

	"strength": {
		"color": "932423",
		"duration": 100,
		"minecraft:slowness": { "amplifier": 3 },
		"minecraft:resistance": { "amplifier": 2 },
		"potion-config:knockback_resistance": { "amplifier": 2 },
		"potion-config:particles": { "color": "5A6C81" },
		"after": {
			"duration": 300,
			"minecraft:strength": { "amplifier": 1 },
			"minecraft:resistance": { "amplifier": 1 },
			"potion-config:knockback_resistance": {},
			"potion-config:particles": { "color": "932423" }
		}
	},
	"harming": {
		"type": "linger",
		"color": "430A09",
		"duration": 10,
		"minecraft:instant_damage": {},
		"potion-config:particles": { "color": "430A09", "amplifier": 200 }
	},
	"poison": {
		"type": "splash",
		"color": "4E9331",
		"duration": 600,
		"minecraft:poison": {},
		"potion-config:particles": { "color": "4E9331" }
	},
	"night_vision": {
		"color": "1F1FA1",
		"duration": 40,
		"minecraft:blindness": {},
		"potion-config:particles": { "color": "1F1FA1" },
		"after": {
			"duration": 3600,
			"minecraft:night_vision": {},
			"potion-config:particles": { "color": "1F1FA1" }
		}
	},
	"invisibility": {
		"color": "7F8392",
		"duration": 40,
		"minecraft:glowing": {},
		"potion-config:particles": { "color": "FFFFFF", "amplifier": 200 },
		"after": {
			"duration": 1200,
			"minecraft:invisibility": {}
		}
	},
	"slow_falling": {
		"color": "FFEFD1",
		"duration": 1800,
		"minecraft:slow_falling": {},
		"potion-config:particles": { "color": "FFEFD1" }
	},

	"haste": {
		"replaces": "minecraft:leaping",
		"color": "D9C043",
		"duration": 40,
		"minecraft:mining_fatigue": {},
		"potion-config:particles": { "color": "D9C043" },
		"after": {
			"duration": 3600,
			"minecraft:haste": { "amplifier": 1 },
			"potion-config:finesse": {},
			"potion-config:particles": { "color": "D9C043" }
		}
	},
	"dolphins_grace": {
		"type": ["portal"],
		"color": "88A3BE",
		"duration": 100,
		"minecraft:dolphins_grace": { "amplifier": 1 },
		"minecraft:water_breathing": {},
		"potion-config:particles": { "color": "88A3BE" }
	},
	"gliding": {
		"type": ["portal"],
		"color": "787878",
		"duration": 1200,
		"potion-config:elytra": {},
		"potion-config:particles": { "color": "787878" },
		"after": {
			"duration": 80,
			"potion-config:no_fall_damage": {}
		}
	},
	"elytra_boost": {
		"type": ["portal"],
		"color": "787878",
		"duration": 200,
		"potion-config:elytra": { "amplifier": 1 },
		"potion-config:particles": { "color": "787878", "amplifier": 50 }
	},
	"jump_pad": {
		"type": ["floor"],
		"color": "1B8BCB",
		"duration": 10,
		"minecraft:jump_boost": { "amplifier": 7 },
		"potion-config:particles": { "color": "1B8BCB", "amplifier": 10 },
		"after": {
			"duration": 40,
			"potion-config:no_fall_damage": {}
		}
	},
	"boost_pad": {
		"type": ["floor"],
		"color": "C83A0E",
		"duration": 40,
		"minecraft:speed": { "amplifier": 2 },
		"potion-config:particles": { "color": "C83A0E", "amplifier": 5 }
	},
	"gambling": {
		"color": "F87D23",
		"duration": 3600,
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
	"rainbow": {
		"type": "linger",
		"color": [20, "ff0000", "ffff00", "00ff00", "00ffff", "0000ff", "ff00ff"],
		"duration": 600,
		"potion-config:particles": {
			"amplifier": 50,
			"color": [20, "ff0000", "ffff00", "00ff00", "00ffff", "0000ff", "ff00ff"]
		}
	},

	"mundane": {
		"type": "linger",
		"color": [0, "F9FFFE", "F9FFFE", "3AB3DA"],
		"duration": 200,
		"potion-config:particles": {
			"amplifier": 50,
			"color": [0, "F9FFFE", "F9FFFE", "3AB3DA"]
		}
	},
	"thick": {
		"type": "linger",
		"color": [20, "ff0000", "ffff00"],
		"duration": 100,
		"potion-config:flames": { "amplifier": 4 },
		"potion-config:particles": {
			"amplifier": 50,
			"color": [20, "ff0000", "ffff00"]
		}
	},
	"awkward": {
		"type": "splash",
		"color": [40, "000000", "000000", "ffffff", "ffffff"],
		"duration": 200,
		"minecraft:poison": { "chance": 0.3 },
		"minecraft:regeneration": { "chance": 0.3 },
		"minecraft:glowing": { "chance": 0.5 },
		"potion-config:particles": {
			"amplifier": 50,
			"color": [40, "000000", "000000", "ffffff", "ffffff"]
		}
	}
 }""";

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
		for (Map.Entry<String, JsonElement> entry : jsonObject.get("milk").getAsJsonObject().entrySet()) {
			Identifier entityId = new Identifier(entry.getKey());
			EntityType entityType = Registry.ENTITY_TYPE.get(entityId);
			if (entityType == EntityType.PIG && !entityId.equals(Registry.ENTITY_TYPE.getId(EntityType.PIG))) {
				LOGGER.warn("Mob " + entityId + " doesn't exist. (milk)");
				continue;
			}
			Potion potion = Registry.POTION.get(getPotionIdentifier(entry.getValue().getAsString()));
			if (potion == Potions.EMPTY) {
				LOGGER.warn("Potion " + entry.getValue() + " doesn't exist. (milk)");
				continue;
			}
			MILK_POTIONS.put(entityType, potion);
		}
		for (Entry<String, JsonElement> entry : jsonObject.get("fuel").getAsJsonObject().entrySet()) {
			Identifier identifier = new Identifier(entry.getKey());
			if (!Registry.ITEM.containsId(identifier)) {
				LOGGER.warn("Unknown fuel item " + identifier);
				continue;
			}
			FUELS.put(identifier, entry.getValue().getAsInt());
		}
		if (jsonObject.has("milk_bucket")) {
			Identifier identifier = new Identifier(jsonObject.get("milk_bucket").getAsString());
			Potion potion = Registry.POTION.get(identifier);
			if (!CUSTOM_POTIONS.containsKey(potion)) {
				throw new RuntimeException("MilkBucket potion " + identifier + " doesn't exist!");
			}
			MILK_BUCKET_POTION = potion;
		}
		if (jsonObject.has("honey_bottle")) {
			Identifier identifier = new Identifier(jsonObject.get("honey_bottle").getAsString());
			Potion potion = Registry.POTION.get(identifier);
			if (!CUSTOM_POTIONS.containsKey(potion)) {
				throw new RuntimeException("HoneyBottle potion " + identifier + " doesn't exist!");
			}
			HONEY_BOTTLE_POTION = potion;
		}
	}
	private static final String CONFIG_FILE_OTHER = CONFIG_DIR + "other.json";
	private static final String DEFAULT_OTHER = """
{
	"witch": {
		"water_breathing": "water_breathing",
		"fire_resistance": "fire_resistance",
		"healing": "strong_healing",
		"swiftness": "lemonade",
		"splash_harming": "harming",
		"splash_healing": "strong_regeneration",
		"splash_regeneration": "strong_regeneration",
		"splash_slowness": "paralysis",
		"splash_poison": "poison",
		"splash_weakness": "weakness"
	},
	"wandering_trader_night": "invisibility",
	"milk": {
		"cow": "potion-config:milk",
		"goat": "potion-config:milk"
	},
	"fuel": {
		"minecraft:blaze_powder": 20,
		"lapis_lazuli": 10
	},
	"milk_bucket": "potion-config:milk",
	"honey_bottle": "potion-config:honey"
}
""";
}
