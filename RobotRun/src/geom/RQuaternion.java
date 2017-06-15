package geom;

import global.Fields;
import processing.core.PVector;

/**
 * A form of orientation in the complex plain.
 * 
 * @author Joshua Hooker
 */
public class RQuaternion implements Cloneable {
	
	/**
	 * Returns the sum q1 + q2 + q3 + ... + qn, where n is number
	 * of quaternions given. None of the given quaternions are
	 * modified in the process.
	 */
	public static RQuaternion addValues(RQuaternion... quatChain) {
		RQuaternion sum = quatChain[0].clone();
		// Add values of all quaternions
		for (int idx = 1; idx < quatChain.length; ++idx) {
			sum.addValues(quatChain[idx]);
		}
		
		return sum;
	}
	
	/**
	 * Returns the product q1 * q2 * q3 * ... * qn, where n is
	 * number of quaternions given. No quaternion is modified in
	 * the process.
	 */
	public static RQuaternion mult(RQuaternion... quatChain) {
		RQuaternion product = quatChain[0].clone();
		// Multiply quaternions in the order they are given
		for (int idx = 1; idx < quatChain.length; ++idx) {
			product.mult(quatChain[idx]);
		}
		
		return product;
	}
	
	/**
	 * Returns the unit quaternion form of q, without
	 * changing q.
	 */
	public static RQuaternion normalize(RQuaternion q) {
		RQuaternion copy = q.clone();
		q.normalize();
		return copy;
	}

	/**
	 * Rotates q around u by theta and returns the result, without modifying q. It is
	 * assumed that axis is a unit vector.
	 */	
	public static RQuaternion rotateAroundAxis(RQuaternion q, PVector u, float theta) {
		RQuaternion rotated = q.clone();
		rotated.rotateAroundAxis(u, theta);
		return rotated;
	}

	/**
	 * Rotates v around axis by theta and returns the result.
	 */
	public static PVector rotateVectorAroundAxis(PVector v, PVector axis, float theta) {
		RQuaternion q = new RQuaternion(axis, theta);
		return q.rotateVector(v);
	}
	
	/**
	 * Returns q, scaled by scalar, without modifying q.
	 */
	public static RQuaternion scalarMult(float scalar, RQuaternion q) {
		RQuaternion copy = q.clone();
		copy.scalarMult(scalar);
		return copy;
	}

	/**
	 * Given two input quaternions, 'q1' and 'q2', computes the spherical-
	 * linear interpolation from 'q1' to 'q2' for a given fraction of the
	 * complete transformation 'q1' to 'q2', denoted by 0 <= 'mu' <= 1. 
	 */
	public static RQuaternion minSLERP(RQuaternion q1, RQuaternion q2, float mu) {
		if (mu == 0f) {
			return q1;
			
		} else if (mu == 1f) {
			return q2;
		}
		
		float cOmega = q1.dot(q2);
		RQuaternion q3 = q2.clone(), q4;
		
		// If we would go the long way around, take the short way around instead.
		if (cOmega < 0f) {
			cOmega *= -1;
			q3.scalarMult(-1);		
		}
		
		// Now that we are going the short way around, if the long way was requested, take that.
		
		if (cOmega > 0.99999995f) {
			q4 = RQuaternion.scalarMult(1f - mu, q1);
			q3.scalarMult(mu);
			
		} else {
			double omega = Math.acos(cOmega);
			double sinOmega = Math.sin(omega);			
			float scaleQ1 = (float)( Math.sin(omega * (1 - mu)) / sinOmega );
			float scaleQ3 = (float)( Math.sin(omega * mu) / sinOmega );
			
			q4 = RQuaternion.scalarMult(scaleQ1, q1);
			q3.scalarMult(scaleQ3);
		}
		
		q4.addValues(q3);
		q4.normalize();
		return q4;
	}
	
