package org.NooLab.somfluid.components;

 

 

public class SomSimulator {

	public SomSimulator(){
		
	}
	
	
}

/*


	public String adaptDataAmountsbySurrogates(int offset) {

		String datafile;
		int i, n, k, record_count, scalefactori, extension, c, vc, s, si = 0;
		double obs_density, scalefactor, _v, _dev, _vt, _tv_value;
		SOM_data sd;
		String _tv_label, hs1;

		float[] values;
		String[] simRawDataRecord;

		DataRecord templateRecord, simulatedRecord;
		ArrayList<DataRecord> simulatedRecords = new ArrayList<DataRecord>();
		ArrayList<DataRecord> data = new ArrayList<DataRecord>();

		Vector<String[]> rawData;
		Vector<String[]> simulatedrawData = new Vector<String[]>();

		String storageRecord;
		Vector<String> storageRecords = new Vector<String>();

		sd = somconnector.getSomData();
		datafile = sd.sourcefile;

		Random rndgen = new Random();

		rawData = sd.getRawData();

		// 1. we need number of TV classes
		n = sd.getSetofTV().size();

		// 2. we need amount of records
		k = sd.getData().size();

		obs_density = k / n;

		if ((k < 160) || (obs_density < 30)) {
			_v = 160 / k;
			scalefactor = (30.0 / obs_density);

			scalefactor = Math.max(_v, scalefactor);
			// sd.enlargeDatabySimulation();

			scalefactori = (int) Math.round(scalefactor - 1);

			record_count = (int) Math.round(scalefactor * k);

			SimulatedRecordCount = record_count - k;
			data = sd.getData();

			templateRecord = data.get(1);
			vc = templateRecord.size;

			values = new float[vc];
			simRawDataRecord = new String[vc];

			si = k;
			if (scalefactor > 1.0) {
				for (i = 0; i < k; i++) {

					// for each of the records we add *(scalefactori -1) new
					// ones
					_tv_label = rawData.get(i)[sd.TVLabelcolumn];
					hs1 = rawData.get(i)[sd.TVcolumn];
					_v = Double.parseDouble(hs1);
					_tv_value = _v;

					extension = scalefactori;
					if ((scalefactori == 0) && (scalefactor > 0)) {
						// choose a probability whather to create 1 record or
						// not
					}

					templateRecord = data.get(i);
					vc = templateRecord.size;

					for (s = 0; s < extension; s++) {
						si = si + 1;
						// System.out.print("records simulated : "+si);
						// System.arraycopy(arg0, arg1, arg2, arg3, arg4)

						simulatedRecord = new DataRecord(si, values);

						for (c = offset; c < vc; c++) {

							_v = templateRecord.getValueatVectorPos(c);
							_vt = _v;

							if (_v != 0) {
								_dev = (1 - _v);
								if (_dev > 0.5) {
									_dev = _v;
								}
								_dev = _dev * rndgen.nextGaussian() / 15;
								_vt = _v + _dev;

								// only if already normalized !
								 
							} // _v!=0

							simulatedRecord.setValueatVectorPosRaw(c,
									(float) _vt);

						} // c-> all columns
						simulatedRecord.setValueatVectorPosRaw(sd.TVcolumn,
								(float) _tv_value);
						simulatedRecord.setValueatVectorPosRaw(0,
								(float) (si * 1.0f));

						// translate simulatedRecord = float[] into String[]
						for (c = 0; c < vc; c++) {

							if (c == sd.TVLabelcolumn) {
								simRawDataRecord[c] = _tv_label;
							} else {
								_v = simulatedRecord.getValueatVectorPos(c);
								if (c == 0) {
									simRawDataRecord[c] = String.format("%.1f",
											_v);
								} else {
									simRawDataRecord[c] = String.format("%.5f",
											_v);
								}
							}
						} // c-> translate all cols of record

						// add to extension of raw data
						simulatedrawData.add(simRawDataRecord.clone());

					} // s->amount added

					s = 0;
				} // i-> all existing data

			} // scalefactor>1 ?

			// col headers...

			rawData.add(0, sd.ColumnHeaders);

			for (s = 0; s < simulatedrawData.size(); s++) {

				rawData.add(simulatedrawData.get(s));
			}
			// save to a different filename

			hs1 = sd.sourcefile;
			hs1 = hs1.replace(".dat", "-sim.dat");

			sd.sourcefile = hs1;
			// set datafile to this file
			for (i = 0; i < rawData.size(); i++) {

				storageRecord = utils.arr2text(rawData.get(i));
				storageRecords.add(storageRecord);
			}

			sd.savetoFile(storageRecords, hs1, 0);
			datafile = hs1;
		} // size count considerations

		return datafile;
	}

	public void prepareSimulation(SOM_profiles _SPs) {
		int vc, pc;

		SPparent = this.mSom.SoP;

		// . . . . . . . . . . . . . . . . . . . . .

		if (SPparent != null) {
			rndgen = SPparent.rnd;
		} else {
			rndgen = new Random(randomseed);
		}

		SOMdata = new SOM_data();

		pc = _SPs.profileLabels.length;
		vc = _SPs.profileVariables.length;

		SOMdata.setParentSOMobject(this);
		SOMdata.vectorsize = vc;

		SOMdata.columnseparator = "\t";
		SOMdata.IDcolumn = 0;
	}

	public int simulateDatafromProfiles(SOM_profiles _SPs, String filename,
			int expectedSOMsize, double enlargeTableby) {
		// width=height of map

		// we take the profiles from the profiles object, (holds the table of
		// values, labels and variables
		// and create data

		int i, r, c, record_count, pc, vc, p,err, return_value = -1;
		double _f, _v, _dev, _vt;
		double[] _simrecord;
		String _columnheaders;

		String recstr;
		Vector<String> _table = new Vector<String>();

		try {
			err = 0;
			// DataStream datastream = new DataStream(0);
			SPparent = this.mSom.SoP;

			// . . . . . . . . . . . . . . . . . . . . .

			if (SPparent != null) {
				rndgen = SPparent.rnd;
			} else {
				rndgen = new Random(randomseed);
			}

			SOMdata = new SOM_data();

			pc = _SPs.profileLabels.length;
			vc = _SPs.profileVariables.length;

			SOMdata.setParentSOMobject(this);
			SOMdata.vectorsize = vc;

			_columnheaders = "ID";

			for (c = 0; c < vc; c++) {
				_columnheaders = _columnheaders + "\t"
						+ _SPs.profileVariables[c];
			}
			_columnheaders = _columnheaders + "\t" + "TV" + "\t" + "TV_Label";
			_table.add(_columnheaders);

			SOMdata.vectorsize = SOMdata.vectorsize + 3;

			_f = 1.0;
			if (expectedSOMsize >= 8) {
				_f = Math.sqrt(expectedSOMsize - 7)
						* Math.log10(expectedSOMsize * expectedSOMsize);
				_f = (12.0 * (expectedSOMsize * expectedSOMsize)) / (1);
			}

			record_count = pc * pc * vc * vc * 10;
			record_count = (int) Math.round(record_count + _f);

			if (enlargeTableby > 0) {
				record_count = (int) Math.round(record_count * enlargeTableby);
				if (record_count < 20) {
					record_count = 20;
				}
			}

			SimulatedRecordCount = record_count;

			_simrecord = new double[vc];

			for (r = 0; r < record_count; r++) {

				recstr = "";

				if (r < pc) {
					p = r;
				} else {
					p = rndgen.nextInt(pc);
				}

				for (c = 0; c < vc; c++) {

					_v = _SPs.profilesTable[p][c];

					_dev = (1 - _v);
					if (_dev > 0.5) {
						_dev = _v;
					}
					_dev = _dev * rndgen.nextGaussian() / 4;
					_vt = _v + _dev;

					if (_vt < 0) {
						_vt = 0;
					}
					;
					if (_vt > 1) {
						_vt = 1;
					}
					;

					_simrecord[c] = _vt;

				} // c-> all columns

				if (utils == null) {
					utils = new SomUtilities();
				}
				recstr = utils.arr2text(_simrecord, 4);
				recstr = recstr.replace("  ", " ");
				recstr = recstr.replace(" ", "\t");

				// adding the profile identifier
				recstr = r + "\t" + recstr + "\t" + p + "\t"
						+ _SPs.profileLabels[p];

				_table.add(recstr);

			} // r-> all simulated records

			if (filename.length() > 0) {
				filename = "/" + filename;
				filename = filename.replace("//", "/");

				// save the table

				// datastream = new DataStream(0);
				filename = utils.prepareFilepath(filename,1); // 1 == use system temp dir
				simulatedFile = "";

				SOMdata.columnseparator = "\t";
				SOMdata.IDcolumn = 0;
				SOMdata.sourcefile = "";
				SOMdata.TVcolumn = _simrecord.length + 1;
				SOMdata.setTargetVariableIndex(SOMdata.TVcolumn);
				SOMdata.TVLabelcolumn = _simrecord.length + 2;

				String tmp = SOMdata
						.getDataValueasStr(2, SOMdata.TVLabelcolumn);

				r = SOMdata.savetoFile(_table, filename, 0);
				if (r == 0) {
					simulatedFile = filename;
					SOMdata.sourcefile = filename;
				}
				;
				return_value = 0;
			}

		} catch (Exception e) {
			System.out.println("filenme "+filename);
			e.printStackTrace();
		}

		return return_value;
	}

	public int getClassLabelbymajorValue(String majorityvalue) {
		int rVal = -1;
		String tmp, vStr;
		double _v0, _v1;

		for (int i = 0; i < SOMclasses.size(); i++) {

			tmp = SOMclasses.get(i).classLabel;
			vStr = SOMclasses.get(i).classtableValue;

			_v0 = 0.0;
			_v1 = 0.01;
			if (_v0 == _v1) {

			} // if _v0 ?

		} // i->

		return rVal;
	}

*/