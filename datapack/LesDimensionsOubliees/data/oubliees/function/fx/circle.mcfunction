$summon minecraft:text_display ~ ~0.06 ~ {Tags:["oubliees.fx","oubliees.spin","oubliees.shrink","oubliees.new_fx"],text:{text:"$(glyph)",color:"$(color)"},background:0,billboard:"fixed",teleport_duration:2,brightness:{sky:15,block:15},Rotation:[0f,0f],transformation:{left_rotation:[-0.7071f,0f,0f,0.7071f],right_rotation:[0f,0f,0f,1f],translation:[0f,0f,1.1f],scale:[9f,9f,9f]}}
scoreboard players set @e[type=text_display,tag=oubliees.new_fx] oubliees.life 26
tag @e[tag=oubliees.new_fx] remove oubliees.new_fx
