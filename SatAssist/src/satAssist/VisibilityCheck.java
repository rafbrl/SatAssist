package satAssist;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;

import org.apache.commons.math3.util.FastMath;
import org.orekit.bodies.BodyShape;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.data.DataProvidersManager;
import org.orekit.errors.OrekitException;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.TopocentricFrame;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.analytical.tle.TLE;
import org.orekit.propagation.analytical.tle.TLEPropagator;
import org.orekit.propagation.events.ElevationDetector;
import org.orekit.propagation.events.EventDetector;
import org.orekit.propagation.events.handlers.EventHandler;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;
import org.orekit.utils.PVCoordinates;

public class VisibilityCheck {
	private static final Charset charset = Charset.forName("US-ASCII");

	public static ArrayList<ContactEvent> contactList = new ArrayList<ContactEvent>();
	//public static String satName;
	
    //public static void main(String[] args) {
	
	public static void main(String[] args) {
		
		try {
            // configure Orekit
            Autoconfiguration.configureOrekit();

			BufferedReader reader = Files.newBufferedReader(new File(SatelliteAssistance.tleFile).toPath(), charset);
			String line = null;
			
			ArrayList<TLE> tleList = new ArrayList<>();
			
			while ((line = reader.readLine()) != null) {
				if (line.length() > 0) {
					if (line.charAt(0) != '#') {
						String satName = line.toString().split("\\[")[0];
						line = reader.readLine();
						String line1 = line.toString();
						line = reader.readLine();
						String line2 = line.toString();
						
						//System.out.println(satName);
						
						// Create TLE element using info read above.
						TLE tle = new TLE(line1, line2);
						//BEGINDATE = tle.getDate();
						
						tleList.add(tle);
						System.out.println(satName +"   " + tle.getI() + "   " + tle.getPerigeeArgument() + "   " + tle.getRaan());
					}
				}
			}
			
			for (TLE t : tleList) {
				int i = 0;
				for (TLE t2 : tleList) {
					if ((t.getRaan()*1.14 > t2.getRaan()) && (t2.getRaan() > t.getRaan()*0.86 )) {
						i++;
					}
				}
				System.out.println(i);
			}
			
		}  catch (IOException x) {
			System.out.println("Error openning the TLE file!");
		} catch (OrekitException oe) {
            System.out.println("Orekit error!");
        } 
	}
	
