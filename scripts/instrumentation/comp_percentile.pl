#compute percentile for the input values, and output average of the remaining samples

$SRC_FILE = $ARGV[0];

open(SRC_FILE) or die ("Could not open source file");
open(OUTPUT, ">>latency_processed_percentile.dat") or die ("cannot create output file");

$percentile = $ARGV[1];
@data = ();

#Fill data with the samples
foreach $line (<SRC_FILE>) {
	chomp($line);
	($time, $value) = split(' ', $line);
	push(@data, $value);
}
#Order the data
@sorted_data = sort {$a <=> $b} @data;
#Compute index of percentile
$data_size = @sorted_data;
$index = ($percentile/100)*($data_size+1);

#print actual percentile number
print "index".$index."\n";
print "Percentile " . $percentile . " is: " . @sorted_data[$index] . "\n";


#Slice the array so that the values on top of index are removed
@data_processed = @sorted_data[0..$index];
foreach (@data_processed) {
	#uncomment to generate filtered file
	#print OUTPUT $_ . "\n";
}
close(SRC_FILE);
close(OUTPUT);
