package converters;

import main.UsersGraph;

public class MOCConverter implements Converter {

	/**
	 * <p>Converts a UsersGraph into a dataset file suitable for the MOC clustering algorithm.</p>
	 * The expected input is a matrix <b>M</b> with <b>N</b> rows and <b>D</b> columns, where N is the number of samples and D the number of features.<br>
	 * The element M<sub>ij</sub> is the j<sup>th</sup> feature of the i<sup>th</sup> element of the dataset.<br>
	 * Since the algorithm is probabilistic and does not make use of distance functions
	 * @param g The UsersGraph to be converted
	 */
	public void translate(UsersGraph g) {
		// TODO Auto-generated method stub

	}

}
