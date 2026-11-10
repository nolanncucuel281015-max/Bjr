effect give @s minecraft:resistance 6 4 true
particle minecraft:reverse_portal ~ ~1 ~ 0.5 1 0.5 0.4 120
playsound minecraft:block.end_portal.spawn player @s ~ ~ ~ 0.6 1.4
function oubliees:fx/circle {color:"#ffd84a",glyph:"۞"}
title @s times 10 50 20
title @s subtitle {"text":"Les Dimensions Oubliées","color":"gray"}
$title @s title {text:"$(name)",color:"$(color)",bold:true}
