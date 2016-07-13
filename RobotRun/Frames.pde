Frame[] toolFrames = null;
Frame[] userFrames = null;

public abstract class Frame {
  private PVector origin;
  // The unit vectors representing the x, y, z axes (in row major order)
  private float[][] axes;
  /* The three points used to define a coordinate axis for 6-Point Method
   * of Tool Frames and 3-Point or 4_Point Methods of User Frames */
  public Point[] axesTeachPoints;
  // For Direct Entry
  public PVector DEOrigin;
  public float[] DEAxesOffsets;

  public Frame() {
    origin = new PVector(0,0,0);
    axes = new float[3][3];
    // Create identity matrix
    for(int diag = 0; diag < 3; ++diag) {
      axes[diag][diag] = 1f;
    }
    
    axesTeachPoints = new Point[] { null, null, null };
    
    DEOrigin = null;
    DEAxesOffsets = null;
  }

  public PVector getOrigin() { return origin; }
  public void setOrigin(PVector in) { origin = in; }

  /**
   * Return the W, P, R values of the this frames coordinate
   * axes with respect to the World Frame axes.
   */
  public PVector getWpr() { return matrixToEuler(axes); }
  /* Returns a set of axes unit vectors representing the axes
   * of the frame in reference to the Native Coordinate System. */
  public float[][] getNativeAxes() { return axes.clone(); }
  /* Returns a set of axes unit vectors representing the axes
   * of the frame in reference to the World Coordinate System. */
  public float[][] getWorldAxes() {
    float[][] wAxes = new float[3][3];
    
    for(int col = 0; col < wAxes[0].length; ++col) {
      wAxes[0][col] = -axes[0][col];
      wAxes[1][col] = axes[2][col];
      wAxes[2][col] = -axes[1][col];
    }
    
    /*for(int row = 0; row < wAxes[0].length; ++row) {
      wAxes[row][0] = -axes[row][0];
      wAxes[row][1] = axes[row][2];
      wAxes[row][2] = -axes[row][1];
    }*/
    
    return wAxes;
  }

  public void setAxis(int idx, PVector in) {
    
    if(idx >= 0 && idx < axes.length) {
      axes[idx][0] = in.x;
      axes[idx][1] = in.y;
      axes[idx][2] = in.z;
    }
  }

  public void setAxes(float[][] axesVectors) {
    axes = axesVectors.clone();
  }
  
  /**
   * Sets the Frame's point at the given index in its list of taugh points.
   * For a Tool Frame, the list of possible taught points includes the three
   * points for the teaching of the TCP offset as well as the three points
   * for the Coordinate Axes; six points in total. The valid values for indices
   * are as follows:
   * 
   * 0 -> TCP teach point 1
   * 1 -> TCP teach point 2
   * 2 -> TCP teach point 3
   * 3 -> Orient Origin point
   * 4 -> X-Direction point
   * 5 -> Y-Direction point
   * 
   * For a User Frame, the list of possible taugh points includes the three
   * points for the Coordinate Axes as well as the Axes Origin point; four
   * in total. The valid values for index are as follows:
   * 
   * 0 -> Orient Origin point
   * 1 -> X-Direction point
   * 2 -> Y-Direction point
   * 3 -> Axes Origin point
   * 
   * Because the orientation of a point is only necessary for the creation of the
   * TCP offset, the orientation of the given point will be ignored for all other
   * points aside from the TCP teach points and only the position of the point will
   * be recorded.
   * 
   * @param p    A point, which contains the position and orientation of the Robot
   *             at a specific point in space
   * @param idx  The index, at which to save the given point, in the Frame's list
   *             of taught points
   */
  public abstract void setPoint(Point p, int idx);
  
  /**
   * Returns the position of the teach point at the given index in the Frame's list
   * of teach points. Valid indices are described in setPoint().
   */
  public abstract Point getPoint(int idx);
  
