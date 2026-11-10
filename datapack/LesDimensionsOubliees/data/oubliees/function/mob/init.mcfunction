tag @s add oubliees.lv
execute if dimension minecraft:overworld store result storage oubliees:tmp lvl int 1 run random value 1..4
execute if dimension minecraft:the_nether store result storage oubliees:tmp lvl int 1 run random value 15..18
execute if dimension minecraft:the_nether run attribute @s minecraft:max_health modifier add oubliees:dimension 0.30 add_multiplied_base
execute if dimension minecraft:the_nether run attribute @s minecraft:attack_damage modifier add oubliees:dimension 0.24 add_multiplied_base
execute if dimension minecraft:the_end store result storage oubliees:tmp lvl int 1 run random value 30..33
execute if dimension minecraft:the_end run attribute @s minecraft:max_health modifier add oubliees:dimension 0.60 add_multiplied_base
execute if dimension minecraft:the_end run attribute @s minecraft:attack_damage modifier add oubliees:dimension 0.48 add_multiplied_base
execute if dimension oubliees:celestial store result storage oubliees:tmp lvl int 1 run random value 6..9
execute if dimension oubliees:corrupted store result storage oubliees:tmp lvl int 1 run random value 16..19
execute if dimension oubliees:corrupted run attribute @s minecraft:max_health modifier add oubliees:dimension 0.15 add_multiplied_base
execute if dimension oubliees:corrupted run attribute @s minecraft:attack_damage modifier add oubliees:dimension 0.12 add_multiplied_base
execute if dimension oubliees:ocean store result storage oubliees:tmp lvl int 1 run random value 26..29
execute if dimension oubliees:ocean run attribute @s minecraft:max_health modifier add oubliees:dimension 0.30 add_multiplied_base
execute if dimension oubliees:ocean run attribute @s minecraft:attack_damage modifier add oubliees:dimension 0.24 add_multiplied_base
execute if dimension oubliees:chaos store result storage oubliees:tmp lvl int 1 run random value 40..43
execute if dimension oubliees:chaos run attribute @s minecraft:max_health modifier add oubliees:dimension 0.50 add_multiplied_base
execute if dimension oubliees:chaos run attribute @s minecraft:attack_damage modifier add oubliees:dimension 0.40 add_multiplied_base
execute if dimension oubliees:eternal store result storage oubliees:tmp lvl int 1 run random value 54..57
execute if dimension oubliees:eternal run attribute @s minecraft:max_health modifier add oubliees:dimension 0.70 add_multiplied_base
execute if dimension oubliees:eternal run attribute @s minecraft:attack_damage modifier add oubliees:dimension 0.56 add_multiplied_base
execute if dimension oubliees:depths store result storage oubliees:tmp lvl int 1 run random value 68..71
execute if dimension oubliees:depths run attribute @s minecraft:max_health modifier add oubliees:dimension 0.90 add_multiplied_base
execute if dimension oubliees:depths run attribute @s minecraft:attack_damage modifier add oubliees:dimension 0.72 add_multiplied_base
execute if dimension oubliees:void store result storage oubliees:tmp lvl int 1 run random value 82..85
execute if dimension oubliees:void run attribute @s minecraft:max_health modifier add oubliees:dimension 1.20 add_multiplied_base
execute if dimension oubliees:void run attribute @s minecraft:attack_damage modifier add oubliees:dimension 0.96 add_multiplied_base
execute store result entity @s Health float 1 run attribute @s minecraft:max_health get
execute store result score @s oubliees.hpl run data get entity @s Health 10
execute store result score @s oubliees.hpmax run attribute @s minecraft:max_health get 10
execute if entity @s[type=minecraft:zombie] run data modify storage oubliees:tmp type set value "zombie"
execute if entity @s[type=minecraft:husk] run data modify storage oubliees:tmp type set value "husk"
execute if entity @s[type=minecraft:drowned] run data modify storage oubliees:tmp type set value "drowned"
execute if entity @s[type=minecraft:zombie_villager] run data modify storage oubliees:tmp type set value "zombie_villager"
execute if entity @s[type=minecraft:skeleton] run data modify storage oubliees:tmp type set value "skeleton"
execute if entity @s[type=minecraft:stray] run data modify storage oubliees:tmp type set value "stray"
execute if entity @s[type=minecraft:bogged] run data modify storage oubliees:tmp type set value "bogged"
execute if entity @s[type=minecraft:wither_skeleton] run data modify storage oubliees:tmp type set value "wither_skeleton"
execute if entity @s[type=minecraft:spider] run data modify storage oubliees:tmp type set value "spider"
execute if entity @s[type=minecraft:cave_spider] run data modify storage oubliees:tmp type set value "cave_spider"
execute if entity @s[type=minecraft:creeper] run data modify storage oubliees:tmp type set value "creeper"
execute if entity @s[type=minecraft:enderman] run data modify storage oubliees:tmp type set value "enderman"
execute if entity @s[type=minecraft:witch] run data modify storage oubliees:tmp type set value "witch"
execute if entity @s[type=minecraft:slime] run data modify storage oubliees:tmp type set value "slime"
execute if entity @s[type=minecraft:magma_cube] run data modify storage oubliees:tmp type set value "magma_cube"
execute if entity @s[type=minecraft:blaze] run data modify storage oubliees:tmp type set value "blaze"
execute if entity @s[type=minecraft:ghast] run data modify storage oubliees:tmp type set value "ghast"
execute if entity @s[type=minecraft:phantom] run data modify storage oubliees:tmp type set value "phantom"
execute if entity @s[type=minecraft:pillager] run data modify storage oubliees:tmp type set value "pillager"
execute if entity @s[type=minecraft:vindicator] run data modify storage oubliees:tmp type set value "vindicator"
execute if entity @s[type=minecraft:evoker] run data modify storage oubliees:tmp type set value "evoker"
execute if entity @s[type=minecraft:ravager] run data modify storage oubliees:tmp type set value "ravager"
execute if entity @s[type=minecraft:piglin] run data modify storage oubliees:tmp type set value "piglin"
execute if entity @s[type=minecraft:piglin_brute] run data modify storage oubliees:tmp type set value "piglin_brute"
execute if entity @s[type=minecraft:zombified_piglin] run data modify storage oubliees:tmp type set value "zombified_piglin"
execute if entity @s[type=minecraft:hoglin] run data modify storage oubliees:tmp type set value "hoglin"
execute if entity @s[type=minecraft:zoglin] run data modify storage oubliees:tmp type set value "zoglin"
execute if entity @s[type=minecraft:guardian] run data modify storage oubliees:tmp type set value "guardian"
execute if entity @s[type=minecraft:elder_guardian] run data modify storage oubliees:tmp type set value "elder_guardian"
execute if entity @s[type=minecraft:shulker] run data modify storage oubliees:tmp type set value "shulker"
execute if entity @s[type=minecraft:silverfish] run data modify storage oubliees:tmp type set value "silverfish"
execute if entity @s[type=minecraft:endermite] run data modify storage oubliees:tmp type set value "endermite"
execute if entity @s[type=minecraft:vex] run data modify storage oubliees:tmp type set value "vex"
execute if entity @s[type=minecraft:breeze] run data modify storage oubliees:tmp type set value "breeze"
execute if entity @s[type=minecraft:creaking] run data modify storage oubliees:tmp type set value "creaking"
execute if entity @s[type=minecraft:warden] run data modify storage oubliees:tmp type set value "warden"
execute if entity @s[type=minecraft:wolf] run data modify storage oubliees:tmp type set value "wolf"
execute if entity @s[type=minecraft:polar_bear] run data modify storage oubliees:tmp type set value "polar_bear"
execute if entity @s[type=minecraft:goat] run data modify storage oubliees:tmp type set value "goat"
execute if entity @s[type=minecraft:llama] run data modify storage oubliees:tmp type set value "llama"
execute if entity @s[type=minecraft:panda] run data modify storage oubliees:tmp type set value "panda"
execute if entity @s[type=minecraft:bee] run data modify storage oubliees:tmp type set value "bee"
execute if entity @s[type=minecraft:iron_golem] run data modify storage oubliees:tmp type set value "iron_golem"
execute if entity @s[type=minecraft:cow] run data modify storage oubliees:tmp type set value "cow"
execute if entity @s[type=minecraft:pig] run data modify storage oubliees:tmp type set value "pig"
execute if entity @s[type=minecraft:sheep] run data modify storage oubliees:tmp type set value "sheep"
execute if entity @s[type=minecraft:chicken] run data modify storage oubliees:tmp type set value "chicken"
execute if entity @s[type=minecraft:rabbit] run data modify storage oubliees:tmp type set value "rabbit"
execute if entity @s[type=minecraft:horse] run data modify storage oubliees:tmp type set value "horse"
execute if entity @s[type=minecraft:fox] run data modify storage oubliees:tmp type set value "fox"
execute if entity @s[type=minecraft:mooshroom] run data modify storage oubliees:tmp type set value "mooshroom"
execute if entity @s[type=minecraft:armadillo] run data modify storage oubliees:tmp type set value "armadillo"
execute if entity @s[type=minecraft:allay] run data modify storage oubliees:tmp type set value "allay"
execute if entity @s[type=minecraft:glow_squid] run data modify storage oubliees:tmp type set value "glow_squid"
execute if entity @s[type=minecraft:turtle] run data modify storage oubliees:tmp type set value "turtle"
execute if entity @s[type=minecraft:parrot] run data modify storage oubliees:tmp type set value "parrot"
execute if entity @s[type=minecraft:camel] run data modify storage oubliees:tmp type set value "camel"
execute if entity @s[type=minecraft:frog] run data modify storage oubliees:tmp type set value "frog"
execute if entity @s[type=minecraft:sniffer] run data modify storage oubliees:tmp type set value "sniffer"
execute if entity @s[tag=oubliees.summon] run return 0
execute if data entity @s CustomName run return 0
function oubliees:mob/name with storage oubliees:tmp
