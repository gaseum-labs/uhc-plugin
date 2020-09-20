# uhc-plugin

## Nether

### Portals
Destination portals in the nether will be created at the exact same coordinates of the portal in the overworld. Portals are centered on their nether portal block with the lowest x y and z coordinates. This change allows for more spacing of players in the nether and guarantees that every portal will be linked to a unique portal at the same coordinates in the other dimension. Before this fix, the overworld border only allowed for a roughly 120 x 120 area in the nether where portals could be generated. This led to duplicate linking and a guaranteed fight that usually ended the game as soon as teams made it to the nether. There are only two situations where a destination portal will not be at the same coordinates as the portal you entered: if the destination portal in the nether would spawn you below the lava ocean or in or above the bedrock ceiling; if the destination portal in the overworld would spawn you outside the border. Portals too low or too high in the nether are raised or lowered respectively. There is a 10 block buffer from the border in the overworld where a destination portal can generate so you are guaranteed to be at least 10 blocks away from the border when you come back from the nether.

### Nether Warts
1 in every 4 chunks attempts to generate a single nether wart between y levels 32 to 99. The placement within the chunk depends on the biome it attempts to generate in. For Nether Wastes, nether wart can only generate on the soul sand beaches that appear at y levels 32 to 34. This makes finding places where nether warts can generate rarer. But once you find a spot there is a high chance for clustering. For Basalt Deltas, nether wart can generate anywhere on top of a magma block. The magma block is converted to soul sand. magma blocks typically generate on the side of lava pools and at the bottom of basalt chimneys. For Crimson and Warped Forests, nether wart can replace any of the corresponding fungus or root for the biome. The nylium block below will be converted to soul sand. For Soul Sand Valleys, nether wart can generate anywhere there is soul sand which is almost anywhere.

### Blazes
Blazes now spawn naturally in the nether in all biomes by having a chance to replace the spawn of another mob. Approximately 1 in every 20 spawns will be replaced by blazes. This chance is increased to 1 in 12.5 in Basalt Deltas as a bonus for the difficulty of navigating in the area. Blazes are always guaranteed to drop exactly one blaze rod when killed by a player.

### Ghasts
Ghasts are guaranteed to drop exactly one ghast tear when killed by a player (as well as a single gunpowder). Ghast tear allows you to create potions of regeneration and end crystals.

## Overworld

### Apples
For every 200 leaves blocks you mine without shears or that decay, you are guaranteed 1 apple. The apple may drop anywhere from the 1st to the 200th but you cannot get more or less than 1 apple for every 200 leaves. This rate matches the average number of leaves you would have to mine before you would expect an apple drop in vanilla. Every type of leaves can drop apples. Sticks and saplings dropped from leaves work on the same system. It takes on average 50 blocks for each stick and 20 blocks for each sapling. These rates also match vanilla.

### Melons
1 in every 28 chunks attempts to generate a single melon somewhere on top of a grass block between y levels 63 to 99. This melon can only be generated in Forest, Flower Forest, Birch Forest, Tall Birch Forest, Dark Forest, Dark Forest Hills, Swamp, Swamp Hills, River, and Beach biomes.

### Oxeye Daisies
For each chunk, all but one oxeye daisy is removed from the surface of the world. This affects the two biomes where oxeyes can naturally generate: Plains, Sunflower Plains, and Flower Forests. Even with this removal, it is still common to find 2 or even 3 oxeye daisies in a cluster that has generated on chunk boundaries. Oxeye daisies may still generate on underground dirt patches which are unaffected by the removal. Only one check is performed per x, z coordinate, and due to the mountainous terrain of Flower Forests, this makes it possible that two oxeyes are allowed to stay if one is directly above the other on an overhang. Note that you may still use bonemeal in Plains, Sunflower Plains, and Flower Forests to generate oxeye daisies. Tip for using bonemeal: every x, z coordinate in a flower forest can only generate one type of flower, so find an oxeye daisy patch first, then bonemeal the ground there. You are guaranteed to get oxeye daisies as flowers.

### Mushroom Blocks
To stop dark forests from becoming by far the best and sometimes only way of acquiring mushrooms, the drop rates from mushroom blocks have been greatly reduced. For every 50 mushroom blocks you mine without silk touch, you are guaranteed 1 mushroom of the corresponding type. The mushroom may drop anywhere from the 1st to the 50th but you cannot get more or less than 1 mushroom for every 50 blocks. There is a separate count for red and brown mushroom blocks. Stems do not drop mushrooms. Breaking mushroom blocks is further nerfed, now taking slightly longer with no more instant mining given an iron axe or greater. Now an iron axe or greater is required to get mushrooms to drop. Stone tools and non-axes will cause the mushroom blocks to not drop anything. This ensures you have to actively collect mushrooms instead of merely passing through a dark forest and immediately acquiring enough mushrooms for the entire game.

### Chickens
To make it harder to reliably get two stacks of arrows by the end of every game, the feather rate for chickens is cut in half. On average, you can expect one feather for every two chickens you kill.

