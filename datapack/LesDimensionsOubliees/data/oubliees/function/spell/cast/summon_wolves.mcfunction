summon minecraft:wolf ^1.5 ^ ^1.5 {Tags:["oubliees.summon","oubliees.new"],CustomName:{"text":"✦ Loup spectral","color":"aqua"},CustomNameVisible:1b,PersistenceRequired:1b,DeathLootTable:"minecraft:empty",attributes:[{id:"minecraft:max_health",base:30d},{id:"minecraft:scale",base:1.0d},{id:"minecraft:movement_speed",base:0.32d},{id:"minecraft:attack_damage",base:6d}],Health:30f,CollarColor:3b}
summon minecraft:wolf ^-1.5 ^ ^1.5 {Tags:["oubliees.summon","oubliees.new"],CustomName:{"text":"✦ Loup spectral","color":"aqua"},CustomNameVisible:1b,PersistenceRequired:1b,DeathLootTable:"minecraft:empty",attributes:[{id:"minecraft:max_health",base:30d},{id:"minecraft:scale",base:1.0d},{id:"minecraft:movement_speed",base:0.32d},{id:"minecraft:attack_damage",base:6d}],Health:30f,CollarColor:3b}
summon minecraft:wolf ^ ^ ^2.5 {Tags:["oubliees.summon","oubliees.new"],CustomName:{"text":"✦ Loup spectral","color":"aqua"},CustomNameVisible:1b,PersistenceRequired:1b,DeathLootTable:"minecraft:empty",attributes:[{id:"minecraft:max_health",base:30d},{id:"minecraft:scale",base:1.0d},{id:"minecraft:movement_speed",base:0.32d},{id:"minecraft:attack_damage",base:6d}],Health:30f,CollarColor:3b}
execute as @e[type=wolf,tag=oubliees.new] run data modify entity @s Owner set from entity @p[tag=oubliees.caster] UUID
scoreboard players set @e[tag=oubliees.new] oubliees.life 45
execute as @e[tag=oubliees.new] at @s run particle minecraft:soul ~ ~1 ~ 0.4 0.8 0.4 0.05 30
execute as @e[tag=oubliees.new] at @s run function oubliees:fx/circle {color:"#60ffd0",glyph:"۞"}
tag @e[tag=oubliees.new] remove oubliees.new
playsound minecraft:entity.evoker.prepare_summon player @a ~ ~ ~ 1 1
function oubliees:fx/nova {block:"lime_stained_glass",size:60}
advancement grant @s only oubliees:summon
