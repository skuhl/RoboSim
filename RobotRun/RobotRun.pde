import controlP5.*;
import org.apache.commons.math3.linear.*;

import java.util.*;
import java.util.regex.Pattern;
import java.nio.*;
import java.nio.file.*;
import java.io.*;
import java.awt.event.KeyEvent;

private static final int OFF = 0, ON = 1;
PFont fnt_con14, fnt_con12, fnt_conB;

private Camera camera;
// The position at which the Robot is drawn
private final PVector ROBOT_POSITION = new PVector(200, 300, 200);
ArmModel armModel;

ControlP5 cp5;
WindowManager manager;
Stack<Screen> display_stack;

ArrayList<Program> programs = new ArrayList<Program>();

/*******************************/
/*      Global Variables       */
/*******************************/

// for Execution
public boolean execSingleInst = false; 
public boolean motionFault = false; //indicates motion error with the Robot
int EXEC_SUCCESS = 0, EXEC_FAILURE = 1, EXEC_PARTIAL = 2;

/*******************************/
/*      Debugging Stuff        */
/*******************************/

private ArrayList<String> buffer;
private Point displayPoint;


public void setup() {
  //size(1200, 800, P3D);
  size(1080, 720, P3D);
  
  //create font and text display background
  fnt_con14 = createFont("data/Consolas.ttf", 14);
  fnt_con12 = createFont("data/Consolas.ttf", 12);
  fnt_conB = createFont("data/ConsolasBold.ttf", 12);
  
  camera = new Camera();
  
  //load model and save data
  armModel = new ArmModel();
  intermediatePositions = new ArrayList<Point>();
  activeScenario = null;
  showOOBs = true;
  
  loadState();
  
  //set up UI
  cp5 = new ControlP5(this);
  // Expllicitly draw the ControlP5 elements
  cp5.setAutoDraw(false);
  manager = new WindowManager(cp5, fnt_con12, fnt_con14);
  display_stack = new Stack<Screen>();
  gui();
  
  buffer = new ArrayList<String>();
  displayPoint = null;
}

public void draw() {
  // Apply the camera for drawing objects
  directionalLight(255, 255, 255, 1, 1, 0);
  ambientLight(150, 150, 150);

  background(127);
  
  hint(ENABLE_DEPTH_TEST);
  background(255);
  noStroke();
  noFill();
  
  pushMatrix();
  camera.apply();
  
  Program p = activeProgram();
  
  updateAndDrawObjects(activeScenario, p, armModel);
  displayAxes();
  displayTeachPoints();
  
  WorldObject wldObj = manager.getActiveWorldObject();
  
  if (wldObj != null) {
    pushMatrix();
    
    if (wldObj instanceof Part) {
      Fixture reference = ((Part)wldObj).getFixtureRef();
      
      if (reference != null) {
        // Draw part's orientation with reference to its fixture
        reference.applyCoordinateSystem();
      }
    }
    
    displayOriginAxes(wldObj.getLocalCenter(), wldObj.getLocalOrientationAxes(), 500f, color(0));
    
    popMatrix();
  }
  
  if (displayPoint != null) {
    // Display the point with its local orientation axes
    displayOriginAxes(displayPoint.position, displayPoint.orientation.toMatrix(), 100f, color(0, 100, 15));
  }
  
  //TESTING CODE: DRAW INTERMEDIATE POINTS
  noLights();
  noStroke();
  pushMatrix();
  //if(intermediatePositions != null) {
  //  int count = 0;
  //  for(Point pt : intermediatePositions) {
  //    if(count % 20 == 0) {
  //      pushMatrix();
  //      stroke(0);
  //      translate(pt.position.x, pt.position.y, pt.position.z);
  //      sphere(5);
  //      popMatrix();
  //    }
  //    count += 1;
  //  }
  //}
  popMatrix();
  popMatrix();
  
  hint(DISABLE_DEPTH_TEST);
  // Apply the camera for drawing text and windows
  ortho();
  showMainDisplayText();
  cp5.draw();
  //println(frameRate + " fps");
}

/*****************************************************************************************************************
 NOTE: All the below methods assume that current matrix has the camrea applied!
 *****************************************************************************************************************/

/**
 * Updates the position and orientation of the Robot as well as all the World
 * Objects associated with the current scenario. Updates the bounding box color,
 * position and oientation of the Robot and all World Objects as well. Finally,
 * all the World Objects and the Robot are drawn.
 * 
 * @param s       The currently active scenario
 * @param active  The currently selected program
 * @param model   The Robot Arm model
 */
