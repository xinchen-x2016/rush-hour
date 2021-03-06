package solver;

import java.io.*;
import java.util.*;
import java.util.List;

import Exception.*;
import java.awt.*;
import javax.swing.*;

public class Window extends JFrame{

	private HashMap<Integer, Vehicle> vehicles;
	private int[][] plan;
	protected int[] moved;
	protected ArrayList<Move> his;
	private int width;
	private int height;
	
	private Label[][] label;
	private GridLayout grid;
	private JPanel chessboard;
	private Color[] color;
	
 	public void changeState(String s) throws VehiclesIntersectException, VehiclesInvalidException {
		int[] newMoved = HashToMoved(s);
		List<Integer> l = new LinkedList<>();
		for(int i = 1; i < moved.length; i++) {
			if(moved[i] != newMoved[i - 1]) {
				remove(vehicles.get(i));
				l.add(i);
			}
		}
		for(int i : l) {
			moved[i] = newMoved[i - 1];
			add(vehicles.get(i));
		}
	}
	
	public int[] HashToMoved(String s) {
		int[] ret = new int[vehicles.size()];
		int index = 0, j = 0;
		for(int i = 1; i < s.length(); i++) {
			if(s.charAt(i) == '+' || s.charAt(i) == '-') {
				ret[index++] = Integer.parseInt(s.substring(j, i));
				j = i;
			}
		}
		ret[index] = Integer.parseInt(s.substring(j, s.length()));
		return ret;
	}
	
	//initialize a window with width w and height h
	public Window(int w, int h) {
		this.width = w;
		this.height = h;
		plan = new int[width][height];
		this.vehicles = new HashMap<Integer, Vehicle>();
		his = new ArrayList<>();
		
		chessboard = new JPanel();
		grid = new GridLayout(w,h);
		chessboard.setLayout(grid);
		label = new Label[w][h];
		for(int i=0; i<label.length; i++){
			for(int j=0; j<label[i].length; j++){
				label[i][j].setBackground(Color.gray);
				chessboard.add(label[i][j]);
			}
		}
		setVisible(true);
	}
	
	//initialize a window with a test file
	public Window(File f) throws VehiclesIntersectException, VehiclesInvalidException, InvalidFileException {
		List<String> S = FileToStrings(f);
		if(S.size() < 2) throw new InvalidFileException();
		
		this.height = this.width = Integer.parseInt(S.get(0));
		Vehicle.vehicle_number = Integer.parseInt(S.get(1));
		moved = new int[Vehicle.vehicle_number + 1];
		
		plan = new int[width][height];
		this.vehicles = new HashMap<>();
		
		chessboard = new JPanel();
		grid = new GridLayout(this.width,this.height);
		chessboard.setLayout(grid);
		label = new Label[this.width][this.height];
		for(int i=0; i<label.length; i++){
			for(int j=0; j<label[i].length; j++){
				label[i][j].setBackground(Color.gray);
				chessboard.add(label[i][j]);
			}
		}
		setVisible(true);
		color = new Color[Vehicle.vehicle_number];
		color[0] = Color.red;
		int k = 1;
		while(k<Vehicle.vehicle_number){
			Color t = new Color((new Double(Math.random()*128)).intValue()+128,
					(new Double(Math.random()*128)).intValue()+128,
					(new Double(Math.random()*128)).intValue()+128);
			if(t.equals(Color.gray) || t.equals(Color.red))
				k--;
			for(int j=0; j<k; j++)
				if(color[j].equals(t))
					k--;
			color[k] = t;
			k++;
		}
		
		for(int i = 2; i < S.size(); i++) {
			Vehicle temp = new Vehicle(S.get(i));
			add(temp);
		}
		his = new ArrayList<>();
		System.out.println("Initialized successfully!");
	}
	
	public int[][] getPlan() {
		return plan;
	}
	
	public int width() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int height() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}
	
	public HashMap<Integer, Vehicle> getVehicles() {
		return vehicles;
	}
	
	public void setVehicles(HashMap<Integer, Vehicle> vehicles) {
		this.vehicles = vehicles;
	}
	
	public Vehicle getVehicle(int label){
		return vehicles.get(new Integer(label));
	}
	
	private List<String> FileToStrings(File f) {
		List<String> List = new ArrayList<>();
		BufferedReader reader = null;
		try{
			String tempString;
			reader = new BufferedReader(new FileReader(f));
			while ((tempString = reader.readLine()) != null) {
				List.add(tempString.trim());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
		return List;
	}
	
	public void setPlan(int[][] plan) {
		this.plan = plan;
	}
	
	
	public boolean isValid(int x, int y) {
		return x >= 0 && x < this.width && y >= 0 && y < this.height;
	}
	
	public boolean isFree(int x, int y) {
		return plan[x][y] == 0;
	}
	
	protected int getUp(Vehicle V) {
		return V.orientation == 'v' ? moved[V.label] + V.up() : V.up();
	}
	
	protected int getRight(Vehicle V) {
		return V.orientation == 'h' ? moved[V.label] + V.right() : V.right();
	}
	
	protected int getLeft(Vehicle V) {
		return V.orientation == 'h' ? moved[V.label] + V.left() : V.left();
	}
	
	protected int getDown(Vehicle V) {
		return V.orientation == 'v' ? moved[V.label] + V.down() : V.down();
	}
	
	public void add(Vehicle V) throws VehiclesIntersectException, VehiclesInvalidException {
		vehicles.put(V.label, V);
		for(int i = getLeft(V); i < getRight(V); i++) {
			for(int j = getUp(V); j < getDown(V); j++) {
				if(isValid(i, j)) {
					if(isFree(i,j)){
						plan[i][j] = V.label;
						label[i][j].setBackground(color[V.label]);
					}
					else throw new VehiclesIntersectException();
				}
				else throw new VehiclesInvalidException();
			}
		}
	}
	
	public void remove(Vehicle V) {
		vehicles.remove(V);
		for(int i = getLeft(V); i < getRight(V); i++) {
			for(int j = getUp(V); j < getDown(V); j++) {
				plan[i][j] = 0;
				label[i][j].setBackground(Color.gray);
			}
		}
	}
	
	public boolean isMovable(Vehicle V, int cases) {
		int x, y;
		if(V.orientation == 'h') {
			y = getUp(V);
			if(cases > 0) {
				x = getRight(V) - 1 + cases;
				if(!isValid(x, y) || !isFree(x, y)) return false;
			}
			if(cases < 0) {
				x = getLeft(V) + cases;
				if(!isValid(x, y) || !isFree(x, y)) return false;
			}
		}
		else {
			x = getLeft(V);
			if(cases > 0) {
				y = getDown(V) - 1 + cases;
				if(!isValid(x, y) || !isFree(x, y)) return false;
			}
			if(cases < 0) {
				y = getUp(V) + cases;
				if(!isValid(x, y) || !isFree(x, y)) return false;
			}
		}
		return true;
	}
	
	public void move(Vehicle V, int cases) throws VehiclesIntersectException, VehiclesInvalidException {
		remove(V);
		moved[V.label] += cases;
		add(V);
		his.add(new Move(V, cases));
	}
	
	public boolean isWin() {
		Vehicle v = vehicles.get(new Integer(1));
		return plan[plan.length - 1][getUp(v)] == 1;
	}
	
	public String toString() {
		StringBuilder S = new StringBuilder();
			for(int j = 0; j < plan[0].length; j++) {
				for(int i = 0; i < plan.length; i++) {
				S.append(plan[i][j]);
				S.append(' ');
			}
			S.append('\n');
		}
		return S.toString();
	}
	
}
