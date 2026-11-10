execute unless score @s oubliees.level matches 1.. run function oubliees:player/first_join
scoreboard players remove @s[scores={oubliees.cd=1..}] oubliees.cd 1
function oubliees:player/school
execute if score @s oubliees.use matches 1.. run function oubliees:player/use
scoreboard players set @s oubliees.use 0
execute if score @s oubliees.kills > @s oubliees.killsl run function oubliees:player/kill_xp
execute if score #t20 oubliees.tmp matches 0 run function oubliees:player/second
execute if score #t5 oubliees.tmp matches 0 if score @s oubliees.school matches 1.. run function oubliees:hud/show
execute if score @s oubliees.menu matches 1.. run function oubliees:menu/dispatch
