# Potion Config Mod

## Setup

Allows you to completely reconfigure Minecraft's potions and potion recipes
When you first start Minecraft after installing this mod, a config file will be created that imitates Minecraft's default potion system.


TODO:

    hook into beacon and heart of the sea
    remove lingering potion -> tipped arrow recipes from REI (KubeJS?)

    add StatusEffect that causes another whenever some trigger occurs (onGrounded)
    add InstantEffect that removes the specified StatusEffect. Problem: how to specify which one to remove?
    reduce error messages in console
    show unstable potions in REI brewing
    don't show each brewing recipe 3 times in REI
    prevent use of invalid potions, add recipe to convert them to the correct bottle type

## Before publishing:
- check potion replacement of Witch, Wandering Trader
- check Stray's arrow
- remove "true || " from PotionConfigMod.loadConfig