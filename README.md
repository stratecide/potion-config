# Potion Config Mod

Allows you to completely reconfigure Minecraft's potions and potion recipes
When you first start Minecraft after installing this mod, a config file will be created that imitates Minecraft's default potion system.

## How to Use

### Config Files

### Resource Pack

If your config adds Floor Blocks or Portal Blocks, they'll have default textures provided by this mod.
If you want to change their appearance, you can change their textures by creating a resource pack.

for Portal Blocks: put your texture into
- assets/potion-config/textures/block/portal_<potion id>.png

for Floor Blocks: put your textures into
- assets/potion-config/textures/block/floor_<potion id>/top.png
- assets/potion-config/textures/block/floor_<potion id>/sides.png
- assets/potion-config/textures/block/floor_<potion id>/bottom.png

### Data Pack

This mod doesn't replace potions from any loot tables.
If loot tables contain potions with ids that aren't present in your config, they'll become "Uncraftable Potion"s.
To avoid finding uncraftable potions, the following vanilla loot tables may have to be modified using a data pack:
- data/minecraft/loot_tables/entities/stray.json
- data/minecraft/loot_tables/chests/ancient_city.json
- data/minecraft/loot_tables/chests/buried_treasure.json
- data/minecraft/loot_tables/gameplay/piglin_bartering.json

Furthermore, the structures igloo/bottom and end_city/ship contain potions in their NBT data.
Those can't be replaced as easily, so I recommend defining the following potions:
- splash potion with id "weakness"
- normal potion with id "strong_healing"

## future plans

- remove lingering potion -> tipped arrow recipes from REI (KubeJS?)
- add EXP effect to handle Experience Bottles
- handle Dragon's Breath and Experience Bottles
- replace tipped arrows from fletching villager (gift and trade)
- add StatusEffect that causes another whenever some trigger (onGrounded, inWater, ...) occurs
- reduce error messages in console
- show unstable potions in REI brewing
- don't show each brewing recipe 3 times in REI
- prevent use of invalid potions, add recipe to convert them to the correct bottle type
- add recipes to JEI, EMI

## Checklist before publishing:
- check potion replacement of Witch, Wandering Trader
- check Stray's arrow, loot
- remove "true || " from PotionConfigMod.loadConfig