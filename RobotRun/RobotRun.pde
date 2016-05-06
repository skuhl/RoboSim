import controlP5.*;

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
//CubeModel cube;
Model eeModelSuction;
Model eeModelClaw;
Model eeModelClawPincer;

final int ENDEF_NONE = 0, ENDEF_SUCTION = 1, ENDEF_CLAW = 2;
int activeEndEffector = ENDEF_NONE;
int endEffectorStatus = OFF;

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
MotionInstruction singleInstruction = null;
int currentInstruction;
int EXEC_PROCESSING = 0, EXEC_FAILURE = 1, EXEC_SUCCESS = 2;

/*******************************/

/*******************************/
/*        Shape Stuff          */

/* The Y corrdinate of the ground plane */
public static final float PLANE_Y = 200.5f;
private Shape floor;

public Object[] objects;

/*******************************/

// for store or load program state
FileInputStream in = null;
FileOutputStream out = null;

public void setup() {
  size(1200, 800, P3D);
  cp5 = new ControlP5(this);
  gui();
  for (int n = 0; n < pr.length; n++) pr[n] = new Point();
  armModel = new ArmModel(ARM_STANDARD);
  eeModelSuction = new Model("VACUUM_2.STL", color(40));
  eeModelClaw = new Model("GRIPPER.STL", color(40));
  eeModelClawPincer = new Model("GRIPPER_2.STL", color(200,200,0));
  intermediatePositions = new ArrayList<PVector>();
  int loadit = loadState();
  for (int n = 0; n < toolFrames.length; n++) {
    toolFrames[n] = new Frame();
    userFrames[n] = new Frame();
  }
  
  // Create the floor of the environment
  floor = new Polygon(new PVector[] { new PVector(-1000000, 200.5, -10000000), new PVector(-1000000, 200.5, 10000000), new PVector(10000000, 200.5, 10000000), new PVector(10000000, 200.5, -10000000) },
                                          color(160, 160, 160), color(160, 160, 160));
  // Intialize world objects
  Shape box = new Box(new PVector(0, -200, 0), 35, color(0, 0, 255), color(0, 0, 0));
  objects = new Object[1];
  objects[0] = new Object(box, new Box(new PVector(0, -200, 0), 35, color(0, 255, 0)));
}

boolean doneMoving = true;

public void draw() {
  
  //lights();
  directionalLight(255, 255, 255, 1, 1, 0);
  ambientLight(150, 150, 150);

  background(127);
  
  if (!doneMoving) doneMoving = executeProgram(currentProgram, armModel);
  else if (singleInstruction != null) {
    if (executeSingleInstruction(singleInstruction)) singleInstruction = null;    
  }
  
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

  noLights();
  
  // TESTING CODE: DRAW INTERMEDIATE POINTS
  /*noStroke();
  pushMatrix();
  if (intermediatePositions != null) {
    for (PVector v : intermediatePositions) {
      pushMatrix();
      translate(v.x, v.y, v.z);
      sphere(10);
      popMatrix();
    }
  }
  popMatrix(); /* */
  // TESTING CODE: DRAW END EFFECTOR POSITION
  /*pushMatrix();
  //applyCamera();
  noFill();
  stroke(255, 0, 0);
  applyModelRotation(armModel);
  sphere(50);
  translate(0, 0, -400);
  stroke(0, 255, 0);
  sphere(50);
  popMatrix(); /* */
  // END TESTING CODE
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
  popMatrix(); /* */
  // END TESTING CODE
  
  // Create ground plane under the robot's base
  floor.draw();
  
  for (Object s : objects) {
    s.draw();
    s.applyGravity();
  }
  
  popMatrix();

  hint(DISABLE_DEPTH_TEST);
  
  showMainDisplayText();
//  println(frameRate + " fps");
}

void applyCamera() {
  translate(width/1.5,height/1.5);
  translate(panX, panY); // for pan button
  scale(myscale);
  rotateX(myRotX); // for rotate button
  rotateY(myRotY); // for rotate button /* */
}