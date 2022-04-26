#! /usr/bin/env bash

JDK11=~/.jdks/adopt-openjdk-11.0.12

whisker () {
  cd whisker || exit
  git apply < ../patches/whisker-0001.patch
  yarn
  yarn build
  cd ..
}

hintgen () {
    # sbtx: https://github.com/dwijnand/sbt-extras
    ./sbtx -java-home $JDK11 serve/docker:publishLocal
}

gui () {
    cd ../gui || exit
    git apply < ../patches/scratch-gui-0001.diff
    npm install
    npm run build
    cp ../run_conf/server-info.txt build/
    docker build -t scratch-gui .
    cd ..
}

echo -e "Building Whisker…\n\n"
whisker

echo -e "\n\nBuilding Hint Generator…\n\n"
hintgen

echo -e "\n\nBuilding Scratch GUI…\n\n"
gui

