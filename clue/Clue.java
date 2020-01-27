package clue;

import java.util.Hashtable;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import bunch.api.*;

/* Command line user executable for bunch */

/* To compile on unix:
   CLASSPATH=bunch.jar
   export CLASSPATH
   javac clue/Clue.java
 */

public class Clue {

	private final static int MDG_INPUT_FILE_NAME		= 0;
	private final static int OUTPUT_DIRECTORY			= 1;
	private final static int OUTPUT_FILE                = 2;
	private final static int CLUSTERING_APPROACH		= 3;
	private final static int MDG_OUTPUT_MODE			= 4;
	private final static int OUTPUT_FORMAT				= 5;
	private final static int MQ_CALCULATOR_CLASS		= 6;
	private final static int ECHO_RESULTS_TO_CONSOLE	= 7;
	private final static int CLUSTERING_ALG				= 8;

	private final static int ALG_HC_POPULATION_SZ		= 9;
	private final static int ALG_HC_HC_PCT				= 10;
	private final static int ALG_HC_RND_PCT				= 11;
	private final static int ALG_HC_SA_CLASS			= 12;
	private final static int ALG_HC_SA_CONFIG			= 13;

	private final static int ALG_GA_NUM_GENERATIONS		= 14;
	private final static int ALG_GA_SELECTION_METHOD	= 15;
	private final static int ALG_GA_POPULATION_SZ		= 16;
	private final static int ALG_GA_CROSSOVER_PROB		= 17;
	private final static int ALG_GA_MUTATION_PROB		= 18;

	private final static int LIBRARY_LIST				= 19;
	private final static int OMNIPRESENT_CLIENTS		= 20;
	private final static int OMNIPRESENT_SUPPLIERS		= 21;
	private final static int OMNIPRESENT_BOTH			= 22;

	private final static int TIMEOUT_TIME				= 23;
	private final static int OUTPUT_TREE                = 24;
	private final static int USER_DIRECTED_CLUSTER_SIL  = 25;
	private final static int LOCK_USER_SET_CLUSTERS     = 26;
	private final static int DEBUG                      = 27;


	private final static String[] g_keywords =
	{
		"MDG_INPUT_FILE_NAME",
		"OUTPUT_DIRECTORY",
		"OUTPUT_FILE",
		"CLUSTERING_APPROACH",
		"MDG_OUTPUT_MODE",
		"OUTPUT_FORMAT",
		"MQ_CALCULATOR_CLASS",
		"ECHO_RESULTS_TO_CONSOLE",
		"CLUSTERING_ALG",

		"ALG_HC_POPULATION_SZ",
		"ALG_HC_HC_PCT",
		"ALG_HC_RND_PCT",
		"ALG_HC_SA_CLASS",
		"ALG_HC_SA_CONFIG",

		"ALG_GA_NUM_GENERATIONS",
		"ALG_GA_SELECTION_METHOD",
		"ALG_GA_POPULATION_SZ",
		"ALG_GA_CROSSOVER_PROB",
		"ALG_GA_MUTATION_PROB",

		"LIBRARY_LIST",
		"OMNIPRESENT_CLIENTS",
		"OMNIPRESENT_SUPPLIERS",
		"OMNIPRESENT_BOTH",

		"TIMEOUT_TIME",
		"OUTPUT_TREE",

		"USER_DIRECTED_CLUSTER_SIL",
		"LOCK_USER_SET_CLUSTERS",

		"DEBUG"
	};

	private final static String[] g_properties =
	{
		BunchProperties.MDG_INPUT_FILE_NAME,
		BunchProperties.OUTPUT_DIRECTORY,
		BunchProperties.OUTPUT_FILE,
		BunchProperties.CLUSTERING_APPROACH,
		BunchProperties.MDG_OUTPUT_MODE,
		BunchProperties.OUTPUT_FORMAT,
		BunchProperties.MQ_CALCULATOR_CLASS,
		BunchProperties.ECHO_RESULTS_TO_CONSOLE,
		BunchProperties.CLUSTERING_ALG,

		BunchProperties.ALG_HC_POPULATION_SZ,
		BunchProperties.ALG_HC_HC_PCT,
		BunchProperties.ALG_HC_RND_PCT,
		BunchProperties.ALG_HC_SA_CLASS,
		BunchProperties.ALG_HC_SA_CONFIG,

		BunchProperties.ALG_GA_NUM_GENERATIONS,
		BunchProperties.ALG_GA_SELECTION_METHOD,
		BunchProperties.ALG_GA_POPULATION_SZ,
		BunchProperties.ALG_GA_CROSSOVER_PROB,
		BunchProperties.ALG_GA_MUTATION_PROB,

		BunchProperties.LIBRARY_LIST,
		BunchProperties.OMNIPRESENT_CLIENTS,
		BunchProperties.OMNIPRESENT_SUPPLIERS,
		BunchProperties.OMNIPRESENT_BOTH,

		BunchProperties.TIMEOUT_TIME,
		BunchProperties.OUTPUT_TREE,

		BunchProperties.USER_DIRECTED_CLUSTER_SIL,
		BunchProperties.LOCK_USER_SET_CLUSTERS,

		null
	};

	private static void errorProperty(int i, String value)
	{
		System.err.println("Unexpected value for " + g_keywords[i] + " '" + value + "'");
	}

