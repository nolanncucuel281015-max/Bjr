execute if score @s oubliees.menu matches 1 run function oubliees:menu/sheet
execute if score @s oubliees.menu matches 2 run function oubliees:guide/give
execute if score @s oubliees.menu matches 10..13 run function oubliees:menu/select
scoreboard players set @s oubliees.menu 0
