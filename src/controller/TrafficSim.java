package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Set;

import javax.swing.Timer;

import view.TrafficGUI;
import model.Car;
import model.TrafficCircle;
import model.TrafficQueue;

public class TrafficSim {
	
	// QUEUE_ENTER_INTERVAL = 8 for yield, 3 for green/red
	
	private int NUM_QUEUES = 4;
	private int TIMER_INTERVAL = 10; // milliseconds
	private double MAX_VELOCITY = 0.1; // meters per second
	public double D2V_DECAY_RATE = 0.03; // decay rate. Larger means larger decay in velocity with increase in density
	public double PROB_DECAY_RATE = 0.1; // decay rate for probability instead
	private int ATOMIC_SECS_PER_SEC = 10;
	private int QUEUE_ENTER_INTERVAL = 3; 
	private double YIELD_ENTER_PROB = 0.25;
	
	private TrafficCircle mainCircle;
	private ArrayList<TrafficQueue> queues;
	public Timer timer;
	private int atomicTime = 0; // system time granularity. Increments with timer tick
	public enum TrafficMode {
		GREEN,
		RED,
		YIELD
	}
	public TrafficMode currentMode = TrafficMode.YIELD;

	
	public static void main(String[] args) {
		TrafficSim sim = new TrafficSim();
		sim.start();
	}
	
