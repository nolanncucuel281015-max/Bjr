effect give @s minecraft:slowness 2 2
data modify entity @s TicksFrozen set value 200
damage @s 3 minecraft:freeze
particle minecraft:snowflake ~ ~1 ~ 0.3 0.5 0.3 0.02 12
