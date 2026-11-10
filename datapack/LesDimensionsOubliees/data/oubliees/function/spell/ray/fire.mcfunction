scoreboard players set #steps oubliees.tmp 0
scoreboard players set #hit oubliees.tmp 0
execute anchored eyes positioned ^ ^ ^0.8 run function oubliees:spell/ray/fire_step
playsound minecraft:entity.blaze.shoot player @a ~ ~ ~ 1 1.3
