#!/usr/bin/env bash

mkdir -p $PREFIX/jar
cd $SRC_DIR/ && $SRC_DIR/gradlew
cp $SRC_DIR/build/libs/gatk-*-local.jar $PREFIX/jar/gatk.jar
cp $SRC_DIR/bin/gatk $PREFIX/bin/gatk
rm $SRC_DIR/build/gatk*.zip