  /**
   * Based on value of method, an attempt will be made to set the current origin offset and axes vectors.
   * For the value of method:
   * 0 -> 3-Point Method
   * 1 -> 6-Point Methd for Tool Frames or 4-Point Method for User Frames
   * 2 -> Direct Entry Method
   * 
   * Assuming that all the correct fields have been initialized to values, which will produce valid output,
   * for a given method, then the Frames origin offset and axes vectors are modified to fit the given method.
   * For Tool Frames, the 3-Point method uses the TCPTeacchPoints list exclusively to constuct the TCP offset;
   * the 6-Point Method uses both the TCPTeachPoints list and the axesTeachPoints list. For User Frames, the
   * 3-Point uses the axesTeachPoints list exculsively; the 4-Point Method uses both the axesTeachPoint list
   * as well as the orientOrigin point. For either Tool or User Frames, the Direct Entry Method uses the
   * DEOrigin opint and the DEAxesOffsets array.
   * 
   * @param method  an integer between 0-2, which determines, which teaching method will be used to contruct
   *                the Frame
   * @returning     If the Frame was set or not; it is possible that some of the fields are not initialized or
   *                will not produce valid output.
   */
  public abstract boolean setFrame(int method);
  
  /**
   * A variation of toStringArray(), where each element of the
   * String array contains a pair of values of the frame ( element
   * 0 is X and W, element 1 is Y and P, and element 2 is Z and R ).
   * Each element of the array is treated as its own line, when used
   * to display a Frame's values in the Virtual Pendant. This method
   * is designed to reduce the number of lines necessay to display
   * the values of the Frames X, Y, Z, W, P, and R fields.
   * 
   * @returning  A 3 element String array, containing paired sets of
   *             the Frames X, Y, Z, W, P, R fields.
   */
  public String[] toCondensedStringArray() {
    String[] lines = new String[3];
    String[] values = toStringArray();
    /* The '-12' formatting parameter unifies the starting position
     * of values 3 - 4 on each line, while allowing the maximum length
     * of values 0-2. */
    lines[0] = String.format("%-12s%s", values[0], values[3]);
    lines[1] = String.format("%-12s%s", values[1], values[4]);
    lines[2] = String.format("%-12s%s", values[2], values[5]);
    
    return lines;
  }

  /**
   * Returns a string array, where each entry is one of
   * the Frames six Cartesian values: (X, Y, Z, W, P,
   * and R) and their respective labels.
   *
   * @return  A 6-element String array
   */
  public String[] toStringArray() {
    
    String[] values = new String[6];
    
    PVector wOrigin = origin;
    if (this instanceof UserFrame) {
      // Convert to World frame reference
      wOrigin = convertNativeToWorld(origin);
    }
    
    values[0] = String.format("X: %4.3f", wOrigin.x);
    values[1] = String.format("Y: %4.3f", wOrigin.y);
    values[2] = String.format("Z: %4.3f", wOrigin.z);
    // Convert angles to degrees
    PVector wpr = getWpr();
    values[3] = String.format("W: %4.3f", wpr.x * RAD_TO_DEG);
    values[4] = String.format("P: %4.3f", wpr.y * RAD_TO_DEG);
    values[5] = String.format("R: %4.3f", wpr.z * RAD_TO_DEG);
    
    return values;
  }
} // end Frame class

public class ToolFrame extends Frame {
  // For 3-Point and Six-Point Methods
  public Point[] TCPTeachPoints;
  
  /**
   * Initialize all fields
   */
  public ToolFrame() {
    super();
    TCPTeachPoints = new Point[] { null, null, null };
  }
  
  public void setPoint(Point p, int idx) {
    
    /* Map the index into the 'Point array' to the
     * actual values stored in the frame */
    switch (idx) {
      case 0:
      case 1:
      case 2:
        TCPTeachPoints[idx] = p;
        return;
        
      case 3:
      case 4:
      case 5:
        axesTeachPoints[ idx % 3 ] = p;
        return;
        
      default:
    }
  }
  
