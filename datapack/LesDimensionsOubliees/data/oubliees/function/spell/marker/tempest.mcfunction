execute store result score #m oubliees.tmp run scoreboard players get @s oubliees.life
scoreboard players operation #m oubliees.tmp %= #10 oubliees.tmp
execute if score #m oubliees.tmp matches 0 run function oubliees:spell/marker/tempest_bolt
particle minecraft:electric_spark ~ ~6 ~ 4 0.5 4 0 10
particle minecraft:cloud ~ ~7 ~ 4 0.3 4 0 6
