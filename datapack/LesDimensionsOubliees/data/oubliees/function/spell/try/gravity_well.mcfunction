execute if score @s oubliees.level matches ..19 run return run title @s actionbar {"text":"🔒 Puits gravitationnel : niveau 20 requis","color":"red"}
execute if score @s oubliees.cd matches 1.. run return run function oubliees:spell/feedback/cooldown
execute unless score @s oubliees.mana matches 50.. run return run function oubliees:spell/feedback/no_mana
scoreboard players remove @s oubliees.mana 50
scoreboard players set @s oubliees.cd 300
scoreboard players add @s oubliees.xp 1
function oubliees:spell/power {base:20}
data modify storage oubliees:cast school set value "void"
tag @s add oubliees.caster
function oubliees:fx/circle {color:"#b066ff",glyph:"۞"}
function oubliees:spell/cast/gravity_well
tag @s remove oubliees.caster
advancement grant @s only oubliees:first_spell
function oubliees:player/check_level
