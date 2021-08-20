<img src="uhc icon.png" alt="UHC Enchanting Table Icon" width="200"/> 
<h1>
    UHC Plugin 
</h1>

A Minecraft 1.17 UHC plugin built in Kotlin for PaperMC

**UHC Plugin is built exclusively for private games and is not general-purpose. It is probably not what you are looking for**

## Continuous Beta Releases!

Check out the [Automatic Release](https://github.com/Codeland473/uhc-plugin/releases/tag/auto) for the latest version of the Plugin Jar

**WARNING:** Releases are not guaranteed to work and bugs are expected

### Hosting the Plugin Yourself

1. Set up a 1.17.1 [PaperMC server](https://papermc.io/downloads#Paper-1.17)
2. Download the latest [UHC Plugin Jar](https://github.com/Codeland473/uhc-plugin/releases/download/auto/uhc-plugin-all.jar)
3. Also download the latest [Protocollib Plugin Jar](https://github.com/dmulloy2/ProtocolLib)
4. Place both plugin jars into the `plugins/` directory in your Server

### Starting A Game

1. Add yourself and other players to teams using the `/uhca team random [team size]` command
2. Right click while holding the `Open UHC Settings` item in your inventory 
3. Click on the `Start Game` item in the opened inventory

## For Developers
### Requirements

* Kotlin 1.5
* Java 16
* [PaperMC 1.17.1](https://papermc.io/downloads#Paper-1.17)
* [Protocollib 4.7.0](https://github.com/dmulloy2/ProtocolLib)

### Setting up the Project

Clone the repo: `git clone https://github.com/Codeland473/uhc-plugin`

Create the file `user.gradle` in the project directory

Then...

### In IntelliJ

Open the project directory in Intellij IDEA

Click `Add Configuration..` then `+ (Add New Configuration)`

Select `Gradle` from the dropdown

In the `Gradle Project` field type `uhc-plugin` or the project directory

In the `Tasks` field type `shadowJar`

Click Apply

Now you can run the configuration to build the jar

### In the command line

Navigate to the directory of the project and run `gradle shadowJar`
