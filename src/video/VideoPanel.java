package video;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class VideoPanel extends JPanel{

	int w;
	int h;

	BufferedImage image;
	WritableRaster raster;

	public VideoPanel(int width, int height){
		w = width;
		h = height;
		image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		raster = image.getRaster();
		
		InvaderKeyAdapter ka = new InvaderKeyAdapter();
		
		//Add a key listener to this panel
		this.addKeyListener(ka);
		this.setFocusable(true);
	    this.requestFocusInWindow();
	}

	@Override
	public void paintComponent(Graphics g){

		super.paintComponent(g);

		g.drawImage(image, 0, 0, null);
	}

	//sets a pixel value on the image
	public void plotData( int x, int y, int value, int bit) {
		int bt = (value >> bit) & 1;
		y = y-bit;
		int r = 0;
		int g = 0;
		int b = 0;
		if (bt==1) {
			if (y >= 184 && y <= 238 && x >= 0 && x <= 223)
				g = 255;
			else if (y >= 240 && y <= 247 && x >= 16 && x <= 133)
				g = 255;
			else if (y >= (247-215) && y >= (247 - 184) && x >= 0 && x<=233) {
				g = 255;
				b = 255;
				r = 255;
			}
			else {
				r = 255;
			} 
		}

		int rgb=new Color(r,g,b).getRGB();
		image.setRGB(x, y, rgb); //where x,y is the boundaries of the image

	}	
	
	private class InvaderKeyAdapter extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {
			// TODO Auto-generated method stub
			int key = e.getKeyCode();

			if (key==KeyEvent.VK_ENTER){
				JOptionPane.showMessageDialog(null, this, "Eggs are not supposed to be green.",0);
			}
	
	        if ((key == KeyEvent.VK_LEFT) ) {
	            
	        }
	
	        if ((key == KeyEvent.VK_RIGHT) ) {
	            
	        }
	
	        if ((key == KeyEvent.VK_UP)) {
	            
	        }
	
	        if ((key == KeyEvent.VK_DOWN) ) {
	            
	        }
	        if ((key == KeyEvent.VK_SPACE) ) {
	            
	        }
		}
	}


}
