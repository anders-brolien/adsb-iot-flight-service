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
		ScanRequest scanRequest = new ScanRequest().withTableName("adsb");
		try {
			AmazonDynamoDBClient client;
			client = new AmazonDynamoDBClient();
			client.setRegion(Region.getRegion(Regions.US_WEST_2));
			ScanResult scanResult = client.scan(scanRequest);
			FeatureCollection result = convert(scanResult.getItems());
			return result;

		} catch (Exception e) {
			log.error("", e);
			throw e;
		}
	}

	private FeatureCollection convert(List<Map<String, AttributeValue>> items) {
		FeatureCollection result = new FeatureCollection();
		for (Map<String, AttributeValue> item : items) {			
			Map<String, AttributeValue> payload = item.get("payload").getM();			
			if (payload.get("validposition").getN().equals("1")) {				
				double lat = Double.parseDouble(payload.get("lat").getN());
				double lon = Double.parseDouble(payload.get("lon").getN());
				String cs = payload.get("flight").getS();
				int alt = Integer.parseInt(payload.get("altitude").getN());
				long timestamp = Long.parseLong(payload.get("timestamp").getN());
				String hex = payload.get("hex").getS();
				Feature f = new Feature();
				
				f.setGeometry(new Point(new LngLatAlt(lon, lat)));
				f.setId(hex);
				f.setProperty("altitude", alt);
				f.setProperty("callsign", cs);
				f.setProperty("timestamp", timestamp);
				result.add(f);
				
			}
			
		}

		return result;
	}

}
