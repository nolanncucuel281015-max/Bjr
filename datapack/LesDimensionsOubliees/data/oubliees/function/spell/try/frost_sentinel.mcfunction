execute if score @s oubliees.level matches ..15 run return run title @s actionbar {"text":"🔒 Sentinelle de givre : niveau 16 requis","color":"red"}
execute if score @s oubliees.cd matches 1.. run return run function oubliees:spell/feedback/cooldown
execute unless score @s oubliees.mana matches 45.. run return run function oubliees:spell/feedback/no_mana
scoreboard players remove @s oubliees.mana 45
scoreboard players set @s oubliees.cd 600
scoreboard players add @s oubliees.xp 1
function oubliees:spell/power {base:0}
data modify storage oubliees:cast school set value "ice"
tag @s add oubliees.caster
function oubliees:fx/circle {color:"#8fe0ff",glyph:"۞"}
function oubliees:spell/cast/frost_sentinel
tag @s remove oubliees.caster
advancement grant @s only oubliees:first_spell
function oubliees:player/check_level
