# ![UHC Enchanting Table Icon](uhc icon.png) UHC Plugin

A Minecraft 1.16 UHC plugin built in Kotlin for PaperMC


## Features

* Minecraft UHC in a randomly generated world
* Configurable game length and border size
* Configurable game phases
* Custom world generation options
* Tons of "quirks," custom rules applied to the game
* Discord integration allowing automatic voice call separation
* Waiting area with lobby PVP
* Vanilla-like experience
* Custom nether spawning and game balance changes
* Up to 91 teams

## Tools

* Kotlin 1.4
* Java 13 or higher
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
