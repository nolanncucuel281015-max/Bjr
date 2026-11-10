scoreboard players set #steps oubliees.tmp 0
scoreboard players set #hit oubliees.tmp 0
execute anchored eyes positioned ^ ^ ^0.8 run function oubliees:spell/ray/void_step
playsound minecraft:entity.enderman.teleport player @a ~ ~ ~ 1 1.3
