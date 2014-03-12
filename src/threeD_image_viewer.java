import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;


public class threeD_image_viewer {

	public static void main(String[] args) throws IOException {
		System.out.print("Press 1 for Quicksort and 2 for MergeSort:");
		Scanner r= new Scanner(System.in);
		int sort_type=r.nextInt();
		System.out.print("Do you want to draw the objects wireframe? (Y/N):");
		boolean wireframe=true;
		if(r.next().toLowerCase().equals("n")){wireframe=false;};
		String filename="";
		while(filename.equals("") || !new File("example 3D images\\" + filename).exists()){
			System.out.print("Please enter the object's filename:");
			filename=r.next();
			if(!new File("example 3D images\\" + filename).exists()){
				System.out.println("Invalid file. Please reenter");
			}
		}
		r.close();
		System.out.println("WASD to move, arrow keys to rotate, numpad +/- to zoom.");
		createAndShowGUI(500,500,sort_type,wireframe,filename);
	}
		
	private static void createAndShowGUI(int height, int width,int sort_type,boolean wireframe, String filename) throws IOException {
        JFrame f = new JFrame("3D Image");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(height,width);
		f.add(new Pane(height,width,sort_type,wireframe,filename));
        f.pack();
        f.setVisible(true);
    }
}

@SuppressWarnings("serial")
class Pane extends JPanel {
	private double[][] points;
	private ArrayList<int[]> polygons;
	private int width;
	private int height;
	private int comparisoncounter=0;//just using a global to make it easier
	private int datamovements=0;//just using a global to make it easier
	private boolean draw_wireframe=true;
	private int sort_type=2;
	private double offset_x_axis=0.0;
	private double offset_y_axis=0.0;
	private double offset_z_axis=0.0;
	
