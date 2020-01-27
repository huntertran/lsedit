package aa;


// (Features in common) / ((Features in common) + (Features in first not in second) + (Features in second not in first) )

public class Jaccard extends SimilarityMetric
{
	public double compute(Node n1, Node n2)
	{
		double numerator = AssocCoefficient.a(n1, n2);
		double denominator = numerator + AssocCoefficient.b(n1, n2) + AssocCoefficient.c(n1, n2);
		double result;
		if (denominator == 0.0)
			result = 0.0;
		else
			result = numerator / denominator;
		return result;
	}
}
