
package tca.base;

public class Cell
{
	// cell-specific data
	public int fMaxSpeed;
	public int fSpeed;
	public int fSpaceGap;
	public int fVehicleID;
	public int fVehicleLength;
	public int fLightID; //-1 якщо нема
	public int color; //-1 якщо нема, 0 червоний, 1 зелений


	public Cell()
	{
		clear();
	}

	public void clear()
	{
		fMaxSpeed = 0;
		fSpeed = -1;
		fSpaceGap = 0;
		fVehicleID = -1;
		fVehicleLength = 1;
		fLightID = -1;
		color=-1;
	}

	public void copyTo(Cell cell)
	{
		cell.fMaxSpeed = fMaxSpeed;
		cell.fSpeed = fSpeed;
		cell.fSpaceGap = fSpaceGap;
		cell.fVehicleID = fVehicleID;
		cell.fVehicleLength = fVehicleLength;
		cell.fLightID = fLightID;
		cell.color=color;
	}
}
