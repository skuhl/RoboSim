package geom;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import global.RMath;
import processing.core.PVector;

public class RMatrix extends Array2DRowRealMatrix {
	private static final long serialVersionUID = -7714758888094250418L;
	
	public RMatrix(float[][] f) {
		this(RMath.floatToDouble(f));
	}
	
	public RMatrix(double[][] data) {
		super(data, false);
	}
	
	public RMatrix(RealMatrix m) {
		super(m.getData());
	}
	
	public RMatrix multiply(RMatrix m) {
		return new RMatrix( super.multiply(m) );
	}
	
	public PVector multiply(PVector v) {
		//RMatrix m = new RMatrix(new double[][] {{v.x}, {v.y}, {v.z}, {1}});
		//RMatrix result = this.multiply(m);
		
		float w = getEntryF(3, 0);
		
		PVector u = new PVector(
				getEntryF(0, 0),
				getEntryF(1, 0),
				getEntryF(2, 0)
		);
		
		return u.div(w);
	}
	
	public float[][] getDataF() {
		return RMath.doubleToFloat(getData());
	}
	
	public RMatrix getInverse() {
		SingularValueDecomposition s = new SingularValueDecomposition(this);
		return new RMatrix(s.getSolver().getInverse());
	}
	
	@Override
	public String toString() {
		String str = new String();
		
		for (int row = 0; row < getRowDimension(); ++row) {
			str += "[ ";
			
			for (int column = 0; column < getColumnDimension(); ++column) {
				String val = String.format("%4.3f", this.getEntry(row, column));
				// Add padding
				str += String.format("%9s ", val);
			}
			
			str += "]\n";
		}
		
		
		return str;
	}
	
	@Override
	public RMatrix transpose() {
		return new RMatrix(super.transpose());
	}
	
	public float getEntryF(int row, int column) {
		return (float)getEntry(row, column);
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
	
	public RMatrix copy() {
		return new RMatrix( getData().clone() );
	}
}
