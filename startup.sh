#!/bin/bash
echo "cooking the executable"
cd src
make
cd ..
echo "cooking is over"
echo "Tests yay"
echo "Tests" > analysis.out
for file in ./tst/*
do
  echo "$file"
  echo "$file" >>analysis.out
  ./src/ "$file" >> analysis.out
done
