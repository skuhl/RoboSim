import controlP5.*;
import org.apache.commons.math3.linear.*;

import java.util.*;
import java.nio.*;
import java.nio.file.*;
import java.io.*;
import java.io.Serializable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

final int OFF = 0, ON = 1;

ArmModel armModel;
Model eeModelSuction;
Model eeModelClaw;
Model eeModelClawPincer;

final int ENDEF_NONE = 0, ENDEF_SUCTION = 1, ENDEF_CLAW = 2;

float lastMouseX, lastMouseY;
float cameraTX = 0, cameraTY = 0, cameraTZ = 0;
float cameraRX = 0, cameraRY = 0, cameraRZ = 0;
boolean spacebarDown = false;

ControlP5 cp5;
Textarea myTextarea;
Accordion accordion;

ArrayList<Program> programs = new ArrayList<Program>();

/* global variables for toolbar */

// for pan button
int cursorMode = ARROW;
int clickPan = 0;
float panX = 1.0; 
float panY = 1.0;
boolean doPan = false;

// for rotate button
int clickRotate = 0;
float myRotX = 0.0;
float myRotY = 0.0;
boolean doRotate = false;

float myscale = 0.5;

/*******************************/
/* other global variables      */

// for Execution
Program currentProgram;
boolean execSingleInst = false;
MotionInstruction singleInstruction = null;
int currentInstruction;
int EXEC_PROCESSING = 0, EXEC_FAILURE = 1, EXEC_SUCCESS = 2;

/*******************************/

/*******************************/
/*        Shape Stuff          */

// The Y corrdinate of the ground plane
public static final float PLANE_Z = 200.5f;
public Object[] objects;

/*******************************/

// for store or load program state
FileInputStream in = null;
FileOutputStream out = null;

public void setup(){
  size(1200, 800, P3D);
  ortho();
  
  cp5 = new ControlP5(this);
  gui();
  for (int n = 0; n < pr.length; n++) pr[n] = new Point();
  armModel = new ArmModel();
  eeModelSuction = new Model("VACUUM_2.STL", color(40));
  eeModelClaw = new Model("GRIPPER.STL", color(40));
  eeModelClawPincer = new Model("GRIPPER_2.STL", color(200,200,0));
  intermediatePositions = new ArrayList<PVector>();
  loadState();
  
  /*for (int n = 0; n < toolFrames.length; n++) {
    toolFrames[n] = new Frame();
    userFrames[n] = new Frame();
  }*/
   
  // Intialize world objects
  objects = new Object[2];
  pushMatrix();
  resetMatrix();
  translate(-100, 100, -350);
  //printHCMatrix(getTransformationMatrix());
  
  objects[0] = new Object(125, 60, 300, color(255, 0, 0), color(255, 0, 255));
 
 translate(-250, 0, 0);
  
  objects[1] = new Object(250, 125, 500, color(255, 0, 255), color(255, 255, 255));
  
  popMatrix();
  //createTestProgram();
}

boolean doneMoving = true;

