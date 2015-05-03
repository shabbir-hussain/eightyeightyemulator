package video;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public class VideoFrame extends JFrame implements ActionListener{
	
	public static int SCREEN_WIDTH;
	public static int SCREEN_HEIGHT;
	public static final String APP_NAME = "SpaceInvaders";
	      	

	VideoPanel p;
	
	public VideoFrame(int width, int height){
		SCREEN_WIDTH = width;
		SCREEN_HEIGHT = height;
		
		int space=50;
		
		//set the Frame properties
		setSize(SCREEN_WIDTH+space,SCREEN_HEIGHT+space);	
		setTitle(APP_NAME);									
		
		p = new VideoPanel(SCREEN_WIDTH,SCREEN_HEIGHT);
		
		add(p);
		
		Timer t = new Timer(100,this);
		t.start();
		
	
		
		//set the x button as the default close operation
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);					
		setLocationRelativeTo(null);
		setVisible(true);
		
	}
	
	public static void main(String args[]){
		new VideoFrame(224,256);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
		
		p.repaint();
	}
	

	   public void plotData( int x, int y, int value, int bit) {
		   this.p.plotData(x, y, value, bit);
	   }


}
