# $(base) en dixièmes ; puissance = base × (100 + 3 × niveau) / 100
$scoreboard players set #b oubliees.tmp $(base)
scoreboard players operation #l oubliees.tmp = @s oubliees.level
scoreboard players operation #l oubliees.tmp *= #3 oubliees.tmp
scoreboard players add #l oubliees.tmp 100
scoreboard players operation #b oubliees.tmp *= #l oubliees.tmp
scoreboard players operation #b oubliees.tmp /= #100 oubliees.tmp
execute store result storage oubliees:cast dmg float 0.1 run scoreboard players get #b oubliees.tmp
