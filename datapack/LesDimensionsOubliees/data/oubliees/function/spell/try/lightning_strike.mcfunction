execute if score @s oubliees.level matches ..4 run return run title @s actionbar {"text":"🔒 Éclair : niveau 5 requis","color":"red"}
execute if score @s oubliees.cd matches 1.. run return run function oubliees:spell/feedback/cooldown
execute unless score @s oubliees.mana matches 20.. run return run function oubliees:spell/feedback/no_mana
scoreboard players remove @s oubliees.mana 20
scoreboard players set @s oubliees.cd 60
scoreboard players add @s oubliees.xp 1
function oubliees:spell/power {base:60}
data modify storage oubliees:cast school set value "lightning"
tag @s add oubliees.caster
function oubliees:fx/circle {color:"#ffe24a",glyph:"۞"}
function oubliees:spell/cast/lightning_strike
tag @s remove oubliees.caster
advancement grant @s only oubliees:first_spell
function oubliees:player/check_level
