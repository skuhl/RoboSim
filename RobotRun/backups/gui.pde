/* =========================================================
 * ====                   WARNING                        ===
 * =========================================================
 * The code in this tab has been generated from the GUI form
 * designer and care should be taken when editing this file.
 * Only add/edit code inside the event handlers i.e. only
 * use lines between the matching comment tags. e.g.

 void myBtnEvents(GButton button) { //_CODE_:button1:12356:
     // It is safe to enter your event code here  
 } //_CODE_:button1:12356:
 
 * Do not rename this tab!
 * =========================================================
 */

synchronized public void controllerDraw(GWinApplet appc, GWinData data) { //_CODE_:winController:989213:
  appc.background(230);
} //_CODE_:winController:989213:

public void btnPrevClick(GButton source, GEvent event) { //_CODE_:btnPrev:256787:
  println("btnPrev - GButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btnPrev:256787:

public void btnF1Click(GButton source, GEvent event) { //_CODE_:btnF1:483194:
  println("button1 - GButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btnF1:483194:

public void btnF2Click(GButton source, GEvent event) { //_CODE_:btnF2:767692:
  println("button2 - GButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btnF2:767692:

public void btnF3Click(GButton source, GEvent event) { //_CODE_:btnF3:896235:
  println("button3 - GButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btnF3:896235:

public void btnF4Click(GButton source, GEvent event) { //_CODE_:btnF4:515087:
  println("button4 - GButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btnF4:515087:

public void btnF5Click(GButton source, GEvent event) { //_CODE_:btnF5:860948:
  println("button5 - GButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btnF5:860948:

public void btnNextClick(GButton source, GEvent event) { //_CODE_:btnNext:658022:
  println("button6 - GButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btnNext:658022:

public void btnLShiftClick(GButton source, GEvent event) { //_CODE_:btnLShift:839427:
  println("button1 - GButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btnLShift:839427:

public void btnMenuClick(GButton source, GEvent event) { //_CODE_:btnMenu:318570:
  println("button2 - GButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btnMenu:318570:

public void btnSelectClick(GButton source, GEvent event) { //_CODE_:btnSelect:796786:
  println("button3 - GButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btnSelect:796786:

public void btnEditClick(GButton source, GEvent event) { //_CODE_:btnEdit:726608:
  println("button4 - GButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btnEdit:726608:

public void btnDataClick(GButton source, GEvent event) { //_CODE_:btnData:788283:
  println("button5 - GButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btnData:788283:

public void btnFunctionClick(GButton source, GEvent event) { //_CODE_:btnFunction:444006:
  println("button6 - GButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btnFunction:444006:

public void btnRShiftClick(GButton source, GEvent event) { //_CODE_:btnRShift:296478:
  println("button7 - GButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btnRShift:296478:

public void btnJ6PlusClick(GButton source, GEvent event) { //_CODE_:btnJ6Plus:534633:
  println("button1 - GButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btnJ6Plus:534633:

public void btnJ6MinusClick(GButton source, GEvent event) { //_CODE_:btnJ6Minus:813693:
  println("button2 - GButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btnJ6Minus:813693:

public void btnMinusPercentClick(GButton source, GEvent event) { //_CODE_:btnMinusPercent:780129:
  println("button3 - GButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btnMinusPercent:780129:

public void btnJ5PlusClick(GButton source, GEvent event) { //_CODE_:btnJ5Plus:583082:
  println("button4 - GButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btnJ5Plus:583082:

public void btnJ5MinusClick(GButton source, GEvent event) { //_CODE_:btnJ5Minus:588290:
  println("button5 - GButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btnJ5Minus:588290:

public void btnPlusPercentClick(GButton source, GEvent event) { //_CODE_:btnPlusPercent:861555:
  println("button6 - GButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btnPlusPercent:861555:

public void btnCoordClick(GButton source, GEvent event) { //_CODE_:btnCoord:739161:
  println("button7 - GButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btnCoord:739161:

public void btnBackClick(GButton source, GEvent event) { //_CODE_:btnBack:364123:
  println("button8 - GButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btnBack:364123:

public void btnForwardClick(GButton source, GEvent event) { //_CODE_:btnForward:483643:
  println("button9 - GButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btnForward:483643:

public void btnHoldClick(GButton source, GEvent event) { //_CODE_:btnHold:370815:
  println("button10 - GButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btnHold:370815:

public void btnJ4MinusClick(GButton source, GEvent event) { //_CODE_:btnJ4Minus:336732:
  println("button11 - GButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btnJ4Minus:336732:

public void btnJ4PlusClick(GButton source, GEvent event) { //_CODE_:btnJ4Plus:517151:
  println("button12 - GButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btnJ4Plus:517151:

public void btnJ3MinusClick(GButton source, GEvent event) { //_CODE_:btnJ3Minus:210742:
  if (jointsMoving[2] < 0) jointsMoving[2] = 0;
  else jointsMoving[2] = -1;
} //_CODE_:btnJ3Minus:210742:

public void btnJ3PlusClick(GButton source, GEvent event) { //_CODE_:btnJ3Plus:472659:
  if (jointsMoving[2] > 0) jointsMoving[2] = 0;
  else jointsMoving[2] = 1;
} //_CODE_:btnJ3Plus:472659:

public void btnJ2MinusClick(GButton source, GEvent event) { //_CODE_:btnJ2Minus:766048:
  if (jointsMoving[1] < 0) jointsMoving[1] = 0;
  else jointsMoving[1] = -1;
} //_CODE_:btnJ2Minus:766048:

public void btnJ1MinusClick(GButton source, GEvent event) { //_CODE_:btnJ1Minus:575597:
  if (jointsMoving[0] < 0) jointsMoving[0] = 0;
  else jointsMoving[0] = -1;
} //_CODE_:btnJ1Minus:575597:

public void btnJ2PlusClick(GButton source, GEvent event) { //_CODE_:btnJ2Plus:723794:
  if (jointsMoving[1] > 0) jointsMoving[1] = 0;
  else jointsMoving[1] = 1;
} //_CODE_:btnJ2Plus:723794:

public void btnJ1PlusClick(GButton source, GEvent event) { //_CODE_:btnJ1Plus:337003:
  if (jointsMoving[0] > 0) jointsMoving[0] = 0;
  else jointsMoving[0] = 1;
} //_CODE_:btnJ1Plus:337003:

public void btnUpClick(GImageButton source, GEvent event) { //_CODE_:btnUp:678887:
  println("imgButton1 - GImageButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btnUp:678887:

public void btnRightClick(GImageButton source, GEvent event) { //_CODE_:btnRight:689017:
  println("imgButton2 - GImageButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btnRight:689017:

public void btnDownClick(GImageButton source, GEvent event) { //_CODE_:btnDown:232719:
  println("imgButton3 - GImageButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btnDown:232719:

public void btnLeftClick(GImageButton source, GEvent event) { //_CODE_:btnLeft:440263:
  println("imgButton4 - GImageButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btnLeft:440263:

public void btnTool1Click(GButton source, GEvent event) { //_CODE_:btnTool1:245797:
  println("button2 - GButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btnTool1:245797:

public void btnTool2Click(GButton source, GEvent event) { //_CODE_:btnTool2:838834:
  println("button3 - GButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btnTool2:838834:

public void btnMoveMenuClick(GButton source, GEvent event) { //_CODE_:btnMoveMenu:715147:
  println("button4 - GButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btnMoveMenu:715147:

public void btnSetUpClick(GButton source, GEvent event) { //_CODE_:btnSetUp:606814:
  println("button5 - GButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btnSetUp:606814:

public void btnStatusClick(GButton source, GEvent event) { //_CODE_:btnStatus:394091:
  println("button6 - GButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btnStatus:394091:

public void btnItemClick(GButton source, GEvent event) { //_CODE_:btnItem:570661:
  println("button7 - GButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btnItem:570661:

public void btnResetClick(GButton source, GEvent event) { //_CODE_:btnReset:640696:
  println("button9 - GButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btnReset:640696:

public void btn9Click(GButton source, GEvent event) { //_CODE_:btn9:309197:
  println("button10 - GButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btn9:309197:

public void btn6Click(GButton source, GEvent event) { //_CODE_:btn6:286419:
  println("button11 - GButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btn6:286419:

public void btn3Click(GButton source, GEvent event) { //_CODE_:btn3:928915:
  println("button12 - GButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btn3:928915:

public void btnCommaClick(GButton source, GEvent event) { //_CODE_:btnComma:732420:
  println("button13 - GButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btnComma:732420:

public void btnIOClick(GButton source, GEvent event) { //_CODE_:btnIO:204622:
  println("button14 - GButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btnIO:204622:

public void btn8Click(GButton source, GEvent event) { //_CODE_:btn8:213831:
  println("button15 - GButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btn8:213831:

public void btn2Click(GButton source, GEvent event) { //_CODE_:btn2:326149:
  println("button16 - GButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btn2:326149:

public void btn5Click(GButton source, GEvent event) { //_CODE_:btn5:443988:
  println("button1 - GButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btn5:443988:

public void btnDotClick(GButton source, GEvent event) { //_CODE_:btnDot:858425:
  println("button17 - GButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btnDot:858425:

public void btnPositionClick(GButton source, GEvent event) { //_CODE_:btnPosition:204630:
  println("button18 - GButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btnPosition:204630:

public void btn7Click(GButton source, GEvent event) { //_CODE_:btn7:341756:
  println("button19 - GButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btn7:341756:

public void btn4Click(GButton source, GEvent event) { //_CODE_:btn4:766125:
  println("button20 - GButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btn4:766125:

public void btn1Click(GButton source, GEvent event) { //_CODE_:btn1:289620:
  println("button21 - GButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btn1:289620:

public void btn0Click(GButton source, GEvent event) { //_CODE_:btn0:494103:
  println("button22 - GButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btn0:494103:

public void btnMinusClick(GButton source, GEvent event) { //_CODE_:btnMinus:381057:
  println("button23 - GButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btnMinus:381057:

public void btnEnterClick(GButton source, GEvent event) { //_CODE_:btnEnter:977099:
  println("button24 - GButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btnEnter:977099:

public void btnXClick(GImageButton source, GEvent event) { //_CODE_:btnX:342305:
  println("btnX - GImageButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btnX:342305:

public void btnStepClick(GButton source, GEvent event) { //_CODE_:btnStep:693102:
  println("button1 - GButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btnStep:693102:

public void btnPowerClick(GImageButton source, GEvent event) { //_CODE_:btnPower:850380:
  println("imgButton1 - GImageButton >> GEvent." + event + " @ " + millis());
} //_CODE_:btnPower:850380:



// Create all the GUI controls. 
// autogenerated do not edit
public void createGUI(){
  G4P.messagesEnabled(false);
  G4P.setGlobalColorScheme(GCScheme.BLUE_SCHEME);
  G4P.setCursor(ARROW);
  if(frame != null)
    frame.setTitle("Sketch Window");
  winController = new GWindow(this, "RoboSim Controls", 0, 0, 420, 600, false, JAVA2D);
  winController.addDrawHandler(this, "controllerDraw");
  textDisplay = new GLabel(winController.papplet, 40, 2, 340, 198);
  textDisplay.setTextAlign(GAlign.LEFT, GAlign.TOP);
  textDisplay.setOpaque(false);
  btnPrev = new GButton(winController.papplet, 10, 210, 40, 40);
  btnPrev.setText("Prev");
  btnPrev.addEventHandler(this, "btnPrevClick");
  btnF1 = new GButton(winController.papplet, 70, 210, 40, 40);
  btnF1.setText("F1");
  btnF1.addEventHandler(this, "btnF1Click");
  btnF2 = new GButton(winController.papplet, 130, 210, 40, 40);
  btnF2.setText("F2");
  btnF2.addEventHandler(this, "btnF2Click");
  btnF3 = new GButton(winController.papplet, 190, 210, 40, 40);
  btnF3.setText("F3");
  btnF3.addEventHandler(this, "btnF3Click");
  btnF4 = new GButton(winController.papplet, 250, 210, 40, 40);
  btnF4.setText("F4");
  btnF4.addEventHandler(this, "btnF4Click");
  btnF5 = new GButton(winController.papplet, 310, 210, 40, 40);
  btnF5.setText("F5");
  btnF5.addEventHandler(this, "btnF5Click");
  btnNext = new GButton(winController.papplet, 370, 210, 40, 40);
  btnNext.setText("Next");
  btnNext.addEventHandler(this, "btnNextClick");
  btnLShift = new GButton(winController.papplet, 60, 260, 40, 40);
  btnLShift.setText("SHIFT");
  btnLShift.addEventHandler(this, "btnLShiftClick");
  btnMenu = new GButton(winController.papplet, 110, 265, 35, 35);
  btnMenu.setText("Menu");
  btnMenu.addEventHandler(this, "btnMenuClick");
  btnSelect = new GButton(winController.papplet, 150, 265, 35, 35);
  btnSelect.setText("Select");
  btnSelect.addEventHandler(this, "btnSelectClick");
  btnEdit = new GButton(winController.papplet, 190, 265, 35, 35);
  btnEdit.setText("Edit");
  btnEdit.addEventHandler(this, "btnEditClick");
  btnData = new GButton(winController.papplet, 230, 265, 35, 35);
  btnData.setText("Data");
  btnData.addEventHandler(this, "btnDataClick");
  btnFunction = new GButton(winController.papplet, 280, 265, 35, 35);
  btnFunction.setText("Fctn");
  btnFunction.addEventHandler(this, "btnFunctionClick");
  btnRShift = new GButton(winController.papplet, 320, 260, 40, 40);
  btnRShift.setText("SHIFT");
  btnRShift.addEventHandler(this, "btnRShiftClick");
  btnJ6Plus = new GButton(winController.papplet, 350, 550, 40, 40);
  btnJ6Plus.setText("+Z (J6)");
  btnJ6Plus.addEventHandler(this, "btnJ6PlusClick");
  btnJ6Minus = new GButton(winController.papplet, 305, 550, 40, 40);
  btnJ6Minus.setText("-Z (J6)");
  btnJ6Minus.addEventHandler(this, "btnJ6MinusClick");
  btnMinusPercent = new GButton(winController.papplet, 260, 550, 40, 40);
  btnMinusPercent.setText("-%");
  btnMinusPercent.addEventHandler(this, "btnMinusPercentClick");
  btnJ5Plus = new GButton(winController.papplet, 350, 505, 40, 40);
  btnJ5Plus.setText("+Y (J5)");
  btnJ5Plus.addEventHandler(this, "btnJ5PlusClick");
  btnJ5Minus = new GButton(winController.papplet, 305, 505, 40, 40);
  btnJ5Minus.setText("-Y (J5)");
  btnJ5Minus.addEventHandler(this, "btnJ5MinusClick");
  btnPlusPercent = new GButton(winController.papplet, 260, 505, 40, 40);
  btnPlusPercent.setText("+%");
  btnPlusPercent.addEventHandler(this, "btnPlusPercentClick");
  btnCoord = new GButton(winController.papplet, 260, 460, 40, 40);
  btnCoord.setText("COOR");
  btnCoord.addEventHandler(this, "btnCoordClick");
  btnBack = new GButton(winController.papplet, 260, 415, 40, 40);
  btnBack.setText("BACK");
  btnBack.addEventHandler(this, "btnBackClick");
  btnForward = new GButton(winController.papplet, 260, 370, 40, 40);
  btnForward.setText("FWD");
  btnForward.addEventHandler(this, "btnForwardClick");
  btnHold = new GButton(winController.papplet, 260, 325, 40, 40);
  btnHold.setText("HOLD");
  btnHold.addEventHandler(this, "btnHoldClick");
  btnJ4Minus = new GButton(winController.papplet, 305, 460, 40, 40);
  btnJ4Minus.setText("-X (J4)");
  btnJ4Minus.addEventHandler(this, "btnJ4MinusClick");
  btnJ4Plus = new GButton(winController.papplet, 350, 460, 40, 40);
  btnJ4Plus.setText("+X (J4)");
  btnJ4Plus.addEventHandler(this, "btnJ4PlusClick");
  btnJ3Minus = new GButton(winController.papplet, 305, 415, 40, 40);
  btnJ3Minus.setText("-Z (J3)");
  btnJ3Minus.addEventHandler(this, "btnJ3MinusClick");
  btnJ3Plus = new GButton(winController.papplet, 350, 415, 40, 40);
  btnJ3Plus.setText("+Z (J3)");
  btnJ3Plus.addEventHandler(this, "btnJ3PlusClick");
  btnJ2Minus = new GButton(winController.papplet, 305, 370, 40, 40);
  btnJ2Minus.setText("-Y (J2)");
  btnJ2Minus.addEventHandler(this, "btnJ2MinusClick");
  btnJ1Minus = new GButton(winController.papplet, 305, 325, 40, 40);
  btnJ1Minus.setText("-X (J1)");
  btnJ1Minus.addEventHandler(this, "btnJ1MinusClick");
  btnJ2Plus = new GButton(winController.papplet, 350, 370, 40, 40);
  btnJ2Plus.setText("+Y (J2)");
  btnJ2Plus.addEventHandler(this, "btnJ2PlusClick");
  btnJ1Plus = new GButton(winController.papplet, 350, 325, 40, 40);
  btnJ1Plus.setText("+X (J1)");
  btnJ1Plus.addEventHandler(this, "btnJ1PlusClick");
  btnUp = new GImageButton(winController.papplet, 160, 310, 35, 35, new String[] { "uparrow.png", "uparrow.png", "uparrow.png" } );
  btnUp.addEventHandler(this, "btnUpClick");
  btnRight = new GImageButton(winController.papplet, 195, 325, 35, 35, new String[] { "rightarrow.png", "rightarrow.png", "rightarrow.png" } );
  btnRight.addEventHandler(this, "btnRightClick");
  btnDown = new GImageButton(winController.papplet, 160, 345, 35, 35, new String[] { "downarrow.png", "downarrow.png", "downarrow.png" } );
  btnDown.addEventHandler(this, "btnDownClick");
  btnLeft = new GImageButton(winController.papplet, 125, 325, 35, 35, new String[] { "leftarrow.png", "leftarrow.png", "leftarrow.png" } );
  btnLeft.addEventHandler(this, "btnLeftClick");
  btnTool1 = new GButton(winController.papplet, 180, 425, 40, 30);
  btnTool1.setText("Tool 1");
  btnTool1.addEventHandler(this, "btnTool1Click");
  btnTool2 = new GButton(winController.papplet, 180, 460, 40, 30);
  btnTool2.setText("Tool 2");
  btnTool2.addEventHandler(this, "btnTool2Click");
  btnMoveMenu = new GButton(winController.papplet, 180, 495, 40, 30);
  btnMoveMenu.setText("Move Menu");
  btnMoveMenu.addEventHandler(this, "btnMoveMenuClick");
  btnSetUp = new GButton(winController.papplet, 180, 530, 40, 30);
  btnSetUp.setText("Set Up");
  btnSetUp.addEventHandler(this, "btnSetUpClick");
  btnStatus = new GButton(winController.papplet, 180, 565, 40, 30);
  btnStatus.setText("Status");
  btnStatus.addEventHandler(this, "btnStatusClick");
  btnItem = new GButton(winController.papplet, 135, 390, 40, 30);
  btnItem.setText("Item");
  btnItem.addEventHandler(this, "btnItemClick");
  btnReset = new GButton(winController.papplet, 45, 390, 40, 30);
  btnReset.setText("RESET");
  btnReset.addEventHandler(this, "btnResetClick");
  btn9 = new GButton(winController.papplet, 135, 425, 40, 30);
  btn9.setText("9");
  btn9.addEventHandler(this, "btn9Click");
  btn6 = new GButton(winController.papplet, 135, 460, 40, 30);
  btn6.setText("6");
  btn6.addEventHandler(this, "btn6Click");
  btn3 = new GButton(winController.papplet, 135, 495, 40, 30);
  btn3.setText("3");
  btn3.addEventHandler(this, "btn3Click");
  btnComma = new GButton(winController.papplet, 135, 530, 40, 30);
  btnComma.setText(",");
  btnComma.setTextBold();
  btnComma.addEventHandler(this, "btnCommaClick");
  btnIO = new GButton(winController.papplet, 135, 565, 40, 30);
  btnIO.setText("I/O");
  btnIO.addEventHandler(this, "btnIOClick");
  btn8 = new GButton(winController.papplet, 90, 425, 40, 30);
  btn8.setText("8");
  btn8.addEventHandler(this, "btn8Click");
  btn2 = new GButton(winController.papplet, 90, 495, 40, 30);
  btn2.setText("2");
  btn2.addEventHandler(this, "btn2Click");
  btn5 = new GButton(winController.papplet, 90, 460, 40, 30);
  btn5.setText("5");
  btn5.addEventHandler(this, "btn5Click");
  btnDot = new GButton(winController.papplet, 90, 530, 40, 30);
  btnDot.setText(".");
  btnDot.setTextBold();
  btnDot.addEventHandler(this, "btnDotClick");
  btnPosition = new GButton(winController.papplet, 90, 565, 40, 30);
  btnPosition.setText("Posn");
  btnPosition.addEventHandler(this, "btnPositionClick");
  btn7 = new GButton(winController.papplet, 45, 425, 40, 30);
  btn7.setText("7");
  btn7.addEventHandler(this, "btn7Click");
  btn4 = new GButton(winController.papplet, 45, 460, 40, 30);
  btn4.setText("4");
  btn4.addEventHandler(this, "btn4Click");
  btn1 = new GButton(winController.papplet, 45, 495, 40, 30);
  btn1.setText("1");
  btn1.addEventHandler(this, "btn1Click");
  btn0 = new GButton(winController.papplet, 45, 530, 40, 30);
  btn0.setText("0");
  btn0.addEventHandler(this, "btn0Click");
  btnMinus = new GButton(winController.papplet, 45, 565, 40, 30);
  btnMinus.setText("-");
  btnMinus.addEventHandler(this, "btnMinusClick");
  btnEnter = new GButton(winController.papplet, 180, 390, 40, 30);
  btnEnter.setText("Enter");
  btnEnter.addEventHandler(this, "btnEnterClick");
  btnX = new GImageButton(winController.papplet, 90, 390, 40, 30, new String[] { "xbutton.png", "xbutton.png", "xbutton.png" } );
  btnX.addEventHandler(this, "btnXClick");
  btnStep = new GButton(winController.papplet, 45, 350, 35, 35);
  btnStep.setText("STEP");
  btnStep.addEventHandler(this, "btnStepClick");
  btnPower = new GImageButton(winController.papplet, 5, 305, 40, 40, new String[] { "power.png", "power.png", "power.png" } );
  btnPower.addEventHandler(this, "btnPowerClick");
}

// Variable declarations 
// autogenerated do not edit
GWindow winController;
GLabel textDisplay; 
GButton btnPrev; 
GButton btnF1; 
GButton btnF2; 
GButton btnF3; 
GButton btnF4; 
GButton btnF5; 
GButton btnNext; 
GButton btnLShift; 
GButton btnMenu; 
GButton btnSelect; 
GButton btnEdit; 
GButton btnData; 
GButton btnFunction; 
GButton btnRShift; 
GButton btnJ6Plus; 
GButton btnJ6Minus; 
GButton btnMinusPercent; 
GButton btnJ5Plus; 
GButton btnJ5Minus; 
GButton btnPlusPercent; 
GButton btnCoord; 
GButton btnBack; 
GButton btnForward; 
GButton btnHold; 
GButton btnJ4Minus; 
GButton btnJ4Plus; 
GButton btnJ3Minus; 
GButton btnJ3Plus; 
GButton btnJ2Minus; 
GButton btnJ1Minus; 
GButton btnJ2Plus; 
GButton btnJ1Plus; 
GImageButton btnUp; 
GImageButton btnRight; 
GImageButton btnDown; 
GImageButton btnLeft; 
GButton btnTool1; 
GButton btnTool2; 
GButton btnMoveMenu; 
GButton btnSetUp; 
GButton btnStatus; 
GButton btnItem; 
GButton btnReset; 
GButton btn9; 
GButton btn6; 
GButton btn3; 
GButton btnComma; 
GButton btnIO; 
GButton btn8; 
GButton btn2; 
GButton btn5; 
GButton btnDot; 
GButton btnPosition; 
GButton btn7; 
GButton btn4; 
GButton btn1; 
GButton btn0; 
GButton btnMinus; 
GButton btnEnter; 
GImageButton btnX; 
GButton btnStep; 
GImageButton btnPower; 

