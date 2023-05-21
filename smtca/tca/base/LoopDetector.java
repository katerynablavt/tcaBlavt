
package tca.base;

public class LoopDetector
{
	// detector quantities
	public int    fCellNr;
	public int    fMeasurementInterval;
	public int    fNrOfDetectedVehicles;
	public double fSpaceMeanSpeed;
	public int    fMeanFlowRate;
	public int    fMeanDensity;

	// internal datastructures
	private int    fDetectorRange;
	private int    fLastDetectedVehicleID;
	private int    fLastTimeVehicleDetected;
	private int    fNrOfVehiclesCounted;
	private int    fMeasurementTimeLeft;
	private double fCurrentDensity;
	private double fCurrentFlow;
	private double fCurrentSpaceMeanSpeed;
	private int    fNrOfSamples;

	public LoopDetector(int cellNr, int detectorRange, int measurementInterval)
	{
		fCellNr = cellNr;
		fDetectorRange = detectorRange;
		fMeasurementInterval = measurementInterval;
		reset();
	}

	public void reset()
	{
		fNrOfDetectedVehicles = 0;
		fSpaceMeanSpeed = 0.0;
		fMeanFlowRate = 0;
		fMeanDensity = 0;
		fLastDetectedVehicleID = -1;
		fLastTimeVehicleDetected = -1;
		fNrOfVehiclesCounted = 0;
		fMeasurementTimeLeft = 0;
		fCurrentDensity = 0.0;
		fCurrentFlow = 0.0;
		fCurrentSpaceMeanSpeed = 0.0;
		fNrOfSamples = 0;
	}

	public boolean detectorUpdated()
	{
		return (fMeasurementTimeLeft == fMeasurementInterval);
	}

	// this method should be called every simulation-cycle (i.e., every second)
	public void update(State state, double cellLength)
	{
		int firstCellOfLDRange = state.predecessor(fCellNr,(fDetectorRange / 2) - 1);
		int lastCellOfLDRange = state.successor(fCellNr,(fDetectorRange / 2));

		int currentNrOfVehiclesCounted = 0;
		double currentSumOfSpeedsMeasured = 0.0;

		int currentCellNr = firstCellOfLDRange;
		while (currentCellNr != lastCellOfLDRange) {

			Cell currentCell = state.fCells[currentCellNr];

			if (currentCell != null) {
				++currentNrOfVehiclesCounted;
				currentSumOfSpeedsMeasured += currentCell.fSpeed;
			}

			currentCellNr = state.successor(currentCellNr,1);
		}

		if (currentNrOfVehiclesCounted > 0) {
			fCurrentDensity += currentNrOfVehiclesCounted;
			fCurrentFlow += currentSumOfSpeedsMeasured;
			fCurrentSpaceMeanSpeed += (currentSumOfSpeedsMeasured / (double) currentNrOfVehiclesCounted); 
			++fNrOfSamples;
		}

		// has the measurement-interval elapsed ?
		if (fMeasurementTimeLeft == 0) {

			fCurrentDensity /= ((double) fMeasurementInterval * (double) fDetectorRange);
			fCurrentFlow /= ((double) fMeasurementInterval * (double) fDetectorRange);
			fCurrentSpaceMeanSpeed /= (double) fNrOfSamples; 
			
			// convert space-mean-speed from cells/s to km/h
			fSpaceMeanSpeed = fCurrentSpaceMeanSpeed * cellLength * 3.6;

			// convert density from vehicles/cells to vehicles/km
			fMeanDensity = (int) Math.round(fCurrentDensity * (1000.0 / cellLength));  

			// convert flow from vehicles/time step to vehicles/hour
			fMeanFlowRate = (int) Math.round(fCurrentFlow * 3600.0);  

			// initiate a new measurement
			fNrOfVehiclesCounted = 0;

			fCurrentDensity = 0.0;
			fCurrentFlow = 0.0;
			fCurrentSpaceMeanSpeed = 0.0;
			fNrOfSamples = 0;

			fMeasurementTimeLeft = fMeasurementInterval;
		}
		else {
			--fMeasurementTimeLeft;
		}
	}
}
