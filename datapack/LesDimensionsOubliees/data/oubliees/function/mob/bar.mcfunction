scoreboard players operation #n oubliees.tmp = @s oubliees.hp
scoreboard players operation #n oubliees.tmp *= #10 oubliees.tmp
scoreboard players operation #n oubliees.tmp += @s oubliees.hpmax
scoreboard players remove #n oubliees.tmp 1
scoreboard players operation #n oubliees.tmp /= @s oubliees.hpmax
execute if score #n oubliees.tmp matches 11.. run scoreboard players set #n oubliees.tmp 10
execute if score #n oubliees.tmp matches ..-1 run scoreboard players set #n oubliees.tmp 0
execute store result storage oubliees:tmp n int 1 run scoreboard players get #n oubliees.tmp
data modify storage oubliees:tmp bar set value "green"
execute if score #n oubliees.tmp matches ..5 run data modify storage oubliees:tmp bar set value "yellow"
execute if score #n oubliees.tmp matches ..2 run data modify storage oubliees:tmp bar set value "red"
function oubliees:mob/bar_macro with storage oubliees:tmp
data modify entity @s CustomNameVisible set value 1b
