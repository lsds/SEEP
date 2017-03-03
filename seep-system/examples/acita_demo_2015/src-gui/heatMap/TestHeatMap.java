import javax.swing.JFrame;
import javax.swing.WindowConstants;

import java.net.Socket;
import java.net.ServerSocket;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.*;
import javax.swing.*;
import java.awt.Graphics;

public class TestHeatMap {
	private final static int PORT = 20150;

	public static void main(String args[]){

			Thread workerT = new Thread(new DemoFrameWorker());
			workerT.start();
	}

	private static class DemoFrameWorker implements Runnable
	{
		public void run()
		{

			//Create a ser
			JFrame myFrame = new JFrame("This is my frame");
			myFrame.setSize(300,400);
			myFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			myFrame.setLayout(new BorderLayout());


			Container pane = myFrame.getContentPane();
			//FacePanel panel = new FacePanel();
			double[][] data = HeatMap.generateSinCosData(200);
			HeatMap heatMapPanel = new HeatMap(data, true, Gradient.GRADIENT_BLACK_TO_WHITE);
			pane.add(heatMapPanel);
			//myFrame.add(panel);
			myFrame.pack();
			myFrame.setVisible(true);

			try { Thread.sleep(3000); } catch (InterruptedException e) {}
			heatMapPanel.updateData(HeatMap.generateRampTestData(), true);
			/*
			try
			{
				BufferedImage img = readBufferedImageFromFile("vldb/config/master.png");
				ImageIcon icon = new ImageIcon(img);
				JLabel label = new JLabel("Initial", icon, JLabel.CENTER);
				JLabel label = new JLabel("Initial", JLabel.CENTER);
				label.setVerticalTextPosition(JLabel.BOTTOM);
				label.setHorizontalTextPosition(JLabel.CENTER);
				myFrame.getContentPane().add(label);
				myFrame.pack();
				myFrame.setVisible(true);
				BufferedImage img2 = readBufferedImageFromFile("image.jpg");
				ImageIcon icon2 = new ImageIcon(img2);
				label.setText("Finished");
				label.setIcon(icon2);
				myFrame.pack();
				label.repaint();

			}
			catch(Exception e) { throw new RuntimeException(e); }
			*/
		}

		private BufferedImage readBufferedImageFromFile(String filename)
		{
			try
			{
				BufferedImage img = ImageIO.read(new File(filename));
				return img;
			} 
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}

		private static class FacePanel extends JPanel
		{
			private BufferedImage currentFrame = null;

			public void setFrame(BufferedImage newFrame) { this.currentFrame = newFrame; }

			public void paintComponent(Graphics g)
			{
				super.paintComponent(g);
				Graphics2D g2d = (Graphics2D)g;
				if (currentFrame != null)
				{
					g2d.drawImage(currentFrame, null, currentFrame.getWidth(), currentFrame.getHeight());
				}
			}
		}
	}
}


