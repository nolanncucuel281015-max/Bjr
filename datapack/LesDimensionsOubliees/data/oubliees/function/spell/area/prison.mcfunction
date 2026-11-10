execute as @e[distance=..3.5,type=!#oubliees:non_target,tag=!oubliees.summon,tag=!oubliees.caster] run function oubliees:spell/area/prison_hit
execute align y run function oubliees:fx/spikes
function oubliees:fx/nova {block:"light_blue_stained_glass",size:70}
particle minecraft:snowflake ~ ~1 ~ 2 1 2 0.05 120 force
playsound minecraft:block.glass.break player @a ~ ~ ~ 1.5 0.6
