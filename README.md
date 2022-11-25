<img src="uhc icon.png" alt="UHC Enchanting Table Icon" width="200"/> 

# UHC Plugin

A Minecraft 1.18 UHC plugin built in Kotlin running on a custom PaperMC server

**UHC Plugin is not general use. If you are looking to host games of UHC yourself, you have come to the wrong place.**

## Developers: How to setup

### 0. Use the new setup script instead of steps 1 and 2

* Run uhc-plugin/setup.sh

### 1. Install paperweight to Mavel Local

* Clone the https://github.com/PaperMC/paperweight repo to your local machine
* Run `./gradlew publishToMavenLocal` inside the paperweight root directory

### 2. Build UHC Paper

* Clone the https://github.com/gaseum-labs/uhc-paper repo to your local machine if you are using Linux. If you are using
  Windows, clone the repo to WSL.
* Run `./gradlew applyPatches` inside the uhc-paper root directory
* Next, run `./gradlew createReobfPaperclipJar` and `./gradlew publishDevBundlePublicationToMavenLocal`

### 3. Move UHC Paper artifacts to UHC Plugin project

* Clone this repo to your local machine
* If it doesn't exist already, create a folder named `run` in this project's root directory
* Copy the `./build/libs/paper-paperclip-1.18.2-R0.1-SNAPSHOT-reobf.jar` jar file to this project's `run` folder and
  rename it to `server.jar`
* If you are running the server on Windows and built uhc-paper on WSL,
  copy `.m2\repository\org\gaseumlabs\uhcpaper\dev-bundle` from your WSL to the corresponding files in your Windows
  local Maven directory

### 4. Run the UHC Server

* Run `gradle build runServer` inside this project

## Running the Game (Not Complete)

1. Add yourself and other players to teams using the `/uhca team random [team size]` command
2. Right click while holding the `Open UHC Settings` item in your inventory
3. Click on the `Start Game` item in the opened inventory
4. *Explore features for yourself lol*