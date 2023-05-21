
package tca.base;

import tca.automata.*;
import java.util.*;

public class TrafficCellularAutomaton
{
	// loop detector activation switch
	public static boolean kLoopDetectorsEnabled = true;

	// the automaton's general properties
	public State   fInitialState;
	public State   fState;
	public int     fMaxSpeed;
	public double  fCellLength;
	public boolean fTrafficLightIsRed;
	public int     fInitialMaxSpeed;
	public Vector  fLoopDetectors;
	public int     fDetectorRange;
	public int     fLoopDetectorMeasurementInterval;
	public int     fMultipleOfSampleTimeToSimulate;
	public boolean fPerformGlobalMeasurements;
	public int     fMeasurementTimeLeft;
	public double  fMeanGlobalFlow;
	public double  fSpaceMeanSpeed;
	public double  fGlobalDensity;
	public double  fAverageGapSize;
	public double  fGapSizeVariance;

	// internal datastructures
	private double fSumOfGapSizes;
	private double fSumOfGapSizesSquared;



	public TrafficCellularAutomaton(State initialState, int maxSpeed, double cellLength, int nrOfLoopDetectors, int detectorRange, int loopDetectorMeasurementInterval, int multipleOfSampleTimeToSimulate, boolean performGlobalMeasurements)
	{
		fInitialState = initialState;
		setState(fInitialState);
		fMaxSpeed = maxSpeed;
		fInitialMaxSpeed = fMaxSpeed;
		fCellLength = cellLength;
		createLoopDetectors(nrOfLoopDetectors,detectorRange,loopDetectorMeasurementInterval);
		fDetectorRange = detectorRange;
		fPerformGlobalMeasurements = performGlobalMeasurements;
		fLoopDetectorMeasurementInterval = loopDetectorMeasurementInterval;
		fMultipleOfSampleTimeToSimulate = multipleOfSampleTimeToSimulate;
		reset();
	}

	public void reset()
	{
		setState(fInitialState);
		fTrafficLightIsRed = false;
		resetLoopDetectors();
		fMeasurementTimeLeft = 0;
		fMeanGlobalFlow = 0.0;
		fSpaceMeanSpeed = 0.0;
		fGlobalDensity = 0.0;
		fAverageGapSize = 0.0;
	  fGapSizeVariance = 0.0;
		fSumOfGapSizes = 0.0;
		fSumOfGapSizesSquared = 0.0;
	}

	public void advanceOneStep()
	{
		fState.calcSpaceGaps();

		State nextState = new State(fState);

		applyRules(nextState);

		++nextState.fTime;
		++nextState.timeforred;
		++nextState.timeforgreen;

		fState = nextState;

		if (kLoopDetectorsEnabled) {


			Enumeration loopDetectorEnumeration = fLoopDetectors.elements();
			while (loopDetectorEnumeration.hasMoreElements()) {

				((LoopDetector) loopDetectorEnumeration.nextElement()).update(fState,fCellLength);
			}

			if (fPerformGlobalMeasurements) {

				int nrOfVehicles = fState.fNrOfVehicles;

				// has the measurement-interval elapsed ?
				if (fMeasurementTimeLeft == 0) {

					fGlobalDensity = (double) nrOfVehicles / (double) fState.fCells.length;

					// convert to real-world units
					fGlobalDensity *= (1000.0 / fCellLength);

					fSpaceMeanSpeed /= ((double) (nrOfVehicles * fLoopDetectorMeasurementInterval * fMultipleOfSampleTimeToSimulate));
					fSpaceMeanSpeed *= (fCellLength * 3.6);

					fMeanGlobalFlow = fSpaceMeanSpeed * fGlobalDensity;

					if (nrOfVehicles > 0) {
						fAverageGapSize = (fSumOfGapSizes / (double) (nrOfVehicles * fLoopDetectorMeasurementInterval * fMultipleOfSampleTimeToSimulate));

						if (nrOfVehicles > 1) {
							fGapSizeVariance =
								(1 / ((double) (nrOfVehicles - 1))) *
								(fSumOfGapSizesSquared -
								 (2.0 * fSumOfGapSizes * fAverageGapSize) +
								 (nrOfVehicles * fAverageGapSize * fAverageGapSize));
						}
					}

					fMeasurementTimeLeft = fLoopDetectorMeasurementInterval * fMultipleOfSampleTimeToSimulate;
				}
				else {

					if (fMeasurementTimeLeft == (fLoopDetectorMeasurementInterval * fMultipleOfSampleTimeToSimulate)) {

						// initiate a new measurement
						fMeanGlobalFlow = 0.0;
						fSpaceMeanSpeed = 0.0;
						fGlobalDensity = 0.0;
						fAverageGapSize = 0.0;
				  	    fGapSizeVariance = 0.0;
						fSumOfGapSizes = 0.0;
						fSumOfGapSizesSquared = 0.0;
					}

					// calculate space mean speed
					fSumOfGapSizes = 0.0;
					fSumOfGapSizesSquared = 0.0;
					for (int cellNr = 0; cellNr < fState.fCells.length; ++cellNr) {
						Cell cell = fState.fCells[cellNr];

						if (cell.fSpeed!= -1) {

							fSpaceMeanSpeed += cell.fSpeed;
							fSumOfGapSizes += cell.fSpaceGap;
							fSumOfGapSizesSquared += (cell.fSpaceGap * cell.fSpaceGap);
						}
					}

					--fMeasurementTimeLeft;
				}
			}
		}
	}

