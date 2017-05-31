package geom;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import global.RMath;
import processing.core.PVector;

public class RMatrix extends Array2DRowRealMatrix {
	
	private static final long serialVersionUID = -7714758888094250418L;
	
	public RMatrix(float[][] f) {
		super(RMath.floatToDouble(f));
	}
	
	public RMatrix(RealMatrix m) {
		super(m.getData());
	}
	
	public RMatrix multiply(RMatrix m) {
		return new RMatrix(this.multiply((Array2DRowRealMatrix)m));
	}
	
	public PVector multiply(PVector v) {
		RMatrix m = new RMatrix(new float[][] {{v.x}, {v.y}, {v.z}, {1}});
		RMatrix result = this.multiply(m);
		float[][] data = result.getDataF();
		return new PVector(data[0][0], data[1][0], data[2][0]).div(data[3][0]);
	}
	
	public float[][] getDataF() {
		return RMath.doubleToFloat(getData());
	}
	
	public RMatrix getInverse() {
		SingularValueDecomposition s = new SingularValueDecomposition(this);
		return new RMatrix(s.getSolver().getInverse());
	}
	
	@Override
	public RMatrix transpose() {
		return new RMatrix(super.transpose());
	}
	
	public float getEntryF(int row, int column) {
		return (float)getEntry(row, column);
	}
	
	public RMatrix normalize() {
		float[][] d = getDataF();
		float mag = 0;
		
		for (int i = 0; i < d.length; i += 1) {
			// Find the magnitude of each axis vector
			for (int j = 0; j < d[0].length; j += 1) {
				mag += Math.pow(d[j][i], 2);
			}

			mag = (float) Math.sqrt(mag);
			// Normalize each vector
			for (int j = 0; j < d.length; j += 1) {
				this.setEntry(j, i, d[j][i] /= mag);
			}
			
			mag = 0;
		}
		
		return this;
	}
	
	public RMatrix copy() {
		return new RMatrix(this.getDataF());
	}
}