	/**
	 * Performs SLERP from q1 to q2 by the percent defined by mu. For 0 < mu <= 1,
	 * the shortest path from q1 to q2 is taken. For 0 > mu >= -1, the longer
	 * path from q1 to q2 is taken.
	 * 
	 * @param q1	The initial orientation
	 * @param q2	The end orientation
	 * @param mu	The percent of interpolation from q1 to q2 (sign denotes
	 * 				direction)
	 * @return		The spherical interpolation from q1 to q2 to the percent
	 * 				defined by mu
	 */
	public static RQuaternion signedSLERP(RQuaternion q1, RQuaternion q2,
			float mu) {
		
		if (mu == 0f) {
			return q1;
			
		} else if (mu == 1f) {
			return q2;
		}
		
		float cOmega = q1.dot(q2);
		RQuaternion q3 = q2.clone(), q4;
		
		if (cOmega < 0f) {
			// Enforce the shortest path from q1 to q2
			cOmega *= -1;
			q3.scalarMult(-1);		
		}
		
		if (mu < 0f) {
			// Take the longer path if specified by mu's sign
			mu *= -1f;
			cOmega *= -1;
			q3.scalarMult(-1f);
		}
		
		if (cOmega > 0.99999995f) {
			q4 = RQuaternion.scalarMult(1f - mu, q1);
			q3.scalarMult(mu);
			
		} else {
			double omega = Math.acos(cOmega);
			double sinOmega = Math.sin(omega);			
			float scaleQ1 = (float)( Math.sin(omega * (1 - mu)) / sinOmega );
			float scaleQ3 = (float)( Math.sin(omega * mu) / sinOmega );
			
			q4 = RQuaternion.scalarMult(scaleQ1, q1);
			q3.scalarMult(scaleQ3);
		}
		
		q4.addValues(q3);
		q4.normalize();
		return q4;
	}

	private float w, x, y, z;

	/**
	 * Creates the identity quaternion
	 */
	public RQuaternion() {
		w = 1f;
		x = 0f;
		y = 0f;
		z = 0f;
	}

	/**
	 * Creates a quaternion with the given w, x, y, z values.
	 */
	public RQuaternion(float w, float x, float y, float z) {
		this.w = w;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	/**
	 * Creates a quaternion from a unit vector axis and
	 * rotation around that axis.
	 */
	public RQuaternion(PVector v, float theta) {
		float halfTheta = theta / 2f;
		float sinTheta = (float)Math.sin(halfTheta);
		
		w = (float)Math.cos(halfTheta);
		x = v.x * sinTheta;
		y = v.y * sinTheta;
		z = v.z * sinTheta;
	}
	
	/**
	 * Adds each value (w, x, y, z) of q to this
	 * [quaternion's] corresponding value.
	 */
	public void addValues(RQuaternion q) {
		w += q.w;		
		x += q.x;
		y += q.y;
		z += q.z;
	}

	@Override
	public RQuaternion clone() {
		return new RQuaternion(w, x, y, z);
	}
	
	/**
	 * Return the conjugate of this.
	 */	
	public RQuaternion conjugate() {
		return new RQuaternion(w, -x, -y, -z);
	}
	
	/**
	 * Returns the distance between this quaternion (q) and the given
	 * quaternion (p) based on the distance formula:
	 * 		( (q_w - p_w)^2 + (q_x - p_x)^2 + (q_y - p_y)^2 + (q_z - p_z)^2 )^(1/2)
	 * 
	 * @param q	A non-null quaternion
	 * @return	The distance between the two quaternions
	 */
	public float dist(RQuaternion q) {
		return (float)Math.sqrt(	Math.pow(w - q.w, 2.0) +
									Math.pow(x - q.x, 2.0) +
									Math.pow(y - q.y, 2.0) +
									Math.pow(z - q.z, 2.0)	);
	}

	/**
	 * Returns the dot product of this and q.
	 */	
	public float dot(RQuaternion q) {
		return (w * q.w) + (x * q.x) + (y * q.y) + (z * q.z);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RQuaternion) {
			RQuaternion q = (RQuaternion)obj;
			// Compare w, x, y, z values
			return q.w == w && q.x == x && q.y == y && q.z == z; 
		}

		return false;
	}
	
	/**
	 * Returns a value of this based
	 * on the following map:
	 * 
	 * 0 -> w
	 * 1 -> x
	 * 2 -> y
	 * 3 -> z
	 * *otherwise null
	 */
	public Float getValue(int idx) {
		if (idx == 0) {
			return w;

		} else if (idx == 1) {
			return x;

		} else if (idx == 2) {
			return y;

		}  else if (idx == 3) {
			return z;

		}

		return null;
	}
	
