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

public class AcitaDemo {
	private final static int PORT = 20150;

	public static void main(String args[]){
		try
		{
			ServerSocket serverSocket = new ServerSocket(PORT);

			while (true)
			{
				Socket workerSocket = serverSocket.accept();

				Thread workerT = new Thread(new DemoFrameWorker(workerSocket));
				workerT.start();
			}
		}
		catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private static class DemoFrameWorker implements Runnable
	{
		private final Socket socket; 
		private final ObjectInputStream input;

		DemoFrameWorker(Socket socket)
		{
			this.socket = socket;
			try
			{
				this.input = new ObjectInputStream(socket.getInputStream());	
			} catch(IOException e) { throw new RuntimeException(e); }
		}

		public void run()
		{

			//Create a ser
			JFrame myFrame = new JFrame("Face Recognition Demo");
			myFrame.setSize(300,400);
			myFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

			
			//BufferedImage img = readBufferedImageFromFile("../vldb/config/sink.png");
			//ImageIcon icon = new ImageIcon(img);
			//JLabel label = new JLabel("Waiting for first tuple...", icon, JLabel.CENTER);
			//label.setVerticalTextPosition(JLabel.BOTTOM);
			//label.setHorizontalTextPosition(JLabel.CENTER);
			//myFrame.getContentPane().add(label);
			//myFrame.pack();
			//myFrame.setVisible(true);
			Container pane = myFrame.getContentPane();
			//FacePanel panel = new FacePanel();
			//Panel heatMapPanel = new HeatMap(data, useGraphicsYAxis, Gradient.GRADIENT_BLACK_TO_WHITE);
			//double[][] data = HeatMap.generateSinCosData(200);
			double[][] data = HeatMap.generateBlankTestData();
			HeatMap heatMapPanel = new HeatMap(data, true, Gradient.GRADIENT_BLACK_TO_WHITE);
			pane.add(heatMapPanel);
			//myFrame.add(panel);
			myFrame.pack();
			myFrame.setVisible(true);

			boolean writeImage = true;
			while (true)
			{
				try
				{
					//Read incoming images.		
					int[][] heatMap = (int[][])input.readObject();
					System.out.println("Read heatmap.");
					heatMapPanel.updateData(intMapToDouble(heatMap), true);
				}
				catch(Exception e) { throw new RuntimeException(e); }
			}
		}

		private double[][] intMapToDouble(int[][] intMap)
		{
			double[][] doubleMap = new double[intMap.length][intMap[0].length];
			for (int i = 0; i < intMap.length; i++)
			{
				doubleMap[i] = new double[intMap[i].length];
				for (int j = 0; j < intMap[i].length; j++)
				{
						doubleMap[i][j] = intMap[i][j];
				}
			}
			return doubleMap;
		}

		private BufferedImage readBufferedImage(byte[] frameBytes)
		{
			ByteArrayInputStream bais = new ByteArrayInputStream(frameBytes);
			try
			{
				BufferedImage img = ImageIO.read(bais);
				return img;
			} 
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
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

		private void writeBufferedImage(BufferedImage bufferedImage)
		{
			try
			{
				File outputfile = new File("first.jpg");
				ImageIO.write(bufferedImage, "jpg", outputfile);
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


