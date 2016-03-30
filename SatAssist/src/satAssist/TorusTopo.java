package satAssist;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class TorusTopo {
	public static void createTorusTopo(String prefix, Integer nodes, Integer cap, Integer buff, Integer planes) {
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(SatelliteAssistance.satTorusFile, true)));
			int k = 1;
			int i = 1;
			
			for (i = 1; i < planes; i++) {
				for (int j = 1; j <= nodes/planes; j++) { 
					out.println(prefix + k + ", " + buff +", "+ prefix + (k + nodes/planes) + ", " + buff + ", " + cap + ", ");
					out.println(prefix + k + ", " + buff +", "+ prefix + (1 + (i-1)*(nodes/planes) + ((k)%(nodes/planes))) + ", " + buff + ", " + cap + ", ");
					k++;
				}
			}
			
			for (int j = 1; j <= nodes/planes; j++) { 
				out.println(prefix + k + ", " + buff +", "+ prefix + (1 + (i-1)*(nodes/planes) + ((k)%(nodes/planes))) + ", " + buff + ", " + cap + ", ");
				k++;
			}
			
			out.close();
			
		} catch (IOException x) {
			System.out.println("Error!");
		}
		
	}
}
