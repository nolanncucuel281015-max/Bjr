# sort verrouillé (niveau trop bas) : repli sur le premier sort de l'école
execute if score @s oubliees.school matches 1 if score @s oubliees.spell matches 1 if score @s oubliees.level matches ..7 run scoreboard players set @s oubliees.spell 0
execute if score @s oubliees.school matches 1 if score @s oubliees.spell matches 2 if score @s oubliees.level matches ..14 run scoreboard players set @s oubliees.spell 0
execute if score @s oubliees.school matches 1 if score @s oubliees.spell matches 3 if score @s oubliees.level matches ..24 run scoreboard players set @s oubliees.spell 0
execute if score @s oubliees.school matches 2 if score @s oubliees.spell matches 1 if score @s oubliees.level matches ..7 run scoreboard players set @s oubliees.spell 0
execute if score @s oubliees.school matches 2 if score @s oubliees.spell matches 2 if score @s oubliees.level matches ..11 run scoreboard players set @s oubliees.spell 0
execute if score @s oubliees.school matches 2 if score @s oubliees.spell matches 3 if score @s oubliees.level matches ..15 run scoreboard players set @s oubliees.spell 0
execute if score @s oubliees.school matches 3 if score @s oubliees.spell matches 1 if score @s oubliees.level matches ..4 run scoreboard players set @s oubliees.spell 0
execute if score @s oubliees.school matches 3 if score @s oubliees.spell matches 2 if score @s oubliees.level matches ..14 run scoreboard players set @s oubliees.spell 0
execute if score @s oubliees.school matches 3 if score @s oubliees.spell matches 3 if score @s oubliees.level matches ..29 run scoreboard players set @s oubliees.spell 0
execute if score @s oubliees.school matches 4 if score @s oubliees.spell matches 1 if score @s oubliees.level matches ..2 run scoreboard players set @s oubliees.spell 0
execute if score @s oubliees.school matches 4 if score @s oubliees.spell matches 2 if score @s oubliees.level matches ..7 run scoreboard players set @s oubliees.spell 0
execute if score @s oubliees.school matches 4 if score @s oubliees.spell matches 3 if score @s oubliees.level matches ..29 run scoreboard players set @s oubliees.spell 0
execute if score @s oubliees.school matches 5 if score @s oubliees.spell matches 1 if score @s oubliees.level matches ..2 run scoreboard players set @s oubliees.spell 0
execute if score @s oubliees.school matches 5 if score @s oubliees.spell matches 2 if score @s oubliees.level matches ..19 run scoreboard players set @s oubliees.spell 0
execute if score @s oubliees.school matches 5 if score @s oubliees.spell matches 3 if score @s oubliees.level matches ..39 run scoreboard players set @s oubliees.spell 0
execute if score @s oubliees.school matches 1 if score @s oubliees.spell matches 0 run return run function oubliees:spell/try/fire_bolt
execute if score @s oubliees.school matches 1 if score @s oubliees.spell matches 1 run return run function oubliees:spell/try/fire_nova
execute if score @s oubliees.school matches 1 if score @s oubliees.spell matches 2 run return run function oubliees:spell/try/flame_pillar
execute if score @s oubliees.school matches 1 if score @s oubliees.spell matches 3 run return run function oubliees:spell/try/meteor
execute if score @s oubliees.school matches 2 if score @s oubliees.spell matches 0 run return run function oubliees:spell/try/ice_shard
execute if score @s oubliees.school matches 2 if score @s oubliees.spell matches 1 run return run function oubliees:spell/try/frost_nova
execute if score @s oubliees.school matches 2 if score @s oubliees.spell matches 2 run return run function oubliees:spell/try/frozen_prison
execute if score @s oubliees.school matches 2 if score @s oubliees.spell matches 3 run return run function oubliees:spell/try/frost_sentinel
execute if score @s oubliees.school matches 3 if score @s oubliees.spell matches 0 run return run function oubliees:spell/try/spark
execute if score @s oubliees.school matches 3 if score @s oubliees.spell matches 1 run return run function oubliees:spell/try/lightning_strike
execute if score @s oubliees.school matches 3 if score @s oubliees.spell matches 2 run return run function oubliees:spell/try/chain_lightning
execute if score @s oubliees.school matches 3 if score @s oubliees.spell matches 3 run return run function oubliees:spell/try/tempest
execute if score @s oubliees.school matches 4 if score @s oubliees.spell matches 0 run return run function oubliees:spell/try/natural_heal
execute if score @s oubliees.school matches 4 if score @s oubliees.spell matches 1 run return run function oubliees:spell/try/thorn_bolt
execute if score @s oubliees.school matches 4 if score @s oubliees.spell matches 2 run return run function oubliees:spell/try/summon_wolves
execute if score @s oubliees.school matches 4 if score @s oubliees.spell matches 3 run return run function oubliees:spell/try/stone_guardian
execute if score @s oubliees.school matches 5 if score @s oubliees.spell matches 0 run return run function oubliees:spell/try/blink
execute if score @s oubliees.school matches 5 if score @s oubliees.spell matches 1 run return run function oubliees:spell/try/void_bolt
execute if score @s oubliees.school matches 5 if score @s oubliees.spell matches 2 run return run function oubliees:spell/try/gravity_well
execute if score @s oubliees.school matches 5 if score @s oubliees.spell matches 3 run return run function oubliees:spell/try/singularity
