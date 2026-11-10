summon minecraft:marker ~ ~ ~ {Tags:["oubliees.spell","oubliees.well","oubliees.new"]}
data modify entity @e[type=marker,tag=oubliees.new,limit=1] data.dmg set from storage oubliees:cast dmg
data modify entity @e[type=marker,tag=oubliees.new,limit=1] data.owner set from entity @p[tag=oubliees.caster] UUID
scoreboard players set @e[type=marker,tag=oubliees.new,limit=1] oubliees.life 70
tag @e[type=marker,tag=oubliees.new,limit=1] add oubliees.collapse
data modify entity @e[type=marker,tag=oubliees.new,limit=1] data.big set from storage oubliees:cast dmg
data modify entity @e[type=marker,tag=oubliees.new,limit=1] data.dmg set value 2.0f
tag @e[type=marker,tag=oubliees.new] remove oubliees.new
function oubliees:fx/circle_long {color:"#b066ff",glyph:"۞",life:70}
function oubliees:fx/core {life:70}
