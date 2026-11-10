scoreboard players operation #k oubliees.tmp = @s oubliees.kills
scoreboard players operation #k oubliees.tmp -= @s oubliees.killsl
scoreboard players operation @s oubliees.killsl = @s oubliees.kills
scoreboard players operation #k oubliees.tmp *= #10 oubliees.tmp
scoreboard players add #k oubliees.tmp 4
scoreboard players operation @s oubliees.xp += #k oubliees.tmp
function oubliees:player/check_level
