$SRC_FILE = $ARGV[0];
$OUT_FILE = $ARGV[1];

open(SRC_FILE) or die ("Could not open source file");
open(MYOUTFILE, ">>main-res.dat") or die("Unable to create output file");

foreach $line (<SRC_FILE>) {
	chomp($line);
	($trash, $var1, $var2) = split(' ', $line);
#print $trash . "\n";
	if ($trash eq "#"){
		$out = $var1 . " " . $var2 . "\n";
		#print $out;
		print MYOUTFILE $out;
	}
}
close(SRC_FILE);
close(MYOUTFILE);