	/**
	 * Transforms this into its conjugate.
	 */
	public void invert() {
		x = -x;
		y = -y;
		z = -z;
	}
	/**
	 * Returns the magnitude of the quaternion.
	 */
	public float magnitude() {
		return (float)Math.sqrt( Math.pow(w, 2f) + Math.pow(x, 2f) + Math.pow(y, 2f) + Math.pow(z, 2f) );
	}
	/**
	 * Rotates this by q.
	 */
	public RQuaternion mult(RQuaternion q) {
		float oldW = w, oldX = x, oldY = y, oldZ = z;

		w = oldW * q.w - oldX * q.x - oldY * q.y - oldZ * q.z;
		x = oldW * q.x + oldX * q.w + oldY * q.z - oldZ * q.y;
		y = oldW * q.y - oldX * q.z + oldY * q.w + oldZ * q.x;
		z = oldW * q.z + oldX * q.y - oldY * q.x + oldZ * q.w;
		
		return this;
	}
	/**
	 * Converts this into its unit quaternion form.
	 */
	public RQuaternion normalize() {
		float mag = magnitude();
		w /= mag;
		x /= mag;
		y /= mag;
		z /= mag;
		
		return this;
	}
	
	/**
	 * Rotates this around the given axis vector by theta. It
	 * is assumed that u is a unit vector.
	 */	
	public void rotateAroundAxis(PVector u, float theta) {
		// Represent rotation as a quaternion		
		RQuaternion rotation = new RQuaternion(u, theta);
		mult(rotation);
	}
	
	/**
	 * Rotates v by this and returns the result.
	 */
	public PVector rotateVector(PVector v) {
		RQuaternion quatV = new RQuaternion(0, v.x, v.y, v.z);
		RQuaternion conj = conjugate();
		quatV = RQuaternion.mult(this, quatV, conj);
		// u = q * v * q'
		return new PVector(quatV.x, quatV.y, quatV.z);	
	}
	
	/**
	 * Scales this by the given scalar value.
	 */
	public void scalarMult(float scalar) {
		w *= scalar;
		x *= scalar;
		y *= scalar;
		z *= scalar;
	}
	
	/**
	 * Sets a value of this based on the following
	 * map:
	 *
	 * 0 -> w
	 * 1 -> x
	 * 2 -> y
	 * 3 -> z
	 * *otherwise no change
	 */
	public void setValue(int idx, float newVal) {
		if (idx == 0) {
			w = newVal;

		} else if (idx == 1) {
			x = newVal;

		} else if (idx == 2) {
			y = newVal;

		}  else if (idx == 3) {
			z = newVal;

		}
	}
	
	/**
	 * Returns the 3x3 rotation matrix corresponding
	 * to this [quaternion].
	 */
	public RMatrix toMatrix() {
		double[][] r = new double[3][3];

		r[0][0] = 1 - 2 * (y*y + z*z);
		r[1][0] = 2 * (x*y - w*z);
		r[2][0] = 2 * (w*y + x*z);
		r[0][1] = 2 * (x*y + w*z);
		r[1][1] = 1 - 2 * (x*x + z*z);
		r[2][1] = 2 * (y*z - w*x);
		r[0][2] = 2 * (x*z - w*y);
		r[1][2] = 2 * (w*x + y*z);
		r[2][2] = 1 - 2 * (x*x + y*y);

		return new RMatrix(r).normalize();
	}
	
	/**
	 * Returns a String representing the w, x, y, z values
	 * of this, inside nested brackets.
	 */
	@Override
	public String toString() {
		return String.format("{ %4.3f, (%4.3f, %4.3f, %4.3f) }", w, x, y, z);
	}

	public PVector toVector() {
		float[][] r = toMatrix().getDataF();
		float x, y, z;
		PVector wpr;
		
		x = (float) Math.atan2(-r[2][1], r[2][2]);
		y = (float) Math.atan2(r[2][0], Math.sqrt(r[2][1]*r[2][1] + r[2][2]*r[2][2]));
		z = (float) Math.atan2(-r[1][0], r[0][0]);

		wpr = new PVector(x, y, z);
		return wpr;
	}

	/**
	 * Returns q in reference to this [quaternion's] orientation.
	 */
	public RQuaternion transformQuaternion(RQuaternion q) {
		RQuaternion conj = conjugate();
		return RQuaternion.mult(q, conj);
	}

	public float w() { return w; }

	public float x() { return x; }

	public float y() { return y; }

	public float z() { return z; }
}
