# Régénération du mana : 2 + niveau / 10 par seconde
scoreboard players operation #r oubliees.tmp = @s oubliees.level
scoreboard players operation #r oubliees.tmp /= #10 oubliees.tmp
scoreboard players add #r oubliees.tmp 2
scoreboard players operation @s oubliees.mana += #r oubliees.tmp
scoreboard players operation @s oubliees.mana < @s oubliees.maxmana
function oubliees:armor/check