	public boolean globalMeasurementsUpdated()
	{
		return (fMeasurementTimeLeft == (fLoopDetectorMeasurementInterval * fMultipleOfSampleTimeToSimulate));
	}

	public void setState(State state)
	{
		fState = new State(state);
	}

	protected void applyRules(State nextState)
	{
		// process all cells
		for (int cellNr = 0; cellNr < fState.fCells.length; ++cellNr) {

			Cell sourceCell = fState.fCells[cellNr];

			if (sourceCell.fSpeed != -1) {

				applyRulesToCell(sourceCell,cellNr,nextState);
			}
		}
	}

	protected void applyRulesToCell(Cell sourceCell, int cellNr, State nextState)
	{
	}

	public void resetLoopDetectors()
	{
		Enumeration loopDetectorEnumeration = fLoopDetectors.elements();
		while (loopDetectorEnumeration.hasMoreElements()) {
			((LoopDetector) loopDetectorEnumeration.nextElement()).reset();
		}
	}

	public void setLoopDetectorsMeasurementInterval(int measurementInterval)
	{
		Enumeration loopDetectorEnumeration = fLoopDetectors.elements();
		while (loopDetectorEnumeration.hasMoreElements()) {
			LoopDetector loopDetector = (LoopDetector) loopDetectorEnumeration.nextElement();

			loopDetector.reset();
			loopDetector.fMeasurementInterval = measurementInterval;
			fLoopDetectorMeasurementInterval = measurementInterval;
		}
	}

	private void createLoopDetectors(int nrOfLoopDetectors, int detectorRange, int loopDetectorMeasurementInterval)
	{
		// create (and place) loop detectors
		fLoopDetectors = new Vector();

		for (int detectorNr = 0; detectorNr < nrOfLoopDetectors; ++detectorNr) {

			int cellNr =
				((int) Math.floor(detectorNr * ((double) fState.fCells.length / nrOfLoopDetectors))) +
				(fState.fCells.length / (2 * nrOfLoopDetectors));

			LoopDetector loopDetector = new LoopDetector(cellNr,detectorRange,loopDetectorMeasurementInterval);
			fLoopDetectors.add(loopDetector);
		}
	}

	public void setGlobalMaxSpeed(int maxSpeed)
	{
		fState.setGlobalMaxSpeed(maxSpeed);
		fMaxSpeed = maxSpeed;
	}

	public int getMaxSpeed(int cellNr)
	{
		Cell cell = fState.fCells[cellNr];
		if (cell != null) {
			return cell.fMaxSpeed;
		}
		else {
			return 0;
		}
	}
}
