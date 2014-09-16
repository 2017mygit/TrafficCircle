package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import controller.TrafficSim;
import controller.TrafficSim.TrafficMode;
import model.Car;
import model.TrafficCircle;
import model.TrafficQueue;

import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JRadioButton;
import javax.swing.JComboBox;
import javax.swing.JList;

public class TrafficGUI extends JFrame {

	private JPanel contentPane;
	TrafficCircle mainCircle;
	ArrayList<TrafficQueue> queues;
	private int innerRadius = 140;
	private int outerRadius = 160;
	private int centerRadius;
	private JPanel panelArena;
	private JPanel infoPanel;
	
	JLabel carNumLabel = new JLabel("");
	JLabel numCarsOutLabel = new JLabel("");
	JLabel secondsPassedLabel = new JLabel("");
	JLabel carsPerSecLabel = new JLabel("");
	
	int carRadius = 15;
	int bigRadius = 170;
	int smallRadius = 130;
	
	private Point center = new Point(380,300);
	private final JButton btnStart = new JButton("Start");
	private final JButton btnStop = new JButton("Stop");
	private final JButton btnGreen = new JButton("Green");
	private final JButton btnYellow = new JButton("Yellow");
	private final JButton btnRed = new JButton("Red");
	
	private final JLabel titleLabel = new JLabel("Traffic Simulator");

	/**
	 * Create the frame.
	 */
	public TrafficGUI(TrafficSim sim) {
		
		this.mainCircle = sim.getMainCircle();
		this.queues = sim.getQueues();
		
		this.panelArena = new JPanel() {
			/**
			 * A unique identifier for the panel.
			 */
			private static final long serialVersionUID = 2248926041892506698L;

			/** @override */
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				// g.drawString("testing", 30,30);
				g.setColor(Color.BLACK);
				g.fillOval(center.x-bigRadius,center.y-bigRadius,2*bigRadius,2*bigRadius);
				g.setColor(new Color(200,200,200));
				g.fillOval(center.x-smallRadius,center.y-smallRadius,2*smallRadius,2*smallRadius);
				for (Car car: mainCircle.getCars()) {
					paintCar(car,g);
				}
				for (TrafficQueue queue: queues) {
					paintQueue(queue,g);
				}
				paintLabels(mainCircle, sim.getAtomicTime(), sim.getATOMIC_SECS_PER_SEC(), g);
			}
		};
		titleLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 20));
		panelArena.add(titleLabel);
		
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sim.timer.start();
			}
		});
		
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sim.timer.stop();
			}
		});
		
		btnGreen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sim.currentMode = TrafficMode.GREEN;
			}
		});
		
		btnYellow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sim.currentMode = TrafficMode.YIELD;
			}
		});
		
		btnRed.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sim.currentMode = TrafficMode.RED;
			}
		});
		
		initGUI();
	}
	private void initGUI() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1000, 650);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		contentPane.setBackground(new Color(200,200,200));
		setContentPane(contentPane);
		
		
		
		centerRadius = (innerRadius + outerRadius) / 2;
		this.infoPanel = new JPanel() {
		};
		infoPanel.setLayout(new BoxLayout(infoPanel,BoxLayout.PAGE_AXIS));
		
				
		infoPanel.add(new JLabel("Number of cars in circle:"));
		infoPanel.add(carNumLabel);
		infoPanel.add(new JLabel("Total number of cars through circle:"));
		infoPanel.add(numCarsOutLabel);
		infoPanel.add(new JLabel("Seconds elapsed:"));
		infoPanel.add(secondsPassedLabel);
		infoPanel.add(new JLabel("Average cars out per second:"));
		infoPanel.add(carsPerSecLabel);
		
		infoPanel.add(btnStart);
		infoPanel.add(btnStop);
		infoPanel.add(btnGreen);
		infoPanel.add(btnYellow);
		infoPanel.add(btnRed);

		
		panelArena.setBackground(new Color(200,200,200));
		getContentPane().add(panelArena, BorderLayout.CENTER);
		getContentPane().add(infoPanel,BorderLayout.EAST);
		
	
	}
	
	public void paintCar(Car car, Graphics g) {
		// local coordinates relative to center
		Point local = polar2cartesian(this.centerRadius,car.getAngle());
		g.setColor(car.getColor());
		g.fillOval(center.x+local.x-carRadius, center.y+local.y-carRadius, 2*carRadius,2*carRadius);
	}
	
	public void paintQueue(TrafficQueue queue, Graphics g) {
		int outShift = 50;
		Point local = polar2cartesian(this.centerRadius + outShift,queue.getAngle());
		int fontSize = 20;
		g.setFont(new Font("Arial", Font.PLAIN, fontSize));
		g.setColor(Color.BLACK);
		g.drawString(Integer.toString(queue.getCars().size()), center.x+local.x, center.y+local.y);
	}
	
	public void paintLabels(TrafficCircle mainCircle, int atomicTime, int ATOMIC_SECS_PER_SEC, Graphics g) {
		carNumLabel.setText(Integer.toString(mainCircle.getCars().size()));
		numCarsOutLabel.setText(Integer.toString(mainCircle.numCarsThrough));
		int secsElapsed = atomicTime / ATOMIC_SECS_PER_SEC;
		secondsPassedLabel.setText(Integer.toString(secsElapsed));
		String rawDblString = Double.toString(((double) mainCircle.numCarsThrough)/((double) secsElapsed));
		int n = 4; // how many characters we want to display
		// Stack overflow solution
		String upToNCharacters = rawDblString.substring(0, Math.min(rawDblString.length(), n));
		carsPerSecLabel.setText(upToNCharacters);
		
	}
	
	
	public void start() {
		this.setVisible(true);
	}
	
	public Point polar2cartesian(int radius, double theta) {
		int x = (int) (radius*Math.cos(theta));
		int y = (int) (radius*Math.sin(theta));
		return new Point(x,y);
	}
	

}
