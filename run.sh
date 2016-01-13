#!/bin/bash
cmd="gradle run -Dexec.args='"$@"'"
echo $cmd
eval $cmd
