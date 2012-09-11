$SRC_FILE = $ARGV[0];
$OUT_FILE = $ARGV[1];

open(SRC_FILE) or die ("Could not open source file");
open(MYOUTFILE, ">>main-res.dat") or die("Unable to create output file");

$counter = 1;
foreach $line (<SRC_FILE>) {
	chomp($line);
	($time, $load) = split(' ', $line);
	if ($counter == $time){
		$out = $counter . " " . $load . "\n";
		print MYOUTFILE $out;
	}
	else{
		$dif = $time - $counter;
print "DIF: " . $dif;
		$aux = $counter;
		while($dif > 0){
			$out = $aux . " " . "0\n";
			print MYOUTFILE $out;
			$dif = $dif - 1;
			$aux = $aux + 1;
		}
		$counter = $time;
		$out = $counter . " " . $load . "\n";
		print MYOUTFILE $out;
	}
	$counter = $counter + 1;
}
close(SRC_FILE);
close(MYOUTFILE);
