execute if score @s oubliees.level matches ..29 run return run title @s actionbar {"text":"🔒 Gardien de pierre : niveau 30 requis","color":"red"}
execute if score @s oubliees.cd matches 1.. run return run function oubliees:spell/feedback/cooldown
execute unless score @s oubliees.mana matches 80.. run return run function oubliees:spell/feedback/no_mana
scoreboard players remove @s oubliees.mana 80
scoreboard players set @s oubliees.cd 1200
scoreboard players add @s oubliees.xp 1
function oubliees:spell/power {base:0}
data modify storage oubliees:cast school set value "nature"
tag @s add oubliees.caster
function oubliees:fx/circle {color:"#6ce06c",glyph:"۞"}
function oubliees:spell/cast/stone_guardian
tag @s remove oubliees.caster
advancement grant @s only oubliees:first_spell
function oubliees:player/check_level
