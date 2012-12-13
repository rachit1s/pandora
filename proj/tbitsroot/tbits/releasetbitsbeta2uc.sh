#!/bin/bash
export USER=transbit
export RSYNC_PASSWORD=tBitsrsync4upgrades
touch version.properties
./upgradetbits.sh dist upgrades.mytbits.com::upgrades/tbits/beta
