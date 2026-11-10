$damage @s $(dmg) minecraft:freeze by @p[tag=oubliees.caster]
effect give @s minecraft:slowness 3 2
data modify entity @s[type=!player] TicksFrozen set value 220
particle minecraft:snowflake ~ ~1 ~ 0.3 0.5 0.3 0.02 14
