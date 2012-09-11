#compute percentile for the input values, and output average of the remaining samples

$SRC_FILE = $ARGV[0];
$m = $ARGV[1];
$avg = $ARGV[2];

open(SRC_FILE) or die ("Could not open source file");

$aux = 0;
foreach $line(<SRC_FILE>){
	chomp($line);
	($fake, $load, $time, $null) = split(' ', $line);
	$dif = $time - $avg;
	$dif = $dif * $dif;
	$aux = $aux + $dif
}
$aux = $aux/$m;
$sd = sqrt($aux);
print "SD: ". $sd . "\n";

close(SRC_FILE);