	public static ArrayList<ContactEvent> getContactList(String stationName, double latitude, double longitude, double altitude) {
    	
		
		try {
            // configure Orekit
            Autoconfiguration.configureOrekit();

			BufferedReader reader = Files.newBufferedReader(new File(SatelliteAssistance.tleFile).toPath(), charset);
			String line = null;
			
			while ((line = reader.readLine()) != null) {
				if (line.length() > 0) {
					if (line.charAt(0) != '#') {
						// Read TLE file 3 lines at a time.
						String satName = line.toString().split("\\[")[0].trim();
						line = reader.readLine();
						String line1 = line.toString();
						line = reader.readLine();
						String line2 = line.toString();
						
						//System.out.println(satName);
						
						// Create TLE element using info read above.
						TLE tle = new TLE(line1, line2);
			            //System.out.println(tle.getDate());
			            
			            // Set the propagator to SGP4.
			            TLEPropagator tp = TLEPropagator.selectExtrapolator(tle);

			            // Earth and frame from default data.
			            Frame earthFrame = FramesFactory.getITRF(IERSConventions.IERS_2010, true);
			            BodyShape earth = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
			                                                   Constants.WGS84_EARTH_FLATTENING,
			                                                   earthFrame);
			            
			            // Specify a ground station.
			            // double longitude = FastMath.toRadians(Math.random() * 4.); // Location will be defined manually
			            // double latitude  = FastMath.toRadians(Math.random() * 2.); // Location will be defined manually
			            // double altitude  = Math.random() * 100.;
			            GeodeticPoint station = new GeodeticPoint(latitude, longitude, altitude);
			            TopocentricFrame staFrame = new TopocentricFrame(earth, station, stationName); //+ " ( lat " + latitude + " | long " + longitude + " | alt " + altitude);

			            // Event definition
			            double maxcheck  = 60.0;
			            double threshold =  0.001;
			            double elevation = FastMath.toRadians(5.0);
			            
			            EventDetector staVisi =
			                    new ElevationDetector(maxcheck, threshold, staFrame).
			                    withConstantElevation(elevation).
			                    withHandler(new VisibilityHandler(satName, tp.getPVCoordinates(tle.getDate())));
			            
			        
			            // Attach detector and propagate.
			            tp.addEventDetector(staVisi);
			            
			            //SpacecraftState finalState = 
			            tp.propagate(tle.getDate().shiftedBy(SatelliteAssistance.PROPAGATIONDUR));
			          
					}
				}
			}
        } catch (OrekitException oe) {
            System.out.println("Orekit error!");
        }  catch (IOException x) {
			System.out.println("Error openning the TLE file!");
		}
		/*
		for (int i = 0; i < contactList.size(); i++) {
			System.out.println(contactList.get(i));
		}
		*/
		return contactList;
    }

    public static class VisibilityHandler implements EventHandler<ElevationDetector> {
    	private AbsoluteDate beginDate;
    	private String satelliteName;
    	private PVCoordinates pvcoord;
    	
    	public VisibilityHandler (String s, PVCoordinates c) {
    		this.satelliteName = s;
    		this.pvcoord = c;
    	}
    	
        public Action eventOccurred(final SpacecraftState s, final ElevationDetector detector, final boolean increasing) {
        	
            if (increasing) {
                //System.out.println(" Visibility on " + detector.getTopocentricFrame().getName() + " begins at " + s.getDate());
                this.beginDate = s.getDate();
                return Action.CONTINUE;
            } else {
                //System.out.println(" Visibility on " + detector.getTopocentricFrame().getName() + " ends at " + s.getDate());
                //return Action.STOP;
            	if ((this.beginDate != null) && (s.getDate() != null)) {
            		try {
            			ContactEvent e = new ContactEvent(detector.getTopocentricFrame().getName(), this.satelliteName, this.beginDate, s.getDate());
            			e.setCoord(this.pvcoord);
                		contactList.add(e);
            		} catch (Exception e) {
            			System.out.println(e);
            		}
            	}
                return Action.CONTINUE;
            }
        }

        public SpacecraftState resetState(final ElevationDetector detector, final SpacecraftState oldState) {
            return oldState;
        }

    }
    
    /** Utility class for configuring the library for tutorials runs.
     * @author Luc Maisonobe
     */
    public static class Autoconfiguration {

        /** This is a utility class so its constructor is private.
         */
        private Autoconfiguration() {
        }

        /** Configure the library.
         * <p>Several configuration components are used here. They have been
         * chosen in order to simplify running the tutorials in either a
         * user home or local environment or in the development environment.
         *   <ul>
         *     <li>use a "orekit-data.zip" directory in current directory</li>
         *     <li>use a "orekit-data" directory in current directory</li>
         *     <li>use a ".orekit-data" directory in current directory</li>
         *     <li>use a "orekit-data.zip" directory in user home directory</li>
         *     <li>use a "orekit-data" directory in user home directory</li>
         *     <li>use a ".orekit-data" directory in user home directory</li>
         *     <li>use the "regular-data" directory from the test resources</li>
         *   </ul>
         * </p>
         */
        public static void configureOrekit() {
            //final File home    = new File(System.getProperty("user.home"));
        	final File home    = new File(SatelliteAssistance.homeFolder);
            final File current = new File(System.getProperty("user.dir"));
            StringBuffer pathBuffer = new StringBuffer();
            appendIfExists(pathBuffer, new File(current, "orekit-data.zip"));
            appendIfExists(pathBuffer, new File(current, "orekit-data"));
            appendIfExists(pathBuffer, new File(current, ".orekit-data"));
            appendIfExists(pathBuffer, new File(home,    "orekit-data.zip"));
            appendIfExists(pathBuffer, new File(home,    "orekit-data"));
            appendIfExists(pathBuffer, new File(home,    ".orekit-data"));
            appendIfExists(pathBuffer, "regular-data");
            System.setProperty(DataProvidersManager.OREKIT_DATA_PATH, pathBuffer.toString());
        }

        /** Append a directory/zip archive to the path if it exists.
         * @param path placeholder where to put the directory/zip archive
         * @param file file to try
         */
        private static void appendIfExists(final StringBuffer path, final File file) {
            if (file.exists() && (file.isDirectory() || file.getName().endsWith(".zip"))) {
                if (path.length() > 0) {
                    path.append(System.getProperty("path.separator"));
                }
                path.append(file.getAbsolutePath());
            }
        }

        /** Append a classpath-related directory to the path if the directory exists.
         * @param path placeholder where to put the directory
         * @param directory directory to try
         */
        private static void appendIfExists(final StringBuffer path, final String directory) {
            try {
                final URL url = Autoconfiguration.class.getClassLoader().getResource(directory);
                if (url != null) {
                    if (path.length() > 0) {
                        path.append(System.getProperty("path.separator"));
                    }
                    path.append(url.toURI().getPath());
                }
            } catch (URISyntaxException use) {
                // display an error message and simply ignore the path
                System.err.println(use.getLocalizedMessage());
            }
        }

    }

}