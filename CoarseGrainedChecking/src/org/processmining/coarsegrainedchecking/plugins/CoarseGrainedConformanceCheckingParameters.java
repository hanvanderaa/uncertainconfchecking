package org.processmining.coarsegrainedchecking.plugins;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.processmining.coarsegrainedchecking.approximation.AbstractTraceResultComputer;
import org.processmining.coarsegrainedchecking.approximation.StatisticalTraceResultApproximator;
import org.processmining.coarsegrainedchecking.approximation.TraceResultComputerNoApproximation;
import org.processmining.coarsegrainedchecking.evaluation.SingleTraceResult.ConformanceMode;
import org.processmining.coarsegrainedchecking.probabilisticmodels.AbstractProbabilisticModel;
import org.processmining.coarsegrainedchecking.probabilisticmodels.BinaryRelationProbabilisticModel;
import org.processmining.coarsegrainedchecking.probabilisticmodels.FullTraceEquivalenceProbabilisticModel;
import org.processmining.coarsegrainedchecking.probabilisticmodels.NGramProbabilisticModel;
import org.processmining.coarsegrainedchecking.probabilisticmodels.UniformProbabilisticModel;
import org.processmining.coarsegrainedchecking.utils.TimestampGranularity;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet.TimeUnit;

public class CoarseGrainedConformanceCheckingParameters  {
	
	public int MODEL_RANGE_START = 0;
	public int MODEL_RANGE_END = 500;
	

	public int MAX_RESOLUTIONS_PER_TRACE = 100000;
	public int MAX_ASTAR_STATES = Integer.MAX_VALUE;
	public int ASTAR_THREADS = 1;
	public int MAX_TRACES_TO_CHECK = 100000;
	
	public int TRACE_PROGRESS_DEBUG = 5;
	
	public boolean OLD_CONF_CHECKER = true;
	
	public boolean GENERATE_NEW_LOGS = true;
	
	public int LOG_SIZE = 1000;
	public  int MAX_TRACE_LENGTH = 500;
	
	
	public  int[] NOISE_LEVELS = new int[]{0};
	public  TimestampGranularity[] GRANULARITY_LEVELS = new TimestampGranularity[]{TimestampGranularity.MINUTES};
	
	
	public  String BASE_FOLDER = "input/";
	public  String DATA_FOLDER = "generated/";
	public  String DATA_PATH = BASE_FOLDER + DATA_FOLDER;
		
	public  String OUTPUT_FILE = "Output"; 
	public  String OUTPUT_FOLDER = "output/";
	
	public  TimeUnit SIMULATION_TIME_UNIT = TimeUnit.SECONDS;
	public  int ARRIVAL_RATE = 100;
	public  int THROUGHPUT_RATE = 350;
	public  int SIMULATION_SEED = 1;
	
		public AbstractProbabilisticModel[] probModels = new AbstractProbabilisticModel[]{
			new FullTraceEquivalenceProbabilisticModel(),
			new NGramProbabilisticModel(4),
			new NGramProbabilisticModel(3),
			new NGramProbabilisticModel(2),
			new BinaryRelationProbabilisticModel(),
			new UniformProbabilisticModel()
	};
	
	public AbstractTraceResultComputer[] resultComputers = new AbstractTraceResultComputer[]{
			new TraceResultComputerNoApproximation(),
			new StatisticalTraceResultApproximator(ConformanceMode.FITNESS, 0.01, 0.10),
			new StatisticalTraceResultApproximator(ConformanceMode.FITNESS, 0.05, 0.10)
			}; 
	
	
	public  NumberFormat formatter = new DecimalFormat("#0.00");
	
	
	public CoarseGrainedConformanceCheckingParameters() {
	}


	public boolean isGENERATE_NEW_LOGS() {
		return GENERATE_NEW_LOGS;
	}


	public void setGENERATE_NEW_LOGS(boolean gENERATE_NEW_LOGS) {
		GENERATE_NEW_LOGS = gENERATE_NEW_LOGS;
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
		return DATA_FOLDER;
	}


	public void setMODEL_FOLDER(String mODEL_FOLDER) {
		DATA_FOLDER = mODEL_FOLDER;
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


	public void setMODEL_RANGE_START(int mODEL_RANGE_START) {
		MODEL_RANGE_START = mODEL_RANGE_START;
	}


	public void setMODEL_RANGE_END(int mODEL_RANGE_END) {
		MODEL_RANGE_END = mODEL_RANGE_END;
	}


	public void setMAX_RESOLUTIONS_PER_TRACE(int mAX_RESOLUTIONS_PER_TRACE) {
		MAX_RESOLUTIONS_PER_TRACE = mAX_RESOLUTIONS_PER_TRACE;
	}


	public void setMAX_TRACES_TO_CHECK(int mAX_TRACES_TO_CHECK) {
		MAX_TRACES_TO_CHECK = mAX_TRACES_TO_CHECK;
	}


	public void setTRACE_PROGRESS_DEBUG(int tRACE_PROGRESS_DEBUG) {
		TRACE_PROGRESS_DEBUG = tRACE_PROGRESS_DEBUG;
	}


	public void setDATA_FOLDER(String dATA_FOLDER) {
		DATA_FOLDER = dATA_FOLDER;
	}
	
	

}
