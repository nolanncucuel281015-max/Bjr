execute store result score @s oubliees.rx run data get entity @s Pos[0]
execute store result score @s oubliees.ry run data get entity @s Pos[1]
execute store result score @s oubliees.rz run data get entity @s Pos[2]
execute if dimension minecraft:overworld run scoreboard players set @s oubliees.rdim 0
execute if dimension minecraft:the_nether run scoreboard players set @s oubliees.rdim 1
execute if dimension minecraft:the_end run scoreboard players set @s oubliees.rdim 2
execute if dimension oubliees:celestial run scoreboard players set @s oubliees.rdim 3
execute if dimension oubliees:corrupted run scoreboard players set @s oubliees.rdim 4
execute if dimension oubliees:ocean run scoreboard players set @s oubliees.rdim 5
execute if dimension oubliees:chaos run scoreboard players set @s oubliees.rdim 6
execute if dimension oubliees:eternal run scoreboard players set @s oubliees.rdim 7
execute if dimension oubliees:depths run scoreboard players set @s oubliees.rdim 8
execute if dimension oubliees:void run scoreboard players set @s oubliees.rdim 9
