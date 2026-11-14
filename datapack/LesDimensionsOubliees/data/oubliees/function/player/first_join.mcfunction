scoreboard players set @s oubliees.level 1
scoreboard players set @s oubliees.xp 0
scoreboard players set @s oubliees.xpnext 60
scoreboard players set @s oubliees.maxmana 106
scoreboard players set @s oubliees.mana 106
scoreboard players set @s oubliees.spell 0
scoreboard players set @s oubliees.cd 0
scoreboard players operation @s oubliees.killsl = @s oubliees.kills
function oubliees:guide/give
give @s carrot_on_a_stick[custom_name={"text":"Bâton de feu","color":"#ff7a20","italic":false,"bold":true},item_model="minecraft:blaze_rod",lore=[{"text":"École : Feu","color":"#ff7a20","italic":false},{"text":"","color":"gray","italic":false},{"text":"  Trait de feu — niv. 1","color":"gray","italic":false},{"text":"  Nova de feu — niv. 8","color":"gray","italic":false},{"text":"  Colonne de flammes — niv. 15","color":"gray","italic":false},{"text":"  Météore — niv. 25","color":"gray","italic":false},{"text":"","color":"gray","italic":false},{"text":"Clic droit : lancer le sort","color":"aqua","italic":false},{"text":"Accroupi + clic droit : sort suivant","color":"aqua","italic":false}],custom_data={"oubliees":{"staff":"fire"}},enchantment_glint_override=true,rarity="rare",unbreakable={}]
title @s times 10 70 20
title @s subtitle {"text":"Clic droit avec le bâton pour lancer un sort","color":"yellow"}
title @s title {"text":"Les Dimensions Oubliées","color":"gold","bold":true}
tellraw @s [{"text":"✦ Bienvenue ! ","color":"gold","bold":true},{"text":"Lisez le Grimoire des Anciens, ou tapez ","color":"gray"},{"text":"/trigger oubliees.menu","color":"aqua","click_event":{"action":"suggest_command","command":"/trigger oubliees.menu"}},{"text":" pour votre fiche de personnage.","color":"gray"}]
playsound minecraft:ui.toast.challenge_complete master @s ~ ~ ~ 0.6 1.2
advancement grant @s only oubliees:root
