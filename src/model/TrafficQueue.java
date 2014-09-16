package model;

import java.util.ArrayList;

public class TrafficQueue {
	/** 
	 * List of cars functioning as a queue.
	 */
	private ArrayList<Car> cars;
	
	/**
	 * Angle of the queue relative to the traffic circle
	 */
	private double angle;
	
	/** 
	 * circle to which cars will jump when ready
	 */
	private TrafficCircle circle;
	
	public TrafficQueue(ArrayList<Car> cars, double angle, TrafficCircle circle) {
		this.cars = cars;
		this.angle = angle;
		this.circle = circle;
	}

	public ArrayList<Car> getCars() {
		return cars;
	}

	public void setCars(ArrayList<Car> cars) {
		this.cars = cars;
	}

	public double getAngle() {
		return angle;
	}

	public void setAngle(double angle) {
		this.angle = angle;
	}

	public TrafficCircle getCircle() {
		return circle;
	}

	public void setCircle(TrafficCircle circle) {
		this.circle = circle;
	}
	
	
}
