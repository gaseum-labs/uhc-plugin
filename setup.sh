#!/bin/bash

# paperweight 1.3.11
PAPERWEIGHT_COMMIT=1756148

# TODO add uhc-paper versions
UHC_PAPER_COMMIT=7822b13

# clone repos to the directory at the same level as uhc-plugin
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

fail () {
	echo $1
	exit
}

resetDir () {
	cd "$SCRIPT_DIR/../" || fail "Where did you call this?"
}

installPaperweight () {
	resetDir

	[ ! -d ./paperweight ] && git clone https://github.com/PaperMC/paperweight || echo "Paperweight already installed"

	cd ./paperweight || fail "Paperweight did not clone properly"
	git reset --hard $PAPERWEIGHT_COMMIT

	gradle publishToMavenLocal
}

installUHCPaper () {
	resetDir

	[ ! -d ./uhc-paper ] && git clone https://github.com/gaseum-labs/uhc-paper || echo "UHC paper already installed"

    cd ./uhc-paper || fail "UHC Paper did not clone properly"
    git reset --hard $UHC_PAPER_COMMIT

    gradle applyPatches
    gradle createReobfPaperclipJar
    gradle publishDevBundlePublicationToMavenLocal
}

copyServerJar () {
	resetDir
	cd ./uhc-paper || fail "uhc-paper not installed"

	[ ! -d "$SCRIPT_DIR/run" ] && mkdir "$SCRIPT_DIR/run"
	mv "./build/libs/paper-paperclip-1.18.2-R0.1-SNAPSHOT-reobf.jar" "$SCRIPT_DIR/run/server.jar" || fail "uhc-paper not built"
}

if [ $# == 0 ]
then
	installPaperweight
	installUHCPaper
	copyServerJar
else
	[ $1 == "p" ] && installPaperweight ||\
	[ $1 == "u" ] && installUHCPaper ||\
	[ $1 == "c" ] && copyServerJar ||\
	fail "Unknown argument, please use p, u, or c"
fi

echo "UHC Plugin successfully setup"