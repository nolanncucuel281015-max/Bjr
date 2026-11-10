summon minecraft:marker ~ ~ ~ {Tags:["oubliees.spell","oubliees.meteor","oubliees.new"]}
data modify entity @e[type=marker,tag=oubliees.new,limit=1] data.dmg set from storage oubliees:cast dmg
data modify entity @e[type=marker,tag=oubliees.new,limit=1] data.owner set from entity @p[tag=oubliees.caster] UUID
scoreboard players set @e[type=marker,tag=oubliees.new,limit=1] oubliees.life 20
tag @e[type=marker,tag=oubliees.new] remove oubliees.new
summon minecraft:item_display ~ ~16 ~ {Tags:["oubliees.fx","oubliees.meteor_rock","oubliees.fall"],item:{id:"minecraft:magma_block",count:1},teleport_duration:1,brightness:{sky:15,block:15},transformation:{left_rotation:[0.2f,0.3f,0.1f,0.92f],right_rotation:[0f,0f,0f,1f],translation:[0f,0f,0f],scale:[3f,3f,3f]}}
scoreboard players set @e[type=item_display,tag=oubliees.meteor_rock,distance=..20,limit=1,sort=nearest] oubliees.life 24
function oubliees:fx/circle {color:"#ff5020",glyph:"✺"}
playsound minecraft:entity.ghast.shoot player @a ~ ~ ~ 1.5 0.6