  public Point getPoint(int idx) {
        
    /* Map the index into the 'Point array' to the
     * actual values stored in the frame */
    switch (idx) {
      case 0:
      case 1:
      case 2:
        return TCPTeachPoints[idx];
        
      case 3:
      case 4:
      case 5:
        return axesTeachPoints[ idx % 3 ];
        
      default:
    }
    
    return null;
  }
  
  public boolean setFrame(int method) {
    
    if (method == 2) {
      // Direct Entry Method
      
      if (DEOrigin == null || DEAxesOffsets == null) {
        // No direct entry values have been set
        return false;
      }
      
      setOrigin(DEOrigin);
      setAxes( quatToMatrix(DEAxesOffsets) );
      return true;
    } else if (method >= 0 && method < 2 && TCPTeachPoints[0] != null && TCPTeachPoints[1] != null && TCPTeachPoints[2] != null) {
      // 3-Point or 6-Point Method
      
      if (method == 1 && (axesTeachPoints[0] == null || axesTeachPoints[1] == null || axesTeachPoints[2] == null)) {
        // Missing points for the coordinate axes
        return false;
      }
      
      float[][] pt1_ori = quatToMatrix(TCPTeachPoints[0].ori),
                pt2_ori = quatToMatrix(TCPTeachPoints[1].ori),
                pt3_ori = quatToMatrix(TCPTeachPoints[2].ori);
      
      double[] newTCP = calculateTCPFromThreePoints(toVectorArray(TCPTeachPoints[0].pos), pt1_ori,
                                                 toVectorArray(TCPTeachPoints[1].pos), pt2_ori,
                                                 toVectorArray(TCPTeachPoints[2].pos), pt3_ori);
      
      float[][] newAxesVectors = (method == 1) ? createAxesFromThreePoints(axesTeachPoints[0].pos, axesTeachPoints[1].pos, axesTeachPoints[2].pos) : new float[][] { {1, 0, 0}, {0, 1, 0}, {0, 0, 1} };
      
      if (newTCP == null || newAxesVectors == null) {
        // Invalid point set for the TCP or the coordinate axes
        return false;
      }
      
      setOrigin( new PVector((float)newTCP[0], (float)newTCP[1], (float)newTCP[2]) );
      setAxes(newAxesVectors);
      return true;
    }
    
    return false;
  }
}

public class UserFrame extends Frame {
  // For the 4-Point Method
  public Point orientOrigin;
  
  /**
   * Initialize all fields
   */
  public UserFrame() {
    super();
    orientOrigin = null;
  }
  
  public void setPoint(Point p, int idx) {
    
    /* Map the index into the 'Point array' to the
     * actual values stored in the frame */
    switch(idx) {
      case 0:
      case 1:
      case 2:
        axesTeachPoints[idx] = p;
        return;
        
      case 3:
        orientOrigin = p;
        return;
        
      default:
    }
  }
  
  public Point getPoint(int idx) {
        
    /* Map the index into the 'Point array' to the
     * actual values stored in the frame */
    switch (idx) {
      case 0:
      case 1:
      case 2:
        return axesTeachPoints[idx];
        
      case 3:
        return orientOrigin;
        
      default:
    }
    
    return null;
  }
  
  public boolean setFrame(int mode) {
    
    if (mode == 2) {
      // Direct Entry Method
      
      if (DEOrigin == null || DEAxesOffsets == null) {
        // No direct entry values have been set
        return false;
      }
      
      setOrigin(DEOrigin);
      setAxes( quatToMatrix(DEAxesOffsets) );
      return true;
    } else if (mode >= 0 && mode < 2 && axesTeachPoints[0] != null && axesTeachPoints[1] != null && axesTeachPoints[2] != null) {
      // 3-Point or 4-Point Method
      
      PVector newOrigin = (mode == 0) ? getOrigin() : orientOrigin.pos;
      float[][] newAxesVectors = createAxesFromThreePoints(axesTeachPoints[0].pos, axesTeachPoints[1].pos, axesTeachPoints[2].pos);
      
      if (newOrigin == null || newAxesVectors == null) {
        // Invalid points for the coordinate axes or missing orient origin for the 4-Point Method
        return false;
      }
      
      setAxes(newAxesVectors);
      setOrigin(newOrigin);
      return true;
    }
    
    return false;
  }
}

