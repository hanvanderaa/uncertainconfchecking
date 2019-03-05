package org.processmining.coarsegrainedchecking.evaluation;

import java.util.LinkedHashMap;
import java.util.Map;

public class SingleSettingsResults {

	String netName;
	double noiseLevel;
	String granularity;
	Map<String, Double> rmseMap;
	Map<String, Double> coverageMap;
	Map<String, Double> uncRatioMap;
	double blRMSE;
	
	public SingleSettingsResults(String netName, double noiseLevel, String granularity) {
		super();
		this.netName = netName;
		this.noiseLevel = noiseLevel;
		this.granularity = granularity;
		rmseMap = new LinkedHashMap<String, Double>();
		coverageMap = new LinkedHashMap<String, Double>();
		uncRatioMap = new LinkedHashMap<String, Double>();
	}

	public String getNetName() {
		return netName;
	}

	public double getNoiseLevel() {
		return noiseLevel;
	}

	public String getGranularity() {
		return granularity;
	}
	
	public void addProbModelRes(String probModel, double rmse, double coverage, double uncRatio) {
		rmseMap.put(probModel, rmse);
		coverageMap.put(probModel, coverage);
		uncRatioMap.put(probModel, uncRatio);
	}
	
	public void addBLScore(double rmse) {
		this.blRMSE = rmse;
	}
	
	public double getBLRMSE() {
		return blRMSE;
	}
	
	public double getRMSE(String probModel) {
		return rmseMap.get(probModel);
	}
	
	public double getCoverage(String probModel) {
		return coverageMap.get(probModel);
	}
	
	public double getUncRatio(String probModel) {
		return uncRatioMap.get(probModel);
	}
	
	public String getBestRMSEModel() {
		double bestScore = Double.MAX_VALUE;
		String bestModel = "";
		for (String probModel : rmseMap.keySet()) {
			if (rmseMap.get(probModel) < bestScore) {
				bestScore = rmseMap.get(probModel);
				bestModel = probModel;
			}
		}
		return bestModel;
	}
	
	public String getBestRMSEModel(double minCov, double maxUnc) {
		String lastModel = "";
		for (String probModel : rmseMap.keySet()) {
			double modelCov = coverageMap.get(probModel);
			double modelUnc = uncRatioMap.get(probModel);
			if (modelCov >= minCov && modelUnc <= maxUnc) {
				return probModel;
			}
			lastModel = probModel;
		}
		return lastModel;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((granularity == null) ? 0 : granularity.hashCode());
		result = prime * result + ((netName == null) ? 0 : netName.hashCode());
		long temp;
		temp = Double.doubleToLongBits(noiseLevel);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SingleSettingsResults other = (SingleSettingsResults) obj;
		if (granularity == null) {
			if (other.granularity != null)
				return false;
		} else if (!granularity.equals(other.granularity))
			return false;
		if (netName == null) {
			if (other.netName != null)
				return false;
		} else if (!netName.equals(other.netName))
			return false;
		if (Double.doubleToLongBits(noiseLevel) != Double.doubleToLongBits(other.noiseLevel))
			return false;
		return true;
	}

	
	
}
