<img src="uhc icon.png" alt="UHC Enchanting Table Icon" width="200"/> 
<h1>
    UHC Plugin 
</h1>

A Minecraft 1.16 UHC plugin built in Kotlin for PaperMC

## Purpose

UHC Plugin implements the gamemode of Ultra Hardcore for a standalone, self-hosted Minecraft PaperMC server. It is not intended for use in server networks, though it could theoretically be adapted. The game is balanced around an expected number of players of 6 to 20.

## Features

* Configurable game length and border size
* Competitive balance changes to vanilla Minecraft
* Custom world generation options
* Tons of "quirks," custom rules applied to the game
* Discord integration allowing automatic voice call separation
* PVP Practice area for spectators
* Customizable named and colored teams
* Management features for ops
* And much more (Features document coming soon)

## Tools

* Kotlin 1.4
* Java 11 or higher
* Intellij IDEA 2020.3.2 (or any version would probably work)
* PaperMC 1.16 (latest version preferred) 

## Setting up the Project

Clone the repo: `git clone https://github.com/Codeland473/uhc-plugin`

Create the file `user.gradle` in the project directory

Open the project directory in Intellij IDEA

Click `Add Configuration..` then `+ (Add New Configuration)`

Select `Gradle` from the dropdown

In the `Gradle Project` field type `uhc-plugin` or the project directory

In the `Tasks` field type `shadowJar`

Click Apply

Now you can run the configuration to build the jar

## Setting up the Server

Copy the built jar in `/build/libs` named `UHC Plugin-all.jar` into the `plugins` directory for your paper server

### Note

Compatibility with other plugins is not guaranteed. By its nature this plugin completely takes control of the server

Plugin only works with PaperMC, not Spigot

### Generated Files in Server Directory

* `uhc.properties` - Additional world generation options for uhc
* `discordData.txt` - Fill out with instructions in the file if you want to use discord integration
* `summaries/` - Created when a game completes, contains game summaries
* `nicknames.txt` - Created by the nickname command
* `linkData.txt` - Created by the discord integration when players link
* `ddns.properties` - Optional, API data for Google Domains 
* `scores.txt` - Currently unused

## Starting a game

When players join the server, they will be placed in a lobby where they can build in creative or join the lobby PVP arena.

Right clicking the item called `Open UHC Settings` will open up a menu where ops can change game settings like enabling quirks or setting the game length. 

Once all players are in the server, an op can run the `/uhca team join [color] [player name]` to add players to teams. A quicker way is to run the `/uhca team random [team size]` command to automatically add all players to random teams of a certain size.

An op can run the `/uhca start` command to start the game.
