import processing.core.PVector;

/**
 * A form of orientation.
 */
public class FlQuaternion {
	private float w, x, y, z;

	/**
	 * Creates. the identity quaternion
	 */
	public FlQuaternion() {
		w = 1f;
		x = 0f;
		y = 0f;
		z = 0f;
	}

	/**
	 * Creates a quaternion with the given w, x, y, z values.
	 */
	public FlQuaternion(float w, float x, float y, float z) {
		this.w = w;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * TODO comment method
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
	 * TODO comment method
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
		return (float)Math.sqrt( Math.pow(x, 2f) + Math.pow(z, 2f) + Math.pow(y, 2f) + Math.pow(z, 2f) );
	}

	/**
	 * Converts this into a unit quaternion.
	 */
	public void normalize() {
		float mag = magnitude();
		w /= mag;
		x /= mag;
		y /= mag;
		z /= mag;
	}

	/**
	 * Returns the normalized form of q, without changing q.
	 */
	public static FlQuaternion normalize(FlQuaternion q) {
		FlQuaternion copy = q.cloneInClass();
		q.normalize();
		return copy;
	}

	/**
	 * Scales this by the given value.
	 */
	public void multScalar(float scalar) {
		w *= scalar;
		x *= scalar;
		y *= scalar;
		z *= scalar;
	}
	
	/**
	 * Returns a q scaled by scalar, without modifying q.
	 */
	public static FlQuaternion multScalar(float scalar, FlQuaternion q) {
		FlQuaternion copy = q;
		q.multScalar(scalar);
		return copy;
	}

	/**
	 * Multiplies this by the given quaterion, q.
	 */
	public void mult(FlQuaternion q) {
		float oldW = w,
				oldX = x,
				oldY = y,
				oldZ = z;

		w = oldW * q.getValue(0) - oldX * q.getValue(1) - oldY * q.getValue(2) - oldZ * q.getValue(3);
		x = oldW * q.getValue(1) + oldX * q.getValue(0) + oldY * q.getValue(3) - oldZ * q.getValue(2);
		y = oldW * q.getValue(2) - oldX * q.getValue(3) + oldY * q.getValue(0) + oldZ * q.getValue(1);
		z = oldW * q.getValue(3) + oldX * q.getValue(2) - oldY * q.getValue(1) + oldZ * q.getValue(0);
	}
	
	/**
	 * Returns the product q1 * q2 * q3 * ... * qn, which is
	 * the set quatChain without modifying any of the
	 * quaternions given.
	 */
	public static FlQuaternion mult(FlQuaternion... quatChain) {
		FlQuaternion product = quatChain[0].cloneInClass();
		
		for (int idx = 1; idx < quatChain.length; ++idx) {
			product.mult( quatChain[idx].cloneInClass() );
		}
		
		return product;
	}

	/**
	 * TODO comment method
	 */
	public float[][] toMatrix() {
		float[][] r = new float[3][3];

		r[0][0] = 1 - 2*(getValue(2)*getValue(2) + getValue(3)*getValue(3));
		r[0][1] = 2*(getValue(1)*getValue(2) - getValue(0)*getValue(3));
		r[0][2] = 2*(getValue(0)*getValue(2) + getValue(1)*getValue(3));
		r[1][0] = 2*(getValue(1)*getValue(2) + getValue(0)*getValue(3));
		r[1][1] = 1 - 2*(getValue(1)*getValue(1) + getValue(3)*getValue(3));
		r[1][2] = 2*(getValue(2)*getValue(3) - getValue(0)*getValue(1));
		r[2][0] = 2*(getValue(1)*getValue(3) - getValue(0)*getValue(2));
		r[2][1] = 2*(getValue(0)*getValue(1) + getValue(2)*getValue(3));
		r[2][2] = 1 - 2*(getValue(1)*getValue(1) + getValue(2)*getValue(2));

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
		if (obj instanceof FlQuaternion) {
			FlQuaternion q = (FlQuaternion)obj;
			// Compare w, x, y, z values
			return q.w == w && q.x == x && q.y == y && q.z == z; 
		}

		return false;
	}

	/**
	 * Returns an independent replica of this.
	 */
	private FlQuaternion cloneInClass() {
		return new FlQuaternion(w, x, y, z);
	}

	@Override
	public Object clone() {
		return cloneInClass();
	}

	/**
	 * Returns a String representing the w, x, y, z values of the quaternion
	 * inside nested brackets.
	 */
	@Override
	public String toString() {
		return String.format("{ %4.3f, (%4.3f, %4.3f, %4.3f) }", w, x, y, z);
	}
}
