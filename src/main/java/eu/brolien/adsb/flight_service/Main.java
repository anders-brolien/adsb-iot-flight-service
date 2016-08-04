package eu.brolien.adsb.flight_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import eu.brolien.adsb.flight_service.flight.FlightCleanup;

@SpringBootApplication
public class Main {

	public static void main(String[] args) {
		SpringApplication.run(Main.class, args);
		
		FlightCleanup flightCleanup = new FlightCleanup();
		
	}

}
