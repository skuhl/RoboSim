package robot;

import processing.core.*;

/**
 * A form of orientation in the complex plain.
 */
public class RQuaternion {
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
	 * Returns the magnitude of the quaternion.
	 */
	public float magnitude() {
		return (float)Math.sqrt( Math.pow(w, 2f) + Math.pow(x, 2f) + Math.pow(y, 2f) + Math.pow(z, 2f) );
	}

	/**
	 * Converts this into its unit quaternion form.
	 */
	public void normalize() {
		float mag = magnitude();
		w /= mag;
		x /= mag;
		y /= mag;
		z /= mag;
	}

	/**
	 * Returns the unit quaternion form of q, without
	 * changing q.
	 */
	public static RQuaternion normalize(RQuaternion q) {
		RQuaternion copy = q.cloneInClass();
		q.normalize();
		return copy;
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
	 * Return the conjugate of this.
	 */	
	public RQuaternion conjugate() {
		return new RQuaternion(w, -x, -y, -z);
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
	 * Returns q, scaled by scalar, without modifying q.
	 */
	public static RQuaternion scalarMult(float scalar, RQuaternion q) {
		RQuaternion copy = q.cloneInClass();
		copy.scalarMult(scalar);
		return copy;
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
	
	/**
	 * Returns the sum q1 + q2 + q3 + ... + qn, where n is number
	 * of quaternions given. None of the given quaternions are
	 * modified in the process.
	 */
	public static RQuaternion addValues(RQuaternion... quatChain) {
		RQuaternion sum = quatChain[0].cloneInClass();
		// Add values of all quaternions
		for (int idx = 1; idx < quatChain.length; ++idx) {
			sum.addValues(quatChain[idx]);
		}
		
		return sum;
	}

	/**
	 * Rotates this by q.
	 */
	public void mult(RQuaternion q) {
		float oldW = w, oldX = x, oldY = y, oldZ = z;

		w = oldW * q.w - oldX * q.x - oldY * q.y - oldZ * q.z;
		x = oldW * q.x + oldX * q.w + oldY * q.z - oldZ * q.y;
		y = oldW * q.y - oldX * q.z + oldY * q.w + oldZ * q.x;
		z = oldW * q.z + oldX * q.y - oldY * q.x + oldZ * q.w;
	}
	
	/**
	 * Returns the product q1 * q2 * q3 * ... * qn, where n is
	 * number of quaternions given. No quaternion is modified in
	 * the process.
	 */
	public static RQuaternion mult(RQuaternion... quatChain) {
		RQuaternion product = quatChain[0].cloneInClass();
		// Multiply quaternions in the order they are given
		for (int idx = 1; idx < quatChain.length; ++idx) {
			product.mult(quatChain[idx]);
		}
		
		return product;
	}
	
	/**
	 * Returns q in reference to this [quaternion's] orientation.
	 */
	public RQuaternion transformQuaternion(RQuaternion q) {
		RQuaternion conj = conjugate();
		return RQuaternion.mult(q, conj);
	}
	
	/**
	 * Given two input quaternions, 'q1' and 'q2', computes the spherical-
	 * linear interpolation from 'q1' to 'q2' for a given fraction of the
	 * complete transformation 'q1' to 'q2', denoted by 0 <= 'mu' <= 1. 
	 */
	public static RQuaternion SLERP(RQuaternion q1, RQuaternion q2, float mu) {
		if (mu == 0) {
			return q1;
			
		} else if (mu == 1) {
			return q2;
		}
		
		float cOmega = q1.dot(q2);
		RQuaternion q3 = q2.cloneInClass(), q4;
		
		if (cOmega < 0) {
			cOmega *= -1;
			q3.scalarMult(-1);		
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
	
	/**
	 * Returns the dot product of this and q.
	 */	
	public float dot(RQuaternion q) {
		return (w * q.w) + (x * q.x) + (y * q.y) + (z * q.z);
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
	 * Rotates q around u by theta and returns the result, without modifying q. It is
	 * assumed that axis is a unit vector.
	 */	
	public static RQuaternion rotateAroundAxis(RQuaternion q, PVector u, float theta) {
		RQuaternion rotated = q.cloneInClass();
		rotated.rotateAroundAxis(u, theta);
		return rotated;
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
	 * Rotates v around axis by theta and returns the result.
	 */
	public static PVector rotateVectorAroundAxis(PVector v, PVector axis, float theta) {
		RQuaternion q = new RQuaternion(axis, theta);
		return q.rotateVector(v);
	}

	/**
	 * Returns the 3x3 rotation matrix corresponding
	 * to this [quaternion].
	 */
	public float[][] toMatrix() {
		float[][] r = new float[3][3];

		r[0][0] = 1 - 2 * (y * y + z * z);
		r[0][1] = 2 * (x * y - w * z);
		r[0][2] = 2 * (w * y + x * z);
		r[1][0] = 2 * (x * y + w * z);
		r[1][1] = 1 - 2 * (x * x + z * z);
		r[1][2] = 2 * (y * z - w * x);
		r[2][0] = 2 * (x * z - w * y);
		r[2][1] = 2 * (w * x + y * z);
		r[2][2] = 1 - 2 * (x * x + y * y);

		float[] magnitudes = new float[3];

		for(int v = 0; v < r.length; ++v) {
			// Find the magnitude of each axis vector
			for(int e = 0; e < r[0].length; ++e) {
				magnitudes[v] += Math.pow(r[v][e], 2);
			}

			magnitudes[v] = (float)Math.sqrt(magnitudes[v]);
			// Normalize each vector
			for(int e = 0; e < r.length; ++e) {
				r[v][e] /= magnitudes[v];
			}
		}

		return r;
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
	 * Returns an independent replica of this.
	 */
	private RQuaternion cloneInClass() {
		return new RQuaternion(w, x, y, z);
	}

	@Override
	public Object clone() {
		return cloneInClass();
	}

	/**
	 * Returns a String representing the w, x, y, z values
	 * of this, inside nested brackets.
	 */
	@Override
	public String toString() {
		return String.format("{ %4.3f, (%4.3f, %4.3f, %4.3f) }", w, x, y, z);
	}
}
