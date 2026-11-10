advancement revoke @s only oubliees:key/ocean
give @s echo_shard[custom_name={"text":"Clé — Océan Oublié","color":"#3fd0c8","italic":false,"bold":true},item_model="minecraft:prismarine_crystals",lore=[{"text":"Ouvre un passage vers : Océan Oublié","color":"gray","italic":false},{"text":"Niveau conseillé : 18","color":"yellow","italic":false},{"text":"","color":"gray","italic":false},{"text":"Maintenez clic droit pour voyager.","color":"aqua","italic":false},{"text":"Utilisée dans sa dimension, elle vous ramène.","color":"aqua","italic":false}],custom_data={"oubliees":{"key":"ocean"}},consumable={"consume_seconds":1.6,"animation":"bow","sound":"minecraft:block.portal.trigger","has_consume_particles":false},enchantment_glint_override=true,rarity="epic",max_stack_size=1]
execute if dimension oubliees:ocean run return run function oubliees:travel/return
function oubliees:travel/save
execute in oubliees:ocean run function oubliees:travel/arrive_ocean
