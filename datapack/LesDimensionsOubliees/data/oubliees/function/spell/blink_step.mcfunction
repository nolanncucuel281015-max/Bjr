scoreboard players add #steps oubliees.tmp 1
particle minecraft:portal ~ ~ ~ 0.1 0.1 0.1 0.5 3
execute if score #steps oubliees.tmp matches 24.. run return run function oubliees:spell/blink_land
execute unless block ^ ^ ^0.5 #minecraft:replaceable run return run function oubliees:spell/blink_land
execute positioned ^ ^ ^0.5 run function oubliees:spell/blink_step
