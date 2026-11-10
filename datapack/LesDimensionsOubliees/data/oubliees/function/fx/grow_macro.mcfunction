$execute if entity @s[tag=!oubliees.flat,tag=!oubliees.tall] run data merge entity @s {start_interpolation:0,interpolation_duration:6,transformation:{scale:[$(s)f,$(s)f,$(s)f],translation:[$(h)f,$(h)f,$(h)f]}}
$execute if entity @s[tag=oubliees.flat] run data merge entity @s {start_interpolation:0,interpolation_duration:7,transformation:{scale:[$(s)f,0.08f,$(s)f],translation:[$(h)f,0f,$(h)f]}}
$execute if entity @s[tag=oubliees.tall,tag=!oubliees.thin] run data merge entity @s {start_interpolation:0,interpolation_duration:5,transformation:{scale:[1.2f,$(s)f,1.2f],translation:[-0.6f,0f,-0.6f]}}
$execute if entity @s[tag=oubliees.thin] run data merge entity @s {start_interpolation:0,interpolation_duration:5,transformation:{scale:[0.55f,$(s)f,0.55f],translation:[-0.27f,0f,-0.27f]}}
