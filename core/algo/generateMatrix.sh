#!/bin/bash
for x in {1..16..1};
do 
echo -n $x;
printf "\t";
y=`expr $x % 4`;
if [  $y = 0  ];
then 
echo "" ;
fi;
done 
