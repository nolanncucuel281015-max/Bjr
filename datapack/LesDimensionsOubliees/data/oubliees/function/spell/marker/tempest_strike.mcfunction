$execute positioned ~$(x) ~ ~$(z) run summon minecraft:lightning_bolt ~ ~ ~
$execute positioned ~$(x) ~ ~$(z) as @e[distance=..3,type=!#oubliees:non_target,tag=!oubliees.summon] run function oubliees:spell/hit/lightning with entity @e[type=marker,tag=oubliees.tempest,limit=1,sort=nearest] data
