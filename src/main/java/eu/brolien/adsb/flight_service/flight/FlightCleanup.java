package eu.brolien.adsb.flight_service.flight;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;

public class FlightCleanup {

	private static final Logger log = LoggerFactory.getLogger(FlightCleanup.class);

	public FlightCleanup() {
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this::doCleanup, 2, 2, TimeUnit.MINUTES);
	}

	private void doCleanup() {
		final long now = System.currentTimeMillis();
		try {
			AmazonDynamoDBClient client;
			client = new AmazonDynamoDBClient();
			client.setRegion(Region.getRegion(Regions.US_WEST_2));

			try {
				ScanRequest scanRequest = new ScanRequest().withTableName("adsb");
				ScanResult scanResult = client.scan(scanRequest);
				for (Map<String, AttributeValue> item : scanResult.getItems()) {
					Map<String, AttributeValue> payload = item.get("payload").getM();
					long timestamp = Long.parseLong(payload.get("timestamp").getN());
					String hex = payload.get("hex").getS();
					
					if (now - timestamp > 2 * 60 * 1000) {
						Map<String, AttributeValue> key = new HashMap<>();
						AttributeValue v = new AttributeValue();
						v.setS(hex);
						key.put("hex", v);
						log.info("deleting: " + hex);
						client.deleteItem("adsb", key);
					}
				}

			} catch (Exception e) {
				log.error("", e);
				throw e;
			}

			

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
