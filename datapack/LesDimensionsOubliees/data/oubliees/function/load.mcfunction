scoreboard objectives add oubliees.level dummy
scoreboard objectives add oubliees.xp dummy
scoreboard objectives add oubliees.xpnext dummy
scoreboard objectives add oubliees.mana dummy
scoreboard objectives add oubliees.maxmana dummy
scoreboard objectives add oubliees.spell dummy
scoreboard objectives add oubliees.school dummy
scoreboard objectives add oubliees.cd dummy
scoreboard objectives add oubliees.cds dummy
scoreboard objectives add oubliees.use minecraft.used:minecraft.carrot_on_a_stick
scoreboard objectives add oubliees.kills minecraft.custom:minecraft.mob_kills
scoreboard objectives add oubliees.killsl dummy
scoreboard objectives add oubliees.menu trigger
scoreboard objectives add oubliees.life dummy
scoreboard objectives add oubliees.hp dummy
scoreboard objectives add oubliees.hpl dummy
scoreboard objectives add oubliees.hpmax dummy
scoreboard objectives add oubliees.rx dummy
scoreboard objectives add oubliees.ry dummy
scoreboard objectives add oubliees.rz dummy
scoreboard objectives add oubliees.rdim dummy
scoreboard objectives add oubliees.tmp dummy
scoreboard objectives add oubliees.sneak dummy
scoreboard objectives add oubliees.dmgi dummy
scoreboard players set #2 oubliees.tmp 2
scoreboard players set #3 oubliees.tmp 3
scoreboard players set #5 oubliees.tmp 5
scoreboard players set #10 oubliees.tmp 10
scoreboard players set #20 oubliees.tmp 20
scoreboard players set #100 oubliees.tmp 100
data modify storage oubliees:ui full set value ["","■","■■","■■■","■■■■","■■■■■","■■■■■■","■■■■■■■","■■■■■■■■","■■■■■■■■■","■■■■■■■■■■"]
data modify storage oubliees:ui empty set value ["■■■■■■■■■■","■■■■■■■■■","■■■■■■■■","■■■■■■■","■■■■■■","■■■■■","■■■■","■■■","■■","■",""]
tellraw @a[tag=oubliees.debug] {"text":"[Dimensions Oubliées] data pack chargé","color":"gold"}
