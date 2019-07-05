#!/bin/bash
#========================================================
# Thu 03 Sep 2015 05:00:00 AM JPT 
#  
# sh update_transitions.sh  /home/essam/Dropbox/viterbi/Java/DTraffic/src /home/essam/traffic/Dakar/sep_2015/Dakar_edge-200 /home/essam/traffic/Dakar/sep_2015/Dakar_edge-200/results/updates /home/essam/traffic/SET2 CSV.0_300_th-dist_1000_th-time_60.xml
# sh update_transitions.sh  ~/Dropbox/viterbi/Java/CDRTraffic/src ~/traffic/models/dakar/main_no_proj_1 ~/traffic/models/dakar/main_no_proj_1/results/updates /media/essam/Dell1/traffic/SET2 xml

#OPUS20
#sh update_transitions.sh  /home2/essam/DTraffic/src /home2/essam/Dakar_edge-200 /home2/essam/Dakar_edge-200/results/updates /home2/essam/SET2 CSV.0_300_th-dist_1000_th-time_60.xml
#========================================================

cd $1 
dir=$2
update_dir=$3
alpha="0.9 0.8 0.7 0.6 0.5 0.4 0.3 0.2 0.1"
#alpha="0.8"
# Compile the current source
#javac  -cp .:../jgrapht.jar Viterbi/*.java
#javac -cp .:../diva.jar:../jgrapht.jar:../guava.jar Density/*.java
# For all observation files that ends with the required specifications do the calculations
for v_alpha in $alpha
do
    v_beta=`echo "1.0-$v_alpha"|bc -l`
    echo "Alpha: $v_alpha  Beta: $v_beta"
    updates=$update_dir/alpha-$v_alpha"_"beta-$v_beta 
    echo "Update directory: $updates"
    mkdir $updates
    cp $update_dir/transition.day.00.xml $updates/
    i=0
    for f in $4/*.$5
    do	
	echo "Current Observation file SET2_P$(printf "%02d" $(($i+1))).CSV.0_490_th-dist_1000_th-time_60.xml"

	java -cp .:../diva.jar:../jgrapht.jar:../guava.jar  \
	    -Xss128m \
	    -d64 \
	    -XX:+UseG1GC \
	    -Xms4g \
	    -Xmx4g \
	    -XX:MaxGCPauseMillis=500 \
	    Density.UpdateTrans2  \
	    $dir/voronoi.csv \
	    $dir/map.xy.dist.vor.xml \
	    $4/SET2_P$(printf "%02d" $(($i+1))).CSV.0_490_th-dist_1000_th-time_60.xml \
	    $updates/transition.day.$(printf "%02d" $(($i*8))).xml \
	    $dir/emission.xml \
	    $dir/neighbors.csv \
	    $dir/map \
	    $dir/vit.png \
	    $dir/edges.xml \
	    $dir/towers.csv \
	    $updates \
	    $v_alpha \
	    $v_beta \
	    $(($i*8+1))
	#Increment the counter value	
	i=$(($i+1))
    done
done
