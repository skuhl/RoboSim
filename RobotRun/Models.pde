//To-do list:
//Collision detection

public class Triangle {
  // normal, vertex 1, vertex 2, vertex 3
  public PVector[] components = new PVector[4];
}

public class Model {
  public PShape mesh;
  public boolean[] rotations = new boolean[3]; // is rotating on this joint valid?
  public ArrayList<PVector>[] jointRanges = (ArrayList<PVector>[])new ArrayList[3];
  public float[] currentRotations = new float[3]; // current rotation value
  public float[] testRotations = new float[3]; // used while performing IK
  public float[] targetRotations = new float[3]; // we want to be rotated to this value
  public int[] rotationDirections = new int[3]; // control rotation direction so we
                                                // don't "take the long way around"
  public float rotationSpeed;
  public float[] jointsMoving = new float[3]; // for live control using the joint buttons
  
  public Model(String filename, color col) {
    for (int n = 0; n < 3; n++) {
      rotations[n] = false;
      currentRotations[n] = 0;
      jointRanges[n] = new ArrayList<PVector>();
    }
    rotationSpeed = 0.01;
    loadSTLModel(filename, col);
  }
  
  void resetRot(){
    currentRotations[0] = 0;
    currentRotations[1] = 0;
    currentRotations[2] = 0;
  }
  
  void loadSTLModel(String filename, color col) {
    ArrayList<Triangle> triangles = new ArrayList<Triangle>();
    byte[] data = loadBytes(filename);
    int n = 84; // skip header and number of triangles
  
    while (n < data.length) {
      Triangle t = new Triangle();
      for (int m = 0; m < 4; m++) {
        byte[] bytesX = new byte[4];
        bytesX[0] = data[n+3]; bytesX[1] = data[n+2];
        bytesX[2] = data[n+1]; bytesX[3] = data[n];
        n += 4;
        byte[] bytesY = new byte[4];
        bytesY[0] = data[n+3]; bytesY[1] = data[n+2];
        bytesY[2] = data[n+1]; bytesY[3] = data[n];
        n += 4;
        byte[] bytesZ = new byte[4];
        bytesZ[0] = data[n+3]; bytesZ[1] = data[n+2];
        bytesZ[2] = data[n+1]; bytesZ[3] = data[n];
        n += 4;
        t.components[m] = new PVector(
          ByteBuffer.wrap(bytesX).getFloat(),
          ByteBuffer.wrap(bytesY).getFloat(),
          ByteBuffer.wrap(bytesZ).getFloat()
        );
      }
      triangles.add(t);
      n += 2; // skip meaningless "attribute byte count"
    }
    mesh = createShape();
    mesh.beginShape(TRIANGLES);
    mesh.noStroke();
    mesh.fill(col);
    for (Triangle t : triangles) {
      mesh.normal(t.components[0].x, t.components[0].y, t.components[0].z);
      mesh.vertex(t.components[1].x, t.components[1].y, t.components[1].z);
      mesh.vertex(t.components[2].x, t.components[2].y, t.components[2].z);
      mesh.vertex(t.components[3].x, t.components[3].y, t.components[3].z);
    }
    mesh.endShape();
  } // end loadSTLModel
  
  
  public boolean anglePermitted(int idx, float angle) {
    for (PVector range : jointRanges[idx]) {
      if (angle >= range.x && angle <= range.y) return true;
    }
    return false;
  }
  
  public void draw() {
    shape(mesh);
  }
} // end Model class

final int ARM_TEST = 0, ARM_STANDARD = 1;


public class ArmModel {
  
  public ArrayList<Model> segments = new ArrayList<Model>();
  public int type;
  //public boolean calculatingArms = false, movingArms = false;
  public float motorSpeed;
  public float[] linearMoveSpeeds = new float[3];
  
