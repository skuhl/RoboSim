import controlP5.*;
import org.apache.commons.math3.linear.*;

import java.util.*;
import java.nio.*;
import java.nio.file.*;
import java.io.*;
import javax.swing.tree.TreeModel;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

private static final int OFF = 0, ON = 1;

// Determines what End Effector mapping should be display
public static int EE_MAPPING = 2;

ArmModel armModel;
Model eeModelSuction;
Model eeModelClaw;
Model eeModelClawPincer;

float lastMouseX, lastMouseY;
float cameraTX = 0, cameraTY = 0, cameraTZ = 0;
float cameraRX = 0, cameraRY = 0, cameraRZ = 0;
boolean spacebarDown = false;

ControlP5 cp5;
Stack<Screen> display_stack;

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
int EXEC_SUCCESS = 0, EXEC_FAILURE = 1, EXEC_PARTIAL = 2;

/*******************************/

/*******************************/
/*        Shape Stuff          */

// The Y corrdinate of the ground plane
public static final float PLANE_Z = 200.5f;
public WorldObject[] objects;

/*******************************/

// for store or load program state
FileInputStream in = null;
FileOutputStream out = null;

public void setup() {
  //size(1200, 800, P3D);
  size(1080, 720, P3D);
  ortho();
  
  //load model and save data
  armModel = new ArmModel();
  eeModelSuction = new Model("VACUUM_2.STL", color(40));
  eeModelClaw = new Model("GRIPPER.STL", color(40));
  eeModelClawPincer = new Model("GRIPPER_2.STL", color(200,200,0));
  intermediatePositions = new ArrayList<Point>();
  loadState();
  
  //set up UI
  cp5 = new ControlP5(this);
  display_stack = new Stack<Screen>();
  gui();
  
  // Intialize world objects
  objects = new WorldObject[2];
  pushMatrix();
  resetMatrix();
  
  translate(-100, 100, -350);
  objects[0] = new WorldObject(125, 60, 300, color(255, 0, 0), color(255, 0, 255));

  translate(-250, 0, 0);
  objects[1] = new WorldObject(250, 125, 500, color(255, 0, 255), color(255, 255, 255));
  
  popMatrix();
  //createTestProgram();
  
  /**
  PVector TCP = new PVector(-5, -5, -100);
  PVector nativeTCP = transform(TCP, new PVector(0f, 0f, 0f), nativeRobotPosition(armModel.getJointAngles()).orientation);
  System.out.printf("%s -> %s\n", TCP, nativeTCP);
  /**
  PVector v = new PVector(12f, -11f, 100f);
  PVector worldV = convertNativeToWorld(v);
  PVector nativeV = convertWorldToNative(v);
  System.out.printf("%s -> %s\n%s -> %s\n", v, worldV, v, nativeV);
  /**/
}

