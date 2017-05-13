package com.capgemini.hackathon.device.simulation.routing;

import java.util.*;
import java.util.Locale;

import com.capgemini.hackathon.device.simulation.model.Location;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.util.AllEdgesIterator;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.Directory;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.storage.StorableProperties;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.GHUtility;
import com.graphhopper.util.shapes.BBox;
import com.graphhopper.util.shapes.GHPoint;

/**
 * 
 * @author belmahjo
 *
 */
public class RouteCalculator {

	private static final RouteCalculator INSTANCE = new RouteCalculator();

	private GraphHopper engine = new GraphHopper();
	

	public static RouteCalculator getInstance() {
		return INSTANCE;
	}

	public RouteCalculator() {
	}

	public void init() {
		// Set the location of graphhopper files
		engine.setGraphHopperLocation(Thread.currentThread().getContextClassLoader().getResource("maps").getPath());
		engine.setEncodingManager(new EncodingManager("car"));
		//engine.setCHEnable(false);

		// now this can take minutes if it imports or a few seconds for loading
		// of course this is dependent on the area you import
		engine.importOrLoad();
		System.out.println("GraphHopper initialized");
	}
	
	public List<EdgeIteratorState> adjustWeights(GHResponse response) {
	    LocationIndex index = engine.getLocationIndex();
	    EdgeExplorer explorer = engine.getGraph().createEdgeExplorer();
	    
	    List<EdgeIteratorState> edges = new ArrayList<EdgeIteratorState>();
		if(response.getPoints() != null){
			for(int i =0; i<response.getPoints().getSize();i++)
			{
				QueryResult res = index.findClosest(response.getPoints().getLat(i),response.getPoints().getLon(i), EdgeFilter.ALL_EDGES);
				EdgeIterator iter = explorer.setBaseNode(res.getClosestNode());
				while(iter.next()) {
					iter.setDistance(300);
				}
			}
			
		} else {
			return null;
		}
	    return edges;
	}

	/**
	 * This method calculates the route between start and end position
	 * 
	 * @param latFrom
	 *            latitude of starting position
	 * @param lonFrom
	 *            longitude of starting position
	 * @param latTo
	 *            latitude of destination
	 * @param lonTo
	 *            longitude of destination
	 */
	public GHResponse calculateRoute(double latFrom, double lonFrom, double latTo, double lonTo) {

		// create a request object
		GHRequest req = new GHRequest(latFrom, lonFrom, latTo, lonTo).setWeighting("fastest").setVehicle("car")
				.setLocale(Locale.UK);
		
		GHResponse rsp = engine.route(req);

		return rsp;

	}

}
