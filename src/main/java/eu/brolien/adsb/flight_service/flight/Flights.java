package eu.brolien.adsb.flight_service.flight;

public class Flights {
	
	public class Pos {
		double lat;
		double lon;
		double alt;
	}
	
	public class Flight {
		private Pos pos;
	}
	
	private Flight[] flights;

}