public void draw() {
  ortho();
  
  //lights();
  directionalLight(255, 255, 255, 1, 1, 0);
  ambientLight(150, 150, 150);

  background(127);
  
  pushMatrix();
  resetMatrix();
  applyModelRotation(armModel.getJointAngles());
  // Keep track of the old coordinate frame of the armModel
  armModel.oldEETMatrix = getTransformationMatrix();
  popMatrix();
  
  //execute arm movement
  if(armModel.inMotion) {
    /* If the current instruction is -2, then the Robot's motion is the product of
     * neither jogging or program execution. */
    if (currentInstruction != -2) {
      //run program
      armModel.inMotion = !executeProgram(currentProgram, armModel, execSingleInst);
    } else {
      armModel.inMotion = !armModel.interpolateRotation(liveSpeed / 100f);
    }
  }
  else {
    //respond to manual movement from J button presses
    intermediatePositions.clear();
    armModel.executeLiveMotion();
  }
  
  hint(ENABLE_DEPTH_TEST);
  background(255);
  noStroke();
  noFill();
  
  pushMatrix();
  
  applyCamera();

  pushMatrix(); 
  armModel.draw();
  popMatrix();
  
  if(COLLISION_DISPLAY) {
    armModel.resetBoxColors();
    armModel.checkSelfCollisions();
  }
  
  handleWorldObjects();
  
  if(COLLISION_DISPLAY) { armModel.drawBoxes(); }
  
  /*float[] q = eulerToQuat(armModel.getWPR());
  println(String.format("q = %4.3f, %4.3f, %4.3f, %4.3f", q[0], q[1], q[2], q[3]));*/
  
  noLights();
  
  //TESTING CODE: DRAW INTERMEDIATE POINTS
  //noStroke();
  //pushMatrix();
  //if(intermediatePositions != null) {
  //  int count = 0;
  //  for(Point p : intermediatePositions) {
  //    if(count % 8 == 0) {
  //      pushMatrix();
  //      translate(p.pos.x, p.pos.y, p.pos.z);
  //      sphere(10);
  //      popMatrix();
  //    }
  //    count += 1;
  //  }
  //}
  //popMatrix(); 
  //TESTING CODE: DRAW END EFFECTOR POSITION
  //pushMatrix();
  //noFill();
  //applyModelRotation(armModel.getJointAngles());
  ////EE position
  //stroke(255, 255, 0);
  //sphere(5);
  //translate(0, 0, -100);
  //stroke(30, 0, 150);
  ////EE z axis
  //sphere(6);
  //translate(0, 100, 100);
  //stroke(0, 255, 0);
  ////EE y axis
  //sphere(6);
  //translate(100, -100, 0);
  //stroke(255, 0, 0);
  ////EE x axis
  //sphere(6);
  //popMatrix();
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
  
  /* Draw a point in space */
  if(ref_point != null) {
    pushMatrix();
    translate(ref_point.x, ref_point.y, ref_point.z);
    
    noFill();
    stroke(0, 150, 200);
    sphere(5);
    
    popMatrix();
  }
  
  /*stroke(255, 0, 0);
  // Draw x origin line
  line( -5000, PLANE_Z, 0, 5000, PLANE_Z, 0 );
  // Draw y origin line
  line( 0, PLANE_Z, 5000, 0, PLANE_Z, -5000 );
  
  // Draw grid lines every 100 units, from -5000 to 5000, in the x and y plane, on the floor plane
  stroke(25, 25, 25);
  for(int l = 1; l < 50; ++l) {
    line(100 * l, PLANE_Z, -5000, 100 * l, PLANE_Z, 5000);
    line(-5000, PLANE_Z, 100 * l, 5000, PLANE_Z, 100 * l);
    
    line(-100 * l, PLANE_Z, -5000, -100 * l, PLANE_Z, 5000);
    line(-5000, PLANE_Z, -100 * l, 5000, PLANE_Z, -100 * l);
  }
  
  drawEndEffectorGridMapping();*/
  displayFrameAxes();
  displayTeachPoints();
  
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
  rotateY(myRotY); // for rotate button
}

/**
 * Handles the drawing of world objects as well as collision detection of world objects and the
 * Robot Arm model.
 */
