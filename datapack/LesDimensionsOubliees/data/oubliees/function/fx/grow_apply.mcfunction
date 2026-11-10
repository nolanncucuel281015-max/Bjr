tag @s remove oubliees.grow
execute store result storage oubliees:tmp s float 0.1 run scoreboard players get @s oubliees.dmgi
scoreboard players operation #h oubliees.tmp = @s oubliees.dmgi
scoreboard players operation #h oubliees.tmp *= #5 oubliees.tmp
execute store result storage oubliees:tmp h float -0.01 run scoreboard players get #h oubliees.tmp
function oubliees:fx/grow_macro with storage oubliees:tmp
