package uk.ac.imperial.lsds.seep.infrastructure.monitor.comm.serialization;

import java.io.PrintStream;

/**
 * Concrete metrics reporter that output metrics tuples as strings to a PrintStream 
 * object. This reporter can be used to periodically log metrics on each node running 
 * a SEEP query. The output stream can be anything really: standard output,
 * standard error or a ByteArraOutputStream. 
 * 
 * @author mrouaux
 */
public class StringMetricsSerializer implements MetricsSerializer<PrintStream> {

    private PrintStream os;
    
    @Override
    public void initialize(PrintStream os) {
        this.os = os;
    }

    @Override
    public void serialize(MetricsTuple tuple) {
        os.print(tuple.toString());
    }

    @Override
    public String toString() {
        return "StringMetricsSerializer{" + '}';
    }
}