public void handleWorldObjects() {
  for(WorldObject o : objects) {
    // reset all world the object's hit box colors
    o.hit_box.outline = color(0, 255, 0);
  }
  
  for(int idx = 0; idx < objects.length; ++idx) {
    
    /* Update the transformation matrix of an object held by the Robotic Arm */
    if(objects[idx] == armModel.held && armModel.modelInMotion()) {
      pushMatrix();
      resetMatrix();
      
      // new object transform = EE transform x (old EE transform) ^ -1 x current object transform
      
      applyModelRotation(armModel.getJointAngles());
      
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
    if(COLLISION_DISPLAY) {
      if( armModel.checkObjectCollision(objects[idx]) ) {
        objects[idx].hit_box.outline = color(255, 0, 0);
      }
      
      // Detect collision with other objects
      for(int cdx = idx + 1; cdx < objects.length; ++cdx) {
        
        if(objects[idx].collision(objects[cdx])) {
          // Change hit box color to indicate Object collision
          objects[idx].hit_box.outline = color(255, 0, 0);
          objects[cdx].hit_box.outline = color(255, 0, 0);
          break;
        }
      }
      
      if( objects[idx] != armModel.held && objects[idx].collision(armModel.nativeEEPos().position) ) {
        // Change hit box color to indicate End Effector collision
        objects[idx].hit_box.outline = color(0, 0, 255);
      }
    }
    
    // Draw world object
    objects[idx].draw();
  }
}

/**
 * Display any currently taught points during the processes of either the 3-Point, 4-Point, or 6-Point Methods.
 */
public void displayTeachPoints() {
  // Teach points are displayed only while the Robot is being taught a frame
  if(teachFrame != null && mode.getType() == ScreenType.TYPE_TEACH_POINTS) {
    
    int size = 3;

    if (mode == Screen.TEACH_6PT && teachFrame instanceof ToolFrame) {
      size = 6;
    } else if (mode == Screen.TEACH_4PT && teachFrame instanceof UserFrame) {
      size = 4;
    }
    
    for (int idx = 0; idx < size; ++idx) {
      Point pt = teachFrame.getPoint(idx);
      
      if (pt != null) {
        pushMatrix();
        // Applies the point's position
        translate(pt.position.x, pt.position.y, pt.position.z);
        
        // Draw color-coded sphere for the point
        noFill();
        color pointColor = color(255, 0, 255);
        
        if (teachFrame instanceof ToolFrame) {
          
          if (idx < 3) {
            // TCP teach points
            pointColor = color(130, 130, 130);
          } else if (idx == 3) {
            // Orient origin point
            pointColor = color(255, 130, 0);
          } else if (idx == 4) {
            // Axes X-Direction point
            pointColor = color(255, 0, 0);
          } else if (idx == 5) {
            // Axes Y-Diretion point
            pointColor = color(0, 255, 0);
          }
        } else if (teachFrame instanceof UserFrame) {
          
          if (idx == 0) {
            // Orient origin point
            pointColor = color(255, 130, 0);
          } else if (idx == 1) {
            // Axes X-Diretion point
            pointColor = color(255, 0, 0);
          } else if (idx == 2) {
            // Axes Y-Diretion point
            pointColor = color(0, 255, 0);
          } else if (idx == 3) {
            // Axes Origin point
            pointColor = color(0, 0, 255);
          }
        }
        
        stroke(pointColor);
        sphere(3);
        
        popMatrix();
      }
    }
  }
}

/**
 * Displays the current axes and the origin of the current frame of reference.
 */
public void displayFrameAxes() {
  
  if (curCoordFrame != CoordFrame.JOINT) {
    Frame activeTool = getActiveFrame(CoordFrame.TOOL),
          activeUser = getActiveFrame(CoordFrame.USER);
    
    if (curCoordFrame == CoordFrame.TOOL) {
      /* Draw the axes of the active Tool frame at the Robot End Effector */
      displayOriginAxes(activeTool.getNativeAxes(), armModel.nativeEEPos().position, color(255, 0, 255));
    } else {
      /* Draw axes of the Robot's End Effector frame */
      Point ee_point = armModel.nativeEEPos();
      displayOriginAxes(quatToMatrix( ee_point.orientation ), ee_point.position, color(255, 0, 255));
    }
    
    if(curCoordFrame != CoordFrame.WORLD && activeUser != null) {
      /* Draw the axes of the active User frame */
      displayOriginAxes(activeUser.getNativeAxes(), activeUser.getOrigin(), color(0));
    } else {
      /* Draw the axes of the World frame */
      displayOriginAxes(new float[][] { {1f, 0f, 0f}, {0f, 1f, 0f}, {0f, 0f, 1f} }, new PVector(0f, 0f, 0f), color(0));
    }
  }
}

/**
 * Given a set of 3 orthogonal unit vectors a point in space, lines are
 * drawn for each of the three vectors, which intersect at the origin point.
 *
 * @param axesVectors  A set of three orthogonal unti vectors
 * @param origin       A point in space representing the intersection of the
 *                     three unit vectors
 */
public void displayOriginAxes(float[][] axesVectors, PVector origin, color originColor) {
  
  pushMatrix();    
  // Transform to the reference frame defined by the axes vectors
  applyMatrix(axesVectors[0][0], axesVectors[1][0], axesVectors[2][0], origin.x,
              axesVectors[0][1], axesVectors[1][1], axesVectors[2][1], origin.y,
              axesVectors[0][2], axesVectors[1][2], axesVectors[2][2], origin.z,
              0, 0, 0, 1);
  
  /*applyMatrix(WORLD_AXES[0][0], WORLD_AXES[1][0], WORLD_AXES[2][0], 0,
                WORLD_AXES[0][1], WORLD_AXES[1][1], WORLD_AXES[2][1], 0,
                WORLD_AXES[0][2], WORLD_AXES[1][2], WORLD_AXES[2][2], 0,
                0, 0, 0, 1);*/
  
  // X axis
  stroke(255, 0, 0);
  line(-5000, 0, 0, 5000, 0, 0);
  // Y axis
  stroke(0, 255, 0);
  line(0, -5000, 0, 0, 5000, 0);
  // Z axis
  stroke(0, 0, 255);
  line(0, 0, -5000, 0, 0, 5000);
  
  // Draw a sphere on the positive direction for each axis
  stroke(originColor);
  sphere(4);
  stroke(0);
  translate(50, 0, 0);
  sphere(4);
  translate(-50, 50, 0);
  sphere(4);
  translate(0, -50, 50);
  sphere(4);
  
  popMatrix();
}