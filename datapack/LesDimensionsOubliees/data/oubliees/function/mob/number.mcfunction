$summon minecraft:text_display ~$(ox) ~1.9 ~$(oz) {Tags:["oubliees.fx","oubliees.rise","oubliees.shrink","oubliees.new_fx"],text:{text:"$(t)",color:"$(color)",bold:true},background:0,shadow:true,billboard:"center",teleport_duration:1,brightness:{sky:15,block:15},transformation:{left_rotation:[0f,0f,0f,1f],right_rotation:[0f,0f,0f,1f],translation:[0f,0f,0f],scale:[$(sc)f,$(sc)f,$(sc)f]}}
scoreboard players set @e[type=text_display,tag=oubliees.new_fx] oubliees.life 22
tag @e[tag=oubliees.new_fx] remove oubliees.new_fx
