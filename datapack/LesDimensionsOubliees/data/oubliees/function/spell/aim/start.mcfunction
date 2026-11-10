scoreboard players set #steps oubliees.tmp 0
$data modify storage oubliees:cast area set value "$(area)"
execute anchored eyes positioned ^ ^ ^0.6 run function oubliees:spell/aim/step with storage oubliees:cast
