particle minecraft:reverse_portal ~ ~1 ~ 0.3 0.6 0.3 0.2 60
function oubliees:fx/burst {block:"purple_stained_glass",size:16}
scoreboard players set #steps oubliees.tmp 0
execute anchored eyes positioned ^ ^ ^0.5 run function oubliees:spell/blink_step
playsound minecraft:entity.enderman.teleport player @a ~ ~ ~ 1 1.2
