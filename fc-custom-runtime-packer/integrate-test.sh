#!/usr/bin/env bash

cd ..

# include demo module
echo "include 'demo'" >> settings.gradle
./gradlew :demo:zipBootstrap

cd fc-custom-runtime-packer
./verify-zip-bootstrap.sh

cd ..
# remain the first two line
head -n 2 settings.gradle > temp
mv temp settings.gradle