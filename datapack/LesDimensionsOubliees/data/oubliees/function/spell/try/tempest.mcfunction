execute if score @s oubliees.level matches ..29 run return run title @s actionbar {"text":"🔒 Tempête : niveau 30 requis","color":"red"}
execute if score @s oubliees.cd matches 1.. run return run function oubliees:spell/feedback/cooldown
execute unless score @s oubliees.mana matches 70.. run return run function oubliees:spell/feedback/no_mana
scoreboard players remove @s oubliees.mana 70
scoreboard players set @s oubliees.cd 400
scoreboard players add @s oubliees.xp 1
function oubliees:spell/power {base:50}
data modify storage oubliees:cast school set value "lightning"
tag @s add oubliees.caster
function oubliees:fx/circle {color:"#ffe24a",glyph:"۞"}
function oubliees:spell/cast/tempest
tag @s remove oubliees.caster
advancement grant @s only oubliees:first_spell
function oubliees:player/check_level
