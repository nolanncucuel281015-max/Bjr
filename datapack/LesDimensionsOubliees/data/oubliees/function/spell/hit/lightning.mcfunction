# Joueurs : jamais en créatif ou spectateur ; le PvP du serveur et le tir allié des équipes sont
# appliqués par Minecraft (damage ... by <joueur>). Les effets ne suivent que si le coup a porté.
execute if entity @s[type=player,gamemode=creative] run return 0
execute if entity @s[type=player,gamemode=spectator] run return 0
$execute store success score #ok oubliees.tmp run damage @s $(dmg) minecraft:lightning_bolt by @p[tag=oubliees.caster]
execute if score #ok oubliees.tmp matches 0 run return 0
effect give @s minecraft:glowing 2 0
particle minecraft:electric_spark ~ ~1 ~ 0.3 0.5 0.3 0.1 20
