scoreboard players add #steps oubliees.tmp 1
$execute if score #steps oubliees.tmp matches 64.. run return run function oubliees:spell/area/$(area)
$execute positioned ~-0.5 ~-0.5 ~-0.5 if entity @e[dx=0,dy=0,dz=0,type=!#oubliees:non_target,tag=!oubliees.summon,tag=!oubliees.caster] positioned ~0.5 ~0.5 ~0.5 run return run function oubliees:spell/area/$(area)
$execute unless block ^ ^ ^0.5 #minecraft:replaceable run return run function oubliees:spell/area/$(area)
execute positioned ^ ^ ^0.5 run function oubliees:spell/aim/step with storage oubliees:cast