public void draw(){
  ortho();
  
  //lights();
  directionalLight(255, 255, 255, 1, 1, 0);
  ambientLight(150, 150, 150);

  background(127);
  
  if (!doneMoving){
    doneMoving = executeProgram(currentProgram, armModel, execSingleInst);
  }
  else{
    intermediatePositions.clear();
  }
  
  pushMatrix();
  resetMatrix();
  applyModelRotation(armModel);
  // Keep track of the old coordinate frame of the armModel
  armModel.oldEETMatrix = getTransformationMatrix();
  popMatrix();
  
  armModel.executeLiveMotion(); // respond to manual movement from J button presses
  
  hint(ENABLE_DEPTH_TEST);
  background(255);
  noStroke();
  noFill();
  
  pushMatrix();
   
  applyCamera();

  pushMatrix(); 
  armModel.draw();
  popMatrix();
  
  float[] q = eulerToQuat(armModel.getWPR());
  println("q = " + q[0] + ", " + q[1] + ", " + q[2] + ", " + q[3]);
  //handleWorldObjects();
  
  noLights();
  
  //TESTING CODE: DRAW INTERMEDIATE POINTS
  noStroke();
  pushMatrix();
  if(intermediatePositions != null){
    for(PVector v : intermediatePositions){
      pushMatrix();
      translate(v.x, v.y, v.z);
      sphere(10);
      popMatrix();
    }
  }
  popMatrix(); 
  //TESTING CODE: DRAW END EFFECTOR POSITION
  pushMatrix();
  noFill();
  stroke(0, 0, 0);
  applyModelRotation(armModel);
  //EE position
  sphere(20);
  translate(0, 0, -100);
  stroke(255, 0, 0);
  //EE x axis
  sphere(10);
  translate(0, 100, 100);
  stroke(0, 255, 0);
  //EE y axis
  sphere(10);
  translate(100, -100, 0);
  stroke(0, 0, 255);
  //EE z axis
  sphere(10);
  popMatrix();
  //END TESTING CODE
  // TESTING CODE: DRAW USER FRAME 0
  /*PVector ufo = convertWorldToNative(userFrames[0].getOrigin());
  
  PVector ufx = new PVector(
      ufo.x-userFrames[0].getAxis(0).x*80,
      ufo.y-userFrames[0].getAxis(0).y*80,
      ufo.z-userFrames[0].getAxis(0).z*80
    );
  PVector ufy = new PVector(
      ufo.x-userFrames[0].getAxis(2).x*80,
      ufo.y-userFrames[0].getAxis(2).y*80,
      ufo.z-userFrames[0].getAxis(2).z*80
    );
  PVector ufz = new PVector(
      ufo.x+userFrames[0].getAxis(1).x*80,
      ufo.y+userFrames[0].getAxis(1).y*80,
      ufo.z+userFrames[0].getAxis(1).z*80
    );
  noFill();
  stroke(255, 0, 0);
  pushMatrix();
  translate(ufo.x, ufo.y, ufo.z);
  sphere(15);
  popMatrix();
  stroke(0, 255, 0);
  pushMatrix();
  translate(ufx.x, ufx.y, ufx.z);
  sphere(15);
  popMatrix();
  stroke(0, 0, 255);
  pushMatrix();
  translate(ufy.x, ufy.y, ufy.z);
  sphere(15);
  popMatrix();
  stroke(255, 255, 0);
  pushMatrix();
  translate(ufz.x, ufz.y, ufz.z);
  sphere(15);
  popMatrix();*/
  // END TESTING CODE
  
  drawEndEffectorGridMapping();
  
  stroke(255, 0, 0);
  // Draw x origin line
  line( -5000, PLANE_Z, 0, 5000, PLANE_Z, 0 );
  // Draw y origin line
  line( 0, PLANE_Z, 5000, 0, PLANE_Z, -5000 );
  
  // Draw grid lines every 100 units, from -5000 to 5000, in the x and y plane, on the floor plane
  stroke(25, 25, 25);
  for (int l = 1; l < 50; ++l) {
    line(100 * l, PLANE_Z, -5000, 100 * l, PLANE_Z, 5000);
    line(-5000, PLANE_Z, 100 * l, 5000, PLANE_Z, 100 * l);
    
    line(-100 * l, PLANE_Z, -5000, -100 * l, PLANE_Z, 5000);
    line(-5000, PLANE_Z, -100 * l, 5000, PLANE_Z, -100 * l);
  }
  
  popMatrix();
  
  hint(DISABLE_DEPTH_TEST);
  
  showMainDisplayText();
  //println(frameRate + " fps");
}

void applyCamera() {
  translate(width/1.5,height/1.5);
  translate(panX, panY); // for pan button
  scale(myscale);
  rotateX(myRotX); // for rotate button
  rotateY(myRotY); // for rotate button /* */
}

/* Handles the drawing of world objects as well as collision detection of world objects and the
 * Robot Arm model. */
public void handleWorldObjects() {
  for (Object o : objects) {
    // reset all world the object's hit box colors
    o.hit_box.outline = color(0, 255, 0);
  }
  
  for (int idx = 0; idx < objects.length; ++idx) {
    
    /* Update the transformation matrix of an object held by the Robotic Arm */
    if (objects[idx] == armModel.held && armModel.modelInMotion()) {
      pushMatrix();
      resetMatrix();
      
      // new object transform = EE transform x (old EE transform) ^ -1 x current object transform
      
      applyModelRotation(armModel);
      
      float[][] invEETMatrix = invertHCMatrix(armModel.oldEETMatrix);
      applyMatrix(invEETMatrix[0][0], invEETMatrix[0][1], invEETMatrix[0][2], invEETMatrix[0][3],
                  invEETMatrix[1][0], invEETMatrix[1][1], invEETMatrix[1][2], invEETMatrix[1][3],
                  invEETMatrix[2][0], invEETMatrix[2][1], invEETMatrix[2][2], invEETMatrix[2][3],
                  invEETMatrix[3][0], invEETMatrix[3][1], invEETMatrix[3][2], invEETMatrix[3][3]);
      
      armModel.held.form.applyTransform();
       
      float[][] newObjTMatrix = getTransformationMatrix();
      armModel.held.form.setTransform(newObjTMatrix);
      armModel.held.hit_box.setTransform(newObjTMatrix);
      
      popMatrix();
    }
    
    /* Collision Detection */
    if (COLLISION_DISPLAY) {
      if ( armModel.checkObjectCollision(objects[idx]) ) {
        objects[idx].hit_box.outline = color(255, 0, 0);
      }
        
      // Detect collision with other objects
      for (int cdx = idx + 1; cdx < objects.length; ++cdx) {
        
        if (objects[idx].collision(objects[cdx])) {
          // Change hit box color to indicate Object collision
          objects[idx].hit_box.outline = color(255, 0, 0);
          objects[cdx].hit_box.outline = color(255, 0, 0);
          break;
        }
      }
      
      if ( objects[idx] != armModel.held && objects[idx].collision(armModel.getEEPos()) ) {
        // Change hit box color to indicate End Effector collision
        objects[idx].hit_box.outline = color(0, 0, 255);
      }
    }
    
    // Draw world object
    objects[idx].draw();
  }
}