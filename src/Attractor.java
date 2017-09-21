
public class Attractor
{
	private double x,y;

	public Attractor(double x, double y)
	{
		super();
		this.x = x;
		this.y = y;
	}

	public double getX()
	{
		return x;
	}

	public void setX(double x)
	{
		this.x = x;
	}

	public double getY()
	{
		return y;
	}

	public void setY(double y)
	{
		this.y = y;
	}
	
	public double distanceToVotingPoint(VotingPoint vp)
	{
		double distance=-1;
		distance = Math.sqrt(Math.pow((getY() - vp.getY()), 2) + Math.pow((getX() - vp.getX()), 2));
		
		//---------------------------
		return distance;
	}
}
