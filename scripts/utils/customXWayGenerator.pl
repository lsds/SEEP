$LOGFILE = "datafile3hours.dat";
#$LOGFILE = "datafile20seconds.dat";
$numXways = $ARGV[0];
print $numXways;
open(LOGFILE) or die("Could not open log file.");
open(MYOUTFILE, ">>moreLways.dat") or die("Unable to create output file");
foreach $line (<LOGFILE>) {
	chomp($line);    
	($type, $time, $VID, $speed, $xway, $lane, $dir, $seg, $pos, $qid, $sin, $sout, $dow, $tow, $day) = split(',', $line);
	$counter = 0;
	while ($counter < $numXways){
		$line = $type . "," . $time . "," . $VID . "," . $speed . "," . $counter . "," . $lane . "," . $dir . "," . $seg . "," . $pos . "," . $qid . "," . $sin . "," . $sout . "," . $dow . "," . $tow . "," . $day . ",";
		print MYOUTFILE $line;
		$counter++;
	}
}
close(LOGFILE);
close(MYOUTFILE);
