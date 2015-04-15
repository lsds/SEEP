import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPOutputStream;

import java.io.IOException;

public class Utils {
	
	public static byte [] compress (byte [] data) throws IOException {
		
		ByteArrayOutputStream output = new ByteArrayOutputStream ();
		GZIPOutputStream gzip = new GZIPOutputStream (output);
		
		gzip.write (data);
		gzip.close();
		
		return output.toByteArray ();
	}
}

