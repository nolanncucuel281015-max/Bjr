$execute positioned ~ $(y) ~ run fill ~-3 ~ ~-3 ~3 ~3 ~3 minecraft:air
$execute positioned ~ $(y) ~ run fill ~-3 ~-1 ~-3 ~3 ~-1 ~3 minecraft:$(block)
$tp @s ~ $(y) ~
effect give @s minecraft:slow_falling 10 0
