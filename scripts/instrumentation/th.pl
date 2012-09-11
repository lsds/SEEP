#average 5 seconds of throughput
$SRC_FILE = $ARGV[0];

open(SRC_FILE) or die ("Could not open source file");
open(OUTPUT, ">>th_average.dat") or die ("cannot create output file");

$samples = $ARGV[1];
$avg = 0;
$avgSize = 0;

foreach $line (<SRC_FILE>) {
	chomp($line);
	($time, $es, $size) = split(' ',$line);
	$samples = $samples -1;
	$avg = $avg + $es;
	$avgSize = $avgSize + $size;
	
	if($samples == 0){
		$avg = $avg/$ARGV[1];
		$avgSize = $avgSize/$ARGV[1];
		$out = $time . " " . $avg . " " . $avgSize . "\n";
		print OUTPUT $out;
		$avg = 0;
		$avgSize = 0;
		$samples = $ARGV[1];
	}
}
close(SRC_FILE);
close(OUTPUT);
