
package tca.automata;

import tca.base.*;

public class FukuiIshibashiTCA extends StochasticTCA
{
	public FukuiIshibashiTCA(State initialState, int maxSpeed, double cellLength, int nrOfLoopDetectors, int detectorRange, int loopDetectorMeasurementInterval, int multipleOfSampleTimeToSimulate, boolean performGlobalMeasurements, double slowdownProbability)
	{
		super(initialState,maxSpeed,cellLength,nrOfLoopDetectors,detectorRange,loopDetectorMeasurementInterval,multipleOfSampleTimeToSimulate,performGlobalMeasurements,slowdownProbability);
	}

	protected void applyRulesToCell(Cell sourceCell, int cellNr, State nextState)
	{
		nextState.fCells[cellNr].fSpeed=-1;

		int maxSpeed = getMaxSpeed(cellNr);

		// rule 1: determine instantaneous acceleration and braking
		if (maxSpeed < sourceCell.fSpaceGap) {
			sourceCell.fSpeed = maxSpeed;
		}
		else {
			sourceCell.fSpeed = sourceCell.fSpaceGap;
		}

		// rule 2: randomize
		if ((sourceCell.fSpeed == maxSpeed) && (Math.random() < fSlowdownProbability)) {
			// note that we use random() < probability to capture the zero probability
			--sourceCell.fSpeed;
			if (sourceCell.fSpeed < 0) {
				sourceCell.fSpeed = 0;
			}			
		}

		// rule 3: advance vehicle
//		int targetCellNr = fState.successor(cellNr,sourceCell.fSpeed);
//		Cell targetCell = new Cell();
//		sourceCell.copyTo(targetCell);
//		nextState.fCells[targetCellNr] = targetCell;

		int targetCellNr = fState.successor(cellNr,sourceCell.fSpeed);
		nextState.fCells[targetCellNr].fSpeed = sourceCell.fSpeed;
		nextState.fCells[targetCellNr].fMaxSpeed = sourceCell.fMaxSpeed;
		nextState.fCells[targetCellNr].fSpeed = sourceCell.fSpeed;
		nextState.fCells[targetCellNr].fSpaceGap = sourceCell.fSpaceGap;
		nextState.fCells[targetCellNr].fVehicleID = sourceCell.fVehicleID;
		nextState.fCells[targetCellNr].fVehicleLength = sourceCell.fVehicleLength;

	}
}
