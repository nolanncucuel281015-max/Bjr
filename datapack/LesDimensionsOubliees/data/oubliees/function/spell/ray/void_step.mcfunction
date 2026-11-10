particle minecraft:reverse_portal ~ ~ ~ 0.04 0.04 0.04 0.005 2 force
particle minecraft:dust{color:[0.69,0.4,1.0],scale:1.4} ~ ~ ~ 0.06 0.06 0.06 0 2 force
scoreboard players add #steps oubliees.tmp 1
execute positioned ~-0.5 ~-0.5 ~-0.5 as @e[dx=0,dy=0,dz=0,type=!#oubliees:non_target,tag=!oubliees.summon,tag=!oubliees.caster,limit=1] positioned ~0.5 ~0.5 ~0.5 run function oubliees:spell/ray/hit_void
execute if score #hit oubliees.tmp matches 1 run return 0
execute if score #steps oubliees.tmp matches 60.. run return run function oubliees:fx/burst {block:"purple_stained_glass",size:14}
execute unless block ^ ^ ^0.5 #minecraft:replaceable run return run function oubliees:fx/burst {block:"purple_stained_glass",size:14}
execute positioned ^ ^ ^0.5 run function oubliees:spell/ray/void_step
