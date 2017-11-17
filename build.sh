#!/usr/bin/env bash

mkdir -p $PREFIX/jar
cd $SRC_DIR/ && $SRC_DIR/gradlew installDist
cp $SRC_DIR/build/libs/gatk-4.beta.6-SNAPSHOT.jar $PREFIX/jar/gatk.jar
cp $SRC_DIR/bin/gatk $PREFIX/bin/gatk
