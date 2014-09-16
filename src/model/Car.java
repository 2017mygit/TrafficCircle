package model;

import java.awt.Color;

public class Car {
	/**
	 * Speed at which the car is traveling around the circle
	 */
	double velocity;
	
	/**
	 * Angle describing location of car on the traffic circle. This is the angle of the 
	 * corresponding queue, if the car is still queued. 
	 */
	double angle;
	
	/**
	 * Represents the distance along the circle the car must travel before it can exit.
	 */
	double distanceToTravel;
	
	/** 
	 * Keeps track of the distance traveled by the car to judge when it has reached its destination.
	 */
	double distanceTraveled;
	
	/**
	 * Color of the car 
	 */
	Color color;
	
	public Car(double angle, double distanceToTravel) {
		this.angle = angle;
		this.velocity = 0;
		this.distanceTraveled = 0;
		this.distanceToTravel = distanceToTravel;
		
		// only 200 out of 256 to prevent white cars
		int rand1 = (int) (30+Math.random()*160);
		int rand2 = (int) (30+Math.random()*160);
		int rand3 = (int) (30+Math.random()*160);
		this.color = new Color(rand1,rand2,rand3);
	}
	
	public Color getColor() {
		return color;
	}

	public double getVelocity() {
		return velocity;
	}

	public void setVelocity(double velocity) {
		this.velocity = velocity;
	}

	public double getAngle() {
		return angle;
	}

	public void setAngle(double angle) {
		this.angle = angle;
	}

	public void setDistanceTraveled(double distanceTraveled) {
		this.distanceTraveled = distanceTraveled;
	}
	
	public double getDistanceTraveled() {
		return this.distanceTraveled;
	}

	public double getDistanceToTravel() {
		return distanceToTravel;
	}

	public void setDistanceToTravel(double distanceToTravel) {
		this.distanceToTravel = distanceToTravel;
	}
}
