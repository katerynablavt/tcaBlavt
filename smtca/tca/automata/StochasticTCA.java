
package tca.automata;

import tca.base.*;

public class StochasticTCA extends TrafficCellularAutomaton
{
	// the probability of a vehicle slowing down
	public double fSlowdownProbability;

	public StochasticTCA(State initialState, int maxSpeed, double cellLength, int nrOfLoopDetectors, int detectorRange, int loopDetectorMeasurementInterval, int multipleOfSampleTimeToSimulate, boolean performGlobalMeasurements, double slowdownProbability)
	{
		super(initialState,maxSpeed,cellLength,nrOfLoopDetectors,detectorRange,loopDetectorMeasurementInterval,multipleOfSampleTimeToSimulate,performGlobalMeasurements);
		fSlowdownProbability = slowdownProbability;
	}

	protected void applyRulesToCell(Cell sourceCell, int cellNr, State nextState)
	{
		nextState.fCells[cellNr] = null;

		// rule 1: determine braking and acceleration
		if (sourceCell.fSpeed > sourceCell.fSpaceGap) {
			sourceCell.fSpeed = sourceCell.fSpaceGap;
		}
		else if ((sourceCell.fSpeed < sourceCell.fSpaceGap) && (sourceCell.fSpeed < getMaxSpeed(cellNr))) {
			++sourceCell.fSpeed;
		}

		// rule 2: randomize
		if ((sourceCell.fSpeed > 0) && (Math.random() < fSlowdownProbability)) {
			// note that we use random() < probability to capture the zero probability
			--sourceCell.fSpeed;
		}
		
		// try to stop at traffic light if necessary
		if ((cellNr == 0) && (fTrafficLightIsRed)) {
			sourceCell.fSpeed = 0;
		}

		// rule 3: advance vehicle
		int targetCellNr = fState.successor(cellNr,sourceCell.fSpeed);
		Cell targetCell = new Cell();
		sourceCell.copyTo(targetCell);
		nextState.fCells[targetCellNr] = targetCell;
	}
}
