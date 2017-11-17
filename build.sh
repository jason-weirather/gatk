#!/usr/bin/env bash

mkdir -p $PREFIX/jar
cd $PREFIX/ && $PREFIX/gradlew installDist
cp $SRC_DIR/build/libs/gatk.jar $PREFIX/jar/gatk.jar
cp $SRC_DIR/bin/gatk $PREFIX/bin/gatk
