#!/bin/bash
cmd="./gradlew run -Dexec.args='"$@"'"
echo $cmd
eval $cmd
