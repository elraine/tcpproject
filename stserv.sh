#!/bin/bash
echo "Cours cuisine Tomates Cuites a Point"
echo "cooking the executable"
cd src
make
cd ..
echo "cooking is over"

echo "starting server .. "
cd src
./server 8080
#./server 8081
#./server 8082
cd ..

# echo "starting client"
# cd src
# java Client localhost 8080
# cd ..

#echo "Tests yay"
#echo "Tests" > analysis.log

#for file in ./tst/*
#do
#  echo "$file"
#  echo "$file" >>analysis.out
#  ./src/ "$file" >> analysis.out
#done
