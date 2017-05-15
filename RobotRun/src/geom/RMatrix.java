package geom;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;

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
		float[][] data = result.getFloatData();
		return new PVector(data[0][0], data[1][0], data[2][0]);
	}
	
	public float[][] getFloatData() {
		return RMath.doubleToFloat(getData());
	}
	
	public RMatrix getSVD() {
		SingularValueDecomposition s = new SingularValueDecomposition(this);
		return new RMatrix(s.getSolver().getInverse());
	}
	
	public RMatrix rTranspose() {
		return new RMatrix(this.transpose());
	}
}
