scoreboard players set #ok oubliees.tmp 0
execute positioned as @s store success score #ok oubliees.tmp run spreadplayers ~ ~ 0 200 under 110 false @s
execute if score #ok oubliees.tmp matches 0 run function oubliees:travel/platform {block:"basalt",y:45}
execute at @s run function oubliees:travel/welcome {name:"Terres du Chaos",color:"#ff7a20"}
advancement grant @s only oubliees:dim_chaos
