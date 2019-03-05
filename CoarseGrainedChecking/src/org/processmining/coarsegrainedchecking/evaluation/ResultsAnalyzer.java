package org.processmining.coarsegrainedchecking.evaluation;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;

public class ResultsAnalyzer {
	
	List<SingleSettingsResults> resultsList;

	static final String INPUT_FILE = "/Users/han/git/uncertain_conf/results analysis/200models.csv";
	static final String BL_MODEL_STRING = "NGramMaxProbabilisticModel (2)";
	static final String FTE_STRING = "FullTraceEquivalenceProbabilisticModel";
	static final String BIN_STRING = "BinaryRelationProbabilisticModel";
	static final String N2_STRING = "NGramProbabilisticModel (2)";
	static final String N3_STRING = "NGramProbabilisticModel (3)";
	
	static final double MIN_COV = 0.75;
	static final double MAX_UNC_RATIO = 0.20;
	
	public static void main(String[] args) throws Exception {
		ResultsAnalyzer r = new ResultsAnalyzer();
		r.run();
	}
	
	public void run() throws Exception {
		Double[] noiseLevels = new Double[]{0.0, 50.0, 100.0, null};
		for (Double noise : noiseLevels) {
			System.out.println("Noise level: " + noise);
			loadResults(noise);
			analyzeBestResults();

			double bestRMSE = 1000;
			double bestCov = 0.0;
			double bestUnc = 0.0;
			for (double cov = 1.0; cov > 0.0; cov = cov - 0.025) {
				for (double unc = 0.0; unc < 3.0; unc = unc + 0.025) {
					double score = scoreWithSelectionMeasures(cov, unc);
					if (score < bestRMSE) {
						bestRMSE = score;
						bestCov = cov;
						bestUnc = unc;
					}
				}
			}
			System.out.println("Best Selected model RMSE sum: " + bestRMSE );
			System.out.println("cov: " + bestCov + " unc: " + bestUnc);
		}
	}
	
	public void analyzeBestResults() {
		List<Double> scores = new ArrayList<Double>();
		double sum = 0.0;
		double bl = 0.0;
		double bin = 0.0;
		double fte = 0.0;
		double n2 = 0.0;
		double n3 = 0.0;
		for (SingleSettingsResults res : resultsList) {
			String bestModel = res.getBestRMSEModel();
			double score = res.getRMSE(bestModel);
			sum+= score;
			scores.add(score);
			
			bl += res.getBLRMSE();
			bin += res.getRMSE(BIN_STRING);
			fte += res.getRMSE(FTE_STRING);
			
			n2 += res.getRMSE(N2_STRING);
			n3 += res.getRMSE(N3_STRING);
		}
		System.out.println("Best RMSE sum: " + sum);
		System.out.println("BL RMSE sum: " + bl);
		System.out.println("n2 RMSE sum: " + n2);
//		System.out.println("FTE RMSE sum: " + fte);
//		System.out.println("bin RMSE sum: " + bin);
//		System.out.println("n3 RMSE sum: " + n3);
	}
	
	public double scoreWithSelectionMeasures(double minCov, double maxUnc) {
		List<Double> scores = new ArrayList<Double>();
		double sum = 0.0;
		for (SingleSettingsResults res : resultsList) {
			double score = res.getRMSE(res.getBestRMSEModel(minCov, maxUnc));
			sum+= score;
			scores.add(score);
		}
//		System.out.println("Selected Model RMSE sum: " + sum);
		return sum;
	}
	
	
	private void loadResults(Double noiseFilter) throws Exception {
		CSVReader reader = new CSVReader(new FileReader(INPUT_FILE), ';');
		String[] header = reader.readNext();
		
		resultsList = new ArrayList<SingleSettingsResults>();
		
		System.out.println(Arrays.toString(header));
		int netInd = 0;
		int modelInd = 2;
		int granInd = 3;
		int noiseInd = 4;
		int rmseInd = 26;
		int coverageInd = 33;
		int uncRatioInd = 34;
		
		String[] nextLine;
		SingleSettingsResults currentResults = null;
		
		while ((nextLine = reader.readNext()) != null && !nextLine[0].isEmpty()) {
			String netName = nextLine[netInd];
			String granularity = nextLine[granInd];
			String probModel = nextLine[modelInd];
			double noise = Double.parseDouble(nextLine[noiseInd]);
			double rmse = Double.parseDouble(nextLine[rmseInd]);
			double coverage = Double.parseDouble(nextLine[coverageInd]);
			double uncRatio = Double.parseDouble(nextLine[uncRatioInd]);
			
			SingleSettingsResults lineResults = new SingleSettingsResults(netName, noise, granularity);
			// start analysis of new config
			if (currentResults == null || !currentResults.equals(lineResults)) {
				currentResults = lineResults;
				if (noiseFilter == null || noiseFilter == noise) {
					resultsList.add(currentResults);
				}
			}
			if (probModel.equals(BL_MODEL_STRING)) {
				currentResults.addBLScore(rmse);
			} else {
				currentResults.addProbModelRes(probModel, rmse, coverage, uncRatio);
			}
		}
	}
	
}
