execute as @e[distance=..5.5,type=!#oubliees:non_target,tag=!oubliees.summon,tag=!oubliees.caster] run function oubliees:spell/hit/ice with storage oubliees:cast
function oubliees:fx/nova {block:"light_blue_stained_glass",size:110}
function oubliees:fx/burst {block:"light_blue_stained_glass",size:30}
particle minecraft:snowflake ~ ~1 ~ 2.5 0.5 2.5 0.15 120
playsound minecraft:block.glass.break player @a ~ ~ ~ 1.2 0.8
playsound minecraft:entity.generic.explode player @a ~ ~ ~ 0.6 1.4
