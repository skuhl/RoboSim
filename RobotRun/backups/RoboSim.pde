// Need G4P library
import g4p_controls.*;

import java.util.*;
import java.nio.*;

ArmModel testModel;
float lastMouseX, lastMouseY;
float cameraTX = 0, cameraTY = 0, cameraTZ = 0;
float cameraRX = 0, cameraRY = 0, cameraRZ = 0;
boolean spacebarDown = false;
float[] jointsMoving = new float[6];

public void setup(){
  size(800, 600, P3D);
  createGUI();
  customGUI();
  // Place your setup code here
  for (int n = 0; n < 6; n++) jointsMoving[n] = 0;
  testModel = new ArmModel();
}

public void draw(){
  lastMouseX = mouseX;
  lastMouseY = mouseY;
  moveJoints();
  background(255);
  stroke(100, 100, 255);
  pushMatrix();
  translate(400 + cameraTX, 700 + cameraTY, -500 + cameraTZ);
  rotateX(cameraRX);
  rotateY(cameraRY);
  testModel.draw();
  popMatrix();
}

// Use this method to add additional statements
// to customise the GUI controls
public void customGUI(){

}

void keyPressed() {
  if (key == ' ') spacebarDown = true;
  /*else if (key == 'q') testModel.base.currentRotations[1] += 0.1;
  else if (key == 'e') testModel.base.currentRotations[1] -= 0.1;
  else if (key == 'a') testModel.link1.currentRotations[2] += 0.1;
  else if (key == 'd') testModel.link1.currentRotations[2] -= 0.1;
  else if (key == 'z') testModel.link2.currentRotations[2] += 0.1;
  else if (key == 'c') testModel.link2.currentRotations[2] -= 0.1; /* */
}

void keyReleased() {
  if (key == ' ') spacebarDown = false;
}

void mouseWheel(MouseEvent event) {
  float e = event.getCount();
  if (e < 0) cameraTZ += 25;
  else if (e > 0) cameraTZ -= 25;
}

void mouseDragged() {
  if (!spacebarDown) {
    if (mouseX < lastMouseX) cameraRY -= 0.01;
    else if (mouseX > lastMouseX) cameraRY += 0.01;
    if (mouseY < lastMouseY) cameraRX -= 0.01;
    else if (mouseY > lastMouseY) cameraRX += 0.01;
  } else {
    if (mouseX < lastMouseX) cameraTY -= 25;
    else if (mouseX > lastMouseX) cameraTY += 25;
    if (mouseY < lastMouseY) cameraTX -= 25;
    else if (mouseY > lastMouseY) cameraTX += 25;
  }
}

void moveJoints() {
  testModel.base.currentRotations[1] += 0.02 * jointsMoving[0];
  
  testModel.link1.currentRotations[2] += 0.02 * jointsMoving[1];
  if (testModel.link1.currentRotations[2] > testModel.link1.rotationMaxConstraints[2]) {
    jointsMoving[1] = 0;
    testModel.link1.currentRotations[2] = testModel.link1.rotationMaxConstraints[2];
  } else if (testModel.link1.currentRotations[2] < testModel.link1.rotationMinConstraints[2]) {
    jointsMoving[1] = 0;
    testModel.link1.currentRotations[2] = testModel.link1.rotationMinConstraints[2];
  }
  
  testModel.link2.currentRotations[2] += 0.02 * jointsMoving[2];
  if (testModel.link2.currentRotations[2] > testModel.link2.rotationMaxConstraints[2]) {
    jointsMoving[2] = 0;
    testModel.link2.currentRotations[2] = testModel.link2.rotationMaxConstraints[2];
  } else if (testModel.link2.currentRotations[2] < testModel.link2.rotationMinConstraints[2]) {
    jointsMoving[2] = 0;
    testModel.link2.currentRotations[2] = testModel.link2.rotationMinConstraints[2];
  }
}

void drawModel(Model model) {
  pushMatrix();
  for (Triangle t : model.triangles) {
    beginShape(TRIANGLES);
    normal(t.components[0].x, t.components[1].y, t.components[2].z);
    for (int n = 1; n < 4; n++)
      vertex(t.components[n].x, t.components[n].y, t.components[n].z);
    endShape();
  }
  popMatrix();
}

