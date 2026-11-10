scoreboard players set #ok oubliees.tmp 0
execute positioned as @s store success score #ok oubliees.tmp run spreadplayers ~ ~ 0 200 false @s
execute if score #ok oubliees.tmp matches 0 run function oubliees:travel/platform {block:"blackstone",y:100}
execute at @s run function oubliees:travel/welcome {name:"Dimension Corrompue",color:"#e0405a"}
advancement grant @s only oubliees:dim_corrupted
