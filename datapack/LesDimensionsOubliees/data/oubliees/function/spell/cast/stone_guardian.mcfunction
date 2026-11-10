summon minecraft:iron_golem ^ ^ ^2.5 {Tags:["oubliees.summon","oubliees.new"],CustomName:{"text":"✦ Gardien de pierre","color":"green"},CustomNameVisible:1b,PersistenceRequired:1b,DeathLootTable:"minecraft:empty",attributes:[{id:"minecraft:max_health",base:120d},{id:"minecraft:scale",base:1.15d},{id:"minecraft:movement_speed",base:0.25d},{id:"minecraft:attack_damage",base:18d}],Health:120f,PlayerCreated:1b}
scoreboard players set @e[tag=oubliees.new] oubliees.life 45
execute as @e[tag=oubliees.new] at @s run particle minecraft:soul ~ ~1 ~ 0.4 0.8 0.4 0.05 30
execute as @e[tag=oubliees.new] at @s run function oubliees:fx/circle {color:"#60ffd0",glyph:"۞"}
tag @e[tag=oubliees.new] remove oubliees.new
playsound minecraft:entity.evoker.prepare_summon player @a ~ ~ ~ 1 1
function oubliees:fx/pillar {block:"lime_stained_glass",h:60}
advancement grant @s only oubliees:summon
