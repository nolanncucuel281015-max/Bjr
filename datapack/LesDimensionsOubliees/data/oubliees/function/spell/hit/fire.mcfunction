$damage @s $(dmg) minecraft:in_fire by @p[tag=oubliees.caster]
data modify entity @s[type=!player] Fire set value 100s
particle minecraft:lava ~ ~1 ~ 0.3 0.4 0.3 0 6
