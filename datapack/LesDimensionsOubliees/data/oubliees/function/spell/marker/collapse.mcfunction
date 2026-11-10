data modify entity @s data.dmg set from entity @s data.big
execute as @e[distance=..7,type=!#oubliees:non_target,tag=!oubliees.summon] run function oubliees:spell/hit/void with entity @e[type=marker,tag=oubliees.current,limit=1] data
particle minecraft:explosion_emitter ~ ~1 ~ 0 0 0 0 1 force
particle minecraft:reverse_portal ~ ~1 ~ 1 1 1 1.5 200 force
playsound minecraft:entity.warden.sonic_boom master @a ~ ~ ~ 1.5 0.7
function oubliees:fx/burst {block:"purple_stained_glass",size:60}
function oubliees:fx/nova {block:"purple_stained_glass",size:140}
