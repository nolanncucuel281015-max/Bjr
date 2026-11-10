scoreboard players set #steps oubliees.tmp 0
scoreboard players set #hit oubliees.tmp 0
execute anchored eyes positioned ^ ^ ^0.8 run function oubliees:spell/ray/ice_step
playsound minecraft:block.glass.break player @a ~ ~ ~ 1 1.3
