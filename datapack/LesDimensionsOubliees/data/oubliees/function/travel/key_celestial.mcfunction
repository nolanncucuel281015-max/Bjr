advancement revoke @s only oubliees:key/celestial
give @s echo_shard[custom_name={"text":"Clé — Royaume Céleste","color":"#bfeaff","italic":false,"bold":true},item_model="minecraft:amethyst_shard",lore=[{"text":"Ouvre un passage vers : Royaume Céleste","color":"gray","italic":false},{"text":"Niveau conseillé : 1","color":"yellow","italic":false},{"text":"","color":"gray","italic":false},{"text":"Maintenez clic droit pour voyager.","color":"aqua","italic":false},{"text":"Utilisée dans sa dimension, elle vous ramène.","color":"aqua","italic":false}],custom_data={"oubliees":{"key":"celestial"}},consumable={"consume_seconds":1.6,"animation":"bow","sound":"minecraft:block.portal.trigger","has_consume_particles":false},enchantment_glint_override=true,rarity="epic",max_stack_size=1]
execute if dimension oubliees:celestial run return run function oubliees:travel/return
function oubliees:travel/save
execute in oubliees:celestial run function oubliees:travel/arrive_celestial
