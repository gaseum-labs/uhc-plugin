#!/bin/bash

# paperweight 1.3.11
PAPERWEIGHT_COMMIT=1756148

# TODO add uhc-paper versions
UHC_PAPER_COMMIT=7822b13

# clone repos to the directory at the same level as uhc-plugin
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
cd "$SCRIPT_DIR/../" || ( echo "Where did you call this?" ; exit )

# install paperweight
if [ ! -d ./paperweight ]
then
	git clone https://github.com/PaperMC/paperweight
else
	echo "paperweight already installed"
fi

cd ./paperweight || ( echo "Paperweight did not clone properly" ; exit )
git reset --hard $PAPERWEIGHT_COMMIT
gradle publishToMavenLocal

# install uhc-paper
cd ../

if [ ! -d ./uhc-paper ]
then
	git clone https://github.com/gaseum-labs/uhc-paper
else
	echo "uhc paper already installed"
fi

cd ./uhc-paper || ( echo "UHC Paper did not clone properly" ; exit )
git reset --hard $UHC_PAPER_COMMIT
gradle applyPatches
gradle createReobfPaperclipJar
gradle publishDevBundlePublicationToMavenLocal

echo "UHC Plugin successfully setup"
