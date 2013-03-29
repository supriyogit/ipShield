package javabayes.test;

import java.io.IOException;
import java.util.Vector;

import javabayes.InferenceGraphs.InferenceGraph;
import javabayes.InferenceGraphs.InferenceGraphNode;
import javabayes.InterchangeFormat.IFException;
import javabayes.QuasiBayesianInferences.QBInference;

/**
 * Driver class to test out JavaBayes bayesian network
 */
public class learnedNetwork {
	public static void main(String[] args) {
		InferenceGraph ig = null;
		//String[] nodeNames = {"hearBark", "dogOut", "bowelproblem", "familyOut", "lightOn"};
		try {
			 	ig = new InferenceGraph("/Users/supriyo/Documents/Thesis/AndroidDev/javabayes/src/javabayes/test/dog-problem.bif");
		} catch (IOException e) {
			System.out.println("Inside IOException block");
			e.printStackTrace();
		} catch (IFException e) {
			System.out.println("Inside IFException Block");
			e.printStackTrace();
		}
		if( ig != null)
		{
			InferenceGraphNode node = getNodeForName(ig, "light_on");
			double belief = getBelief(ig, node);
			System.out.println("The probability of the light being on is " + belief);

			String[] sensitiveNames = {"hear_bark", "bowel_problem"};
			String[] evidence = {"true", "false"};
			setObservationValues(sensitiveNames, evidence, ig);
			// Recalculate probability of light being on given evidence
			node = getNodeForName(ig, "light_on");
			belief = getBelief(ig, node);

			System.out.println("The probability of the light being on given a bark was heard " + "and no bowel problem is " + belief);
		}
		else
		{
			System.out.println("Could not create inference graph");
		}
	}
	
	private static InferenceGraphNode getNodeForName(InferenceGraph ig, String nodeName)
	{
		for (InferenceGraphNode node : (Vector<InferenceGraphNode>) ig.get_nodes()) 
		{
			//System.out.println(node.get_name());
			if(node.get_name().equalsIgnoreCase(nodeName))
			{
				System.out.println(node.get_name());
				return node;
			}
		}
		return null;
	}
	
	private static void setObservationValues(String[] sensitiveNames, String[] evidence, InferenceGraph ig)
	{
		for (int i = 0; i < sensitiveNames.length; i++)
		{
			for (InferenceGraphNode node : (Vector<InferenceGraphNode>) ig.get_nodes()) 
			{
				if(node.get_name().equalsIgnoreCase(sensitiveNames[i]))
				{
					node.set_observation_value(evidence[i]);
					System.out.println("Evidence for " + node.get_name() + " is set to " + node.get_observed_value());
				}
			}
		}
	}
	
	
	/**
	 * Helper function to create node since not as straightforward with JavaBayes
	 * to get a pointer back to the node that is being added
	 */
	private static InferenceGraphNode createNode(InferenceGraph ig, String name, String trueVariable, String falseVariable) {
		ig.create_node(0, 0);
		InferenceGraphNode node = (InferenceGraphNode) ig.get_nodes().lastElement();

		node.set_name(name);
		ig.change_values(node, new String[] {trueVariable, falseVariable});

		return node;
	}

	/**
	 * Sets probabilities for a leaf node
	 */
	private static void setProbabilityValues(InferenceGraphNode node, double trueValue, double falseValue) {
		node.set_function_values(new double[] {trueValue, falseValue});
	}

	/**
	 * Returns the index of the variable for the parent that has the given variable 
	 */
	private static int getVariableIndex(InferenceGraphNode node, String parentVariable) {

		for (InferenceGraphNode parent : (Vector<InferenceGraphNode>) node.get_parents()) {
			int variableIndex = 0;

			for (String variable : parent.get_values()) {
				if (variable.equals(parentVariable)) {
					return variableIndex;
				}

				variableIndex++;
			}
		}

		return 0;
	}

	/**
	 * Returns the total number of values for the parent that has the given variable
	 */
	private static int getTotalValues(InferenceGraphNode node, String parentVariable) {
		for (InferenceGraphNode parent : (Vector<InferenceGraphNode>) node.get_parents()) {

			for (String variable : parent.get_values()) {
				if (variable.equals(parentVariable)) {
					return parent.get_number_values();
				}
			}
		}

		return 0;
	}

	/**
	 * Sets probabilities for a node that has a parent
	 */
	private static void setProbabilityValues(InferenceGraphNode node, String parentVariable, 
			double trueValue, double falseValue) {
		int variableIndex = getVariableIndex(node, parentVariable);
		int totalValues = getTotalValues(node, parentVariable);

		double[] probabilities = node.get_function_values();
		probabilities[variableIndex] = trueValue;
		probabilities[variableIndex + totalValues] = falseValue;
		node.set_function_values(probabilities);
	}

	/**
	 * Sets probabilities for a node that has two parents
	 */
	private static void setProbabilityValues(InferenceGraphNode node, String firstParentVariable, 
			String secondParentVariable, double trueValue, double falseValue) {

		int variableIndex = (getVariableIndex(node, firstParentVariable) * 2) + 
				getVariableIndex(node, secondParentVariable);
		int totalValues = getTotalValues(node, firstParentVariable) + 
				getTotalValues(node, secondParentVariable);

		double[] probabilities = node.get_function_values();
		probabilities[variableIndex] = trueValue;
		probabilities[variableIndex + totalValues] = falseValue;
		node.set_function_values(probabilities);
	}

	/**
	 * Gets the belief/true result from the inference of the given node
	 */
	private static double getBelief(InferenceGraph ig, InferenceGraphNode node) {
		QBInference qbi = new QBInference(ig.get_bayes_net(), false);
		qbi.inference(node.get_name());
		return qbi.get_result().get_value(0);
	}
}