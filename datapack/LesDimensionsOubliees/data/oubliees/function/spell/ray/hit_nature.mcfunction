scoreboard players set #hit oubliees.tmp 1
function oubliees:spell/hit/nature with storage oubliees:cast
execute at @s positioned ~ ~1 ~ run function oubliees:fx/burst {block:"lime_stained_glass",size:18}