/**
 * This method calculates a TCP offset for the Robot given a valid set of position and orientation values, where each pair ( [pos, ori1],
 * [pos2, ori2], and [pos3, ori3] ) represent a recorded position and orientation of the Robot. A position contains the X, Y, Z values of
 * the Robot at the point, while the orientation matrix is a rotation matrix, which describes the Robot's orientation at a one of the points.
 * Do to the nature of this algorithm, an average TCP value is calculated from three separate calculations.
 *
 * @param pos1  The X, Y, Z position of the Robot's faceplate at first point
 * @param ori1  The orientation of the Robot at the first point
 * @param pos2  The X, Y, Z position of the Robot's faceplate at the second point
 * @param ori2  The orientation of the Robot at the second point
 * @param pos3  the X, Y, Z position of the Robot's faceplate at the third point
 * @param ori3  The orientation of the Robot at the third point
 * @return      The new TCP for the Robot, null is returned if the given points
 *              are invalid
 */
public double[] calculateTCPFromThreePoints(float[] pos1, float[][] ori1, float[] pos2, float[][] ori2, float[] pos3, float[][] ori3) {
  
  RealVector avg_TCP = new ArrayRealVector(new double[] {0.0, 0.0, 0.0} , false);
  int counter = 3;
  
  while (counter-- > 0) {
    
    RealMatrix Ar = null, Br = null, Cr = null;
    double[] t = new double[3];
    
    if (counter == 0) {
      /* Case 3: C = point 1 */
      Ar = new Array2DRowRealMatrix(floatToDouble(ori2, 3, 3));
      Br = new Array2DRowRealMatrix(floatToDouble(ori3, 3, 3));
      Cr = new Array2DRowRealMatrix(floatToDouble(ori1, 3, 3));
      
      for(int idx = 0; idx < 3; ++idx) {
        /* 2Ct - At - Bt */
        t[idx] = 2 * pos1[idx] - ( pos2[idx] + pos3[idx] );
      }
    } else if (counter == 1) {
      /* Case 2: C = point 2 */
      Ar = new Array2DRowRealMatrix(floatToDouble(ori3, 3, 3));
      Br = new Array2DRowRealMatrix(floatToDouble(ori1, 3, 3));
      Cr = new Array2DRowRealMatrix(floatToDouble(ori2, 3, 3));
      
      for(int idx = 0; idx < 3; ++idx) {
        /* 2Ct - At - Bt */
        t[idx] = 2 * pos2[idx] - ( pos3[idx] + pos1[idx] );
      }      
    } else if (counter == 2) {
      /* Case 1: C = point 3*/
      Ar = new Array2DRowRealMatrix(floatToDouble(ori1, 3, 3));
      Br = new Array2DRowRealMatrix(floatToDouble(ori2, 3, 3));
      Cr = new Array2DRowRealMatrix(floatToDouble(ori3, 3, 3));
      
      for(int idx = 0; idx < 3; ++idx) {
        /* 2Ct - At - Bt */
        t[idx] = 2 * pos3[idx] - ( pos1[idx] + pos2[idx] );
      }     
    }
    
  /****************************************************************
      Three Point Method Calculation
      
      ------------------------------------------------------------
      A, B, C      transformation matrices
      Ar, Br, Cr   rotational portions of A, B, C respectively
      At, Bt, Ct   translational portions of A, B, C repectively
      x            TCP point with respect to the EE
      ------------------------------------------------------------
      
      Ax = Bx = Cx
      Ax = (Ar)x + At
      
      (A - B)x = 0
      (Ar - Br)x + At - Bt = 0
      
      Ax + Bx - 2Cx = 0
      (Ar + Br - 2Cr)x + At + Bt - 2Ct = 0
      (Ar + Br - 2Cr)x = 2Ct - At - Bt
      x = (Ar + Br - 2Cr) ^ -1 * (2Ct - At - Bt)
      
    ****************************************************************/
    
    RealVector b = new ArrayRealVector(t, false);
    /* Ar + Br - 2Cr */
    RealMatrix R = ( ( Ar.add(Br) ).subtract( Cr.scalarMultiply(2) ) ).transpose();
    
    /* (R ^ -1) * b */
    avg_TCP = avg_TCP.add( (new SingularValueDecomposition(R)).getSolver().getInverse().operate(b) );
  }
  
  /* Take the average of the three cases: where C = the first point, the second point, and the third point */
  avg_TCP = avg_TCP.mapMultiply( 1.0 / 3.0 );
  
  if(DISPLAY_TEST_OUTPUT) {
    System.out.printf("(Ar + Br - 2Cr) ^ -1 * (2Ct - At - Bt):\n\n[%5.4f]\n[%5.4f]\n[%5.4f]\n\n", avg_TCP.getEntry(0), avg_TCP.getEntry(1), avg_TCP.getEntry(2));
  }
  
  for(int idx = 0; idx < avg_TCP.getDimension(); ++idx) {
    // Extremely high values may indicate that the given points are invalid
    if(abs((float)avg_TCP.getEntry(idx)) > 1000.0) {
      return null;
    }
  }
  
  return avg_TCP.toArray();
}

