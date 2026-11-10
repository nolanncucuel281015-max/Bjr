scoreboard players remove @s oubliees.life 1
function oubliees:util/tag_owner with entity @s data
execute if entity @s[tag=oubliees.meteor] run function oubliees:spell/marker/meteor
execute if entity @s[tag=oubliees.tempest] run function oubliees:spell/marker/tempest
execute if entity @s[tag=oubliees.well] run function oubliees:spell/marker/well
tag @a remove oubliees.caster
execute if score @s oubliees.life matches ..0 run kill @s
