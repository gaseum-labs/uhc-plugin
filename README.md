<img src="uhc icon.png" alt="UHC Enchanting Table Icon" width="200"/> 
<h1>
    UHC Plugin 
</h1>

A Minecraft 1.17 UHC plugin built in Kotlin for PaperMC

UHC Plugin modifies many aspects of the server and is meant to be run by itself without other plugins.

## Requirements

* Kotlin 1.5
* Java 16
* [PaperMC 1.17](https://papermc.io/)
* [Protocollib 4.7.0](https://github.com/dmulloy2/ProtocolLib) 

## Setting up the Project *(outdated)*

Clone the repo: `git clone https://github.com/Codeland473/uhc-plugin`

Create the file `user.gradle` in the project directory

### In IntelliJ

Open the project directory in Intellij IDEA

Click `Add Configuration..` then `+ (Add New Configuration)`

Select `Gradle` from the dropdown

In the `Gradle Project` field type `uhc-plugin` or the project directory

In the `Tasks` field type `shadowJar`

Click Apply

Now you can run the configuration to build the jar

### In the command line

Navigate to the directory of the project and run `gradle build`

## Setting up the Server

Copy the jar built by the project into the `plugins` directory for your paper server

Also copy the Protocollib jar into the directory

### Generated Files in Server Directory

* `discordData.txt` - Optional, Discord bot token and Discord server id
* `ddns.properties` - Optional, API data for Google Domains
* `summaries/` - Directory containing game summaries

## Starting a game

When players join the server, they will be placed in a lobby where they can build in creative or join the lobby PVP arena.

Right clicking the item called `Open UHC Settings` will open up a menu where ops can change game settings like enabling quirks or setting the game length. 

Once all players are in the server, an op can run the `/uhca team create [player]` and `/uhca team join [team player] [added player]` to create teams. A quicker way is to run the `/uhca team random [team size]` command to automatically add all players to random teams of a certain size.

Then the game world must be loaded with the `/uhca worldRefresh` command

Then the `/uhca start` command will start the game.
