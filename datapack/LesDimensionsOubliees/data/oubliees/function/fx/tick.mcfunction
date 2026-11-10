scoreboard players remove @s oubliees.life 1
execute if entity @s[tag=oubliees.spin] run tp @s ~ ~ ~ ~9 ~
execute if entity @s[tag=oubliees.spin_fast] run tp @s ~ ~ ~ ~-16 ~
execute if entity @s[tag=oubliees.fall] run tp @s ~ ~-0.7 ~ ~8 ~
execute if entity @s[tag=oubliees.rise] run tp @s ~ ~0.06 ~
execute if entity @s[tag=oubliees.grow] run function oubliees:fx/grow_apply
execute if score @s oubliees.life matches 3 if entity @s[tag=oubliees.shrink] run data merge entity @s {start_interpolation:0,interpolation_duration:3,transformation:{scale:[0f,0f,0f],translation:[0f,0f,0f]}}
execute if score @s oubliees.life matches ..0 run kill @s
