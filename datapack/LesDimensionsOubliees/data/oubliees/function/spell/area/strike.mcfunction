summon minecraft:lightning_bolt ~ ~ ~
execute as @e[distance=..2.5,type=!#oubliees:non_target,tag=!oubliees.summon,tag=!oubliees.caster] run function oubliees:spell/hit/lightning with storage oubliees:cast
function oubliees:fx/burst {block:"yellow_stained_glass",size:25}