	public Pane(int height, int width,int sort_type,boolean wireframe, String filename) throws IOException{	
		this.width=width;
		this.height=height;
		this.sort_type=sort_type;
		draw_wireframe=wireframe;
		
		BufferedReader br = new BufferedReader(new FileReader("example 3D images\\" + filename));
		String initializers=br.readLine().trim();
		
		String[] numIdentifiors=initializers.split("\\s+");
		points=new double[Integer.valueOf(numIdentifiors[0])][3];
		polygons=new ArrayList<int[]>(Integer.valueOf(numIdentifiors[1]));//need to use arraylist because the int[] it contains is unknown length
		
		//get points and fill array
		for(int i=0;i<Integer.valueOf(numIdentifiors[0]);i++){
			String point=br.readLine().trim();
			String[] pointxyz=point.split("\\s+");
			for(int c=0;c<pointxyz.length;c++){
				points[i][c]=Double.valueOf(pointxyz[c]);
			}
		}
		//get polygons and fill array
		for(int i=0;i<Integer.valueOf(numIdentifiors[1]);i++){
			String connectiontxtline=br.readLine().trim();
			String[] polygonspoints=connectiontxtline.split("\\s+");
			int[] connection = new int[Integer.valueOf(polygonspoints[0])];
			for(int c=1;c<polygonspoints.length;c++){
				connection[c-1]=Integer.valueOf(polygonspoints[c]);
			}
			polygons.add(connection);
		}
		br.close();
		
		//sort array of polygons by the avg z value of the points in each
		Long time=System.currentTimeMillis();
		
		//quicksort
		if(sort_type==1){
			polygons=quicksort(polygons);
			System.out.println("Quick sort took "+comparisoncounter + " comparisons and " +datamovements+ " data movements");
		}
		
		//merge sort
		else if(sort_type==2){
			polygons=mergesort(polygons);
			System.out.println("Merge sort took "+comparisoncounter + " comparisons and " +datamovements+ " data movements");
		}
		
		System.out.println("It took " + (System.currentTimeMillis()-time) + " milliseconds to sort");
		datamovements=0;
		comparisoncounter=0;
		
		//rotate on keypress
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP,0),"RUP");
		this.getActionMap().put("RUP",new AbstractAction(){
			public void actionPerformed(ActionEvent e) {rotate(-5,0);}	
		});
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,0),"RDOWN");
		this.getActionMap().put("RDOWN",new AbstractAction(){
			public void actionPerformed(ActionEvent e) {rotate(5,0);}	
		});
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,0),"RLEFT");
		this.getActionMap().put("RLEFT",new AbstractAction(){
			public void actionPerformed(ActionEvent e) {rotate(0,5);}	
		});
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,0),"RRIGHT");
		this.getActionMap().put("RRIGHT",new AbstractAction(){
			public void actionPerformed(ActionEvent e) {rotate(0,-5);}	
		});
		
		//move object in x, y and z directions
		//have to move the object only in the draw function, because rotate needs x,y and z to be within -1 to 1
		
		//zoom object in/out (move in z direction)
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD,0),"ZIN");
		this.getActionMap().put("ZIN",new AbstractAction(){
			public void actionPerformed(ActionEvent e) {offset_z_axis+=0.05;repaint();}	
		});
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT,0),"ZOUT");
		this.getActionMap().put("ZOUT",new AbstractAction(){
			public void actionPerformed(ActionEvent e) {offset_z_axis+=-0.05;repaint();}	
		});
		
		//move x and y
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_D,0),"MRIGHT");
		this.getActionMap().put("MRIGHT",new AbstractAction(){
			public void actionPerformed(ActionEvent e) {offset_x_axis+=0.1;repaint();}	
		});
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_A,0),"MLEFT");
		this.getActionMap().put("MLEFT",new AbstractAction(){
			public void actionPerformed(ActionEvent e) {offset_x_axis+=-0.1;repaint();}	
		});
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_W,0),"MUP");
		this.getActionMap().put("MUP",new AbstractAction(){
			public void actionPerformed(ActionEvent e) {offset_y_axis+=-0.1;repaint();}	
		});
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_S,0),"MDOWN");
		this.getActionMap().put("MDOWN",new AbstractAction(){
			public void actionPerformed(ActionEvent e) {offset_y_axis+=0.1;repaint();}	
		});
		/*TODO
		 * DO ALL PREVIOUS OPTIONs USING MOUSE
		*/
		
	}
	
	private void rotate(int latitude,int longitude){
		//rotate by points, not polygons, because polygons share points and these points would get rotated repeatedly
		for(int i=0;i<points.length;i++){
			double oldx=points[i][0];
			double oldy=points[i][1];
			double oldz=points[i][2];
			
			points[i][0] = oldx*Math.cos(Math.toRadians(longitude))+oldz*Math.sin(Math.toRadians(longitude));
			points[i][2] = oldz*Math.cos(Math.toRadians(longitude))-oldx*Math.sin(Math.toRadians(longitude));
			
			points[i][1] = oldy*Math.cos(Math.toRadians(latitude)) - points[i][2]*Math.sin(Math.toRadians(latitude));
			points[i][2] = oldy*Math.sin(Math.toRadians(latitude)) + points[i][2]*Math.cos(Math.toRadians(latitude));
		}
		if(sort_type==1){polygons=quicksort(polygons);}
		else if(sort_type==2){polygons=mergesort(polygons);}
		repaint();
	}
	
	public Dimension getPreferredSize() {
        return new Dimension(width,height);
    }
	
	public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
		for(int[] polygon : polygons){
			int[] xpoints= new int[polygon.length];
			int[] ypoints= new int[polygon.length];
			double zval=0;
			//fill int[] of x and y points converted to screen values
			for(int point=0;point<polygon.length;point++){
				double flatx = points[polygon[point]][0]/((points[polygon[point]][2]/8-1) + offset_z_axis) + offset_x_axis;
				double flaty = points[polygon[point]][1]/((points[polygon[point]][2]/8-1) + offset_z_axis) + offset_y_axis;
				xpoints[point]=(int)(width/2*flatx+width/2);
				ypoints[point]=(int)(height/2*flaty+height/2);
				zval+=points[polygon[point]][2];
			}
			zval=(zval/polygon.length)+1;//+1 adds minimum value to all z values, moving them to start at 0 instead of -1
			int rgbNum =(int) ((zval/2)*255.0);//using a z value scale of 0-2, convert to a 255 rgb scale
			//shape might rotate z vals out of -1 to 1 range, have to check for that
			if(rgbNum<0){rgbNum=0;}
			else if(rgbNum>255){rgbNum=255;}
			g.setColor(new Color(rgbNum,rgbNum,rgbNum));//same z value on all 3 makes greyscale
			
			g.fillPolygon(xpoints, ypoints, polygon.length);
			
			if(draw_wireframe){
				g.setColor(Color.black);
				g.drawPolygon(xpoints, ypoints, polygon.length);
			}
		}
	}
	
	public double avg_zvalue(int[] polygon){
		double avg_z=0;
		for(int p:polygon){
			avg_z+=points[p][2];
		}
		return avg_z/polygon.length;
	}
	
	public ArrayList<int[]> quicksort(ArrayList<int[]> polygons){
		if(polygons.size()<=1){
	        return polygons;//base case, array size of 1 or less is sorted
		}
	    int[] pivot = polygons.remove(polygons.size()/2);//get pivot
	    //ideally, less and greater are perfectly sized here. However, since the're arraylists, they can adapt to worse cases
	    int idealsize=1;
	    if(polygons.size()>2){
	    	idealsize=polygons.size()/2 - 1;
	    }
	    ArrayList<int[]> less =new ArrayList<int[]>(idealsize);
	    ArrayList<int[]> greater =new ArrayList<int[]>(idealsize);
	    for(int[] poly:polygons){
	    	comparisoncounter++;
	    	datamovements++;
	        if(avg_zvalue(poly)<=avg_zvalue(pivot)){
	        	less.add(poly);
	        }
	        else{
	        	greater.add(poly);
	        }
	    }
	    ArrayList<int[]> sorted=new ArrayList<int[]>(polygons.size());
	    ArrayList<int[]> sortdless=quicksort(less);//recurse
	    sorted.addAll(sortdless);datamovements+=sortdless.size();
	    sorted.add(pivot);datamovements++;
	    ArrayList<int[]> sortdgreater=quicksort(greater);//recurse
	    sorted.addAll(sortdgreater);datamovements+=sortdgreater.size();
	    return sorted;
	}
	public ArrayList<int[]> mergesort(ArrayList<int[]> polygons){
		if(polygons.size()<=1){//base case
			return polygons;
		}
		//split list in half (do a deep copy)
		ArrayList<int[]> left=new ArrayList<int[]>();
		for(int[] poly: polygons.subList(0, polygons.size()/2)) {
			left.add(poly.clone());
			datamovements++;
		}
		ArrayList<int[]> right=new ArrayList<int[]>();
		for(int[] poly: polygons.subList(polygons.size()/2,polygons.size())) {
			right.add(poly.clone());
			datamovements++;
		}
		//recuse to split further
		left = mergesort(left);
		right = mergesort(right);
		//combine and sort the two lists
			ArrayList<int[]> combined=new ArrayList<int[]>(left.size()+right.size());
			//ensure both lists still have elements. if not, short circuit
			while(left.size()>0 && right.size()>0){
				comparisoncounter++;
				datamovements+=2;
				if(avg_zvalue(left.get(0))<=avg_zvalue(right.get(0))){
					combined.add(left.get(0));
					left.remove(0);
				}
				else{
					combined.add(right.get(0));
					right.remove(0);
				}
			}
			//short circuit for if/when one list is emptied before other. Dump remaining polygons into combined list
			if(left.size()==0){
				for(int[] poly:right){
					combined.add(poly);
					datamovements++;
				}
			}
			else if(right.size()==0){
				for(int[] poly:left){
					combined.add(poly);
					datamovements++;
				}
			}
		return combined;
	}
	
}
