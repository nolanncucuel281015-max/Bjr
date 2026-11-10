scoreboard players add #t oubliees.tmp 1
scoreboard players operation #t5 oubliees.tmp = #t oubliees.tmp
scoreboard players operation #t5 oubliees.tmp %= #5 oubliees.tmp
scoreboard players operation #t20 oubliees.tmp = #t oubliees.tmp
scoreboard players operation #t20 oubliees.tmp %= #20 oubliees.tmp
scoreboard players enable @a oubliees.menu
execute as @a at @s run function oubliees:player/tick
execute as @e[tag=oubliees.fx] at @s run function oubliees:fx/tick
execute as @e[type=marker,tag=oubliees.spell] at @s run function oubliees:spell/marker/tick
execute if score #t20 oubliees.tmp matches 0 as @e[tag=oubliees.summon] at @s run function oubliees:summon/tick
execute as @a at @s as @e[distance=..40,tag=!oubliees.lv,type=#oubliees:leveled] at @s run function oubliees:mob/init
execute as @e[tag=oubliees.lv] at @s if entity @a[distance=..24] run function oubliees:mob/track