### Spiders
Spiders are guaranteed to drop exactly one string when killed by a player. This rate is the same as vanilla except without 0 and 2 string drops which wildly impact the amount of spiders needed to kill for certain crafts.

## Caves

### Ores
The generation of diamond, lapis, and gold ores have been changed. Coal, iron, redstone, and emerald remain unaffected. Diamond, lapis, and gold now are guaranteed to generate with at least one block in the vein exposed to a cave. These ores can no longer generate not on the side of a cave, thus effectively stopping strip mining like what originally was intended in UHC. The veins are guaranteed to be contiguous (every ore block is connected to another one). The ranges for the ores only reflect a single block that is guaranteed to be exposed to a cave. The rest of the vein may generate slightly above or below these limits. The lower limit for all of these ores is 10 because that is lava fill level, and below that no stone may be exposed to air.

|       |1 in every _ chunks|Low  |High |Min Vein|Max Vein|
|------:|:-----------------:|:---:|:---:|:------:|:------:|
|Diamond| 5                 |10   | 14  | 3      | 5      |
|Lapis  | 4                 |10   | 32  | 3      | 8      |
|Gold   | 3                 |10   | 32  | 5      | 8      |

### Mushrooms
1 in every 12 chunks attempts to generate a brown mushroom between y levels 11 and 42 in caves. The same is true for red mushrooms.

## Game Running

### Phases
There are 3 phases in UHC in which the game is running, as well as Waiting which comes before the game, and Postgame. The order of game phases is Grace, Shrink, Endgame. If the game ends during any of these, it will become Postgame instead of moving to the usual next phase.

### Grace
Upon starting, teams are teleported together in locations around the world. These locations lie on the perimeter of a shape that is halfway between a circle and square (a squircle) of the same radius as the world border. Teams will be spread approximately equidistantly around this squircle with some random deviation in angle. Teleportation searches for valid spots where the top block of the world at that x, z coordinate is solid in a slice of the squircle with minimum radius half of the border radius. During this phase, health can be naturally regenerated. You cannot directly attack other players (although placing lava still works). The border does not move. A timer on the boss bar tells you how much time is left before Shrink. If you die during this phase, you will be respawned somewhere random in the world within the border.

### Shrink
During this phase the world border is constantly shrinking from the starting radius to the final radius. When this phase ends the world border will be at the final radius. During this phase health cannot be naturally regenerated and pvp is enabled including friendly fire. If you die during this phase then you are out of the game permanently. In the nether this phase works slightly differently. There is no border in the nether, allowing you to explore without limit. But if you do not return to the overworld by the time Endgame starts you are instantly killed.

### Endgame
We’ll be discussing the clear blocks version of endgame. First, a random y level between 50 and 60 is chosen as the center. There is a top and bottom y limit, outside which all blocks including bedrock are destroyed, that increasingly converge on the center. The top limit starts at 255 and the bottom limit starts the distance from the center to 255 but in the negative direction. The bottom limit is treated as 0 until it becomes positive. Every second both of these limits converge by 1 until only the center layer remains. After this however 3 blocks above the center layer may be built on without being destroyed. Just like shrink, health can not be naturally generated and pvp is enabled. This phase ends when only one winning team remains.

### Kill Reward
Kill reward is an incentive for aggressing and getting kills. Kill reward triggers when the last surviving member of a team is killed and is applied to each surviving member of the team of the player who landed the final hit. The current reward is regeneration which nets the rewarded team 2 hearts over 10 seconds (about the same regeneration as a golden apple). This compensates for the many hearts that would probably be lost in a team fight.

### Bows
As you all know by now there is a 25% chance that bows will deal an extra half heart of damage (that scales with power). Random crits have no place in any competitive game so they have been removed.

### Care Packages
Care packages were fun while they lasted but we have seen how they transform the game too far away from how UHC was intended. We have a plan to slowly phase out care packages over the next few games. First the items strength and amounts will be nerfed. Then the amount of care packages will be reduced. Ultimately we would like to have them out of the main game. But fear not! Care Packages will not completely go away. They will return to being a CHC where I'm sure they will be a staple for games to come (with the buffs re-added).

### Mentions
In game chat you may now type @[someone’s name] to mention them. The mention text will be the color of that player’s team and they will receive a ding sound. You can also `/mute` a specific player (Deino) to prevent the dinging sound. In addition to mentioning a specific player you can also \@everyone, @players, and @spectators.

## Waiting

### UHC Gui
This item in the form of a music disk lets you see the current setup of the game in a gui. Only admins may change the settings in this menu. If you hold shift while clicking on certain quirks, you get to see a submenu that contains settings specific to the quirk. Even though this item is cleared when the game starts, you may see the menu at any time by using the command `/uhc gui`

### Anti Softlock
This item in the form of dried kelp lets you teleport to the middle of the waiting area if you fall in a hole you cannot get out of or are trapped by the parkour builders.

### Parkour checkpoint
This item in the form of a golden nugget will teleport you to the last gold block you have stood on top of. If you have not yet reached a gold block nothing will happen. If the last gold block you have stood on is removed, you will lose your checkpoint.
