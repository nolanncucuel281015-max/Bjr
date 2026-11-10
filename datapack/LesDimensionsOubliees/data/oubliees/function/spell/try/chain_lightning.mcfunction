execute if score @s oubliees.level matches ..14 run return run title @s actionbar {"text":"🔒 Chaîne d'éclairs : niveau 15 requis","color":"red"}
execute if score @s oubliees.cd matches 1.. run return run function oubliees:spell/feedback/cooldown
execute unless score @s oubliees.mana matches 35.. run return run function oubliees:spell/feedback/no_mana
scoreboard players remove @s oubliees.mana 35
scoreboard players set @s oubliees.cd 120
scoreboard players add @s oubliees.xp 1
function oubliees:spell/power {base:60}
data modify storage oubliees:cast school set value "lightning"
tag @s add oubliees.caster
function oubliees:fx/circle {color:"#ffe24a",glyph:"۞"}
function oubliees:spell/cast/chain_lightning
tag @s remove oubliees.caster
advancement grant @s only oubliees:first_spell
function oubliees:player/check_level
