

$SRC_FILE = $ARGV[0];
$m = $ARGV[1];

open(SRC_FILE) or die ("Could not open source file");

@data = ();

$sum = 0;
#Fill data with the samples
foreach $line (<SRC_FILE>) {
	chomp($line);
	($fake, $fake2, $time, $fake3) = split(' ', $line);
	$sum = $sum + $time;
}
$avg = $sum/$m;
print "AVG: " . $avg . "\n";
close(SRC_FILE);