	public void start() {
		mainCircle = new TrafficCircle();
		queues = new ArrayList<TrafficQueue>();
		for (int j = 0; j < NUM_QUEUES; j++) {
			queues.add(new TrafficQueue(new ArrayList<Car>(), Math.PI*2*j/NUM_QUEUES, mainCircle));
		}
		
		TrafficGUI view = new TrafficGUI(this);
		
		timer = new Timer(TIMER_INTERVAL, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateModel();
				atomicTime++;
				view.repaint();
			}
		});
	
		
		view.start();
		//timer.start();
	}
	
	/**
	 * update the model in response to timer tick signaling time moving forward
	 */
	public void updateModel() {
		if (atomicTime % QUEUE_ENTER_INTERVAL == 0) {
			bringNewCarsToQueues();
		}
		
		// make cars enter circle less often
		if (atomicTime % 8 == 0) {
			attemptQueue2CircleShift();
		}
		//System.out.println("The time is: " + atomicTime);
		//printQueueStates();
		//printCircleState();
		updateCircleCarVelocities();
		// move cars by the second, not by the atomic time
		if (atomicTime % ATOMIC_SECS_PER_SEC == 0) {
			moveCarsAlongCircle();
		}
		
	}
	
	/** 
	 * Update the queues with new cars, based on traffic rules
	 */
	public void bringNewCarsToQueues() {
		for (TrafficQueue queue: queues) {
			double entranceProbability = Math.exp(-PROB_DECAY_RATE*
					// really the density around the queue. Cheating a bit to code fast
					getDensityAroundCar(new Car(queue.getAngle(),0),mainCircle));
			if (Math.random() < entranceProbability) {
				// the car will have to travel 1 to NUM_QUEUES - 1 stops
				int numStopsAway = (int) (Math.random()*(NUM_QUEUES - 1) + 1);
				// to ensure rational division in the next step
				double dblNumStopsAway = (double) numStopsAway;
				// circle math
				double distanceToTravel = 2*Math.PI*dblNumStopsAway/NUM_QUEUES;
				queue.getCars().add(new Car(queue.getAngle(),distanceToTravel));
			}
		}
			
	}
	
	/**
	 * Attempt to shift first cars from queues to the traffic circle
	 */
	public void attemptQueue2CircleShift() {
		double carEnterCircleProb = YIELD_ENTER_PROB; // initialize to stop compiler complaints
		if (currentMode == TrafficMode.YIELD) {
			carEnterCircleProb = YIELD_ENTER_PROB;
		} else if (currentMode == TrafficMode.GREEN){
			carEnterCircleProb = 0.8;
		} else {
			carEnterCircleProb = 0.0;
		}
		
		for (TrafficQueue queue: this.queues) {
			// If there is a car at the front of the line, and the car is lucky:
			if (queue.getCars().size() > 0 && Math.random() < carEnterCircleProb ) {
				Car queue2circleCar = queue.getCars().get(0);
				queue.getCars().remove(queue2circleCar);
				this.mainCircle.addCar(queue2circleCar);
			}
		}
	}
	
	/**
	 * Set velocity of each car in the circle to vary inversely with the density of nearby cars
	 */
	public void updateCircleCarVelocities() {
		for (Car car: this.mainCircle.getCars()) {
			car.setVelocity(density2velocity(getDensityAroundCar(car, this.mainCircle)));
		}
		
	}
	
	/**
	 * Move cars along the circle according to their velocities, having them exit the circle
	 * if they pass their desired exit
	 */
	public void moveCarsAlongCircle() {
		ArrayList<Car> carsToRemove = new ArrayList<Car>();
		for (Car car: this.mainCircle.getCars()) {
			double oldAngle = car.getAngle();
			double newAngle = (oldAngle + car.getVelocity()) % (2*Math.PI);
			car.setDistanceTraveled(car.getDistanceTraveled() + car.getVelocity());
			car.setAngle(newAngle);
			// to avoid removing in the for loop from what we are iterating over to be safe
			if (car.getDistanceTraveled() >= car.getDistanceToTravel()) {
				carsToRemove.add(car);
			}
		}
		// remove all the cars that reached their destination
		for (Car car: carsToRemove) {
			this.mainCircle.getCars().remove(car);
			mainCircle.numCarsThrough++;
		}
		
	}
	
	
	
	/**
	 * Turn a measure of the density around a car into the velocity at which the car should travel
	 */
	public double density2velocity(double d) {
		return MAX_VELOCITY*Math.exp(-D2V_DECAY_RATE*d);
	}
	
	/**
	 * Get density around a car by summing the surrounding car angle differences, where a car in the same 
	 * location is weighted by 1, a car on the opposite side of the circle is weighted by 0, and the rest is 
	 * a linear interpolation between the cars.
	 * A given car does NOT count toward its density around itself
	 */
	public double getDensityAroundCar(Car car, TrafficCircle mainCircle) {
		double out = 0;
		for (Car otherCar: mainCircle.getCars()) {
			if (otherCar != car) {
				double angleBetween = getAngleBetween(car.getAngle(),otherCar.getAngle());
				// linear interpolation of 0 --> 1, Math.PI --> 0, for angle to density calculation
				out += -angleBetween/(Math.PI) + 1; 
			}
		}
		// HACK to get cars to stop at the traffic lights by spiking density. --if light is RED.
		for (TrafficQueue queue: this.queues) {
			double queue2carDist = car.getAngle() - queue.getAngle();
			if (0 < queue2carDist && queue2carDist < 0.4 && currentMode == TrafficMode.RED) {
				out += 5000;
			}
		}
		return out;
	}
	
	/**
	 * helper method for shortest angle along circle. 0 <= x1, x2, < 2*Math.PI
	 */
	public double getAngleBetween(double x1, double x2) {
		double larger = Math.max(x1,x2);
		double smaller = Math.min(x1, x2);
		double alpha = larger - smaller;
		double beta = (2*Math.PI - larger) + smaller;
		return Math.min(alpha,beta);
	}
	
	
	
	
	
	
	
	
	
	/**
	 * Debugging method--printing queues
	 */
	public void printQueueStates() {
		int j = 0;
		for (TrafficQueue queue: queues) {
			System.out.print("Queue " + j + " has " + queue.getCars().size() + " cars ");
			j++;
		}
		System.out.println();
	}
	
	/**
	 * Debugging method--printing circle
	 */
	public void printCircleState() {
		Set<Car> circleCars = this.mainCircle.getCars();
		System.out.println("***");
		System.out.println("Circle angles are:");
		for (Car car: circleCars) {
			System.out.print(car.getAngle() + ",");
		}
		System.out.println("\n");
		System.out.println("Circle velocities are:");
		for (Car car: circleCars) {
			System.out.print(car.getVelocity() + ",");
		}
		System.out.println("\n***");
	}

	public int getATOMIC_SECS_PER_SEC() {
		return ATOMIC_SECS_PER_SEC;
	}

	public void setATOMIC_SECS_PER_SEC(int aTOMIC_SECS_PER_SEC) {
		ATOMIC_SECS_PER_SEC = aTOMIC_SECS_PER_SEC;
	}

	public TrafficCircle getMainCircle() {
		return mainCircle;
	}

	public void setMainCircle(TrafficCircle mainCircle) {
		this.mainCircle = mainCircle;
	}

	public ArrayList<TrafficQueue> getQueues() {
		return queues;
	}

	public void setQueues(ArrayList<TrafficQueue> queues) {
		this.queues = queues;
	}

	public int getAtomicTime() {
		return atomicTime;
	}

	public void setAtomicTime(int atomicTime) {
		this.atomicTime = atomicTime;
	}
	
	
	
	
	
	
	
	
	
	
}
