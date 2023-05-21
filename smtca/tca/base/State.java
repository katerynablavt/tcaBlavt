
package tca.base;

import java.util.*;

public class State
{
	// the state's datastructures
	public Cell[] fCells;
	public int    fTime;
	public int    fNrOfVehicles;
	public int    fNrOfLights;
	public int [] positionsOfLights;

	public int timeforredval =7;
	public int timeforgreenval=timeforredval ;

	public State(int nrOfCells, int nrOfLights)
	{
		fCells = new Cell[nrOfCells];
		positionsOfLights=new int[nrOfLights];
		fTime = 0;
		fNrOfVehicles = 0;
		fNrOfLights=nrOfLights;

	}

	public State(State state)
	{
		this(state.fCells.length, state.fNrOfLights);
		state.copyTo(this);
	}

	public void clear()
	{
		int nrOfCells = fCells.length;
		fCells = new Cell[nrOfCells];
		positionsOfLights=new int[fNrOfLights];
		fTime = 0;
		fNrOfVehicles = 0;
	}

	public void distributeLights(int nrOfLights, boolean distributeHomogeneously, int clicking) {
		// clear lattice
		for (int cellNr = 0; cellNr < fCells.length; cellNr++) {
			fCells[cellNr].fLightID = -1;
			fCells[cellNr].color=-1;
		}

		// create a set containing unique lights IDs
		Vector lightIDs = new Vector(nrOfLights);
		for (int lightID = 0; lightID < nrOfLights; ++lightID) {
			lightIDs.add(lightID);
		}

		if (distributeHomogeneously) {

			// homogeneously distribute some lights over the lattice
			int lightPosition = 0;
			positionsOfLights=new int[nrOfLights];
			for (int lightNr = 0; lightNr < nrOfLights; lightNr++) {
				fCells[lightPosition].fLightID = lightNr;

				// fill cells

					if (clicking==0) { //початку всі зелені
						fCells[lightPosition].color = 1;
					}
					else if (clicking==1){ //рандомно
						if (Math.random()<0.5) {
							fCells[lightPosition].color = 1;
						}else{
							fCells[lightPosition].color = 0;
						}
					}else{ //по черзі по групках по 3 і 2
						int inset = 5;
						int g = 3;
						int r= inset-g;
						if (lightNr%5==1 ||lightNr%5==2||lightNr%5==3){
							fCells[lightPosition].color = 1;
						} else{
							fCells[lightPosition].color = 0;
						}

					}



				positionsOfLights[lightNr]=lightPosition;
//				lightIDs.remove(lightNr);
//todo how change
				lightPosition += (1 + fCells.length/nrOfLights);

			}

		}
		else {
			// random distribution

			int gapforlights;

			// todo gapregulation

			// generate map of positions
			int[] positions = new int[nrOfLights];

			for (int lightNr = 0; lightNr < nrOfLights; ++lightNr) {

				// find a suitable position for the vehicle
				int position = 0;

				boolean positionOk = false;
				while (!positionOk) {
					position = (int) (Math.random() * fCells.length);

					boolean positionEncountered = false;
					for (int i = 0; i < lightNr; ++i) {
						if (positions[i] == position) {
							positionEncountered = true;
						}
					}

					positionOk = (positionEncountered == false);
				}

				positions[lightNr] = position;
			}

			// fill cells
			for (int i = 0; i < nrOfLights; ++i) {
				fCells[positions[i]].fLightID = i;
				if (clicking==0) { //початку всі зелені
					fCells[positions[i]].color = 1;
				}
				else if (clicking==1){ //рандомно
					if (Math.random()<0.5) {
						fCells[positions[i]].color = 1;
					}else{
						fCells[positions[i]].color = 0;
					}
				}else{ //по черзі по групках по 3 і 2
					int inset = 5;
					int g = 3;
					int r= inset-g;
					if (i%5==1 ||i%5==2||i%5==3){
						fCells[positions[i]].color = 1;
					} else{
						fCells[positions[i]].color = 0;
					}

				}

			}
			positionsOfLights=positions;
		}

		fNrOfLights = nrOfLights;



		}

