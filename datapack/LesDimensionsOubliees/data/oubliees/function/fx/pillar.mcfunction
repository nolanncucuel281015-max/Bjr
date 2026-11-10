$summon minecraft:block_display ~ ~ ~ {Tags:["oubliees.fx","oubliees.grow","oubliees.tall","oubliees.spin","oubliees.shrink","oubliees.new_fx"],block_state:{Name:"minecraft:$(block)"},teleport_duration:2,brightness:{sky:15,block:15},transformation:{left_rotation:[0f,0f,0f,1f],right_rotation:[0f,0f,0f,1f],translation:[-0.6f,0f,-0.6f],scale:[1.2f,0.1f,1.2f]}}
$scoreboard players set @e[type=block_display,tag=oubliees.new_fx] oubliees.dmgi $(h)
scoreboard players set @e[type=block_display,tag=oubliees.new_fx] oubliees.life 22
tag @e[tag=oubliees.new_fx] remove oubliees.new_fx
