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

public class FaceRecognitionDemo {
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

			BufferedImage img = readBufferedImageFromFile("../core-emane/vldb/config/sink.png");
			ImageIcon icon = new ImageIcon(img);
			JLabel label = new JLabel("Waiting for first tuple...", icon, JLabel.CENTER);
			label.setVerticalTextPosition(JLabel.BOTTOM);
			label.setHorizontalTextPosition(JLabel.CENTER);
			myFrame.getContentPane().add(label);
			myFrame.pack();
			myFrame.setVisible(true);
			//Container pane = myFrame.getContentPane();
			//FacePanel panel = new FacePanel();
			//pane.add(panel);
			//myFrame.add(panel);
			//frame.pack();
			//myFrame.setVisible(true);

			boolean writeImage = false;
			while (true)
			{
				try
				{
					//Read incoming images.		
					byte[] frameBytes = (byte[])input.readObject();
					String name = (String)input.readObject();
					System.out.println("Read "+frameBytes.length+" byte frame, recognized: "+name);
					BufferedImage newImg = readBufferedImage(frameBytes);
					if (writeImage)
					{
						writeBufferedImage(newImg);
						writeImage = false;
					}

					ImageIcon newIcon = new ImageIcon(newImg);
					label.setIcon(newIcon);
					label.setText("Recognized: "+ (name.isEmpty() ? "Unknown" : name));
					myFrame.pack();
					label.repaint();
				}
				catch(Exception e) { throw new RuntimeException(e); }
			}
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


