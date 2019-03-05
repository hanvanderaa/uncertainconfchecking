package org.processmining.coarsegrainedchecking.plugins;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.processmining.coarsegrainedchecking.utils.TimestampGranularity;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet.TimeUnit;

public class CoarseGrainedConformanceCheckingParameters  {
	
	public int MODEL_RANGE_START = 0;
	public int MODEL_RANGE_END = 400;
	
	
	public boolean GENERATE_NEW_LOGS = true;
	public boolean AGGREGATE_ON_LOG_LEVEL = true;
	public boolean NORMALIZE_PROBABILITIES = true;
	
	public double SCALABILITY_TRHESHOLD = 0.5;
	public boolean SCALABILITY_OPTIMZATION = false;
	public boolean SCALABILITY_OPTIMZATION_MAJORITY = false;
	public boolean SCALABILITY_TOP_K = false;
	public int TOP_K = 3;
	
	
	
	public int LOG_SIZE = 1000;
	public  TimeUnit SIMULATION_TIME_UNIT = TimeUnit.MINUTES;
	public  int MAX_TRACE_LENGTH = 500;
	public  int ARRIVAL_RATE = 1;
	public  int THROUGHPUT_RATE = 5;
	public  int SIMULATION_SEED = 1;
	
	public  int[] NOISE_LEVELS = new int[]{0, 25, 50, 75, 100};
	public  TimestampGranularity[] GRANULARITY_LEVELS = new TimestampGranularity[]{TimestampGranularity.MINUTES};
	
	
	public  String BASE_FOLDER = "input/";
	public  String MODEL_FOLDER = BASE_FOLDER + "filtered_models/";
//	public String MODEL_FOLDER = BASE_FOLDER + "bpi2014/";
	public String LOG_FOLDER = BASE_FOLDER + "logs/";
	
	
	public  String OUTPUT_FILE = "Output"; 
	public  String OUTPUT_FOLDER = "output/";
	
	
	public  NumberFormat formatter = new DecimalFormat("#0.00");
	
	
	public CoarseGrainedConformanceCheckingParameters() {
	}


	public boolean isGENERATE_NEW_LOGS() {
		return GENERATE_NEW_LOGS;
	}


	public void setGENERATE_NEW_LOGS(boolean gENERATE_NEW_LOGS) {
		GENERATE_NEW_LOGS = gENERATE_NEW_LOGS;
	}


	public boolean isAGGREGATE_ON_LOG_LEVEL() {
		return AGGREGATE_ON_LOG_LEVEL;
	}


	public void setAGGREGATE_ON_LOG_LEVEL(boolean aGGREGATE_ON_LOG_LEVEL) {
		AGGREGATE_ON_LOG_LEVEL = aGGREGATE_ON_LOG_LEVEL;
	}


	public boolean isNORMALIZE_PROBABILITIES() {
		return NORMALIZE_PROBABILITIES;
	}


	public void setNORMALIZE_PROBABILITIES(boolean nORMALIZE_PROBABILITIES) {
		NORMALIZE_PROBABILITIES = nORMALIZE_PROBABILITIES;
	}


	public int getLOG_SIZE() {
		return LOG_SIZE;
	}


	public void setLOG_SIZE(int lOG_SIZE) {
		LOG_SIZE = lOG_SIZE;
	}


	public TimeUnit getSIMULATION_TIME_UNIT() {
		return SIMULATION_TIME_UNIT;
	}


	public void setSIMULATION_TIME_UNIT(TimeUnit sIMULATION_TIME_UNIT) {
		SIMULATION_TIME_UNIT = sIMULATION_TIME_UNIT;
	}


	public int getMAX_TRACE_LENGTH() {
		return MAX_TRACE_LENGTH;
	}


	public void setMAX_TRACE_LENGTH(int mAX_TRACE_LENGTH) {
		MAX_TRACE_LENGTH = mAX_TRACE_LENGTH;
	}


	public int getARRIVAL_RATE() {
		return ARRIVAL_RATE;
	}


	public void setARRIVAL_RATE(int aRRIVAL_RATE) {
		ARRIVAL_RATE = aRRIVAL_RATE;
	}


	public int getTHROUGHPUT_RATE() {
		return THROUGHPUT_RATE;
	}


	public void setTHROUGHPUT_RATE(int tHROUGHPUT_RATE) {
		THROUGHPUT_RATE = tHROUGHPUT_RATE;
	}


	public int getSIMULATION_SEED() {
		return SIMULATION_SEED;
	}


	public void setSIMULATION_SEED(int sIMULATION_SEED) {
		SIMULATION_SEED = sIMULATION_SEED;
	}


	public int[] getNOISE_LEVELS() {
		return NOISE_LEVELS;
	}


	public void setNOISE_LEVELS(int[] nOISE_LEVELS) {
		NOISE_LEVELS = nOISE_LEVELS;
	}


	public TimestampGranularity[] getGRANULARITY_LEVELS() {
		return GRANULARITY_LEVELS;
	}


	public void setGRANULARITY_LEVELS(TimestampGranularity[] gRANULARITY_LEVELS) {
		GRANULARITY_LEVELS = gRANULARITY_LEVELS;
	}


	public String getBASE_FOLDER() {
		return BASE_FOLDER;
	}


	public void setBASE_FOLDER(String bASE_FOLDER) {
		BASE_FOLDER = bASE_FOLDER;
	}


	public String getMODEL_FOLDER() {
		return MODEL_FOLDER;
	}


	public void setMODEL_FOLDER(String mODEL_FOLDER) {
		MODEL_FOLDER = mODEL_FOLDER;
	}


	public String getLOG_FOLDER() {
		return LOG_FOLDER;
	}


	public void setLOG_FOLDER(String lOG_FOLDER) {
		LOG_FOLDER = lOG_FOLDER;
	}


	public String getOUTPUT_FILE() {
		return OUTPUT_FILE;
	}


	public void setOUTPUT_FILE(String oUTPUT_FILE) {
		OUTPUT_FILE = oUTPUT_FILE;
	}


	public String getOUTPUT_FOLDER() {
		return OUTPUT_FOLDER;
	}


	public void setOUTPUT_FOLDER(String oUTPUT_FOLDER) {
		OUTPUT_FOLDER = oUTPUT_FOLDER;
	}


	public NumberFormat getFormatter() {
		return formatter;
	}


	public void setFormatter(NumberFormat formatter) {
		this.formatter = formatter;
	}
	
	

}
