# chiffre de dégâts (une décimale si besoin), orange et plus gros à partir de 8
scoreboard players operation #a oubliees.tmp = #d oubliees.tmp
scoreboard players operation #a oubliees.tmp /= #10 oubliees.tmp
scoreboard players operation #b oubliees.tmp = #d oubliees.tmp
scoreboard players operation #b oubliees.tmp %= #10 oubliees.tmp
execute store result storage oubliees:tmp a int 1 run scoreboard players get #a oubliees.tmp
execute store result storage oubliees:tmp b int 1 run scoreboard players get #b oubliees.tmp
function oubliees:mob/number_text with storage oubliees:tmp
execute if score #b oubliees.tmp matches 0 run function oubliees:mob/number_int with storage oubliees:tmp
data modify storage oubliees:tmp color set value "#fff4d0"
data modify storage oubliees:tmp sc set value 1.6
execute if score #d oubliees.tmp matches 80.. run data modify storage oubliees:tmp color set value "#ff8a2a"
execute if score #d oubliees.tmp matches 80.. run data modify storage oubliees:tmp sc set value 2.3
scoreboard players add #ni oubliees.tmp 1
scoreboard players operation #nm oubliees.tmp = #ni oubliees.tmp
scoreboard players operation #nm oubliees.tmp %= #5 oubliees.tmp
data modify storage oubliees:tmp ox set value 0.0
data modify storage oubliees:tmp oz set value 0.0
execute if score #nm oubliees.tmp matches 1 run data modify storage oubliees:tmp ox set value 0.45
execute if score #nm oubliees.tmp matches 2 run data modify storage oubliees:tmp ox set value -0.45
execute if score #nm oubliees.tmp matches 3 run data modify storage oubliees:tmp oz set value 0.45
execute if score #nm oubliees.tmp matches 4 run data modify storage oubliees:tmp oz set value -0.45
function oubliees:mob/number with storage oubliees:tmp
# barre de vie dans le nom (10 cases)
execute if entity @s[tag=oubliees.named] run function oubliees:mob/bar
