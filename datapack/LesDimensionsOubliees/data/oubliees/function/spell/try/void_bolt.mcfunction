execute if score @s oubliees.level matches ..2 run return run title @s actionbar {"text":"🔒 Trait du néant : niveau 3 requis","color":"red"}
execute if score @s oubliees.cd matches 1.. run return run function oubliees:spell/feedback/cooldown
execute unless score @s oubliees.mana matches 12.. run return run function oubliees:spell/feedback/no_mana
scoreboard players remove @s oubliees.mana 12
scoreboard players set @s oubliees.cd 14
scoreboard players add @s oubliees.xp 1
function oubliees:spell/power {base:60}
data modify storage oubliees:cast school set value "void"
tag @s add oubliees.caster
function oubliees:fx/circle {color:"#b066ff",glyph:"۞"}
function oubliees:spell/cast/void_bolt
tag @s remove oubliees.caster
advancement grant @s only oubliees:first_spell
function oubliees:player/check_level
