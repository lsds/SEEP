
	public class Tuple {
		public long timestamp;
		public long instrumentation_ts;
		public Object[] values;

		public Tuple(Object[] values, long timestamp, long instrumentation_ts) {
			this.values = values;
			this.timestamp = timestamp;
			this.instrumentation_ts = instrumentation_ts;
		}
	}