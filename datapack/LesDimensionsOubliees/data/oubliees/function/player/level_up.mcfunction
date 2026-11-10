scoreboard players operation @s oubliees.xp -= @s oubliees.xpnext
scoreboard players add @s oubliees.level 1
# expérience requise : 40 + 20 × niveau ; mana max : 100 + 6 × niveau
scoreboard players operation @s oubliees.xpnext = @s oubliees.level
scoreboard players operation @s oubliees.xpnext *= #20 oubliees.tmp
scoreboard players add @s oubliees.xpnext 40
scoreboard players operation @s oubliees.maxmana = @s oubliees.level
scoreboard players operation @s oubliees.maxmana *= #2 oubliees.tmp
scoreboard players operation @s oubliees.maxmana *= #3 oubliees.tmp
scoreboard players add @s oubliees.maxmana 100
scoreboard players operation @s oubliees.mana = @s oubliees.maxmana
execute if score @s oubliees.level matches 5 run attribute @s minecraft:max_health modifier add oubliees:level_5 2 add_value
execute if score @s oubliees.level matches 10 run attribute @s minecraft:max_health modifier add oubliees:level_10 2 add_value
execute if score @s oubliees.level matches 15 run attribute @s minecraft:max_health modifier add oubliees:level_15 2 add_value
execute if score @s oubliees.level matches 20 run attribute @s minecraft:max_health modifier add oubliees:level_20 2 add_value
execute if score @s oubliees.level matches 25 run attribute @s minecraft:max_health modifier add oubliees:level_25 2 add_value
execute if score @s oubliees.level matches 30 run attribute @s minecraft:max_health modifier add oubliees:level_30 2 add_value
execute if score @s oubliees.level matches 35 run attribute @s minecraft:max_health modifier add oubliees:level_35 2 add_value
execute if score @s oubliees.level matches 40 run attribute @s minecraft:max_health modifier add oubliees:level_40 2 add_value
execute if score @s oubliees.level matches 45 run attribute @s minecraft:max_health modifier add oubliees:level_45 2 add_value
execute if score @s oubliees.level matches 50 run attribute @s minecraft:max_health modifier add oubliees:level_50 2 add_value
execute if score @s oubliees.level matches 10 run attribute @s minecraft:attack_damage modifier add oubliees:level_10 1 add_value
execute if score @s oubliees.level matches 20 run attribute @s minecraft:attack_damage modifier add oubliees:level_20 1 add_value
execute if score @s oubliees.level matches 30 run attribute @s minecraft:attack_damage modifier add oubliees:level_30 1 add_value
execute if score @s oubliees.level matches 40 run attribute @s minecraft:attack_damage modifier add oubliees:level_40 1 add_value
execute if score @s oubliees.level matches 50 run attribute @s minecraft:attack_damage modifier add oubliees:level_50 1 add_value
execute if score @s oubliees.level matches 10 run advancement grant @s only oubliees:level_10
execute if score @s oubliees.level matches 25 run advancement grant @s only oubliees:level_25
execute if score @s oubliees.level matches 50 run advancement grant @s only oubliees:level_50
title @s times 8 50 20
title @s subtitle [{"text":"Mana et vie augmentés — ","color":"yellow"},{"text":"/trigger oubliees.menu","color":"aqua"}]
title @s title [{"text":"NIVEAU ","color":"gold","bold":true},{"score":{"name":"@s","objective":"oubliees.level"},"color":"gold","bold":true}]
playsound minecraft:ui.toast.challenge_complete player @s ~ ~ ~ 0.8 1
playsound minecraft:entity.player.levelup player @a ~ ~ ~ 1 0.8
particle minecraft:totem_of_undying ~ ~1 ~ 0.5 0.8 0.5 0.3 60
function oubliees:fx/pillar {block:"yellow_stained_glass",h:140}
function oubliees:fx/circle {color:"#ffd84a",glyph:"۞"}
function oubliees:player/check_level
