import java.util.concurrent.ArrayBlockingQueue;

//To-do list:
//Collision detection

public class Triangle {
  // normal, vertex 1, vertex 2, vertex 3
  public PVector[] components = new PVector[4];
}

public class Model {
  public PShape mesh;
  public String name;
  public boolean[] rotations = new boolean[3]; // is rotating on this joint valid?
  // Joint ranges follow a clockwise format running from the PVector.x to PVector.y, where PVector.x and PVector.y range from [0, TWO_PI]
  public PVector[] jointRanges = new PVector[3];
  public float[] currentRotations = new float[3]; // current rotation value
  public float[] targetRotations = new float[3]; // we want to be rotated to this value
  public int[] rotationDirections = new int[3]; // control rotation direction so we
                                                // don't "take the long way around"
  public float rotationSpeed;
  public float[] jointsMoving = new float[3]; // for live control using the joint buttons
  
  public Model(String filename, color col) {
    for (int n = 0; n < 3; n++) {
      rotations[n] = false;
      currentRotations[n] = 0;
      jointRanges[n] = null;
    }
    rotationSpeed = 0.01;
    name = filename;
    loadSTLModel(filename, col);
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
    
    if (jointRanges[idx].x < jointRanges[idx].y) {
      // Joint range does not overlap TWO_PI
      return angle >= jointRanges[idx].x && angle < jointRanges[idx].y;
    } else {
      // Joint range overlaps TWO_PI
      return !(angle >= jointRanges[idx].y && angle < jointRanges[idx].x);
    }
  }
  
  public void draw() {
    shape(mesh);
  }
  
} // end Model class

// The apporximate center of the base of the robot
public static final PVector base_center = new PVector(404, 137, -212);

public class ArmModel {
  
  public ArrayList<Model> segments = new ArrayList<Model>();
  public int type;
  //public boolean calculatingArms = false, movingArms = false;
  public float motorSpeed;
  // Indicates translational motion in the World Frame
  public float[] linearMoveSpeeds = new float[3];
  // Indicates rotational motion in the World Frame
  public float[] angularMoveSpeeds = new float[3];
  //public final float[] maxArmRange;
  
  public Object held;
  
  public ArmModel() {
    
    motorSpeed = 4000.0; // speed in mm/sec
    // Joint 1
    Model base = new Model("ROBOT_MODEL_1_BASE.STL", color(200, 200, 0));
    base.rotations[1] = true;
    base.jointRanges[1] = new PVector(0, TWO_PI);
    base.rotationSpeed = radians(350)/60.0;
    // Joint 2
    Model axis1 = new Model("ROBOT_MODEL_1_AXIS1.STL", color(40, 40, 40));
    axis1.rotations[2] = true;
    //axis1.jointRanges[2].add(new PVector(0, 2.01));
    //axis1.jointRanges[2].add(new PVector(4.34, TWO_PI));
    axis1.jointRanges[2] = new PVector(4.34, 2.01);
    axis1.rotationSpeed = radians(350)/60.0;
    // Joint 3
    Model axis2 = new Model("ROBOT_MODEL_1_AXIS2.STL", color(200, 200, 0));
    axis2.rotations[2] = true;
    //axis2.jointRanges[2].add(new PVector(0, 8f * PI / 20f));
    //axis2.jointRanges[2].add(new PVector(12f * PI / 20f, TWO_PI));
    axis2.jointRanges[2] = new PVector(12f * PI / 20f, 8f * PI / 20f);
    axis2.rotationSpeed = radians(400)/60.0;
    // Joint 4
    Model axis3 = new Model("ROBOT_MODEL_1_AXIS3.STL", color(40, 40, 40));
    axis3.rotations[0] = true;
    axis3.jointRanges[0] = new PVector(0, TWO_PI);
    axis3.rotationSpeed = radians(450)/60.0;
    // Joint 5
    Model axis4 = new Model("ROBOT_MODEL_1_AXIS4.STL", color(40, 40, 40));
    axis4.rotations[2] = true;
    //axis4.jointRanges[2].add(new PVector(0, 11f * PI / 20f));
    //axis4.jointRanges[2].add(new PVector(59f * PI / 40f, TWO_PI));
    axis4.jointRanges[2] = new PVector(59f * PI / 40f, 11f * PI / 20f);
    axis4.rotationSpeed = radians(450)/60.0;
    // Joint 6
    Model axis5 = new Model("ROBOT_MODEL_1_AXIS5.STL", color(200, 200, 0));
    axis5.rotations[0] = true;
    axis5.jointRanges[0] = new PVector(0, TWO_PI);
    axis5.rotationSpeed = radians(720)/60.0;
    Model axis6 = new Model("ROBOT_MODEL_1_AXIS6.STL", color(40, 40, 40));
    segments.add(base);
    segments.add(axis1);
    segments.add(axis2);
    segments.add(axis3);
    segments.add(axis4);
    segments.add(axis5);
    segments.add(axis6);
    
    for (int idx = 0; idx < angularMoveSpeeds.length; ++idx) {
      angularMoveSpeeds[idx] = 0;
    }
    
    for (int idx = 0; idx < angularMoveSpeeds.length; ++idx) {
      angularMoveSpeeds[idx] = 0;
    }
    
    held = null;
    
  } // end ArmModel constructor
  
