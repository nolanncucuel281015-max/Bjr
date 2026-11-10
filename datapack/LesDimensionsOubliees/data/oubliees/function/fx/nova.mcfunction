$summon minecraft:block_display ~ ~0.1 ~ {Tags:["oubliees.fx","oubliees.grow","oubliees.flat","oubliees.spin","oubliees.shrink","oubliees.new_fx"],block_state:{Name:"minecraft:$(block)"},teleport_duration:2,brightness:{sky:15,block:15},transformation:{left_rotation:[0f,0f,0f,1f],right_rotation:[0f,0f,0f,1f],translation:[-0.1f,0f,-0.1f],scale:[0.2f,0.08f,0.2f]}}
$scoreboard players set @e[type=block_display,tag=oubliees.new_fx] oubliees.dmgi $(size)
scoreboard players set @e[type=block_display,tag=oubliees.new_fx] oubliees.life 10
tag @e[tag=oubliees.new_fx] remove oubliees.new_fx