	public static void main(String args[]) 
	{
		BunchProperties	bp;
		int				i, j, debug;

		debug = 0;
		try {
			bp = new BunchProperties();
		} catch (Throwable exception) {
			System.err.println("Unable to access the bunch API");
			System.err.println("Make sure that the location of bunch.jar is specified in the CLASSPATH");
			System.err.println("Error : " + exception.getMessage());
			return;
		}

		InputStreamReader	is     = new InputStreamReader(System.in);
		BufferedReader		reader = new BufferedReader(is);
		int					index;
		String				string, keyword, value;
		int					c1;

		try {
			while ((string = reader.readLine()) != null) {
				if (debug >= 3) {
					System.err.println(string);
				}
				index = string.indexOf('=');
				if (index <= 0) {
					continue;
				}
				keyword = string.substring(0, index);
				for (i = g_keywords.length-1; i >= 0; --i) {
					if (keyword.equals(g_keywords[i])) {
						break;
				}	}

				if (i < 0) {
					System.err.println("Unrecognised keyword: " + keyword);
					continue;
				}
				value = string.substring(index+1);
				c1    = value.charAt(0);
				switch (i) {
				case DEBUG:
					switch (c1) {
					case 'S':
						debug = 0;
						break;
					case 'M':
						debug = 1;
						break;
					case 'V':
						debug = 2;
						break;
					case 'D':
						debug = 3;
						break;
					default:
						errorProperty(i, value);
						break;
					}
					continue;
				case OUTPUT_FILE:
				{
					String	exportname = value + ".bunch";

					try {
						File	exportfile = new File(exportname);

						if (exportfile.delete()) {
							if (debug >= 1) {
								System.err.println("Deleted " + exportname);
							}
						} else {
							if (debug >= 3) {
								System.err.println("Did not delete " + exportname);
						}	}
					} catch (Exception error) {
						if (debug >= 3) {
							System.err.println("Unable to delete '" + exportname);
					}	}
					break;
				}
				case CLUSTERING_APPROACH:
					switch (c1) {
					case 'O':
						value = BunchProperties.ONE_LEVEL;
						break;
					case 'A':
						value = BunchProperties.AGGLOMERATIVE;
						break;
					default:
						errorProperty(i, value);
						continue;
					}
					break;
				case MDG_OUTPUT_MODE:
					switch (c1) {
					case 'D':
						value = BunchProperties.OUTPUT_DETAILED;
						break;
					case 'M':
						value = BunchProperties.OUTPUT_MEDIAN;
						break;
					case 'T':
						value = BunchProperties.OUTPUT_TOP;
						break;
					default:
						errorProperty(i, value);
						continue;
					}
					break;
				case OUTPUT_FORMAT:
					switch (c1) {
					case 'D':
						value = BunchProperties.DOT_OUTPUT_FORMAT;
						break;
					case 'T':
						value = BunchProperties.TEXT_OUTPUT_FORMAT;
						break;
					case 'N':
						value = BunchProperties.NULL_OUTPUT_FORMAT;
						break;
					default:
						errorProperty(i, value);
						continue;
					}
					break;
				case CLUSTERING_ALG:
					switch (c1) {
					case 'H':
						value = BunchProperties.ALG_HILL_CLIMBING;
						break;
					case 'G':
						value = BunchProperties.ALG_GA;
						break;
					case 'E':
						value = BunchProperties.ALG_EXHAUSTIVE;
						break;
					default:
						errorProperty(i, value);
						continue;
					}
					break;
				case ALG_GA_SELECTION_METHOD:
					switch (c1) {
					case 'T':
						value = BunchProperties.ALG_GA_SELECTION_TOURNAMENT;
						break;
					case 'R':
						value = BunchProperties.ALG_GA_SELECTION_ROULETTE;
						break; 
					default:
						errorProperty(i, value);
						continue;
					}
					break;

				}
				bp.setProperty(g_properties[i], value);
				if (debug >= 3) {
					System.err.println("bp.setProperty(" + g_properties[i] + "," + value + ")");
				}
			}
			reader.close();
		} catch (Exception error) {
			System.err.println("Exception: " + error.getMessage());
			return;
		}

		if (debug != 0) {
			System.err.println("Executing bunch");
		}

		try {
			BunchAPI api = new BunchAPI();
			api.setProperties(bp);
			api.run();

			if (debug >= 2) {
				Hashtable results = api.getResults();
				System.err.println("Results:");

				String rt = (String)results.get(BunchAPI.RUNTIME);
				String evals = (String)results.get(BunchAPI.MQEVALUATIONS);
				String levels = (String)results.get(BunchAPI.TOTAL_CLUSTER_LEVELS);
				String saMovesTaken = (String)results.get(BunchAPI.SA_NEIGHBORS_TAKEN);

				System.err.println("Runtime = " + rt + " ms.");
				System.err.println("Total MQ Evaluations = " + evals);
				System.err.println("Simulated Annealing Moves Taken = " + saMovesTaken);
				System.err.println();
				Hashtable [] resultLevels = (Hashtable[])results.get(BunchAPI.RESULT_CLUSTER_OBJS);

				//Output detailed information for each level

				for (i = 0; i < resultLevels.length; i++) {
					Hashtable lvlResults = resultLevels[i];
					System.err.println("***** LEVEL "+i+"*****");
					String mq = (String)lvlResults.get(BunchAPI.MQVALUE);
					String depth = (String)lvlResults.get(BunchAPI.CLUSTER_DEPTH);
					String numC = (String)lvlResults.get(BunchAPI.NUMBER_CLUSTERS);

					System.err.println("  MQ Value = " + mq);
					System.err.println("  Best Cluster Depth = " + depth);
					System.err.println("  Number of Clusters in Best Partition = " + numC);
					System.err.println();
			}	}
		} catch (Exception error) {
			System.err.println("Exception executing bunch: " + error.getMessage());
			return;
		}
		if (debug != 0) {
			System.err.println("Executed bunch");
		}

		System.out.println("Ok");
		return;
	}	
}
