  public ArmModel(int in) {
    type = in;
    if (type == ARM_TEST) {
      motorSpeed = 255.0; // speed in mm
      Model base = new Model("Base.STL", color(180, 180, 180));
      base.rotations[1] = true;
      base.jointRanges[1].add(new PVector(Float.MIN_VALUE, Float.MAX_VALUE));
      Model link1 = new Model("Link1.STL", color(180, 180, 180));
      link1.rotations[2] = true;
      link1.jointRanges[2].add(new PVector(0, 2.4));
      link1.jointRanges[2].add(new PVector(3.88, PI*2));
      Model link2 = new Model("Link2.STL", color(180, 180, 180));
      link2.rotations[2] = true;
      link2.jointRanges[2].add(new PVector(0, 2));
      link2.jointRanges[2].add(new PVector(4.28, PI*2));
      Model link3 = new Model("Link3.STL", color(180, 180, 180));
      link3.rotations[0] = true;
      link3.jointRanges[0].add(new PVector(Float.MIN_VALUE, Float.MAX_VALUE));
      segments.add(base);
      segments.add(link1);
      segments.add(link2);
      segments.add(link3);
    } else if (type == ARM_STANDARD) {
      // TODO: FILL IN PROPER JOINT RESTRICTION VALUES.
      motorSpeed = 4000.0; // speed in mm
      // Joint 1
      Model base = new Model("ROBOT_MODEL_1_BASE.STL", color(200, 200, 0));
      base.rotations[1] = true;
      base.jointRanges[1].add(new PVector(Float.MIN_VALUE, Float.MAX_VALUE));
      base.rotationSpeed = radians(350)/60.0;
      // Joint 2
      Model axis1 = new Model("ROBOT_MODEL_1_AXIS1.STL", color(40, 40, 40));
      axis1.rotations[2] = true;
      axis1.jointRanges[2].add(new PVector(0, 2.01));
      axis1.jointRanges[2].add(new PVector(4.34, TWO_PI));
      axis1.rotationSpeed = radians(350)/60.0;
      // Joint 3
      Model axis2 = new Model("ROBOT_MODEL_1_AXIS2.STL", color(200, 200, 0));
      axis2.rotations[2] = true;
      axis2.jointRanges[2].add(new PVector(0, 8f * PI / 20f));
      axis2.jointRanges[2].add(new PVector(12f * PI / 20f, TWO_PI));
      axis2.rotationSpeed = radians(400)/60.0;
      // Joint 4
      Model axis3 = new Model("ROBOT_MODEL_1_AXIS3.STL", color(40, 40, 40));
      axis3.rotations[0] = true;
      axis3.jointRanges[0].add(new PVector(Float.MIN_VALUE, Float.MAX_VALUE));
      axis3.rotationSpeed = radians(450)/60.0;
      // Joint 5
      Model axis4 = new Model("ROBOT_MODEL_1_AXIS4.STL", color(40, 40, 40));
      axis4.rotations[2] = true;
      axis4.jointRanges[2].add(new PVector(0, 11f * PI / 20f));
      axis4.jointRanges[2].add(new PVector(59f * PI / 40f, TWO_PI));
      axis4.rotationSpeed = radians(450)/60.0;
      // Joint 6
      Model axis5 = new Model("ROBOT_MODEL_1_AXIS5.STL", color(200, 200, 0));
      axis5.rotations[0] = true;
      axis5.jointRanges[0].add(new PVector(Float.MIN_VALUE, Float.MAX_VALUE));
      axis5.rotationSpeed = radians(720)/60.0;
      Model axis6 = new Model("ROBOT_MODEL_1_AXIS6.STL", color(40, 40, 40));
      segments.add(base);
      segments.add(axis1);
      segments.add(axis2);
      segments.add(axis3);
      segments.add(axis4);
      segments.add(axis5);
      segments.add(axis6);
    }
  } // end ArmModel constructor
  