public void updateAndDrawObjects(Scenario s, Program active, ArmModel model) {
  model.updateRobot(active);
  
  if (s != null) {
    s.resetObjectHitBoxColors();
  }
  
  model.resetOBBColors(); 
  model.checkSelfCollisions();
  
  if (s != null) {
    s.updateAndDrawObjects(model);
  }
  model.draw();
  
  model.updatePreviousEEOrientation();
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
 * Displays coordinate frame associated with the current Coordinate frame. The active User frame is displayed in the User and Tool
 * Coordinate Frames. The World frame is display in the World Coordinate frame and the Tool Coordinate Frame in the case that no
 * active User frame is set. The active Tool frame axes are displayed in the Tool frame in addition to the current User (or World)
 * frame. Nothing is displayed in the Joint Coordinate Frame.
 */
public void displayAxes() {
  
  Point eePoint = nativeRobotEEPoint(armModel.getJointAngles());
  
  if (axesState == AxesDisplay.AXES && curCoordFrame == CoordFrame.TOOL) {
    Frame activeTool = getActiveFrame(CoordFrame.TOOL);
    
    // Draw the axes of the active Tool frame at the Robot End Effector
    displayOriginAxes(eePoint.position, activeTool.getWorldAxisVectors(), 200f, color(255, 0, 255));
  } else {
    // Draw axes of the Robot's End Effector frame for testing purposes
    //displayOriginAxes(eePoint.position, eePoint.orientation.toMatrix(), 200f, color(255, 0, 255));
    
    /* Draw a pink point for the Robot's current End Effecot position */
    pushMatrix();
    translate(eePoint.position.x, eePoint.position.y, eePoint.position.z);
    
    stroke(color(255, 0, 255));
    noFill();
    sphere(4);
    
    popMatrix();
  }
  
  if (axesState == AxesDisplay.AXES) {
    // Display axes
    if (curCoordFrame != CoordFrame.JOINT) {
      Frame activeUser = getActiveFrame(CoordFrame.USER);
      
      if(curCoordFrame != CoordFrame.WORLD && activeUser != null) {
        // Draw the axes of the active User frame
        displayOriginAxes(activeUser.getOrigin(), activeUser.getWorldAxisVectors(), 10000f, color(0));
        
      } else {
        // Draw the axes of the World frame
        displayOriginAxes(new PVector(0f, 0f, 0f), WORLD_AXES, 10000f, color(0));
      }
    }
    
  } else if (axesState == AxesDisplay.GRID) {
    // Display gridlines spanning from axes of the current frame
    Frame active;
    float[][] displayAxes;
    PVector displayOrigin;
    
    switch(curCoordFrame) {
      case JOINT:
      case WORLD:
        displayAxes = new float[][] { {1f, 0f, 0f}, {0f, 1f, 0f}, {0f, 0f, 1f} };
        displayOrigin = new PVector(0f, 0f, 0f);
        break;
      case TOOL:
        active = getActiveFrame(CoordFrame.TOOL);
        displayAxes = active.getNativeAxisVectors();
        displayOrigin = eePoint.position;
        break;
      case USER:
        active = getActiveFrame(CoordFrame.USER);
        displayAxes = active.getNativeAxisVectors();
        displayOrigin = active.getOrigin();
        break;
      default:
        // No gridlines are displayed in the Joint Coordinate Frame
        return;
    }
    
    // Draw grid lines every 100 units, from -3500 to 3500, in the x and y plane, on the floor plane
    displayGridlines(displayAxes, displayOrigin, 35, 100);
  }
}

/**
 * Given a set of 3 orthogonal unit vectors a point in space, lines are
 * drawn for each of the three vectors, which intersect at the origin point.
 *
 * @param origin       A point in space representing the intersection of the
 *                     three unit vectors
 * @param axesVectors  A set of three orthogonal unti vectors
 * @param axesLength   The length, to which the all axes, will be drawn
 * @param originColor  The color of the point to draw at the origin
 */
public void displayOriginAxes(PVector origin, float[][] axesVectors, float axesLength, color originColor) {
  
  pushMatrix();    
  // Transform to the reference frame defined by the axes vectors
  applyMatrix(axesVectors[0][0], axesVectors[1][0], axesVectors[2][0], origin.x,
              axesVectors[0][1], axesVectors[1][1], axesVectors[2][1], origin.y,
              axesVectors[0][2], axesVectors[1][2], axesVectors[2][2], origin.z,
              0, 0, 0, 1);
  
  // X axis
  stroke(255, 0, 0);
  line(-axesLength, 0, 0, axesLength, 0, 0);
  // Y axis
  stroke(0, 255, 0);
  line(0, -axesLength, 0, 0, axesLength, 0);
  // Z axis
  stroke(0, 0, 255);
  line(0, 0, -axesLength, 0, 0, axesLength);
  
  // Draw a sphere on the positive direction for each axis
  stroke(originColor);
  sphere(4);
  stroke(0);
  translate(100, 0, 0);
  sphere(4);
  translate(-100, 100, 0);
  sphere(4);
  translate(0, -100, 100);
  sphere(4);
  
  popMatrix();
}

/**
 * Gridlines are drawn, spanning from two of the three axes defined by the given axes vector set. The two axes that form a
 * plane that has the lowest offset of the xz-plane (hence the two vectors with the minimum y-values) are chosen to be
 * mapped to the xz-plane and their reflection on the xz-plane are drawn the along with a grid is formed from the the two
 * reflection axes at the base of the Robot.
 * 
 * @param axesVectors     A rotation matrix (in row major order) that defines the axes of the frame to map to the xz-plane
 * @param origin          The xz-origin at which to drawn the reflection axes
 * @param halfNumOfLines  Half the number of lines to draw for one of the axes
 * @param distBwtLines    The distance between each gridline
 */
public void displayGridlines(float[][] axesVectors, PVector origin, int halfNumOfLines, float distBwtLines) {
  int vectorPX = -1, vectorPZ = -1;
  
  // Find the two vectors with the minimum y values
  for (int v = 0; v < axesVectors.length; ++v) {
    int limboX = (v + 1) % axesVectors.length,
        limboY = (limboX + 1) % axesVectors.length;
    // Compare the y value of the current vector to those of the other two vectors
    if (abs(axesVectors[v][1]) >= abs(axesVectors[limboX][1]) && abs(axesVectors[v][1]) >= abs(axesVectors[limboY][1])) {
      vectorPX = limboX;
      vectorPZ = limboY;
      break;
    }
  }
  
  if (vectorPX == -1 || vectorPZ == -1) {
    println("Invalid axes-origin pair for grid lines!");
    return;
  }
  
  pushMatrix();
  // Map the chosen two axes vectors to the xz-plane at the y-position of the Robot's base
  applyMatrix(axesVectors[vectorPX][0], 0, axesVectors[vectorPZ][0], origin.x,
                                     0, 1,                        0, ROBOT_POSITION.y,
              axesVectors[vectorPX][2], 0, axesVectors[vectorPZ][2], origin.z,
                                     0, 0,                        0,        1);
  
  float lineLen = halfNumOfLines * distBwtLines;
  
  // Draw axes lines in red
  stroke(255, 0, 0);
  line(-lineLen, 0, 0, lineLen, 0, 0);
  line(0, 0, -lineLen, 0, 0, lineLen);
  // Draw remaining gridlines in black
  stroke(25, 25, 25);
  for(int linePosScale = 1; linePosScale <= halfNumOfLines; ++linePosScale) {
    line(distBwtLines * linePosScale, 0, -lineLen, distBwtLines * linePosScale, 0, lineLen);
    line(-lineLen, 0, distBwtLines * linePosScale, lineLen, 0, distBwtLines * linePosScale);
    
    line(-distBwtLines * linePosScale, 0, -lineLen, -distBwtLines * linePosScale, 0, lineLen);
    line(-lineLen, 0, -distBwtLines * linePosScale, lineLen, 0, -distBwtLines * linePosScale);
  }
  
  popMatrix();
  mapToRobotBasePlane();
}

/**
 * This method will draw the End Effector grid mapping based on the value of EE_MAPPING:
 *
 *  0 -> a line is drawn between the EE and the grid plane
 *  1 -> a point is drawn on the grid plane that corresponds to the EE's xz coordinates
 *  For any other value, nothing is drawn
 */
public void mapToRobotBasePlane() {
  
  PVector ee_pos = nativeRobotEEPoint(armModel.getJointAngles()).position;
  
  // Change color of the EE mapping based on if it lies below or above the ground plane
  color c = (ee_pos.y <= ROBOT_POSITION.y) ? color(255, 0, 0) : color(150, 0, 255);
  
  // Toggle EE mapping type with 'e'
  switch (mappingState) {
  case LINE:
    stroke(c);
    // Draw a line, from the EE to the grid in the xy plane, parallel to the xy plane
    line(ee_pos.x, ee_pos.y, ee_pos.z, ee_pos.x, ROBOT_POSITION.y, ee_pos.z);
    break;
    
  case DOT:
    noStroke();
    fill(c);
    // Draw a point, which maps the EE's position to the grid in the xy plane
    pushMatrix();
    rotateX(PI / 2);
    translate(0, 0, -ROBOT_POSITION.y);
    ellipse(ee_pos.x, ee_pos.z, 10, 10);
    popMatrix();
    break;
    
  default:
    // No EE grid mapping
  }
}