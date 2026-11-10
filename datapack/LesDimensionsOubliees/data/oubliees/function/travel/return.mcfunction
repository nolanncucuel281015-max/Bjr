execute store result storage oubliees:tmp x int 1 run scoreboard players get @s oubliees.rx
execute store result storage oubliees:tmp y int 1 run scoreboard players get @s oubliees.ry
execute store result storage oubliees:tmp z int 1 run scoreboard players get @s oubliees.rz
data modify storage oubliees:tmp dim set value "minecraft:overworld"
execute if score @s oubliees.rdim matches 0 run data modify storage oubliees:tmp dim set value "minecraft:overworld"
execute if score @s oubliees.rdim matches 1 run data modify storage oubliees:tmp dim set value "minecraft:the_nether"
execute if score @s oubliees.rdim matches 2 run data modify storage oubliees:tmp dim set value "minecraft:the_end"
execute if score @s oubliees.rdim matches 3 run data modify storage oubliees:tmp dim set value "oubliees:celestial"
execute if score @s oubliees.rdim matches 4 run data modify storage oubliees:tmp dim set value "oubliees:corrupted"
execute if score @s oubliees.rdim matches 5 run data modify storage oubliees:tmp dim set value "oubliees:ocean"
execute if score @s oubliees.rdim matches 6 run data modify storage oubliees:tmp dim set value "oubliees:chaos"
execute if score @s oubliees.rdim matches 7 run data modify storage oubliees:tmp dim set value "oubliees:eternal"
execute if score @s oubliees.rdim matches 8 run data modify storage oubliees:tmp dim set value "oubliees:depths"
execute if score @s oubliees.rdim matches 9 run data modify storage oubliees:tmp dim set value "oubliees:void"
execute unless score @s oubliees.rdim matches 0.. run return run function oubliees:travel/home
function oubliees:travel/return_macro with storage oubliees:tmp
execute at @s run function oubliees:travel/welcome {name:"Retour",color:"gold"}