/**
 * Creates a 3x3 rotation matrix based off of two vectors defined by the
 * given set of three points, which are defined by the three given PVectors.
 * The three points are used to form two vectors. The first vector is treated
 * as the negative x-axis and the second one is the psuedo-negative z-axis.
 * These vectors are crossed to form the y-axis. The y-axis is then crossed
 * with the negative x-axis to form the true y-axis.
 *
 * @param p1      the origin reference point used to form the negative x-
 *                and z-axes
 * @param p2      the point used to create the preliminary negative x-axis
 * @param p3      the point used to create the preliminary negative z axis
 * @return        a set of three unit vectors that represent an axes (row
 *                major order)
 */
public float[][] createAxesFromThreePoints(PVector p1, PVector p2, PVector p3) {
  float[][] axes = new float[3][];
  float[] x_ndir = new float[3],
  z_ndir = new float[3];
  
  // From preliminary negative x and z axis vectors
  x_ndir[0] = p2.x - p1.x;
  x_ndir[1] = p2.y - p1.y;
  x_ndir[2] = p2.z - p1.z;
  z_ndir[0] = p3.x - p1.x;
  z_ndir[1] = p3.y - p1.y;
  z_ndir[2] = p3.z - p1.z;
  
  // Form axes
  axes[0] = negate(x_ndir);                         // X axis
  axes[1] = crossProduct(axes[0], negate(z_ndir));  // Y axis
  axes[2] = crossProduct(axes[0], axes[1]);         // Z axis
  
  if((axes[0][0] == 0f && axes[0][1] == 0f && axes[0][2] == 0f) ||
     (axes[1][0] == 0f && axes[1][1] == 0f && axes[1][2] == 0f) ||
     (axes[2][0] == 0f && axes[2][1] == 0f && axes[2][2] == 0f)) {
    // One of the three axis vectors is the zero vector
    return null;
  }
  
  float[] magnitudes = new float[axes.length];
  
  for(int v = 0; v < axes.length; ++v) {
    // Find the magnitude of each axis vector
    for(int e = 0; e < axes[0].length; ++e) {
      magnitudes[v] += pow(axes[v][e], 2);
    }
    
    magnitudes[v] = sqrt(magnitudes[v]);
    // Normalize each vector
    for(int e = 0; e < axes.length; ++e) {
      axes[v][e] /= magnitudes[v];
    }
  }
  
  return axes;
}