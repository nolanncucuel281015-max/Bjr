advancement revoke @s only oubliees:key/eternal
give @s echo_shard[custom_name={"text":"Clé — Monde Éternel","color":"#a8e4ff","italic":false,"bold":true},item_model="minecraft:blue_ice",lore=[{"text":"Ouvre un passage vers : Monde Éternel","color":"gray","italic":false},{"text":"Niveau conseillé : 32","color":"yellow","italic":false},{"text":"","color":"gray","italic":false},{"text":"Maintenez clic droit pour voyager.","color":"aqua","italic":false},{"text":"Utilisée dans sa dimension, elle vous ramène.","color":"aqua","italic":false}],custom_data={"oubliees":{"key":"eternal"}},consumable={"consume_seconds":1.6,"animation":"bow","sound":"minecraft:block.portal.trigger","has_consume_particles":false},enchantment_glint_override=true,rarity="epic",max_stack_size=1]
execute if dimension oubliees:eternal run return run function oubliees:travel/return
function oubliees:travel/save
execute in oubliees:eternal run function oubliees:travel/arrive_eternal
