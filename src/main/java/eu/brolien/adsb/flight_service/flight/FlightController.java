package eu.brolien.adsb.flight_service.flight;

import java.util.List;
import java.util.Map;

import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.LngLatAlt;
import org.geojson.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;

@Controller
@RequestMapping("/flights")
public class FlightController {
	
    private static final Logger log = LoggerFactory.getLogger(FlightController.class);

    @CrossOrigin
	@RequestMapping(method = RequestMethod.GET)
	public @ResponseBody FeatureCollection listFlights() {
    	FeatureCollection result = new FeatureCollection();
    	
    	
		try {
			ScanRequest scanRequest = new ScanRequest().withTableName("adsb");
			AmazonDynamoDBClient client;
			client = new AmazonDynamoDBClient();
			client.setRegion(Region.getRegion(Regions.US_WEST_2));
			ScanResult scanResult = client.scan(scanRequest);
			convertFlights(result, scanResult.getItems());
		} catch (Exception e) {
			log.error("", e);
			throw e;
		}

		try {
			ScanRequest scanRequest = new ScanRequest().withTableName("adsb-device");
			AmazonDynamoDBClient client;
			client = new AmazonDynamoDBClient();
			client.setRegion(Region.getRegion(Regions.US_WEST_2));
			ScanResult scanResult = client.scan(scanRequest);
			convertDevices(result, scanResult.getItems());
		} catch (Exception e) {
			log.error("", e);
			throw e;
		}
		
		return result;
	}

	private void convertFlights(FeatureCollection result, List<Map<String, AttributeValue>> flights) {
		for (Map<String, AttributeValue> item : flights) {			
			Map<String, AttributeValue> payload = item.get("payload").getM();			
			if (payload.get("validposition").getN().equals("1")) {				
				double lat = Double.parseDouble(payload.get("lat").getN());
				double lon = Double.parseDouble(payload.get("lon").getN());
				String cs = payload.get("flight").getS();
				int alt = Integer.parseInt(payload.get("altitude").getN());
				long timestamp = Long.parseLong(payload.get("timestamp").getN());
				long speed = Long.parseLong(payload.get("speed").getN());
				long track = Long.parseLong(payload.get("track").getN());

				String hex = payload.get("hex").getS();
				Feature f = new Feature();
				
				f.setGeometry(new Point(new LngLatAlt(lon, lat)));
				f.setId(hex);
				f.setProperty("altitude", alt);
				f.setProperty("callsign", cs);
				f.setProperty("timestamp", timestamp);
				f.setProperty("speed", speed);
				
				String SIDC = "SFAPMF------";
				if (System.currentTimeMillis() - timestamp > 30 * 1000) {
					SIDC = "SFAAMF------";
				}
				
				f.setProperty("SIDC", SIDC);

				if (payload.get("validtrack").getN().equals("1")) {			
					f.setProperty("bearing", track);
				}
				result.add(f);
				
			}
			
		}
	}
	
	
	private void convertDevices(FeatureCollection result, List<Map<String, AttributeValue>> flights) {
		for (Map<String, AttributeValue> item : flights) {			
			Map<String, AttributeValue> payload = item.get("payload").getM();			
				double lat = Double.parseDouble(payload.get("lat").getN());
				double lon = Double.parseDouble(payload.get("lon").getN());
				String device = payload.get("device").getS();
				long timestamp = Long.parseLong(payload.get("timestamp").getN());
				long startup = Long.parseLong(payload.get("startup").getN());
				long messages = Long.parseLong(payload.get("messages").getN());

				Feature f = new Feature();
				
				f.setGeometry(new Point(new LngLatAlt(lon, lat)));
				f.setId(device);
				f.setProperty("device", device);
				f.setProperty("timestamp", timestamp);
				f.setProperty("startup", startup);
				f.setProperty("messages", messages);
				
				String SIDC = "SFGPESR-----";
				if (System.currentTimeMillis() - timestamp > 5 * 60 * 1000) {
					SIDC = "SFGAESR-----";
				}
				
				f.setProperty("SIDC", SIDC);
				
				result.add(f);
				
			
		}
	}
	

}
