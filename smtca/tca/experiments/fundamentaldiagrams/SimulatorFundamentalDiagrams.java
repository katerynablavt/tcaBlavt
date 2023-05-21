
package tca.experiments.fundamentaldiagrams;

import smtools.miscellaneous.*;
import tca.automata.*;
import tca.base.*;

public class SimulatorFundamentalDiagrams
{
	public SimulatorFundamentalDiagrams()
	{
		int nrOfCells        = 1000;
		int nrOfDensitySteps = 100;
		int transientPeriod  = 1000;
		int simulationPeriod = 10000;

		double timeStepDuration             = 1.2;
		int maxSpeed                        = 5;
		double slowdownProbability          = 0.01;

		boolean distributeHomogeneously     = true;
		boolean saveGlobalDataIncrementally = true;
		boolean performLocalMeasurements    = false;
		boolean usePointDetector            = false;
		int measurementPeriod               = 60;
		int actualMeasurementPeriod         = (int) Math.round((double) measurementPeriod / timeStepDuration);
		int detectorLength                  = maxSpeed;

		boolean distlighthom = false;
		int timechatge = 3;
		int clicking = 1;
		int nrOfLights = 30;

		if (performLocalMeasurements) {
			simulationPeriod /= 10;
		}


		String filename =

			"fundamental-diagrams-";

			if (distributeHomogeneously) {
				filename += "homogeneous-";
			}
			else {
				filename += "compactjam-";
			}

			filename +=

			"sfitca" +
			"-c" + String.valueOf(nrOfCells) +
			"-s" + String.valueOf(nrOfDensitySteps) +
			"-tp" + String.valueOf(transientPeriod) +
			"-sp" + String.valueOf(simulationPeriod) +
			"-ts" + String.valueOf(timeStepDuration) +
			"-vmax" + String.valueOf(maxSpeed) +
			"-sp" + String.valueOf(slowdownProbability) +

			"";

		if (performLocalMeasurements) {
			filename += ("-local-mp" + String.valueOf(measurementPeriod));

			if (usePointDetector) {
				filename += "-point-measurements";
			}
		}
		filename += ("-nrOfLights"+nrOfLights);
		if (distlighthom) {
			filename += ("-homogeneousLight");

		}else{
			filename += ("-randomLight");
		}

			filename += ("-timeCh3"+timechatge);


		if (clicking==0) {
			filename += ("-allGreen");

		}else if (clicking==1){
			filename += ("-randomGreen");
		} else{
			filename += ("-groupGreen");
		}

		filename += ".data";

		double cellLength = 7.5;

		TextFileWriter detectorFile = null;
		try {
			detectorFile = new TextFileWriter(filename);
		}
		catch (Exception exc) {
		}

		Chrono chrono = new Chrono();

		double[][] globalFundamentalDiagrams = new double[nrOfDensitySteps][3];
//
		System.out.println("Start...");

			for (int densityStep = (nrOfDensitySteps - 1); densityStep > 0; --densityStep) {
//		for (int densityStep = 1; densityStep <= nrOfDensitySteps; ++densityStep) {
//		int densityStep = 300;
			int nrOfVehicles = (int) Math.round(((double) densityStep / (double) nrOfDensitySteps) * nrOfCells);
			double globalDensity = (double) nrOfVehicles / (double) nrOfCells;

			// create the TCA's initial state
			State initialState = new State(nrOfCells,nrOfLights);

			initialState.distributeVehicles(nrOfVehicles,maxSpeed,distributeHomogeneously,!distributeHomogeneously);
			initialState.distributeLights(nrOfLights,distlighthom,clicking);
			TrafficCellularAutomaton fTCA =
			new FukuiIshibashiTCA(initialState,maxSpeed,cellLength,14,nrOfCells / 14,60,1,true,slowdownProbability);

			TrafficCellularAutomaton.kLoopDetectorsEnabled = false;

			// allow transient period
			for (int timeStep = 0; timeStep < transientPeriod; ++timeStep) {
				// advance cellular automaton one step
				fTCA.advanceOneStep();

			}
			fTCA.fState.calcSpaceGaps();

			double globalSpaceMeanSpeed = 0.0;
			int measurementTimeLeft = actualMeasurementPeriod - 1;
			double localDensity = 0.0;
			double localFlow = 0.0;
			double localSpaceMeanSpeed = 0.0;
			int pointDetectorNrOfVehiclesCounted = 0;

			final int kEmpty = -1;
			int[] loopDetectorStripVehicleIDsCurrentT = new int[detectorLength];
			int[] loopDetectorStripSpeedsCurrentT = new int[detectorLength];
			int[] loopDetectorStripVehicleIDsPreviousT = new int[detectorLength];
			int[] loopDetectorStripSpeedsPreviousT = new int[detectorLength];
			for (int i = 0; i < detectorLength; ++i) {
				loopDetectorStripVehicleIDsCurrentT[i] = kEmpty;
				loopDetectorStripSpeedsCurrentT[i] = kEmpty;
				loopDetectorStripVehicleIDsPreviousT[i] = kEmpty;
				loopDetectorStripSpeedsPreviousT[i] = kEmpty;
			}

			for (int timeStep = 0; timeStep < simulationPeriod; ++timeStep) {
				// advance cellular automaton one step

				if( timeStep%1000==0){
					fTCA.fState.distributeLights(nrOfLights,distlighthom,clicking);
				}
				fTCA.fState.updateLight(timechatge);
				fTCA.advanceOneStep();

				if (performLocalMeasurements) {

					if (usePointDetector) {

						// collect information of all vehicles located upstream of the loop detector
						// the detector is located right after the strip of cells at the beginning of the lattice
						for (int cellNr = 0; cellNr < detectorLength; ++cellNr) {
							Cell cell = fTCA.fState.fCells[cellNr];

							if (cell != null) {
								loopDetectorStripVehicleIDsCurrentT[cellNr] = cell.fVehicleID;
								loopDetectorStripSpeedsCurrentT[cellNr] = cell.fSpeed;
							}
							else {
								loopDetectorStripVehicleIDsCurrentT[cellNr] = kEmpty;
								loopDetectorStripSpeedsCurrentT[cellNr] = kEmpty;
							}
						}

						// cross-reference this information with the first vehicle downstream of the loop detector
						boolean vehicleFound = false;
						int cellNr = detectorLength;
						while ((!vehicleFound) && (cellNr < (detectorLength + maxSpeed))) {

							Cell cell = fTCA.fState.fCells[cellNr];
							if (cell != null&&cell.fSpeed!=-1) {

								vehicleFound = true;

								// cross-reference this first vehicle downstream of the loop detector
								// with the information from the strip at the previous time step
								boolean vehicleMatch = false;
								int i = 0;
								while ((!vehicleMatch) && (i < detectorLength)) {

									if (cell.fVehicleID == loopDetectorStripVehicleIDsPreviousT[i]) {

										vehicleMatch = true;

										// only record moving vehicles
										if (loopDetectorStripSpeedsPreviousT[i] != 0) {
											localSpaceMeanSpeed += (1.0 / (double) loopDetectorStripSpeedsPreviousT[i]);
											++pointDetectorNrOfVehiclesCounted;
										}
									}
									else {
										++i;
									}
								}
							}
							else {
								++cellNr;
							}
						}

						// update loop detector with the current information
						for (int i = 0; i < detectorLength; ++i) {
							loopDetectorStripVehicleIDsPreviousT[i] = loopDetectorStripVehicleIDsCurrentT[i];
							loopDetectorStripSpeedsPreviousT[i] = loopDetectorStripSpeedsCurrentT[i];
						}

						if (measurementTimeLeft == 0) {

							if (pointDetectorNrOfVehiclesCounted != 0) {

								localSpaceMeanSpeed /= (double) pointDetectorNrOfVehiclesCounted;
								if (localSpaceMeanSpeed != 0.0) {
									localSpaceMeanSpeed = 1.0 / localSpaceMeanSpeed;
								}
								localFlow = (double) pointDetectorNrOfVehiclesCounted / actualMeasurementPeriod;

								if (localSpaceMeanSpeed != 0.0) {
									localDensity = localFlow / localSpaceMeanSpeed;
								}
								else {
									localDensity = 0.0;
								}

								try {
									detectorFile.writeDouble(localDensity);
									detectorFile.writeString("\t");

									detectorFile.writeDouble(localFlow);
									detectorFile.writeString("\t");

									detectorFile.writeDouble(localSpaceMeanSpeed);
									detectorFile.writeLn();
								}
								catch (Exception exc) {
								}
							}

							pointDetectorNrOfVehiclesCounted = 0;
							localDensity = 0.0;
							localFlow = 0.0;
							localSpaceMeanSpeed = 0.0;
							measurementTimeLeft = actualMeasurementPeriod - 1;
						}
						else {
							--measurementTimeLeft;
						}
					} // use point detector
					else {
						// locally calculate the number of vehicles and the local space-mean speed
						// using a detector of finite length situated at cell #0
						int nrOfVehiclesCounted = 0;
						double localSpaceMeanSpeedSum = 0.0;
						for (int cellNr = 0; cellNr < detectorLength; ++cellNr) {
							Cell cell = fTCA.fState.fCells[cellNr];

							if (cell != null && cell.fSpeed!=-1) {
								++nrOfVehiclesCounted;
								localSpaceMeanSpeedSum += cell.fSpeed;
							}
						}
						if (nrOfVehiclesCounted != 0) {
							localDensity += nrOfVehiclesCounted;
							localFlow += localSpaceMeanSpeedSum;
						}

						// check if measurement period has elapsed
						if (measurementTimeLeft == 0) {

							if (localDensity != 0.0) {

								localSpaceMeanSpeed = localFlow / localDensity;
								localDensity /= ((double) actualMeasurementPeriod * (double) detectorLength);
								localFlow /= ((double) actualMeasurementPeriod * (double) detectorLength);

								try {
									detectorFile.writeDouble(localDensity);
									detectorFile.writeString("\t");

									detectorFile.writeDouble(localFlow);
									detectorFile.writeString("\t");

									detectorFile.writeDouble(localSpaceMeanSpeed);
									detectorFile.writeLn();
								}
								catch (Exception exc) {
								}
							}

							localDensity = 0.0;
							localFlow = 0.0;
							localSpaceMeanSpeed = 0.0;
							measurementTimeLeft = actualMeasurementPeriod - 1;
						}
						else {
							--measurementTimeLeft;
						}
					} // use a detector of finite length
				} // perform local measurements
				else {
					// perform global measurements
					// globalDensity was calculated above
					// calculate global space-mean speed
					for (int cellNr = 0; cellNr < fTCA.fState.fCells.length; ++cellNr) {
						Cell cell = fTCA.fState.fCells[cellNr];

						if (cell.fSpeed != -1) {

							globalSpaceMeanSpeed += cell.fSpeed;
						}
					}
				} // perform global measurements
			}

			if (!performLocalMeasurements) {
				globalSpaceMeanSpeed /= (nrOfVehicles * simulationPeriod);
				double globalFlow = globalDensity * globalSpaceMeanSpeed;

				globalFundamentalDiagrams[densityStep - 1][0] = globalDensity;
				globalFundamentalDiagrams[densityStep - 1][1] = globalFlow;
				globalFundamentalDiagrams[densityStep - 1][2] = globalSpaceMeanSpeed;

				if (saveGlobalDataIncrementally) {
					// store fundamental diagram data to file
					try {
						detectorFile.writeDouble(globalDensity);
						detectorFile.writeString("\t");

						detectorFile.writeDouble(globalFlow);
						detectorFile.writeString("\t");

						detectorFile.writeDouble(globalSpaceMeanSpeed);
						detectorFile.writeLn();
					}
					catch (Exception exc) {
					}
				}
			}

			double percentCompleted = 100.0 - (((double) densityStep / (double) (nrOfDensitySteps - 2)) * 100.0);
//			double percentCompleted = ((double) densityStep / (double) (nrOfDensitySteps + 1.0)) * 100.0;

			if (percentCompleted >= 1.0) {

				// compute the time needed for the total computation
				double totalTimeNeeded = ((double) chrono.getElapsedTimeInMilliseconds() / percentCompleted) * 100.0;

				// compute the time left for completion
				int timeLeft = (int) Math.round(totalTimeNeeded - (percentCompleted * (totalTimeNeeded / 100.0)));
				String timeSpentString = "";
				String timeLeftString = "";
				try {
					timeSpentString = TimeFormatter.getTruncatedTimeString(new Time(chrono.getElapsedTimeInMilliseconds()));
					timeLeftString = TimeFormatter.getTruncatedTimeString(new Time(timeLeft));
				}
				catch (Exception exc) {
				}

				System.out.println(timeSpentString + " / " + timeLeftString + " [" + String.valueOf((int) Math.floor(percentCompleted)) + "%]");
			}
		}//отут!

		//if ((!performLocalMeasurements) && (!saveGlobalDataIncrementally)) {
			if ((!performLocalMeasurements) && (saveGlobalDataIncrementally)) {
			// store global fundamental diagram data to file
			try {
				for (int densityStep1 = 0; densityStep1 < nrOfDensitySteps; ++densityStep1) {
					detectorFile.writeDouble(globalFundamentalDiagrams[densityStep1][0]);
					detectorFile.writeString("\t");

					detectorFile.writeDouble(globalFundamentalDiagrams[densityStep1][1]);
					detectorFile.writeString("\t");

					detectorFile.writeDouble(globalFundamentalDiagrams[densityStep1][2]);
					detectorFile.writeLn();
				}
			}
			catch (Exception exc) {
			}
		}

		System.out.println("Done.");
	}

	public static void main(String[] args)
	{
		SimulatorFundamentalDiagrams simulator = new SimulatorFundamentalDiagrams();
	}
}