  public void draw() {
    if (type == ARM_TEST) {
      noStroke();
      rotateY(segments.get(0).currentRotations[1]);
      segments.get(0).draw();
      translate(0, -200, 0);
      rotateZ(PI);
      //rotateY(PI/2.0);
      segments.get(1).draw();
      rotateZ(-PI);
      translate(-25, -130, 0);
      rotateZ(segments.get(1).currentRotations[2]);
      translate(-25, -130, 0);
      rotateZ(PI);
      segments.get(2).draw();
      rotateZ(-PI);
      translate(0, -120, 0); 
      rotateZ(segments.get(2).currentRotations[2]);
      translate(0, -120, 0);
      rotateZ(PI);
      segments.get(3).draw();
    } else if (type == ARM_STANDARD) {
      noStroke();
      fill(200, 200, 0);
      
      translate(600, 200, 0);

      rotateZ(PI);
      rotateY(PI/2);
      segments.get(0).draw();
      rotateY(-PI/2);
      rotateZ(-PI);
      
      fill(50);
    
      translate(-50, -166, -358); // -115, -213, -413
      rotateZ(PI);
      translate(150, 0, 150);
      rotateY(segments.get(0).currentRotations[1]);
      translate(-150, 0, -150);
      segments.get(1).draw();
      rotateZ(-PI);
    
      fill(200, 200, 0);
    
      translate(-115, -85, 180);
      rotateZ(PI);
      rotateY(PI/2);
      translate(0, 62, 62);
      rotateX(segments.get(1).currentRotations[2]);
      translate(0, -62, -62);
      segments.get(2).draw();
      rotateY(-PI/2);
      rotateZ(-PI);
    
      fill(50);
   
      translate(0, -500, -50);
      rotateZ(PI);
      rotateY(PI/2);
      translate(0, 75, 75);
      rotateX(segments.get(2).currentRotations[2]);
      translate(0, -75, -75);
      segments.get(3).draw();
      rotateY(PI/2);
      rotateZ(-PI);
    
      translate(745, -150, 150);
      rotateZ(PI/2);
      rotateY(PI/2);
      translate(70, 0, 70);
      rotateY(segments.get(3).currentRotations[0]);
      translate(-70, 0, -70);
      segments.get(4).draw();
      rotateY(-PI/2);
      rotateZ(-PI/2);
    
      fill(200, 200, 0);
    
      translate(-115, 130, -124);
      rotateZ(PI);
      rotateY(-PI/2);
      translate(0, 50, 50);
      rotateX(segments.get(4).currentRotations[2]);
      translate(0, -50, -50);
      segments.get(5).draw();
      rotateY(PI/2);
      rotateZ(-PI);
    
      fill(50);
    
      translate(150, -10, 95);
      rotateY(-PI/2);
      rotateZ(PI);
      translate(45, 45, 0);
      rotateZ(segments.get(5).currentRotations[0]);
      translate(-45, -45, 0);
      segments.get(6).draw();
            
      // next, the end effector
      if (activeEndEffector == ENDEF_SUCTION) {
        rotateY(PI);
        translate(-88, -37, 0);
        eeModelSuction.draw();
      } else if (activeEndEffector == ENDEF_CLAW) {
        rotateY(PI);
        translate(-88, 0, 0);
        eeModelClaw.draw();
        rotateZ(PI/2);
        if (endEffectorStatus == OFF) {
          translate(10, -85, 30);
          eeModelClawPincer.draw();
          translate(55, 0, 0);
          eeModelClawPincer.draw();
        } else if (endEffectorStatus == ON) {
          translate(28, -85, 30);
          eeModelClawPincer.draw();
          translate(20, 0, 0);
          eeModelClawPincer.draw();
        }
      }
    }
  }// end draw arm model
  
  public void resetRot(){
    for(Model m: segments){
      m.resetRot();
    }
  }
  
  public PVector getWpr() {
    PVector out = new PVector(0,0,0);
    PVector tmp = new PVector(0,0,0);
    for (Model a : segments) {
      tmp.x += a.currentRotations[0];
      tmp.y += a.currentRotations[1];
      tmp.z += a.currentRotations[2];
    }
    out.x = clampAngle(tmp.y);
    out.y = clampAngle(tmp.z);
    out.z = clampAngle(tmp.x);
    return out;
  } // end get WPR
  
  public float[] getJointRotations() {
    float[] j = new float[6];
    for (int n = 0; n < 6; n++) {
      for (int r = 0; r < 3; r++) {
        if (segments.get(n).rotations[r]) {
          j[n] = segments.get(n).currentRotations[r];
          break;
        }
      }
    }
    return j;
  } // end get joint rotations
  
