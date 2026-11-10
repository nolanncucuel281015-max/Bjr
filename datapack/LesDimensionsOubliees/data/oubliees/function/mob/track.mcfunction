execute store result score @s oubliees.hp run data get entity @s Health 10
scoreboard players operation #d oubliees.tmp = @s oubliees.hpl
scoreboard players operation #d oubliees.tmp -= @s oubliees.hp
execute if score #d oubliees.tmp matches 5.. run function oubliees:mob/damaged
execute if score #d oubliees.tmp matches ..-10 run function oubliees:mob/healed
scoreboard players operation @s oubliees.hpl = @s oubliees.hp
