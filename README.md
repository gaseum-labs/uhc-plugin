<img src="uhc icon.png" alt="UHC Enchanting Table Icon" width="200"/> 

# UHC Plugin

A Minecraft 1.18.2 UHC plugin built in Kotlin running on a custom PaperMC server

**UHC Plugin is not general use. If you are looking to host games of UHC yourself, you have come to the wrong place.**

## Developers: How to set up

* This project works best on Linux or WSL. We don't recommend using Windows
* Clone this repo to your local machine with `git clone https://github.com/gaseum-labs/uhc-plugin`

### New way: Using the setup script

* Run `./setup.sh` in this repo directory

### Old way: Manual setup

#### 1. Install paperweight to Maven Local

* Clone the https://github.com/PaperMC/paperweight repo to your local machine
* Run `./gradlew publishToMavenLocal` inside the paperweight root directory

#### 2. Build UHC Paper

* Clone the https://github.com/gaseum-labs/uhc-paper repo to your local machine if you are using Linux. If you are using
  Windows, clone the repo to WSL.
* Run `./gradlew applyPatches` inside the uhc-paper root directory
* Next, run `./gradlew createReobfPaperclipJar` and `./gradlew publishDevBundlePublicationToMavenLocal`

#### 3. Move UHC Paper artifacts to UHC Plugin project

* If it doesn't exist already, create a folder named `run` in this repo's root directory
* Copy the `./build/libs/paper-paperclip-1.18.2-R0.1-SNAPSHOT-reobf.jar` jar file to this project's `run` folder and
  rename it to `server.jar`

## Run the UHC Server

* Run `gradle build runServer` inside this project
* Or add an equivalent Gradle run configuration in Intellij 

## Starting the game

1. Make sure you are opped on the server
2. Add yourself and other players to teams using the `/uhca team random [team size]` command
3. Right click while holding the `Open UHC Settings` item in your inventory
4. Click on the `Start Game` item in the opened inventory