  public boolean interpolateRotation(float speed) {
    boolean allDone = true;
    for (Model a : segments) {
      for (int r = 0; r < 3; r++) {
        if (a.rotations[r]) {
          if (abs(a.currentRotations[r] - a.targetRotations[r]) > a.rotationSpeed*2)
          {
            allDone = false;
            a.currentRotations[r] += a.rotationSpeed * a.rotationDirections[r] * speed;
            a.currentRotations[r] = clampAngle(a.currentRotations[r]);
          }
        }
      } // end loop through rotation axes
    } // end loop through arm segments
    return allDone;
  } // end interpolate rotation
  
  public void instantRotation() {
    for (Model a : segments) {
      for (int r = 0; r < 3; r++) {
        a.currentRotations[r] = a.targetRotations[r];
      }
    }
  }
  
  void executeLiveMotion() {
    
    if (curCoordFrame == COORD_JOINT) {
      
      for (Model model : segments) {
        for (int n = 0; n < 3; n++) {
          if (model.rotations[n]) {
            float trialAngle = model.currentRotations[n] +
              model.rotationSpeed * model.jointsMoving[n] * liveSpeed;
            trialAngle = clampAngle(trialAngle);
            
            if (model.anglePermitted(n, trialAngle)) {
              
              // Caculate the distance that the end effector is from the center of the robot's base
              PVector ee_pos = calculateEndEffectorPosition(armModel, false);
              // This is not the exact center, it is a rough estimate 
              PVector base_center = new PVector(404, 137, -212);
              float dist = PVector.dist(ee_pos, base_center);
              
              
              /* If the End Effector is within a certain distance from the robot's base,
               * then determine if the given angle will bring the robot closer to the
               * base; if so then end the robot's movement, otherwise allow the robot to
               * continue moving. */
              if (dist < 405f) {
                
                float old_angle = model.currentRotations[n];
                model.currentRotations[n] = trialAngle;
                
                // Caculate the distance that the end effector is from the center of the robot's base for the test angle
                PVector new_ee_pos = calculateEndEffectorPosition(armModel, false);
                float new_dist = PVector.dist(new_ee_pos, base_center);
                
                if (new_dist < dist) {
                  // end robot arm movement
                  model.currentRotations[n] = old_angle;
                  model.jointsMoving[n] = 0;
                }
              } else {
                model.currentRotations[n] = trialAngle;
              }  
            } else model.jointsMoving[n] = 0;
          }
        }
      }
    } else if (curCoordFrame == COORD_WORLD) {
      if (linearMoveSpeeds[0] != 0 || linearMoveSpeeds[1] != 0 || linearMoveSpeeds[2] != 0) {
        PVector startFrom = new PVector(0,0,0);
        if (intermediatePositions.size() == 1) startFrom = intermediatePositions.get(0);
        else startFrom = calculateEndEffectorPosition(armModel, false);
        PVector move = new PVector(linearMoveSpeeds[0], linearMoveSpeeds[1], linearMoveSpeeds[2]);
        if (activeUserFrame >= 0 && activeUserFrame < userFrames.length) {
          PVector[] frame = userFrames[activeUserFrame].axes;
          move.y = -move.y;
          move.z = -move.z;
          move = vectorConvertTo(move, frame[0], frame[1], frame[2]);
        }
        intermediatePositions.clear();
        float distance = motorSpeed/60.0 * liveSpeed;
        intermediatePositions.add(new PVector(startFrom.x + move.x * distance,
                                              startFrom.y + move.y * distance,
                                              startFrom.z + move.z * distance));
        attemptIK(this, 0);
        instantRotation();
      }
    }
  } // end execute live motion

} // end ArmModel class

void printCurrentModelCoordinates(String msg) {
  print(msg + ": " );
  print(modelX(0, 0, 0) + " ");
  print(modelY(0, 0, 0) + " ");
  print(modelZ(0, 0, 0));
  println();
}