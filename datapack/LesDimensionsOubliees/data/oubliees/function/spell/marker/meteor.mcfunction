execute if score @s oubliees.life matches 1.. run particle minecraft:flame ~ ~0.2 ~ 2 0 2 0 6
execute if score @s oubliees.life matches 1.. run particle minecraft:dust{color:[1.0,0.48,0.12],scale:2} ~ ~0.1 ~ 2.5 0 2.5 0 6
execute if score @s oubliees.life matches 0 run function oubliees:spell/marker/meteor_impact
