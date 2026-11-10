summon minecraft:marker ~ ~ ~ {Tags:["oubliees.spell","oubliees.tempest","oubliees.new"]}
data modify entity @e[type=marker,tag=oubliees.new,limit=1] data.dmg set from storage oubliees:cast dmg
data modify entity @e[type=marker,tag=oubliees.new,limit=1] data.owner set from entity @p[tag=oubliees.caster] UUID
scoreboard players set @e[type=marker,tag=oubliees.new,limit=1] oubliees.life 100

tag @e[type=marker,tag=oubliees.new] remove oubliees.new
function oubliees:fx/circle_long {color:"#ffe24a",glyph:"✵",life:100}
