scoreboard players set #hit oubliees.tmp 1
function oubliees:spell/hit/fire with storage oubliees:cast
execute at @s positioned ~ ~1 ~ run function oubliees:fx/burst {block:"orange_stained_glass",size:18}
