effect give @a[distance=..6] minecraft:instant_health 1 0
effect give @a[distance=..6] minecraft:regeneration 6 1
execute if score @s oubliees.level matches 20.. run effect give @a[distance=..6] minecraft:instant_health 1 1
particle minecraft:heart ~ ~1.2 ~ 1.5 0.6 1.5 0 16
particle minecraft:happy_villager ~ ~1 ~ 2 1 2 0 40
function oubliees:fx/pillar {block:"lime_stained_glass",h:50}
playsound minecraft:block.amethyst_block.chime player @a ~ ~ ~ 1.5 1.2
