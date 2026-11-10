advancement revoke @s only oubliees:key/chaos
give @s echo_shard[custom_name={"text":"Clé — Terres du Chaos","color":"#ff7a20","italic":false,"bold":true},item_model="minecraft:blaze_powder",lore=[{"text":"Ouvre un passage vers : Terres du Chaos","color":"gray","italic":false},{"text":"Niveau conseillé : 26","color":"yellow","italic":false},{"text":"","color":"gray","italic":false},{"text":"Maintenez clic droit pour voyager.","color":"aqua","italic":false},{"text":"Utilisée dans sa dimension, elle vous ramène.","color":"aqua","italic":false}],custom_data={"oubliees":{"key":"chaos"}},consumable={"consume_seconds":1.6,"animation":"bow","sound":"minecraft:block.portal.trigger","has_consume_particles":false},enchantment_glint_override=true,rarity="epic",max_stack_size=1]
execute if dimension oubliees:chaos run return run function oubliees:travel/return
function oubliees:travel/save
execute in oubliees:chaos run function oubliees:travel/arrive_chaos
