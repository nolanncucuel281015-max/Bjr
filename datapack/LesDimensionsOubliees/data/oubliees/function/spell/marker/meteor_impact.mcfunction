execute as @e[distance=..4.5,type=!#oubliees:non_target,tag=!oubliees.summon] run function oubliees:spell/hit/fire with entity @e[type=marker,tag=oubliees.meteor,limit=1,sort=nearest] data
particle minecraft:explosion_emitter ~ ~1 ~ 0 0 0 0 1 force
particle minecraft:lava ~ ~1 ~ 2 1 2 0 40 force
playsound minecraft:entity.generic.explode master @a ~ ~ ~ 2 0.8
kill @e[type=item_display,tag=oubliees.meteor_rock,distance=..30,limit=1,sort=nearest]
function oubliees:fx/burst {block:"orange_stained_glass",size:50}
function oubliees:fx/nova {block:"orange_stained_glass",size:110}