Model loadSTLModel(String filename) {
  byte[] data = loadBytes(filename);
  Model model = new Model();
  int n = 84; // skip header and number of triangles
  
  // debug
  /*float smallestX = Float.MAX_VALUE;
  float biggestX = Float.MIN_VALUE;
  float smallestY = Float.MAX_VALUE;
  float biggestY = Float.MIN_VALUE;
  float smallestZ = Float.MAX_VALUE;
  float biggestZ = Float.MIN_VALUE; /* */
  // end debug
  
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
      // debug
      /*if (m > 0) {
        if (t.components[m].x > biggestX) biggestX = t.components[m].x;
        if (t.components[m].x < smallestX) smallestX = t.components[m].x;
        if (t.components[m].y > biggestY) biggestY = t.components[m].y;
        if (t.components[m].y < smallestY) smallestY = t.components[m].y;
        if (t.components[m].z > biggestZ) biggestZ = t.components[m].z;
        if (t.components[m].z < smallestZ) smallestZ = t.components[m].z;
      } /* */
      // end debug
    }
    n += 2; // skip meaningless "attribute byte count"
    model.triangles.add(t);
  }
  // DEBUG
  /*for (Triangle t : model.triangles) {
    println("Triangle: ");
    println("Normal: " + t.components[0].x + " " +
          t.components[0].y + " " + t.components[0].z);
    println("V1: " + t.components[1].x + " " +
          t.components[1].y + " " + t.components[1].z);
    println("V2: " + t.components[2].x + " " +
          t.components[2].y + " " + t.components[2].z);
    println("V3: " + t.components[3].x + " " +
          t.components[3].y + " " + t.components[3].z);
    println("-----");
  } /* */
  // END DEBUG
  // debug
  /*println("Size X: " + (biggestX - smallestX));
  println("Size Y: " + (biggestY - smallestY));
  println("Size Z: " + (biggestZ - smallestZ)); /* */
  // end debug
  return model;
}

public class Triangle {
  public PVector[] components = new PVector[4];
  // normal, v1, v2, v3
}

public class Model {
  public ArrayList<Triangle> triangles = new ArrayList<Triangle>();
  public boolean[] rotations = new boolean[3];
  public float[] rotationMinConstraints = new float[3];
  public float[] rotationMaxConstraints = new float[3];
  public float[] currentRotations = new float[3];
  
  public Model() {
    for (int n = 0; n < 3; n++) {
      rotations[n] = false;
      rotationMinConstraints[n] = Float.MIN_VALUE;
      rotationMaxConstraints[n] = Float.MAX_VALUE;
      currentRotations[n] = 0;
    }
  }
}

public class ArmModel {
  Model base;
  Model link1;
  Model link2;
  Model link3;
  
  public ArmModel() {
    base = loadSTLModel("Base.STL");
    base.rotations[1] = true;
    link1 = loadSTLModel("Link1.STL");
    link1.rotations[2] = true;
    link1.rotationMinConstraints[2] = -2.4;
    link1.rotationMaxConstraints[2] = 2.4;
    link2 = loadSTLModel("Link2.STL");
    link2.rotations[2] = true;
    link2.rotationMinConstraints[2] = -2;
    link2.rotationMaxConstraints[2] = 2;
    link3 = loadSTLModel("Link3.STL");
    link3.rotations[0] = true;
    // end effector rotates around X and Y axes
    // doesn't seem to be included in model files yet
  }
  
  public void draw() {
    rotateY(base.currentRotations[1]);
    drawModel(base);
    translate(0, -200, 0);
    rotateZ(PI);
    //rotateY(PI/2.0);
    drawModel(link1);
    rotateZ(-PI);
    translate(-25, -130, 0);
    rotateZ(link1.currentRotations[2]);
    translate(-25, -130, 0);
    rotateZ(PI);
    drawModel(link2);
    rotateZ(-PI);
    translate(0, -120, 0);
    rotateZ(link2.currentRotations[2]);
    translate(0, -120, 0);
    rotateZ(PI);
    drawModel(link3);
  }
} // end ArmModel class

void printCurrentModelCoordinates(String msg) {
  print(msg + ": " );
  print(modelX(0, 0, 0) + " ");
  print(modelY(0, 0, 0) + " ");
  print(modelZ(0, 0, 0));
  println();
}
