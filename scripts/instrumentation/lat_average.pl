#compute percentile for the input values, and output average of the remaining samples

$SRC_FILE = $ARGV[0];

open(SRC_FILE) or die ("Could not open source file");
open(OUTPUT, ">>latency_averaged.dat") or die ("cannot create output file");

$currentTime = 0;
@current = ();


foreach $line (<SRC_FILE>) {
	chomp($line);
	($time, $value) = split(' ', $line);
	#Detect when the current time finish.
	if($time > $currentTime){
		#end of time, compute average
		$size = @current;
		$sum = 0;
		foreach $v (@current){
			$sum = $sum + $v;
		}
		print "suma: " . $sum . "\n";
		if($size != 0){
			$avg = $sum/$size;
			print "avg: " . $avg . "\n";
			$out = $currentTime . " " . $avg . "\n";
			print OUTPUT $out;
			@current = ();
			#save the current time (first of next second)
			push(@current, $value);
			#update currentTime
			$currentTime = $time;
		}
	}
	else{
		#add value
		push(@current, $value);
	}
}
close(SRC_FILE);
close(OUTPUT);
