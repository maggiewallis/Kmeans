import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JTextArea;

public class KMeansPanel extends JPanel implements KMeansConstants
{
	int K; // how many attractors?
	boolean needsClear; // next repaint - should we wipe out the previous attractor locations?
	double minX, minY, maxX, maxY; // the range of the data we've loaded.
	BufferedImage lastScreen; // used to store the previous appearance of the screen so we 
							  //   can show trails of + signs, indicating the motion of the attractors
	JTextArea descriptionTA; // the textArea in the west pane of the layout, so we can show data there.
	
	ArrayList<Attractor> tenAttractors; // we will always have ten attractors 
										//   in the list, but only use K of them.
	ArrayList<Attractor> newAttractors;
	ArrayList<Attractor> oldAttractors;
	ArrayList<VotingPoint> data; // this is where we store all the GPS coordinates.
	
	KMeansPanel()
	{
		super();
		K = 4;
		setBackground(Color.BLACK);
		tenAttractors = new ArrayList<Attractor>();
		for (int i=0; i<10; i++)
			tenAttractors.add(new Attractor(0,0));
		
		data = new ArrayList<VotingPoint>();
		
		needsClear = true;
		minX = -1;
		minY = -1;
		maxX = 1;
		maxY = 1;
		randomize(true);
	}
	
	public void paintComponent(Graphics g)
	{
		// handle the trails... a bit fancy.
		if (needsClear)
		{
			System.out.println("Clearing.");
			super.paintComponent(g);
			needsClear = false;
			lastScreen = new BufferedImage(getWidth(),getHeight(),BufferedImage.TYPE_INT_RGB);
			
		}
		else if (lastScreen != null)
		{
			g.drawImage(lastScreen, 0, 0,null);
		}
		else
			lastScreen = new BufferedImage(getWidth(),getHeight(),BufferedImage.TYPE_INT_RGB);
		
		// capture this image for next time.
		Graphics g2 = lastScreen.createGraphics();
				
		// Draw Voting Points
		for (VotingPoint datum:data)
		{
			g2.setColor(COLOR_LIST[datum.getWhichAttractor()]);
			g2.fillOval(interpolateX(datum.getX())-VP_RADIUS, interpolateY(datum.getY())-VP_RADIUS, 2*VP_RADIUS, 2*VP_RADIUS);
		}
		// Draw Attractors
		for (int k=0; k<K; k++)
		{
			int x = interpolateX(tenAttractors.get(k).getX());
			int y = interpolateY(tenAttractors.get(k).getY());
			g2.setColor(Color.BLACK);
			g2.fillRect(x-ATT_RADIUS, y-1, 2*ATT_RADIUS, 3);
			g2.fillRect(x-1, y-ATT_RADIUS, 3, 2*ATT_RADIUS);
			g2.setColor(COLOR_LIST[k]);
			g2.drawLine(x-ATT_RADIUS,y,x+ATT_RADIUS,y);
			g2.drawLine(x, y-ATT_RADIUS, x,y+ATT_RADIUS);
		}
		
		g.drawImage(lastScreen, 0, 0, null);
		
		for (int k=0; k<K; k++)
		{
			int x = interpolateX(tenAttractors.get(k).getX());
			int y = interpolateY(tenAttractors.get(k).getY());
			g.setColor(Color.WHITE);
			g.drawOval(x-ATT_RADIUS-1,y-ATT_RADIUS-1,2*ATT_RADIUS+2,2*ATT_RADIUS+2);
		}
			
	}
	
	public int interpolateX(double xVal)
	{
		return (int)((xVal-minX)/(maxX-minX)*(this.getWidth()));
	}
	
	
	public int interpolateY(double yVal)
	{
		return (int)((yVal-minY)/(maxY-minY)*(this.getHeight()));
	}
	
	
	public void findRangeOfData()
	{
		minX = +9e9; // these are member variables. I've reset them for you.
		minY = +9e9;
		maxX = -9e9;
		maxY = -9e9;
		//find the largest and smallest values for x and y in the data VotingPoints arraylist
		//double newX;
		//double newY;
		for (int i=0; i < data.size(); i++)
		{
			 if (maxX < data.get(i).getX())
			{
				maxX = data.get(i).getX();
			}
			if (maxY < data.get(i).getY())
			{
				maxY = data.get(i).getY();
			}
			if (minX > data.get(i).getX())
			{
				minX = data.get(i).getX();
			}
			if (minY > data.get(i).getY())
			{
				minY = data.get(i).getY();
			}
		}
		System.out.print(maxX + " " +maxY + " " + minX + " " + minY);

		
		//--------------------------------------------
		if (minX == maxX || minY == maxY)
		{
			throw new RuntimeException("Range not large enough.");
		}
	}
	
	
	public void loadFile(File theFile)
	{
		System.out.println("Loading: "+theFile);
		BufferedReader fileIn;
		try
		{
			fileIn = new BufferedReader(new FileReader(theFile));
		}
		catch (FileNotFoundException fnfExp)
		{
			System.out.println("File not found. Since this came from a file dialog, that's pretty weird.");
			return;
		}
		int i =1;
		String line = null;
		try
		{
			line = fileIn.readLine(); 
			i = 2;
			line = fileIn.readLine(); 
		}
		catch (IOException ioExp)
		{
			System.out.println("Error trying to read file at line: "+i+".");
			ioExp.printStackTrace();
		}
		while(line != null)
		{
			try
			{
				i++;
				
				double longit = 0;
				double latid = 0;
				String[] vars = line.split("\t");
				longit = Double.parseDouble(vars[1]);
				latid = Double.parseDouble(vars[0]);
				VotingPoint newPoint = new VotingPoint(longit, latid);
				data.add(newPoint);
				
				//------------------------------
				line = fileIn.readLine();
			}
			catch (IOException ioExp)
			{
				System.out.println("Error trying to read file at line: "+i+".");
				ioExp.printStackTrace();
			}
			catch (NumberFormatException nfExp)
			{
				System.out.println("Trouble parsing line "+i+": \""+line+"\"");
				continue;
			}
		}
		try
		{
			fileIn.close();
		}
		catch (IOException ioExp)
		{
			System.out.println("Error trying to close file.");
			ioExp.printStackTrace();
		}
		
		findRangeOfData();
		clearTrails();
	}
	public ArrayList<VotingPoint> getData()
	{
		return data;
	}
	
