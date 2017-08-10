package geom;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import global.RMath;
import processing.core.PVector;

public class RMatrix extends Array2DRowRealMatrix {
	private static final long serialVersionUID = -7714758888094250418L;
	
	public RMatrix(double[][] data) {
		super(data, false);
	}
	
	public RMatrix(float[][] f) {
		this(RMath.floatToDouble(f));
	}
	
	public RMatrix(RealMatrix m) {
		super(m.getData());
	}
	
	public RMatrix copy() {
		return new RMatrix( getData().clone() );
	}
	
	public float[][] getDataF() {
		return RMath.doubleToFloat(getData());
	}
	
	public float getEntryF(int row, int column) {
		return (float)getEntry(row, column);
	}
	
	public RMatrix getInverse() {
		SingularValueDecomposition s = new SingularValueDecomposition(this);
		return new RMatrix(s.getSolver().getInverse());
	}
	
	public PVector multiply(PVector v) {
		//Incorrect size for pvector multiplication
		if(this.getColumnDimension() != 4)
			return null;
		
		RMatrix m = new RMatrix(new double[][] {{v.x}, {v.y}, {v.z}, {1}});
		RMatrix result = this.multiply(m);

		float x = result.getEntryF(0, 0);
		float y = result.getEntryF(1, 0);
		float z = result.getEntryF(2, 0);
		float w = result.getEntryF(3, 0);
		
		return new PVector(x/w, y/w, z/w);
	}
	
	public RMatrix multiply(RMatrix m) {
		return new RMatrix( super.multiply(m) );
	}
	
	public RMatrix normalize() {
		for (int col = 0; col < getColumnDimension(); col += 1) {
			double mag = 0;
			// Find the magnitude of each axis vector
			for (int row = 0; row < getRowDimension(); row += 1) {
				double val = getEntry(row, col);
				mag += Math.pow(val, 2);
			}

			mag = Math.sqrt(mag);
			// Normalize each vector
			for (int row = 0; row < getRowDimension(); row += 1) {
				double val = getEntry(row, col);
				setEntry(row, col, val / mag);
			}
		}
		
		return this;
	}
	
	@Override
	public String toString() {
		return toString(4, 3);
	}
	
	/**
	 * Generates a string representation of the matrix, where each row of the
	 * matrix is surrounded by brackets and ends with a new line character. In
	 * addition, the given precision specifications are applied to each value
	 * in the matrix.
	 * 
	 * @param digitsBefore	The default number of digits, before the decimal
	 * 						point, to account for each value in the matrix.
	 * 						This value counts towards the padding applied to a
	 * 						matrix value
	 * @param digitsAfter	The maximum number of digits to account for after
	 * 						the decimal point for each value in the matrix.
	 * 						This value counts towards the total padding applied
	 * 						to each matrix value
	 * @return				A string representation of the matrix with space
	 * 						padding for all matrix values
	 */
	public String toString(int digitsBefore, int digitsAfter) {
		String str = new String();
		final String NUM_FORMAT = String.format("%%%d.%df", digitsBefore,
				digitsAfter);
		final String TOTAL_FORMAT = String.format("%%%ds ", digitsBefore +
				digitsAfter + 2);
		
		for (int row = 0; row < getRowDimension(); ++row) {
			str += "[ ";
			
			for (int column = 0; column < getColumnDimension(); ++column) {
				String val = String.format(NUM_FORMAT, this.getEntry(row,
						column));
				// Add padding
				str += String.format(TOTAL_FORMAT, val);
			}
			
			str += "]\n";
		}
		
		
		return str;
	}
	
	@Override
	public RMatrix transpose() {
		return new RMatrix(super.transpose());
	}
}
