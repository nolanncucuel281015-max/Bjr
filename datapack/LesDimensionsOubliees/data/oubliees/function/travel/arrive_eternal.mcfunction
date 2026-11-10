scoreboard players set #ok oubliees.tmp 0
execute positioned as @s store success score #ok oubliees.tmp run spreadplayers ~ ~ 0 200 false @s
execute if score #ok oubliees.tmp matches 0 run function oubliees:travel/platform {block:"packed_ice",y:100}
execute at @s run function oubliees:travel/welcome {name:"Monde Éternel",color:"#a8e4ff"}
advancement grant @s only oubliees:dim_eternal
