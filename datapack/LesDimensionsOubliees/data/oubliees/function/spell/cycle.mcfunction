scoreboard players add @s oubliees.spell 1
execute if score @s oubliees.spell matches 4.. run scoreboard players set @s oubliees.spell 0
execute if score @s oubliees.school matches 1 if score @s oubliees.spell matches 1 if score @s oubliees.level matches ..7 run scoreboard players set @s oubliees.spell 0
execute if score @s oubliees.school matches 1 if score @s oubliees.spell matches 2 if score @s oubliees.level matches ..14 run scoreboard players set @s oubliees.spell 0
execute if score @s oubliees.school matches 1 if score @s oubliees.spell matches 3 if score @s oubliees.level matches ..24 run scoreboard players set @s oubliees.spell 0
execute if score @s oubliees.school matches 2 if score @s oubliees.spell matches 1 if score @s oubliees.level matches ..7 run scoreboard players set @s oubliees.spell 0
execute if score @s oubliees.school matches 2 if score @s oubliees.spell matches 2 if score @s oubliees.level matches ..11 run scoreboard players set @s oubliees.spell 0
execute if score @s oubliees.school matches 2 if score @s oubliees.spell matches 3 if score @s oubliees.level matches ..15 run scoreboard players set @s oubliees.spell 0
execute if score @s oubliees.school matches 3 if score @s oubliees.spell matches 1 if score @s oubliees.level matches ..4 run scoreboard players set @s oubliees.spell 0
execute if score @s oubliees.school matches 3 if score @s oubliees.spell matches 2 if score @s oubliees.level matches ..14 run scoreboard players set @s oubliees.spell 0
execute if score @s oubliees.school matches 3 if score @s oubliees.spell matches 3 if score @s oubliees.level matches ..29 run scoreboard players set @s oubliees.spell 0
execute if score @s oubliees.school matches 4 if score @s oubliees.spell matches 1 if score @s oubliees.level matches ..2 run scoreboard players set @s oubliees.spell 0
execute if score @s oubliees.school matches 4 if score @s oubliees.spell matches 2 if score @s oubliees.level matches ..7 run scoreboard players set @s oubliees.spell 0
execute if score @s oubliees.school matches 4 if score @s oubliees.spell matches 3 if score @s oubliees.level matches ..29 run scoreboard players set @s oubliees.spell 0
execute if score @s oubliees.school matches 5 if score @s oubliees.spell matches 1 if score @s oubliees.level matches ..2 run scoreboard players set @s oubliees.spell 0
execute if score @s oubliees.school matches 5 if score @s oubliees.spell matches 2 if score @s oubliees.level matches ..19 run scoreboard players set @s oubliees.spell 0
execute if score @s oubliees.school matches 5 if score @s oubliees.spell matches 3 if score @s oubliees.level matches ..39 run scoreboard players set @s oubliees.spell 0
playsound minecraft:item.book.page_turn player @s ~ ~ ~ 1 1.4
function oubliees:hud/show
