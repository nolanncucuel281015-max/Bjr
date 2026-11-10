execute if score @s oubliees.level matches ..0 run return run title @s actionbar {"text":"🔒 Trait de feu : niveau 1 requis","color":"red"}
execute if score @s oubliees.cd matches 1.. run return run function oubliees:spell/feedback/cooldown
execute unless score @s oubliees.mana matches 10.. run return run function oubliees:spell/feedback/no_mana
scoreboard players remove @s oubliees.mana 10
scoreboard players set @s oubliees.cd 12
scoreboard players add @s oubliees.xp 1
function oubliees:spell/power {base:60}
data modify storage oubliees:cast school set value "fire"
tag @s add oubliees.caster
function oubliees:fx/circle {color:"#ff7a20",glyph:"۞"}
function oubliees:spell/cast/fire_bolt
tag @s remove oubliees.caster
advancement grant @s only oubliees:first_spell
function oubliees:player/check_level
