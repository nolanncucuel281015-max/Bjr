execute positioned ~ ~-0.5 ~ as @e[distance=..3,type=!#oubliees:non_target,tag=!oubliees.summon,tag=!oubliees.caster] run function oubliees:spell/hit/fire with storage oubliees:cast
execute align y run function oubliees:fx/pillar {block:"orange_stained_glass",h:120}
function oubliees:fx/nova {block:"orange_stained_glass",size:60}
particle minecraft:flame ~ ~2 ~ 0.6 3 0.6 0.05 160 force
particle minecraft:lava ~ ~1 ~ 1 1 1 0 20 force
playsound minecraft:item.firecharge.use player @a ~ ~ ~ 1.5 0.7
