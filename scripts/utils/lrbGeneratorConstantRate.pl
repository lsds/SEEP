$LOGFILE = "datafile3hours.dat";
#$LOGFILE = "datafile20seconds.dat";
$es = $ARGV[0];
print $es;
open(LOGFILE) or die("Could not open log file.");
open(MYOUTFILE, ">>constantLRB.dat") or die("Unable to create output file");

sub printToFile{
	@buf = $_[0];
	$num = $_[1];
	foreach $event (@buf){
		print MYOUTFILE, $event . "\n";
	}
	if($num > 0){
		$idx = 0;
		while ($num > 0){
			if(defined(buf[$idx])){
				print MYOUTFILE, $event . "\n";
			}
			else{
				$idx = 0;
			}
			$num = $num - 1;
			$idx = $idx + 1;
		}
	}
}


@buffer = ();
$counter = 0;
$currentTime = 5000;

foreach $line (<LOGFILE>) {
	chomp($line);
	($type, $time, $VID, $speed, $xway, $lane, $dir, $seg, $pos, $qid, $sin, $sout, $dow, $tow, $day) = split(',', $line);
	push(@buffer, $line);
	$counter = $counter + 1;
	if($time > $currentTime){
		if($counter < $es){
			$remaining = 1000 - $buffer;
			printToFile(@buffer, $remaining);			
			$counter = 0;
			$buffer = ();
		}
		else{
			#print buffer with the 1000 events
			printToFile(@buffer, 0);
			$counter = 0;
			$buffer = ();
		}
	}
	if($counter == 1000){
		#print buffer to file (buffer with 1000 events).
		printToFile(@buffer, 0);
		$counter = 0;
		$buffer = ();
	}
}

close(LOGFILE);
close(MYOUTFILE);
