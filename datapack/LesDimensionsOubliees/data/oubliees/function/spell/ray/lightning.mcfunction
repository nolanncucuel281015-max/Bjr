scoreboard players set #steps oubliees.tmp 0
scoreboard players set #hit oubliees.tmp 0
execute anchored eyes positioned ^ ^ ^0.8 run function oubliees:spell/ray/lightning_step
playsound minecraft:entity.lightning_bolt.thunder player @a ~ ~ ~ 1 1.3
