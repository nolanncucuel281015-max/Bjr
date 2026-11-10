tag @s add oubliees.current
execute as @e[distance=1.2..7,type=!#oubliees:non_target,tag=!oubliees.summon] at @s facing entity @e[type=marker,tag=oubliees.current,limit=1] feet run tp @s ^ ^ ^0.3
particle minecraft:reverse_portal ~ ~1 ~ 3 1 3 0.3 20
particle minecraft:dust{color:[0.69,0.4,1.0],scale:2} ~ ~1 ~ 0.4 0.4 0.4 0 6
execute store result score #m oubliees.tmp run scoreboard players get @s oubliees.life
scoreboard players operation #m oubliees.tmp %= #20 oubliees.tmp
execute if score #m oubliees.tmp matches 0 as @e[distance=..2.5,type=!#oubliees:non_target,tag=!oubliees.summon] run function oubliees:spell/hit/void with entity @e[type=marker,tag=oubliees.current,limit=1] data
execute if score @s oubliees.life matches 0 if entity @s[tag=oubliees.collapse] run function oubliees:spell/marker/collapse
tag @s remove oubliees.current
