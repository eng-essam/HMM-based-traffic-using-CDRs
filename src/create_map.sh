#!/bin/bash
#----------------------------------------------------------------------
# Sat Feb 27 02:15:40 EET 2016
# create map and transition table require for mapping CDRs into traffic
#-----------------------------------------------------------------------

#dir=~/traffic/models/senegal/
#dir=~/traffic/models/alex/all/etisalat

dir=~/traffic/models/dakar/main
echo " Extract map from OSM file"
java -cp .:../lib/jcoord-1.0.jar \
    -Xss1g -d64 -XX:+UseG1GC -Xms15g -Xmx15g -XX:MaxGCPauseMillis=500 \
    netconvert.test.main_netconvert \
    $dir/map.osm \
    $dir/edges.xml

echo "Create map representation"
java -Xss1g \
    -d64 \
    -XX:+UseG1GC \
    -Xms15g \
    -Xmx15g \
    -XX:MaxGCPauseMillis=500 \
    mergexml.NetConsTest \
    $dir/edges.xml \
    $dir/map.xy.dist.xml

echo "Assign voronoi exits"
java -cp .:../lib/diva.jar \
    -Xss1g -d64 \
    -XX:+UseG1GC \
    -Xms15g \
    -Xmx15g \
    -XX:MaxGCPauseMillis=500 \
    Voronoi.write_voronoi_exits_classified_roads 2 \
    $dir/voronoi.csv \
    $dir/edges.xml \
    $dir/map.xy.dist.xml \
    $dir/map.xy.dist.vor.xml

echo "Generate probabilities"
java -cp .:../lib/jahmm.jar:../lib/diva.jar:../lib/jgrapht.jar:../lib/guava.jar \
    -Xss1g \
    -d64 \
    -XX:+UseG1GC \
    -Xms15g \
    -Xmx15g \
    -XX:MaxGCPauseMillis=500 \
    AViterbi.Probs_Generator 6000\
    $dir/map.xy.dist.vor.xml \
    $dir/transition.xml \
    $dir/emission.xml \
    $dir/neighbors.csv \
    $dir/towers.csv \
    $dir/edges.xml
