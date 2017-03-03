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
			JFrame myFrame = new JFrame("Heat Map Demo");
			myFrame.setSize(500,750);
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
			double[][] data = HeatMap.generateBlankTestData(100);
			//HeatMap heatMapPanel = new HeatMap(data, true, Gradient.GRADIENT_BLACK_TO_WHITE);
			//HeatMap heatMapPanel = new HeatMap(data, true, Gradient.GRADIENT_GREEN_YELLOW_ORANGE_RED);
			//HeatMap heatMapPanel = new HeatMap(data, true, Gradient.GRADIENT_RAINBOW);
			HeatMap heatMapPanel = new HeatMap(data, true, Gradient.GRADIENT_MATPLOTLIB);
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
					/*
					int[][] heatMap = (int[][])input.readObject();
					System.out.println("Read heatmap: "+posCountsToString(heatMap));
					heatMapPanel.updateData(intMapToDouble(heatMap), true);
					*/
					String serializedHeatMap = input.readObject().toString();
					System.out.println("Read heatmap: "+serializedHeatMap);
					heatMapPanel.updateData(heatMapToImgData(serializedHeatMap), true);
					//System.out.println(input.readObject().toString());
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

		private String posCountsToString(int[][] posCounts)
		{
			String occupiedTiles = "";

			for (int x = 0; x < posCounts.length; x++)
			{
				for (int y = 0; y < posCounts[0].length; y++)
				{
					if (posCounts[x][y] > 0)
					{
						String tileCount = "" + x + "," + y + "," + posCounts[x][y];

						if (!occupiedTiles.isEmpty()) { occupiedTiles += ";"; }
						occupiedTiles += tileCount;
					}
				}
			}
			return occupiedTiles;
		}

		private double[][] heatMapToImgData(String serialized)
		{

			String[] parts = serialized.split(";");
			String[] metadata = parts[0].split(",");
			double tileWidth = Double.parseDouble(metadata[0]);
			double tileHeight = Double.parseDouble(metadata[1]);
			int xTiles = Integer.parseInt(metadata[2]);
			int yTiles = Integer.parseInt(metadata[3]);


			double[][] posCounts = new double[xTiles][yTiles];

			for (int pos = 1; pos < parts.length; pos++)
			{
				String[] posCount = parts[pos].split(",");
				if (posCount.length != 3) { throw new RuntimeException("Logic error, invalid pos."); }
				int x = Integer.parseInt(posCount[0]);
				int y = Integer.parseInt(posCount[1]);
				int count = Integer.parseInt(posCount[2]);
				posCounts[x][y] = count;
			}

			return posCounts;
		}
	}
}


