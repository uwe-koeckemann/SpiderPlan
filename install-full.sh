# !/bin/bash

echo "========================================"
echo "= spiderplan"
echo "========================================"
cd spiderplan/
sbt --warn compile test publishM2
cd ../

echo "========================================"
echo "= spiderplan_coordination_oru"
echo "========================================"
cd lib/coordination_oru/
sbt --warn clean compile test publishM2
cd ../../

echo "========================================"
echo "= spiderplan_prolog"
echo "========================================"
cd lib/prolog/
sbt --warn clean compile test publishM2
cd ../../
