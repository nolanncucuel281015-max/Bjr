summon minecraft:block_display ~ ~1 ~ {Tags:["oubliees.fx","oubliees.spin_fast","oubliees.shrink","oubliees.new_fx"],block_state:{Name:"minecraft:black_concrete"},teleport_duration:2,transformation:{left_rotation:[0f,0f,0f,1f],right_rotation:[0f,0f,0f,1f],translation:[-0.6f,-0.6f,-0.6f],scale:[1.2f,1.2f,1.2f]}}
summon minecraft:block_display ~ ~1 ~ {Tags:["oubliees.fx","oubliees.spin","oubliees.shrink","oubliees.new_fx"],block_state:{Name:"minecraft:purple_stained_glass"},teleport_duration:2,brightness:{sky:15,block:15},transformation:{left_rotation:[0f,0f,0f,1f],right_rotation:[0f,0f,0f,1f],translation:[-1.1f,-1.1f,-1.1f],scale:[2.2f,2.2f,2.2f]}}
$scoreboard players set @e[type=block_display,tag=oubliees.new_fx] oubliees.life $(life)
tag @e[tag=oubliees.new_fx] remove oubliees.new_fx
