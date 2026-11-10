execute positioned ~ ~-1.6 ~ if block ~ ~ ~ #minecraft:replaceable if block ~ ~1 ~ #minecraft:replaceable run tp @s ~ ~ ~
execute at @s run particle minecraft:reverse_portal ~ ~1 ~ 0.3 0.6 0.3 0.2 60
execute at @s run function oubliees:fx/burst {block:"purple_stained_glass",size:16}