	public void distributeVehicles(int nrOfVehicles, int maxSpeed, boolean distributeHomogeneously, boolean startWithCompactJam)
	{
		// clear lattice
		for (int cellNr = 0; cellNr < fCells.length; ++cellNr) {
			fCells[cellNr] = new Cell();
		}

		// create a set containing unique vehicle IDs
		Vector vehicleIDs = new Vector(nrOfVehicles);
		for (int vehicleID = 0; vehicleID < nrOfVehicles; ++vehicleID) {
			vehicleIDs.add(new Integer(vehicleID));
		}

		 if (distributeHomogeneously) {

			// how many vehicles can be distributed homogeneously ?
			int nrOfHomogeneouslyPlacedVehicles = (int) Math.floor((double) fCells.length / (double) (1 + maxSpeed));

			if (nrOfVehicles < nrOfHomogeneouslyPlacedVehicles) {
				nrOfHomogeneouslyPlacedVehicles = nrOfVehicles;
			}

			// homogeneously distribute some vehicles over the lattice
			int vehiclePosition = 0;
			for (int vehicleNr = 0; vehicleNr < nrOfHomogeneouslyPlacedVehicles; ++vehicleNr) {

				Cell cell = new Cell();
				cell.fMaxSpeed = maxSpeed;
				cell.fSpeed = maxSpeed;

				int randomVehicleID = (int) Math.floor((Math.random() * vehicleIDs.size()));
				cell.fVehicleID = ((Integer) vehicleIDs.get(randomVehicleID)).intValue();
				vehicleIDs.remove(randomVehicleID);

				fCells[vehiclePosition] = cell;
//				System.out.println("v is in 1    " +vehicleNr);
				vehiclePosition += (1 + maxSpeed);
			}

			// distribute excess of vehicles uniformly among the free positions
			int vehiclesLeftToDistribute = nrOfVehicles - nrOfHomogeneouslyPlacedVehicles;

			if (vehiclesLeftToDistribute > 0) {

				// fill cells
				for (int i = 0; i < vehiclesLeftToDistribute; ++i) {

					Cell cell = new Cell();
					cell.fMaxSpeed = maxSpeed;
					cell.fSpeed = maxSpeed;

					int randomVehicleID = (int) Math.floor((Math.random() * vehicleIDs.size()));
					cell.fVehicleID = ((Integer) vehicleIDs.get(randomVehicleID)).intValue();
					vehicleIDs.remove(randomVehicleID);

					// find a suitable free position
					boolean positionOk = false;
					while (!positionOk) {				
						vehiclePosition = (int) (Math.random() * fCells.length);

						if (fCells[vehiclePosition].fSpeed==-1) {
							positionOk = true;

						}
					}
				
					fCells[vehiclePosition] = cell;
//					System.out.println("v is in 2     " +i);
				}
			}
		}
		else {
			// random distribution

			// generate map of positions
			int[] positions = new int[nrOfVehicles];

			for (int vehicleNr = 0; vehicleNr < nrOfVehicles; ++vehicleNr) {

				// find a suitable position for the vehicle
				int position = 0;

				boolean positionOk = false;
				while (!positionOk) {
					position = (int) (Math.random() * fCells.length);

					boolean positionEncountered = false;
					for (int i = 0; i < vehicleNr; ++i) {
						if (positions[i] == position) {
							positionEncountered = true;
						}
					}

					positionOk = (positionEncountered == false);
				}

				positions[vehicleNr] = position;
			}

			// fill cells
			for (int i = 0; i < nrOfVehicles; ++i) {
				Cell cell = new Cell();
				cell.fMaxSpeed = maxSpeed;
				cell.fSpeed = maxSpeed;
				cell.fVehicleID = i;
				fCells[positions[i]] = cell;
//				System.out.println("v is in 3    " +i);
			}
		}

		fNrOfVehicles = nrOfVehicles;



//		distributeLigths(fNrOfLights,distributeHomogeneously, true,1);
	}

	public void setGlobalMaxSpeed(int maxSpeed)
	{
		for (int cellNr = 0; cellNr < fCells.length; ++cellNr) {
			Cell cell = fCells[cellNr];
			if (cell != null) {
				cell.fMaxSpeed = maxSpeed;
			}
		}
	}

	public void calcSpaceGaps()
	{
		try {
			// find position of first vehicle
			int firstVehiclePosition = 0;

			boolean firstVehicleFound = false;
			while ((!firstVehicleFound) && (firstVehiclePosition < fCells.length)) {

				Cell cell = fCells[firstVehiclePosition];
				if (cell.fSpeed != -1) {
					firstVehicleFound = true;
				}
				else {
					++firstVehiclePosition;
				}
			}


			if (!firstVehicleFound) {
				// no vehicles present in cellular automaton
				return;
			}


			// is there exactly one vehicle present at the end of the cellular automaton's lattice ?
			if ((firstVehiclePosition == (fCells.length - 1)) || (fNrOfVehicles == 1)) {
				Cell cell = fCells[firstVehiclePosition];
				cell.fSpaceGap = fCells.length - 1;
				return;
			}


			// calc space gaps for all vehicles
			int lastKnownVehiclePosition = firstVehiclePosition;
			int currentCell = lastKnownVehiclePosition + 1;



			// scan all subsequent cells in the lattice
			while (currentCell < fCells.length) {

				// find next vehicle
				Cell cell = fCells[currentCell];
				if (cell.fSpeed != -1 || cell.color==0) {

					// calc gap with previous vehicle


						fCells[lastKnownVehiclePosition].fSpaceGap = currentCell - lastKnownVehiclePosition - 1;
						lastKnownVehiclePosition = currentCell;

				}

				++currentCell;
			}

			// calc space gap for last vehicle
			//todo як додати світлофор??
			fCells[lastKnownVehiclePosition].fSpaceGap = (fCells.length - lastKnownVehiclePosition - 1) + firstVehiclePosition;
		}
		catch (NullPointerException exc) {
			// just in case ...
		}
	}



	public void copyTo(State state)
	{
		for (int cellNr = 0; cellNr < fCells.length; ++cellNr) {

			Cell cell = fCells[cellNr];

			if (cell != null) {
				state.fCells[cellNr] = new Cell();
				cell.copyTo(state.fCells[cellNr]);
			}
		}

		state.fTime = fTime;
//		state.timeforgreen=timeforgreen;
//		state.timeforred=timeforred;
		state.fNrOfVehicles = fNrOfVehicles;
		state.fNrOfLights=fNrOfLights;
		state.positionsOfLights=positionsOfLights;
	}

	public int predecessor(int cellNr, int cellsToSkip)
	{
		int pred = cellNr - cellsToSkip;
		if (pred < 0) {
			pred += fCells.length;
		}

		return pred;
	}

	public int successor(int cellNr, int cellsToSkip)
	{
		return ((cellNr + cellsToSkip) % fCells.length);
	}

	public void updateLight(int timechangingequal) {

		for (Cell cell:fCells
			 ) {
			if(cell.tg==timechangingequal*timeforgreenval){
				cell.tg=0;
				cell.color=0;

			}
			if(cell.tr==timeforredval){
				cell.tr=0;
				cell.color=1;

			}
		}
	}

	public void updatetimers() {
		for (Cell cell:
			 fCells) {
			if(cell.color==1){
				cell.tg++;
			}else{
				cell.tr++;
			}
		}
	}
}
