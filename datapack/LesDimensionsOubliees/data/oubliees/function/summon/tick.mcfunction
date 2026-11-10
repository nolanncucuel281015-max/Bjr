scoreboard players remove @s oubliees.life 1
execute if entity @s[tag=oubliees.sentinel] as @e[distance=..8,type=#oubliees:hostile,limit=3,sort=nearest] run function oubliees:summon/sentinel_freeze
particle minecraft:soul_fire_flame ~ ~1 ~ 0.3 0.5 0.3 0.01 3
execute if score @s oubliees.life matches ..0 run function oubliees:summon/expire
