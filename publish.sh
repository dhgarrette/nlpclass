#!/bin/bash

projectname=nlpclass
version_len=3

rm -rf target/scala-2.10/${projectname}_2.10-*
sbt publish

rm -rf target/scala-2.10/api
sbt doc


for f in target/scala-2.10/${projectname}_2.10-*
do
  len=24+${#projectname}
  version=${f:$len:$version_len}
  dir=public_html/maven-repository/snapshots/dhg/${projectname}_2.10/$version-SNAPSHOT/
  ssh k mkdir -p $dir
  scp $f k:$dir
  
  rm -rf ~/.ivy2/cache/dhg/${projectname}_2.10/*-$version*
  rm -rf ~/.ivy2/cache/dhg/${projectname}_2.10/*/*-$version*

done

scp -r target/scala-2.10/api k:public_html/maven-repository/snapshots/dhg/${projectname}_2.10/$version-SNAPSHOT

