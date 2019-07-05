parent=~/traffic/models/alex/all
for carrier in mobinil  etisalat  vodafone; do 
    echo $carrier
    echo "========================"
    dir=$parent/$carrier; 

    echo " Extract map from OSM file"
    java -cp .:../lib/jcoord-1.0.jar \
 	-Xss1g -d64 -XX:+UseG1GC -Xms5g -Xmx5g -XX:MaxGCPauseMillis=500 \
 	netconvert.test.main_netconvert \
 	$dir/map.osm \
 	$dir/edges.xml
 
 # the minimum distnace between towers in the current sample is 24 
 	echo "Partition edges into small segments:"
 	java -Xss1g \
 	-d64 \
 	-XX:+UseG1GC \
 	-Xms5g \
 	-Xmx5g \
 	-XX:MaxGCPauseMillis=500 \
 	mergexml.Interpolate_edges \
 	$dir/edges.xml 20
	
 	mv $dir/edges.xml $dir/edges.org.xml
 	mv $dir/edges.interpolated.xml $dir/edges.xml
	
    echo "Create map representation"
    java -Xss1g \
 	-d64 \
 	-XX:+UseG1GC \
 	-Xms5g \
 	-Xmx5g \
 	-XX:MaxGCPauseMillis=500 \
 	mergexml.NetConsTest \
 	$dir/edges.xml \
 	$dir/map.xy.dist.xml

    echo "Assign voronoi exits"
    java -cp .:../lib/diva.jar \
 	-Xss1g -d64 \
 	-XX:+UseG1GC \
 	-Xms5g \
 	-Xmx5g \
 	-XX:MaxGCPauseMillis=500 \
 	Voronoi.write_voronoi_exits_classified_roads 3 \
 	$dir/voronoi.csv \
 	$dir/edges.xml \
 	$dir/map.xy.dist.xml \
 	$dir/map.xy.dist.vor.xml

    echo "Generate probabilities"
    java -cp .:../lib/jahmm.jar:../lib/diva.jar:../lib/jgrapht.jar:../lib/guava.jar \
	-Xss1g \
	-d64 \
	-XX:+UseG1GC \
	-Xms5g \
	-Xmx5g \
	-XX:MaxGCPauseMillis=500 \
	AViterbi.Probs_Generator \
	8000 \
	$dir/map.xy.dist.vor.xml \
	$dir/transition.xml \
	$dir/emission.xml \
	$dir/neighbors.csv \
	$dir/towers.csv \
	$dir/edges.xml

    echo "------------------------------------------------"
done
