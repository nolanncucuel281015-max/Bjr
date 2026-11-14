# Joueurs : jamais en créatif ou spectateur ; le PvP du serveur et le tir allié des équipes sont
# appliqués par Minecraft (damage ... by <joueur>). Les effets ne suivent que si le coup a porté.
execute if entity @s[type=player,gamemode=creative] run return 0
execute if entity @s[type=player,gamemode=spectator] run return 0
$execute store success score #ok oubliees.tmp run damage @s $(dmg) minecraft:in_fire by @p[tag=oubliees.caster]
execute if score #ok oubliees.tmp matches 0 run return 0
data modify entity @s[type=!player] Fire set value 100s
particle minecraft:lava ~ ~1 ~ 0.3 0.4 0.3 0 6
