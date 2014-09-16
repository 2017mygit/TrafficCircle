package model;

import java.util.HashSet;
import java.util.Set;

public class TrafficCircle {
	/**
	 * List of cars which are currently on the circle
	 */
	Set<Car> cars;
	public int numCarsThrough = 0;
	
	public TrafficCircle() {
		cars = new HashSet<Car>();
	}

	public Set<Car> getCars() {
		return cars;
	}

	public void setCars(Set<Car> cars) {
		this.cars = cars;
	}
	
	public void addCar(Car car) {
		this.cars.add(car);
	}
	
	
}