	public void steptosolution()
	{
		for (int i = 0; i < K; i++)
		{
				double X = tenAttractors.get(i).getX();
				double Y = tenAttractors.get(i).getY();
				oldAttractors = tenAttractors;
				findClosest();
				reAverage();
				double newX = tenAttractors.get(i).getX();
				double newY = tenAttractors.get(i).getY();
				while (X != newX && Y != newY)
				{
					 X = tenAttractors.get(i).getX();
					 Y = tenAttractors.get(i).getY();
					oldAttractors = tenAttractors;
					findClosest();
					reAverage();
					 newX = tenAttractors.get(i).getX();
					 newY = tenAttractors.get(i).getY();
					 if (X == newX && Y== newY)
						{
							break;
						}
				}

		}
		
		//check all of the attractors each time
	}
	
	public void findClosest()
	{
		System.out.println("calling findClosest();");
		for (int i = 0; i < data.size(); i++)
		{
			double maxDiff = (double)Math.pow(10, 9);
			for (int j = 0; j < K; j++)
			{
				double Xdiff = data.get(i).getX() - tenAttractors.get(j).getX();
				double Ydiff = data.get(i).getY() - tenAttractors.get(j).getY();
				double diff = Math.sqrt(Math.pow(Xdiff, 2) + Math.pow(Ydiff, 2));
				if (diff < maxDiff)
				{
					maxDiff = diff; 
					data.get(i).whichAttractor = j;
				}

			}
		}
		

		//------------------------------
		repaint();
	}
	
	public void reAverage()
	{
		System.out.println("calling reAverage();");

		for (int i = 0; i < K; i++)
		{
			double totalX = 0;
			double totalY = 0;
			int counter = 0;
			for (int j = 0; j < data.size(); j++)
			{
				if (data.get(j).whichAttractor == i)
				{
					totalX = data.get(j).getX() + totalX;
					totalY = data.get(j).getY() + totalY;
					counter++;
					
				}
				if (counter != 0)
				{
					double avgY = totalY/counter;
					double avgX = totalX/counter;
					tenAttractors.get(i).setX(avgX);
					tenAttractors.get(i).setY(avgY);
				}
			}
		}
		
		//-------------------------------
		updateTA();
		repaint();
	}
	

	public void setK(int inK)
	{
		K = inK;
		System.out.println("Setting K to: "+inK);
		clearTrails();
	}
	
	public void randomize(boolean shouldPickLocations)
	{
		System.out.println("Randomizing. Using locations = "+shouldPickLocations);
		for (Attractor att:tenAttractors)
		{
			if (shouldPickLocations || data.isEmpty())
			{
				// set the attractor's location to a random location from anywhere 
				//      in (minX<-->maxX) x (minY<-->maxY)
				for (int i = 0; i < K; i++)
				{
					double Xval = minX + (maxX + minX)*(Math.random());
					double Yval = minY + (maxY + minY)*(Math.random());
					tenAttractors.get(i).setX(Xval);
					tenAttractors.get(i).setY(Yval);

				}
				//---------------------------------------
			}
			else
			{
				// set the attractor's location to match that of a random VotingPoint in the data Al. 
				for (int i = 0; i < K; i++)
				{
					int randomm = (int)(Math.random()*(data.size()));
		
					tenAttractors.get(i).setX(data.get(randomm).getX());
					tenAttractors.get(i).setY(data.get(randomm).getY());
				}
				//------------------------------------------
			}
		}
		updateTA();
		clearTrails();
		
	}
	/**
	 * we've just been told to clear the trails by the user vi UI. 
	 */
	public void clearTrails()
	{
		System.out.println("calling clear.");
		needsClear = true;
		repaint();
	}
	
	
	public void setDescriptionField(JTextArea TA)
	{
		descriptionTA = TA;
	}
	
	public void updateTA()
	{
		if (descriptionTA == null)
			return;
		String output = descriptionTA.getText();
		DecimalFormat numberFormat = new DecimalFormat("0.000000");
		
		output+="-----------------------\n";
		descriptionTA.setText(output);
	}
	
}