  public void draw() {
    
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
  }//end draw arm model
  
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
  }//end get WPR
  
  //returns the rotational values for each arm joint
  public float[] getJointRotations() {
    float[] rot = new float[6];
    for (int i = 0; i < segments.size(); i += 1) {
      for (int j = 0; j < 3; j += 1) {
        if (segments.get(i).rotations[j]) {
          rot[i] = segments.get(i).currentRotations[j];
          break;
        }
      }
    }
    return rot;
  }//end get joint rotations
  
  //convenience method to set all joint rotation values of the robot arm
  public void setJointRotations(float[] rot){
    for(int i = 0; i < segments.size(); i += 1){
      for(int j = 0; j < 3; j += 1){
        if(segments.get(i).rotations[j]){
          segments.get(i).currentRotations[j] = rot[i];
        }
      }
    }
  }//end set joint rotations
  
  public boolean interpolateRotation(float speed) {
    boolean done = true;
    for (Model a : segments){
      for (int r = 0; r < 3; r++){
        if (a.rotations[r]){
          if (abs(a.currentRotations[r] - a.targetRotations[r]) > a.rotationSpeed*speed){
            done = false;
            a.currentRotations[r] += a.rotationSpeed * a.rotationDirections[r] * speed;
            a.currentRotations[r] = clampAngle(a.currentRotations[r]);
          }
        }
      } // end loop through rotation axes
    } // end loop through arm segments
    return done;
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
      for (int i = 0; i < segments.size(); i += 1) {
        Model model = segments.get(i);
        
        for (int n = 0; n < 3; n++) {
          if (model.rotations[n]) {
            float trialAngle = model.currentRotations[n] +
              model.rotationSpeed * model.jointsMoving[n] * liveSpeed;
              trialAngle = clampAngle(trialAngle);
            
            if (model.anglePermitted(n, trialAngle)) {
              // Caculate the distance that the end effector is from the center of the robot's base
              PVector ee_pos = calculateEndEffectorPosition(armModel, armModel.getJointRotations());
              // This is not the exact center, it is a rough estimate 
              float dist = PVector.dist(ee_pos, base_center);
              
              /* If the End Effector is within a certain distance from the robot's base,
               * then determine if the given angle will bring the robot closer to the
               * base; if so then end the robot's movement, otherwise allow the robot to
               * continue moving. */
              if (dist < 405f) {
                
                float old_angle = model.currentRotations[n];
                model.currentRotations[n] = trialAngle;
                
                // Caculate the distance that the end effector is from the center of the robot's base for the test angle
                PVector new_ee_pos = calculateEndEffectorPosition(armModel, armModel.getJointRotations());
                float new_dist = PVector.dist(new_ee_pos, base_center);
                
                if (new_dist < dist) {
                  // end robot arm movement
                  model.currentRotations[n] = old_angle;
                  model.jointsMoving[n] = 0;
                }
              } 
              else {
                model.currentRotations[n] = trialAngle;
              }  
            } 
            else {
              model.jointsMoving[n] = 0;
            }
          }
        }
      }
      updateButtonColors();
    } else if (curCoordFrame == COORD_WORLD) {
      //only move if our movement vector is non-zero
      if (linearMoveSpeeds[0] != 0 || linearMoveSpeeds[1] != 0 || linearMoveSpeeds[2] != 0) {
        PVector startFrom = new PVector(0,0,0);
        
        //determine movement start point
        if (intermediatePositions.size() == 1){
          startFrom = intermediatePositions.get(0);
        } else{
          startFrom = calculateEndEffectorPosition(armModel, armModel.getJointRotations());
        }
        
        PVector move = new PVector(linearMoveSpeeds[0], linearMoveSpeeds[1], linearMoveSpeeds[2]);
        //convert to user frame coordinates if currently in a user frame
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
        
        int r = calculateIKJacobian(intermediatePositions.get(0));
        if(r == EXEC_FAILURE){
          intermediatePositions.clear();
          updateButtonColors();
          linearMoveSpeeds[0] = 0;
          linearMoveSpeeds[1] = 0;
          linearMoveSpeeds[2] = 0;
        }
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