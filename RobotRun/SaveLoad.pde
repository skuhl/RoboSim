/**
 * This method saves all programs, frames, and initialized registers,
 * each to separate files
 */
public void saveState() {
  saveProgramBytes( new File(sketchPath("tmp/programs.bin")) );
  saveFrameBytes( new File(sketchPath("tmp/frames.bin")) );
  saveRegisterBytes( new File(sketchPath("tmp/registers.bin")) );
  saveScenarioBytes( new File(sketchPath("tmp/scenarios.bin")) );
}

/**
 * Load program, frames, and registers from their respective
 * binary files.
 *
 * @return  a byte value
 */
public byte loadState() {
  byte[] fileFlags = new byte[] { 1, 1, 1, 1 };
  
  File f = new File(sketchPath("tmp/"));
  if(!f.exists()) { f.mkdirs(); }
  
  /* Load and Initialize the Tool and User Frames */
  
  File frameFile = new File( sketchPath("tmp/frames.bin") );
  
  if(frameFile.exists()) {
    // Load both the User and Tool Frames
    int ret = loadFrameBytes(frameFile);
    
    if(ret == 0) {
      println("Successfully loaded frames!");
      fileFlags[1] = 0;
    } else {
      println("Failed to load frames ...");
      
    }
  }
  
  // Create new frames if they could not be loaded
  if(fileFlags[1] == 1) {
    
    toolFrames = new Frame[10];
    userFrames = new Frame[10];
    
    for(int n = 0; n < toolFrames.length; n += 1) {
      toolFrames[n] = new ToolFrame();
      userFrames[n] = new UserFrame();
    }
  }
  
  
  /* Load and Initialize the Position Register and Registers */
  
  File regFile = new File(sketchPath("tmp/registers.bin"));
  
  if(regFile.exists()) {
    int ret = loadRegisterBytes(regFile);
    
    if(ret == 0) {
      println("Successfully loaded registers!");
      fileFlags[2] = 0;
    } else {
      println("Failed to load registers ...");
    }
  }
  
  File scenarioFile = new File(sketchPath("tmp/scenarios.bin"));
  
  if(scenarioFile.exists()) {
    int ret = loadScenarioBytes(scenarioFile);   //<>// //<>// //<>// //<>// //<>// //<>// //<>//
    
    if(ret == 0) {
      println("Successfully loaded scenarios!");
      fileFlags[3] = 0;
    } else {
      println("Failed to load scenarios ...");
    }
  }
  
  // Initialize uninitialized registers and position registers to with null fields
  for(int reg = 0; reg < DAT_REG.length; reg += 1) {
    
    if(DAT_REG[reg] == null) {
      DAT_REG[reg] = new DataRegister(reg);
    }
    
    if(GPOS_REG[reg] == null) {
      GPOS_REG[reg] = new PositionRegister(reg);
    }
  }
  
  // Associated each End Effector with an I/O Register
  int idx = 0;
  IO_REG[idx] = new IORegister(idx, (EEType.SUCTION).name(), OFF);
  ++idx;
  
  IO_REG[idx] = new IORegister(idx, (EEType.CLAW).name(), OFF);
  ++idx;
  
  IO_REG[idx] = new IORegister(idx, (EEType.POINTER).name(), OFF);
  ++idx;
  
  IO_REG[idx] = new IORegister(idx, (EEType.GLUE_GUN).name(), OFF);
  ++idx;
  
  IO_REG[idx] = new IORegister(idx, (EEType.WIELDER).name(), OFF);
  ++idx;
  
  for(; idx < IO_REG.length; idx += 1) {
    // Intialize the rest of the I/O registers
    IO_REG[idx] = new IORegister(idx, OFF);
  }
  
  /* Load all saved Programs */
  
  File progFile = new File( sketchPath("tmp/programs.bin") );
  
  if(progFile.exists()) {
    int ret = loadProgramBytes(progFile);
    
    if(ret == 0) {
      println("Successfully loaded programs!");
      fileFlags[0] = 0;
    } else {
      println("Failed to load programs ...");
    }
  }
  
  byte ret = 0;
  
  for (int bdx = 0; bdx < fileFlags.length; bdx += 1) {
    // Move each flag to a separate bit spot
    ret += (fileFlags[bdx] << bdx);
  }
  
  return ret;
}

/**
 * Saves all the Programs currently in ArrayList programs to the
 * given file.
 * 
 * @param dest  where to save all the programs
 * @return      0 if the save was successful,
 *              1 if dest could not be created or found,
 *              2 if an error occurs when saving the Programs
 */
private int saveProgramBytes(File dest) {
  
  try {
    // Create dest if it does not already exist
    if(!dest.exists()) {      
      try {
        dest.createNewFile();
        System.out.printf("Successfully created %s.\n", dest.getName());
      } catch (IOException IOEx) {
        System.out.printf("Could not create %s ...\n", dest.getName());
        IOEx.printStackTrace();
        return 1;
      }
    } 
    
    FileOutputStream out = new FileOutputStream(dest);
    DataOutputStream dataOut = new DataOutputStream(out);
    // Save the number of programs
    dataOut.writeInt(programs.size());
    
    for(Program prog : programs) {
      // Save each program
      saveProgram(prog, dataOut);
    }
    
    dataOut.close();
    out.close();
    return 0;
    
  } catch (FileNotFoundException FNFEx) {
    // Could not locate dest
    System.out.printf("%s does not exist!\n", dest.getName());
    FNFEx.printStackTrace();
    return 1;
    
  } catch (IOException IOEx) {
    // An error occrued with writing to dest
    System.out.printf("%s is corrupt!\n", dest.getName());
    IOEx.printStackTrace();
    return 2;
  }
}

/**
 * Loads all Programs stored in the given file. This method expects that the number of
 * programs to be stored is stored at the immediate beginning of the file as an integer.
 * Though, no more then 200 programs will be loaded.
 * 
 * @param src  The file from which to load the progarms
 * @return     0 if the load was successful,
 *             1 if src could not be found,
 *             2 if an error occured while reading the programs,
 *             3 if the end of the file is reached before all the expected programs are
 *               read
 */
private int loadProgramBytes(File src) {
  
  try {
    FileInputStream in = new FileInputStream(src);
    DataInputStream dataIn = new DataInputStream(in);
    // Read the number of programs stored in src
    int size = max(0, min(dataIn.readInt(), 200));
    
    while(size-- > 0) {
      // Read each program from src
      programs.add( loadProgram(dataIn) );
    }
    
    dataIn.close();
    in.close();
    return 0;
    
  } catch (FileNotFoundException FNFEx) {
    // Could not locate src
    System.out.printf("%s does not exist!\n", src.getName());
    FNFEx.printStackTrace();
    return 1;
    
  } catch (EOFException EOFEx) {
    // Reached the end of src unexpectedly
    System.out.printf("End of file, %s, was reached unexpectedly!\n", src.getName());
    EOFEx.printStackTrace();
    return 3;
    
  } catch (IOException IOEx) {
    // An error occured with reading from src
    System.out.printf("%s is corrupt!\n", src.getName());
    IOEx.printStackTrace();
    return 2;
  }
}

/**
 * Saves the data associated with the given Program to the given output stream.
 * Not all the Points in a programs Point array are stored: only the Points
 * associated with a MotionInstruction are saved after the MotionInstruction,
 * to which it belongs.
 * 
 * @param  p            The program to save
 * @param  out          The output stream to which to save the Program
 * @throws IOException  If an error occurs with saving the Program
 */
private void saveProgram(Program p, DataOutputStream out) throws IOException {
  
  if (p == null) {
    // Indicates a null value is saved
    out.writeByte(0);
    
  } else {
    // Indicates a non-null value is saved
    out.writeByte(1);
    
    out.writeUTF(p.name);
    out.writeInt(p.nextPosition);
    out.writeInt(p.instructions.size());
    // Save each instruction
    for(Instruction inst : p.instructions) {
      saveInstruction(inst, out);
      // Save only the Points associated with a MotionInstruction
      if(inst instanceof MotionInstruction) {
        savePoint(p.LPosReg[ ((MotionInstruction)inst).positionNum ], out);
      }
    }
  }
}

/**
 * Creates a program from data in the given input stream. A maximum of
 * 500 instructions will be read for a single program
 * 
 * @param in            The input stream to read from
 * @return              A program created from data in the input stream,
 *                      or null
 * @throws IOException  If an error occurs with reading from the input
 *                      stream
 */
private Program loadProgram(DataInputStream in) throws IOException {
  // Read flag byte
  byte flag = in.readByte();
  
  if (flag == 0) {
    return null;
    
  } else {
    // Read program name
    String name = in.readUTF();
    Program prog = new Program(name);
    // Read the next register value
    int nReg = in.readInt();
    prog.setNextPosition(nReg);
    // Read the number of instructions stored for this porgram
    int numOfInst = max(0, min(in.readInt(), 500));
    
    while(numOfInst-- > 0) {
      // Read each instruction
      Instruction inst = loadInstruction(in);
      prog.addInstruction(inst);
      // Read the points stored after each MotionIntruction
      if(inst instanceof MotionInstruction) {
        Point pt = loadPoint(in);
        prog.setPosition(((MotionInstruction)inst).positionNum, pt);
      }
    }
    
    return prog;
  }
}

/**
 * Saves the data associated with the given Point object to the file opened
 * by the given output stream. Null Points are saved a single zero byte.
 * 
 * @param   p            The Point of which to save the data
 * @param   out          The output stream used to save the Point
 * @throws  IOException  If an error occurs with writing the data of the Point
 */
private void savePoint(Point p, DataOutputStream out) throws IOException {
  
  if (p == null) {
    // Indicate a null value is saved
    out.writeByte(0);
    
  } else {
    // Indicate a non-null value is saved
    out.writeByte(1);
    // Write position of the point
    savePVector(p.position, out);
    // Write point's orientation
    saveRQuaternion(p.orientation, out);
    // Write the joint angles for the point's position
    saveFloatArray(p.angles, out);
    
    if (p.angles == null) {
      println("null angles!");
    }
  }
}

/**
 * Loads the data of a Point from the file opened by the given
 * input stream. It is possible that null will be returned by
 * this method if a null Point was saved.
 *
 * @param  in           The input stream used to read the data of
 *                      a Point
 * @return              The Point stored at the current position
 *                      of the input stream
 * @throws IOException  If an error occurs with reading the data
 *                      of the Point
 */
private Point loadPoint(DataInputStream in) throws IOException {
  // Read flag byte
  byte val = in.readByte();
  
  if (val == 0) {
    return null;
    
  } else {
    // Read the point's position
    PVector position = loadPVector(in);
    // Read the point's orientation
    RQuaternion orientation = loadRQuaternion(in);
    // Read the joint angles for the joint's position
    float[] angles = loadFloatArray(in);
    
    if (angles == null) {
      println("null angles!");   //<>// //<>// //<>// //<>// //<>// //<>// //<>//
    }
    
    return new Point(position, orientation, angles);
  }
}

/**
 * Saves the data stored in the given instruction to the file opened by the give output
 * stream. Currently, this method will only work for instructions of type: Motion, Frame
 * and Tool.
 * 
 * @param inst          The instruction of which to save the data
 * @pararm out          The output stream used to save the given instruction
 * @throws IOException  If an error occurs with saving the instruction
 */
private void saveInstruction(Instruction inst, DataOutputStream out) throws IOException {
  
  // Each Instruction subclass MUST have its own saving code block associated with its unique data fields
  if (inst instanceof MotionInstruction) {
    MotionInstruction m_inst = (MotionInstruction)inst;
    // Flag byte denoting this instruction as a MotionInstruction
    out.writeByte(2);
    // Write data associated with the MotionIntruction object
    out.writeBoolean(m_inst.isCommented());
    out.writeInt(m_inst.motionType);
    out.writeInt(m_inst.positionNum);
    out.writeBoolean(m_inst.isGPosReg);
    out.writeFloat(m_inst.speed);
    out.writeInt(m_inst.termination);
    out.writeInt(m_inst.userFrame);
    out.writeInt(m_inst.toolFrame);
    
    MotionInstruction subInst = m_inst.getSecondaryPoint();
    
    if (subInst != null) {
      // Save secondary point for circular instructions
      out.writeByte(1);
      saveInstruction(subInst, out);
      
    } else {
      // No secondary point
      out.writeByte(0);
    }
    
  } else if(inst instanceof FrameInstruction) {
    FrameInstruction f_inst = (FrameInstruction)inst;
    // Flag byte denoting this instruction as a FrameInstruction
    out.writeByte(3);
    // Write data associated with the FrameInstruction object
    out.writeBoolean(f_inst.isCommented());
    out.writeInt(f_inst.frameType);
    out.writeInt(f_inst.frameIdx);
    
  } else if(inst instanceof IOInstruction) {
    IOInstruction t_inst = (IOInstruction)inst;
    // Flag byte denoting this instruction as a ToolInstruction
    out.writeByte(4);
    // Write data associated with the ToolInstruction object
    out.writeBoolean(t_inst.isCommented());
    out.writeInt(t_inst.reg);
    out.writeInt( saveint(t_inst.state) );
    
  } else if(inst instanceof LabelInstruction) {
    LabelInstruction l_inst = (LabelInstruction)inst;
    
    out.writeByte(5);
    out.writeBoolean(l_inst.isCommented());
    out.writeInt(l_inst.labelNum);
    
  } else if(inst instanceof JumpInstruction) {
    JumpInstruction j_inst = (JumpInstruction)inst;
    
    out.writeByte(6);
    out.writeBoolean(j_inst.isCommented());
    out.writeInt(j_inst.tgtLblNum);
    
  } else if (inst instanceof CallInstruction) {
    CallInstruction c_inst = (CallInstruction)inst;
    
    out.writeByte(7);
    out.writeBoolean(c_inst.isCommented());
    out.writeInt(c_inst.getProgIdx());
    
  } else if (inst instanceof RegisterStatement) {
    RegisterStatement rs = (RegisterStatement)inst;
    Register r = rs.getReg();
    
    out.writeByte(8);
    out.writeBoolean(rs.isCommented());
    
    // In what type of register will the result of the statement be placed?
    int regType;
    
    if (r instanceof IORegister) {
      regType = 2;
      
    } else if (r instanceof PositionRegister) {
      regType = 1;
      
    } else {
      regType = 0;
    }
    
    out.writeInt(regType);
    out.writeInt(r.getIdx());
    out.writeInt(rs.getPosIdx());
    
    saveExpression(rs.getExpression(), out);
  
  }/* Add other instructions here! */
    else if (inst instanceof Instruction) {
    /// A blank instruction
    out.writeByte(1);
    out.writeBoolean(inst.isCommented());
    
  } else {
    // Indicate a null-value is saved
    out.writeByte(0);
  }
  
  
}

/**
 * The next instruction stored in the file opened by the given input stream
 * is read, created, and returned. This method is currently only functional
 * for instructions of type: Motion, Frame, and Tool.
 *
 * @param in            The input stream from which to read the data of an
 *                      instruction
 * @return              The instruction saved at the current position of the
 *                      input stream
 * @throws IOException  If an error occurs with reading the data of the
 *                      instruciton
 */
private Instruction loadInstruction(DataInputStream in) throws IOException {
  Instruction inst = null;
  // Read flag byte
  byte instType = in.readByte();
  
  if(instType == 2) {
    // Read data for a MotionInstruction object
    boolean isCommented = in.readBoolean();
    int mType = in.readInt();
    int reg = in.readInt();
    boolean isGlobal = in.readBoolean();
    float spd = in.readFloat();
    int term = in.readInt();
    int uFrame = in.readInt();
    int tFrame = in.readInt();
    
    inst = new MotionInstruction(mType, reg, isGlobal, spd, term, uFrame, tFrame);
    inst.setIsCommented(isCommented);
    
    byte flag = in.readByte();
    
    if (flag == 1) {
      // Load the second point associated with a circular type motion instruction
     ((MotionInstruction)inst).setSecondaryPoint((MotionInstruction)loadInstruction(in));
    }
    
  } else if(instType == 3) {
    // Read data for a FrameInstruction object
    boolean isCommented = in.readBoolean();
    inst = new FrameInstruction( in.readInt(), in.readInt() );
    inst.setIsCommented(isCommented);
    
  } else if(instType == 4) {
    // Read data for a ToolInstruction object
    boolean isCommented = in.readBoolean();
    int reg = in.readInt();
    int setting = in.readInt();
    
    inst = new IOInstruction(reg, loadint(setting));
    inst.setIsCommented(isCommented);
    
  } else if (instType == 5) {
    boolean isCommented = in.readBoolean();
    int labelNum = in.readInt();
    
    inst = new LabelInstruction(labelNum);
    inst.setIsCommented(isCommented);
    
  } else if (instType == 6) {
    boolean isCommented = in.readBoolean();
    int tgtLabelNum = in.readInt();
    
    inst = new JumpInstruction(tgtLabelNum);
    inst.setIsCommented(isCommented);
    
  } else if (instType == 7) {
    boolean isCommented = in.readBoolean();
    int pdx = in.readInt();
    
    inst = new CallInstruction(pdx);
    inst.setIsCommented(isCommented);
    
  } else if (instType == 8) {
    boolean isCommented = in.readBoolean();
    int regType = in.readInt();
    int regIdx = in.readInt();
    int posIdx = in.readInt();
    Expression expr = loadExpression(in);
    
    if (regType == 2) {
      inst = new RegisterStatement(DAT_REG[regIdx], expr);
      inst.setIsCommented(isCommented);
      
    } else if (regType == 1) {
      inst = new RegisterStatement(GPOS_REG[regIdx], posIdx, expr);
      inst.setIsCommented(isCommented);
      
    } else if (regType == 0) {
      inst = new RegisterStatement(IO_REG[regIdx], expr);
      inst.setIsCommented(isCommented);
      
    }
    
  }/* Add other instructions here! */
    else if (instType == 1) {
    inst = new Instruction();
    boolean isCommented = in.readBoolean();
    inst.setIsCommented(isCommented);
    
  } else {
    return null;
  }
  
  return inst;
}

/**
 * Convert the given End Effector status
 * to a unique integer value.
 */
private int saveint(int stat) {
  switch (stat) {
    case ON:  return 0;
    case OFF: return 1;
    default:  return -1;
  }
}

/**
 * Converts a valid integer value to its
 * corresponding End Effector Status.
 */
private int loadint(int val) {
  switch (val) {
    case 0:   return ON;
    case 1:   return OFF;
    default:  return -1;
  }
}

/**
 * Given a valid file path, both the Tool Frame and then the User
 * Frame sets are saved to the file. First the length of a list
 * is saved and then its respective elements.
 *
 * @param dest  the file to which the frame sets will be saved
 * @return      0 if successful,
 *              1 if dest could not be created or found
 *              2 if an error occurs with writing to the file
 */
private int saveFrameBytes(File dest) {
  
  try {
    // Create dest if it does not already exist
    if(!dest.exists()) {
      try {
        dest.createNewFile();
        System.out.printf("Successfully created %s.\n", dest.getName());
      } catch (IOException IOEx) {
        System.out.printf("Could not create %s ...\n", dest.getName());
        IOEx.printStackTrace();
      }
    }
    
    FileOutputStream out = new FileOutputStream(dest);
    DataOutputStream dataOut = new DataOutputStream(out);
    
    // Save Tool Frames
    dataOut.writeInt(toolFrames.length);
    for(Frame frame : toolFrames) {
      saveFrame(frame, dataOut);
    }
    
    // Save User Frames
    dataOut.writeInt(userFrames.length);
    for(Frame frame : userFrames) {
      saveFrame(frame, dataOut);
    }
    
    dataOut.close();
    out.close();
    return 0;
    
  } catch (FileNotFoundException FNFEx) {
    // Could not find dest
    System.out.printf("%s does not exist!\n", dest.getName());
    FNFEx.printStackTrace();
    return 1;
    
  } catch (IOException IOEx) {
    // Error with writing to dest
    System.out.printf("%s is corrupt!\n", dest.getName());
    IOEx.printStackTrace();
    return 2;
  }
}

/**
 * Loads both the Tool and User Frames from the file path denoted
 * by the given String. The Tool Frames are expected to come before
 * the Usser Frames. In addition, it is expected that both frame
 * sets store the length of the set before the first element.
 * 
 * @param src  the file, which contains the data for the Tool and
 *             User Frames
 * @return     0 if successful,
 *             1 if an error occurs with accessing the give file
 *             2 if an error occurs with reading from the file
 *             3 if the end of the file is reached before reading
 *             all the data for the frames
 */
private int loadFrameBytes(File src) {
  int idx = -1;
  
  try {
    FileInputStream in = new FileInputStream(src);
    DataInputStream dataIn = new DataInputStream(in);
    
    // Load Tool Frames
    int size = max(0, min(dataIn.readInt(), 10));
    toolFrames = new ToolFrame[size];
    
    
    for(idx = 0; idx < size; ++idx) {
      toolFrames[idx] = loadFrame(dataIn);
    }
    
    // Load User Frames
    size = max(0, min(dataIn.readInt(), 10));
    userFrames = new UserFrame[size];
    
    for(idx = 0; idx < size; ++idx) {
      userFrames[idx] = loadFrame(dataIn);
    }
    
    dataIn.close();
    in.close();
    return 0;
    
  } catch (FileNotFoundException FNFEx) {
    // Could not find src
    System.out.printf("%s does not exist!\n", src.getName());
    FNFEx.printStackTrace();
    return 1;
    
  } catch (EOFException EOFEx) {
    // Reached the end of src unexpectedly
    System.out.printf("End of file, %s, was reached unexpectedly!\n", src.getName());
    EOFEx.printStackTrace();
    return 3;
    
  } catch (IOException IOEx) {
    // Error with reading from src
    System.out.printf("%s is corrupt!\n", src.getName());
    IOEx.printStackTrace();
    return 2;
  }
}

/**
 * Saves the data of the given frame's origin, orientation and axes vectors
 * to the file opened by the given DataOutputStream.
 * 
 * @param f    A non-null frame object
 * @param out  An output stream used to write the given frame to a file
 * @throw IOException  if an error occurs with writing the frame to the file
 */
private void saveFrame(Frame f, DataOutputStream out) throws IOException {
  
  // Save a flag to indicate what kind of frame was saved
  if (f == null) {
    out.writeByte(0);
    return;
    
  } else if (f instanceof ToolFrame) {
    out.writeByte(1);
  
  } else if (f instanceof UserFrame) {
    out.writeByte(2);
  
  } else {
    throw new IOException("Invalid Frame!");
  }
  
  if (f instanceof UserFrame) {
    // Write User frame origin
    savePVector(f.getOrigin(), out);
    
  } else {
    // Write Tool frame TCP offset
    savePVector( ((ToolFrame)f).getTCPOffset(), out );
  }
  
  // Write frame axes
  saveRQuaternion(f.orientationOffset, out);
  
  // Write frame orientation points
  for (Point pt : f.axesTeachPoints) {
    savePoint(pt, out);
  }
  
  // Write frame manual entry origin value
  savePVector(f.DEOrigin, out);
  // Write frame manual entry origin value
  saveRQuaternion(f.DEOrientationOffset, out);
  
  if (f instanceof ToolFrame) {
    ToolFrame tFrame = (ToolFrame)f;
    // Save points for the TCP teaching of the frame
    for (Point p : tFrame.TCPTeachPoints) {
      savePoint(p, out);
    }
    
  } else {
    // Save point for the origin offset of the frame
    savePoint( ((UserFrame)f).orientOrigin, out );
  }
}

/**
 * Loads the data associated with a Frame object (origin,
 * orientation and axes vectors) from the file opened by
 * the given DataOutputStream.
 *
 * @param out  An input stream used to read from a file
 * @return     The next frame stored in the file
 * @throw IOException  if an error occurs while reading the frame
 *                     from to the file
 */
private Frame loadFrame(DataInputStream in) throws IOException {
  
  Frame f = null;
  byte type = in.readByte();
  
  if (type == 0) {
    return null;
  } else if (type == 1) {
    f = new ToolFrame();
  
  } else if (type == 2) {
    f = new UserFrame();
  
  } else {
    //println(type);
    throw new IOException("Invalid Frame type!");
  }
  
  PVector v = loadPVector(in);
  
  if (f instanceof UserFrame) {
    // Read origin value
    ((UserFrame)f).setOrigin(v);
  } else {
    // Read TCP offset values
    ((ToolFrame)f).setTCPOffset(v);
  }

  // Read axes quaternion values
  f.setOrientation( loadRQuaternion(in) );
  
  // Read origin values
  f.axesTeachPoints = new Point[3];
  // Read in orientation points
  for (int idx = 0; idx < 3; ++idx) {
    f.axesTeachPoints[idx] = loadPoint(in);
  }
  
  // Read manual entry origin values
  f.DEOrigin = loadPVector(in);
  f.DEOrientationOffset = loadRQuaternion(in);
  
  if (f instanceof ToolFrame) {
    ToolFrame tFrame = (ToolFrame)f;
    
    // Load points for the TCP teaching of the frame
    for (int idx = 0; idx < 3; ++idx) {
      tFrame.TCPTeachPoints[idx] = loadPoint(in);
    }
    
  } else {
    // Load point for the origin offset of the frame
    ((UserFrame)f).orientOrigin = loadPoint(in);
  }
  
  return f;
}

/**
 * Saves all initialized Register and Position Register Entries with their
 * respective indices in their respective lists to dest. In addition, the
 * number of Registers and Position Registers saved is saved to the file
 * before each respective set of entries.
 * 
 * @param dest  Some binary file to which to save the Register entries
 * @return      0 if the save was successful,
 *              1 if dest could not be found pr created
 *              2 if an error occrued while writing to dest
 */
private int saveRegisterBytes(File dest) {
  
  try {
    
    // Create dest if it does not already exist
    if(!dest.exists()) {
      try {
        dest.createNewFile();
        System.out.printf("Successfully created %s.\n", dest.getName());
      } catch (IOException IOEx) {
        System.out.printf("Could not create %s ...\n", dest.getName());
        IOEx.printStackTrace();
      }
    }
    
    FileOutputStream out = new FileOutputStream(dest);
    DataOutputStream dataOut = new DataOutputStream(out);
    
    int numOfREntries = 0,
    numOfPREntries = 0;
    
    ArrayList<Integer> initializedR = new ArrayList<Integer>(),
    initializedPR = new ArrayList<Integer>();
    
    // Count the number of initialized entries and save their indices
    for(int idx = 0; idx < DAT_REG.length; ++idx) {
      if(DAT_REG[idx].value != null || DAT_REG[idx].comment != null) {
        initializedR.add(idx);
        ++numOfREntries;
      }
      
      if(GPOS_REG[idx].point != null || GPOS_REG[idx].comment != null) {
        initializedPR.add(idx);
        ++numOfPREntries;
      }
    }
    
    dataOut.writeInt(numOfREntries);
    // Save the Register entries
    for(Integer idx : initializedR) {
      dataOut.writeInt(idx);
      
      if(DAT_REG[idx].value == null) {
        // save for null Float value
        dataOut.writeFloat(Float.NaN);
      } else {
        dataOut.writeFloat(DAT_REG[idx].value);
      }
      
      if(DAT_REG[idx].comment == null) {
        dataOut.writeUTF("");
      } else {
        dataOut.writeUTF(DAT_REG[idx].comment);
      }
    }
    
    dataOut.writeInt(numOfPREntries);
    // Save the Position Register entries
    for(Integer idx : initializedPR) {
      dataOut.writeInt(idx);
      savePoint(GPOS_REG[idx].point, dataOut);
      
      if(GPOS_REG[idx].comment == null) {
        dataOut.writeUTF("");
      } else {
        dataOut.writeUTF(GPOS_REG[idx].comment);
      }
      
      dataOut.writeBoolean(GPOS_REG[idx].isCartesian);
    }
    
    dataOut.close();
    out.close();
    return 0;
    
  } catch (FileNotFoundException FNFEx) {
    // Could not be located dest
    System.out.printf("%s does not exist!\n", dest.getName());
    FNFEx.printStackTrace();
    return 1;
    
  } catch (IOException IOEx) {
    // Error occured while reading from dest
    System.out.printf("%s is corrupt!\n", dest.getName());
    IOEx.printStackTrace();
    return 2;
  }
}

/**
 * Loads all the saved Registers and Position Registers from the
 * given binary file. It is expected that the number of entries
 * saved for both the Registers and Position Registers exist in the
 * file before each respective list. Also, the index of an entry
 * in the Register (or Position Register) list should also exist
 * before each antry in the file.
 * 
 * @param src  The binary file from which to load the Register and
 *             Position Register entries
 * @return     0 if the load was successful,
 *             1 if src could not be located,
 *             2 if an error occured while reading from src
 *             3 if the end of file is reached in source, before
 *               all expected entries were read
 */
private int loadRegisterBytes(File src) {
  
  try {
    FileInputStream in = new FileInputStream(src);
    DataInputStream dataIn = new DataInputStream(in);
    
    int size = max(0, min(dataIn.readInt(), DAT_REG.length));
    
    // Load the Register entries
    while(size-- > 0) {
      // Each entry is saved after its respective index in REG
      int reg = dataIn.readInt();
      
      Float v = dataIn.readFloat();
      // Null values are saved as NaN
      if(Float.isNaN(v)) { v = null; }
      
      String c = dataIn.readUTF();
      // Null comments are saved as ""
      if(c.equals("")) { c = null; }
      
      DAT_REG[reg] = new DataRegister(reg, c, v);
    }
    
    size = max(0, min(dataIn.readInt(), GPOS_REG.length));
    
    // Load the Position Register entries
    while(size-- > 0) {
      // Each entry is saved after its respective index in POS_REG
      int idx = dataIn.readInt();
      
      Point p = loadPoint(dataIn);
      String c = dataIn.readUTF();
      // Null comments are stored as ""
      if(c == "") { c = null; }
      boolean isCartesian = dataIn.readBoolean();
      
      GPOS_REG[idx] = new PositionRegister(idx, c, p, isCartesian);
    }
    
    dataIn.close();
    in.close();
    return 0;
    
  } catch (FileNotFoundException FNFEx) {
    // Could not be located src
    System.out.printf("%s does not exist!\n", src.getName());
    FNFEx.printStackTrace();
    return 1;
    
  } catch (EOFException EOFEx) {
    // Unexpectedly reached the end of src
    System.out.printf("End of file, %s, was reached unexpectedly!\n", src.getName());
    EOFEx.printStackTrace();
    return 3;
    
  } catch (IOException IOEx) {
    // Error occrued while reading from src
    System.out.printf("%s is corrupt!\n", src.getName());
    IOEx.printStackTrace();
    return 2;
  }
}

/**
 * Saves all the scenarios stored in SCENARIOS to given
 * destination binary file.
 * 
 * @param dest  The binary file to which to save the scenarios
 * @returning   0  if the saving of scenarios is successful,
 *              1  if the file could not be found,
 *              2  if some other error occurs with writing
 *                 to dest
 */
private int saveScenarioBytes(File dest) {
  
  try {
     
    // Create dest if it does not already exist
    if(!dest.exists()) {
      try {
        dest.createNewFile();
        System.out.printf("Successfully created %s.\n", dest.getName());
      } catch (IOException IOEx) {
        System.out.printf("Could not create %s ...\n", dest.getName());
        IOEx.printStackTrace();
      }
    }
    
    FileOutputStream out = new FileOutputStream(dest);
    DataOutputStream dataOut = new DataOutputStream(out);
    
    int numOfScenarios = SCENARIOS.size();
    // Save the number of scenarios
    dataOut.writeInt(numOfScenarios);
    
    if (activeScenario == null) {
      // No active scenario
      dataOut.writeUTF("");
    } else {
      // Save the name of the active scenario
      dataOut.writeUTF(activeScenario.getName());
    }
    
    // Save all the scenarios
    for (int sdx = 0; sdx < SCENARIOS.size(); ++sdx) {
      Scenario s = SCENARIOS.get(sdx);
      
      if (s.getName().equals( activeScenario.getName() )) {
        // Update the previous version of the active scenario
        s = (Scenario)activeScenario.clone();
        SCENARIOS.set(sdx, s);
      }
      
      saveScenario(s, dataOut);
    }
    
    dataOut.close();
    out.close();
    return 0;
    
  } catch (FileNotFoundException FNFEx) {
    // Could not be located dest
    System.out.printf("%s does not exist!\n", dest.getName());
    FNFEx.printStackTrace();
    return 1;
    
  } catch (IOException IOEx) {
    // Error occrued while writing to dest
    System.out.printf("%s is corrupt!\n", dest.getName());
    IOEx.printStackTrace();
    return 2;
    
  }
}

/**
 * Attempts to load scenarios from the given binary file.
 * It is expected that an integer representing the number
 * of scenarios is saved in the file first, followed by the
 * index of the previously active scenario, and finally at
 * least that number of scenarios.
 * 
 * @param src  The binary file from which to read scenarios
 * @returning  0  if loading was succssful,
 *             1  if the file could not be found,
 *             2  if the file is corrupt,
 *             3  if the end of file is reached unexpectedly,
 *             4  if an error occurs with loading a .stl file
 *                for the shape of a world object
 */
private int loadScenarioBytes(File src) {
  
  try {
    FileInputStream in = new FileInputStream(src);   //<>// //<>// //<>// //<>// //<>// //<>// //<>//
    DataInputStream dataIn = new DataInputStream(in);
    
    int numOfScenarios = dataIn.readInt();
    String activeScenarioName = dataIn.readUTF();
    
    // Load all scenarios saved
    while (numOfScenarios-- > 0) {
      Scenario s = loadScenario(dataIn);
      
      if (s.getName().equals(activeScenarioName)) {
        // Set the active scenario
        activeScenario = (Scenario)s.clone();
      }
      
      SCENARIOS.add(s);
    }
    
    dataIn.close();
    in.close();
    return 0;
    
  } catch (FileNotFoundException FNFEx) {
    // Could not be located src
    System.out.printf("%s does not exist!\n", src.getName());
    FNFEx.printStackTrace();
    return 1;
    
  } catch (EOFException EOFEx) {
    // Unexpectedly reached the end of src
    System.out.printf("End of file, %s, was reached unexpectedly!\n", src.getName());
    EOFEx.printStackTrace();
    return 3;
    
  } catch (IOException IOEx) {
    // Error occrued while reading from src
    System.out.printf("%s is corrupt!\n", src.getName());
    IOEx.printStackTrace();
    return 2;
    
  } catch (NullPointerException NPEx) {
    // Error with loading a .stl model
    System.out.printf("Missing source file!\n");
    NPEx.printStackTrace();
    return 4;
  }
}

private void saveExpression(Expression e, DataOutputStream out) throws IOException {
  
  if (e == null) {
    // Indicate the object saved is null
    out.writeByte(0);
    
  } else {
    out.writeByte(1);
    
    int exprLen = e.getSize();
    // Save the length of the expression
    out.writeInt(exprLen);
    
    // Save each expression element
    for (int idx = 0; idx < exprLen; ++idx) {
      saveExpressionElement(e.get(idx), out);
    }
  }
}

private Expression loadExpression(DataInputStream in) throws IOException, ClassCastException {
  byte nullFlag = in.readByte();
  
  if (nullFlag == 1) {
    Expression e = new Expression();
    // Read in expression length
    int len = in.readInt();
    
    for (int idx = 0; idx < len; ++idx) {
      // Read in an expression element
      ExpressionElement ee = loadExpressionElement(in);
      
      e.insertElement(idx);
      // Add it to the expression
      if (ee instanceof Operator) {
        e.setOperator(idx, (Operator)ee);
        
      } else {
        e.setOperand(idx, (ExprOperand)ee);
      }
    }
    
    return e;
  }
  
  return null;
}

private void saveExpressionElement(ExpressionElement ee, DataOutputStream out) throws IOException {
  
  if (ee == null) {
    // Indicate the object saved is null
    out.writeByte(0);
    
  } else {
    
    if (ee instanceof Operator) {
      // Operator
      Operator op = (Operator)ee;
      
      out.writeByte(1);
      out.writeInt( op.getOpID() );
      
    } else if (ee instanceof AtomicExpression) {
      // Subexpression
      AtomicExpression ae = (AtomicExpression)ee;
      
      out.writeByte(2);
      saveExpressionElement(ae.arg1, out);
      saveExpressionElement(ae.arg2, out);
      saveExpressionElement(ae.op, out);
      
    } if (ee instanceof ExprOperand) {
      ExprOperand eo = (ExprOperand)ee;
      
      out.writeByte(3);
      // Indicate that the object is non-null
      out.writeInt(eo.type);
      
      if (eo.type == ExpressionElement.FLOAT) {
        // Constant float
        out.writeFloat( eo.getDataVal() );
        
      } else if (eo.type == ExpressionElement.BOOL) {
        // Constant boolean
        out.writeBoolean( eo.getBoolVal() );
        
      } else if (eo.type == ExpressionElement.DREG ||
                 eo.type == ExpressionElement.IOREG ||
                 eo.type == Expression.PREG ||
                 eo.type == ExpressionElement.PREG_IDX) {
        
        // Data, Position, or IO register
        out.writeInt( eo.getRdx() );
        
        if (eo.type == ExpressionElement.PREG_IDX) {
          // Specific portion of a point
          out.writeInt( eo.getPosIdx() );
        }
        
      } else if (eo.type == ExpressionElement.POSTN) {
        // Robot position
        savePoint(eo.getPointVal(), out);
        
      } // Otherwise it is unitialized
    } 
  }
}

private ExpressionElement loadExpressionElement(DataInputStream in) throws
          IOException, ClassCastException {
  
  byte nullFlag = in.readByte();
  
  if (nullFlag == 1) {
    // Read in an operator
    int opFlag = in.readInt();
    return Operator.getOpFromID(opFlag);
    
  } else if (nullFlag == 2) {
    // Read in an atomic expression operand
    ExprOperand a0 = (ExprOperand)loadExpressionElement(in);
    ExprOperand a1 = (ExprOperand)loadExpressionElement(in);
    Operator op = (Operator)loadExpressionElement(in);
    
    return new AtomicExpression(a0, a1, op);
    
  } else if (nullFlag == 3) {
    // Read in a normal operand
    ExprOperand eo;
    int opType = in.readInt();
    
    if (opType == ExpressionElement.FLOAT) {
      // Constant float
      Float val = in.readFloat();
      eo = new ExprOperand(val);
      
    } else if (opType == ExpressionElement.BOOL) {
      // Constant boolean
      Boolean val = in.readBoolean();
      eo = new ExprOperand(val);
      
    } else if (opType == ExpressionElement.DREG ||
               opType == ExpressionElement.IOREG ||
               opType == Expression.PREG ||
               opType == ExpressionElement.PREG_IDX) {
      // Note: the register value of the operand is set to null!
      
      // Data, Position, or IO register
      Integer rdx = in.readInt();
      
      if (opType == ExpressionElement.DREG) {
        // Data register
        return new ExprOperand(DAT_REG[rdx], rdx);
        
      } else if (opType == ExpressionElement.PREG) {
        // Position register
        eo = new ExprOperand(GPOS_REG[rdx], rdx);
        
      } else if (opType == ExpressionElement.PREG_IDX) {
        // Specific portion of a point
        Integer pdx = in.readInt();
        eo = new ExprOperand(GPOS_REG[rdx], rdx, pdx);
        
      } else if (opType == ExpressionElement.IOREG) {
        // I/O register
        eo = new ExprOperand(IO_REG[rdx], rdx);
        
      } else {
        eo = new ExprOperand();
      }
      
      return eo;
      
    } else if (opType == ExpressionElement.POSTN) {
      // Robot position
      Point pt = loadPoint(in);
      return new ExprOperand(pt);
    }
  }
  
  // Uninitialized
  return new ExprOperand();
}

/**
 * Saveds all the data associated with a scenario to the given output stream.
 * First a single flag byte is saved to the stream followed by the number of
 * objects in the scenario and then exxactly that number of world objects.
 * 
 * @param s    The scenario to save
 * @param out  The output stream to which to save the scenario
 * @throws     IOException if an erro occurs with writing to the output stream
 */
private void saveScenario(Scenario s, DataOutputStream out) throws IOException {
  
  if (s == null) {
    // Indicate the value saved is null
    out.writeByte(0);
    
  } else {
    // Indicate the value saved is non-null
    out.writeByte(1);
    // Write the name of the scenario
    out.writeUTF(s.getName());
    // Save the number of world objects in the scenario
    out.writeInt( s.size() );
    
    for (WorldObject wldObj : s) {
      // Save all the world objects associated with the scenario
      saveWorldObject(wldObj, out);  
    }
  }
}

/**
 * Attempts to load the data of a Scenario from the given input stream. It is expected that
 * the stream contains a single byte (the flag byte) followed by a String representing the
 * name of the scenario. After the name of the scenario, there should be an positive integer
 * value followed by exactly that many world objects.
 * 
 * @param in   The input stream from which to read bytes
 * @returning  The Scenario pulled from the input stream
 * @throws     IOException  if an error occurs with reading from the input stream
 *             NullPointerException  if a world object has a model shape, whose source file is
 *             corrupt or missing
 */
private Scenario loadScenario(DataInputStream in) throws IOException, NullPointerException {
  // Read flag byte
  byte flag = in.readByte();   //<>// //<>// //<>// //<>// //<>// //<>// //<>//
  
  if (flag == 0) {
    return null;
    
  } else {
    // Read the name of the scenario
    String name = in.readUTF();
    Scenario s = new Scenario(name);
    // An extra set of only the loaded fixtures
    ArrayList<Fixture> fixtures = new ArrayList<Fixture>();
    // A list of parts which have a fixture reference defined
    ArrayList<LoadedPart> partsWithReferences = new ArrayList<LoadedPart>();
    
    // Read the number of objects in the scenario
    int size = in.readInt();
    // Read all the world objects contained in the scenario
    while (size-- > 0) {
      Object loadedObject = loadWorldObject(in);
      
      if (loadedObject instanceof WorldObject) {
        // Add all normal world objects to the scenario
        s.addWorldObject( (WorldObject)loadedObject );
        
        if (loadedObject instanceof Fixture) {
          // Save an extra reference of each fixture
          fixtures.add( (Fixture)loadedObject );
        }
        
      } else if (loadedObject instanceof LoadedPart) {
        LoadedPart lPart = (LoadedPart)loadedObject;
        
        if (lPart.part != null) {
          // Save the part in the scenario
          s.addWorldObject(lPart.part);
          
          if (lPart.referenceName != null) {
            // Save any part with a defined reference
            partsWithReferences.add(lPart);
          }
        }
      }
    }
    
    // Set all the Part's references
    for (LoadedPart lPart : partsWithReferences) {
      for (Fixture f : fixtures) {
        if (lPart.referenceName.equals(f.getName())) {
          lPart.part.setFixtureRef(f);
        }
      }
    }
    
    return s;
  }
}

/**
 * Saved all the fields associated with the given world object to the given data output
 * stream. First a single byte (the flag byte) is saved to the stream followed by the
 * name and shape of the object and finally the fields associated with subclass of the object.
 * 
 * @param wldObj  The world object to save
 * @param out     The output stream to which to save the world object
 * @throws        IOException if an error occurs with writing to the output stream
 */
private void saveWorldObject(WorldObject wldObj, DataOutputStream out) throws IOException {
  
  if (wldObj == null) {   //<>// //<>// //<>// //<>// //<>// //<>// //<>//
    // Indicate that the value saved is null
    out.writeByte(0);
    
  } else {
    if (wldObj instanceof Part) {
      // Indicate that the value saved is a Part
      out.writeByte(1);
    } else if (wldObj instanceof Fixture) {
      // Indicate that the value saved is a Fixture
      out.writeByte(2);
    }
    
    // Save the name and form of the object
    out.writeUTF(wldObj.getName());
    saveShape(wldObj.getForm(), out);
    // Save the local orientation of the object
    savePVector(wldObj.getLocalCenter(), out);
    saveFloatArray2D(wldObj.getLocalOrientationAxes(), out);
    
    if (wldObj instanceof Part) {
      Part part = (Part)wldObj;
      String refName = "";
      
      savePVector(part.getOBBDims(), out);
      
      if (part.getFixtureRef() != null) {
        // Save the name of the part's fixture reference
        refName = part.getFixtureRef().getName();
      }
      
      out.writeUTF(refName);
    }
  }
}

/**
* TODO recomment this
 * Attempts to load the data associated with a world object from the given data input stream. It
 * is expected that the input stream contains a single byte (for the flag byte) followed by the
 * name and shape of the object, which is followde by the data specific to the object's subclass.
 * 
 * @param in   The input stream from which to read bytes
 * @returning  The world object pulled from the input streaam (which can be null!)
 * @throws     IOException  if an error occurs with rading from the input stream
 *             NullPointerExpcetion  if the world object has a model shape and its source file is
 *             corrupt or missing
 */
private Object loadWorldObject(DataInputStream in) throws IOException, NullPointerException {
  // Load the flag byte
  byte flag = in.readByte();   //<>// //<>// //<>// //<>// //<>// //<>// //<>//
  Object wldObjFields = null;
  
  if (flag != 0) {
    // Load the name and shape of the object
    String name = in.readUTF();
    Shape form = loadShape(in);
    // Load the object's local orientation
    PVector center = loadPVector(in);
    float[][] orientationAxes = loadFloatArray2D(in);
    CoordinateSystem localOrientation = new CoordinateSystem();
    localOrientation.setOrigin(center);
    localOrientation.setAxes(orientationAxes);
    
    if (flag == 1) {
      // Load the part's bounding-box and fixture reference name
      PVector OBBDims = loadPVector(in);
      String refName = in.readUTF();
      
      if (refName.equals("")) {
        // A part object
        wldObjFields = new Part(name, form, OBBDims, localOrientation, null);
      } else {
        // A part object with its reference's name
        wldObjFields = new LoadedPart( new Part(name, form, OBBDims, localOrientation, null), refName );
      }
      
    } else if (flag == 2) {
      // A fixture object
      wldObjFields = new Fixture(name, form, localOrientation);
    } 
  }
  
  return wldObjFields;
}

/**
 * Saves all the data associated with the given shape, in the form of bytes,
 * to the given data output stream. First flag byte is saved, which indicates
 * what subclass the object is (or if the object is null). Then the fields
 * associated with the subclass saved followed by the color fields common among
 * all shapes.
 * 
 * @param shape  The shape to save
 * @param out    The output stream, to which to save the given shape
 * @throws       IOException  if an error occurs with writing to the output stream
 */
private void saveShape(Shape shape, DataOutputStream out) throws IOException {
  if (shape == null) {
    // Indicate the saved value is null
    out.writeByte(0);
    
  } else {
    if (shape instanceof Box) {
      // Indicate the saved value is a box
      out.writeByte(1);
    } else if (shape instanceof Cylinder) {
      // Indicate the value saved is a cylinder
      out.writeByte(2);
    } else if (shape instanceof ModelShape) {
      // Indicate the value saved is a complex shape
      out.writeByte(3);
    }
    
    // Write fill color value
    saveInteger(shape.getFillValue(), out);
    
    if (shape instanceof Box) {
      // Write stroke value
      saveInteger(shape.getStrokeValue(), out);
      // Save length, height, and width of the box
      out.writeFloat(shape.getDim(DimType.LENGTH));
      out.writeFloat(shape.getDim(DimType.HEIGHT));
      out.writeFloat(shape.getDim(DimType.WIDTH));
      
    } else if (shape instanceof Cylinder) {
      // Write stroke value
      saveInteger(shape.getStrokeValue(), out);
      // Save the radius and height of the cylinder
      out.writeFloat(shape.getDim(DimType.RADIUS));
      out.writeFloat(shape.getDim(DimType.HEIGHT));
      
    } else if (shape instanceof ModelShape) {
      ModelShape m = (ModelShape)shape;
      
      out.writeFloat(m.getDim(DimType.SCALE));
      // Save the source path of the complex shape
      out.writeUTF(m.getSourcePath()); 
    }
  }
}

/**
 * Attempts to load a Shape from the given data input stream. It is expected that the
 * stream contains a single byte (the flag byte) followed by the fields unique to the
 * subclass of the Shape object saved, which are followed by the color fields of the Shape.
 * 
 * @param in   The input stream, from which to read bytes
 * @returning  The shape object pulled from the input stream (which can be null!)
 * @throws     IOException  if an error occurs with reading from the input stream
 *             NullPointerException  if the shape stored is a model shape and its source
 *             file is either invalid or does not exist
 */
private Shape loadShape(DataInputStream in) throws IOException, NullPointerException {
  // Read flag byte
  byte flag = in.readByte();
  Shape shape = null;
  
  if (flag != 0) {
    // Read fiil color
    Integer fill = loadInteger(in);
          
    if (flag == 1) {
      // Read stroke color
      Integer strokeVal = loadInteger(in);
      float x = in.readFloat(),
            y = in.readFloat(),
            z = in.readFloat();
      // Create a box
      shape = new Box(fill, strokeVal, x, y, z);
      
    } else if (flag == 2) {
      // Read stroke color
      Integer strokeVal = loadInteger(in);
      float radius = in.readFloat(),
            hgt = in.readFloat();
      // Create a cylinder
      shape = new Cylinder(fill, strokeVal, radius, hgt);
      
    } else if (flag == 3) {
      float scale = in.readFloat();
      String srcPath = in.readUTF();
      
      // Creates a complex shape from the srcPath located in RobotRun/data/
      shape = new ModelShape(srcPath, fill, scale);
    }
  }
  
  return shape;
}

/**
 * Writes the integer object to the given data output stream. Null values are accepted.
 */
private void saveInteger(Integer i, DataOutputStream out) throws IOException {
  
  if (i == null) {
    // Write byte flag
    out.writeByte(0);
    
  } else {
    // Write byte flag
    out.writeByte(1);
    // Write integer value
    out.writeInt(i);
  }
}

/**
 * Attempts to read an Integer object from the given data input stream.
 */
private Integer loadInteger(DataInputStream in) throws IOException {
  // Read byte flag
  byte flag = in.readByte();
  
  if (flag == 0) {
    return null;
    
  } else {
    // Read integer value
    return in.readInt();
  }
}

/**
 * Saves the x, y, z fields associated with the given PVector Object to the
 * given output stream. Null values for p are accepted.
 */
private void savePVector(PVector p, DataOutputStream out) throws IOException {
  
  if (p == null) {
    // Write flag byte
    out.writeByte(0);
    
  } else {
    // Write flag byte
    out.writeByte(1);
    // Write vector data
    out.writeFloat(p.x);
    out.writeFloat(p.y);
    out.writeFloat(p.z);
  }
}

/**
 * Attempts to load a PVector object from the given input stream.
 */
private PVector loadPVector(DataInputStream in) throws IOException {
  // Read flag byte
  int val = in.readByte();
  
  if (val == 0) {
    return null;
    
  } else {
    // Read vector data
    PVector v = new PVector();
    v.x = in.readFloat();
    v.y = in.readFloat();
    v.z = in.readFloat();
    return v;
  }
}

/**
 * Saves the data associated with the given quaternion to the given output stream.
 */
private void saveRQuaternion(RQuaternion q, DataOutputStream out) throws IOException {
  if (q == null) {
    // Write flag byte
    out.writeByte(0);
  
  } else {
    // Write flag byte
    out.writeByte(1);
    
    for (int idx = 0; idx < 4; ++idx) {
      // Write each quaternion value
      out.writeFloat(q.getValue(idx));
    }
  }
}

/**
 * Attempts to construct a quaternion object from the data in the given input stream.
 */
private RQuaternion loadRQuaternion(DataInputStream in) throws IOException {
  // Read flag byte
  byte flag = in.readByte();
  
  if (flag == 0) {
    return null;
    
  } else {
    // Read values of the quaternion
    float w = in.readFloat(),
          x = in.readFloat(),
          y = in.readFloat(),
          z = in.readFloat();
    
    return new RQuaternion(w, x, y, z);
  }
}

/**
 * Saves the list of floats to the given data output stream. A flag byte is stored
 * first, ten the length of list followed by each consecutive value in the list.
 * 
 * @param list  The array of floats to save
 * @param out   The output stream, to which to save the float array
 * @throws      IOException  if an error occurs with writing to the output stream
 */
private void saveFloatArray(float[] list, DataOutputStream out) throws IOException {
  if (list == null) {
    // Write flag value
    out.writeByte(0);
    
  } else {
    // Write flag value
    out.writeByte(1);
    // Write list length
    out.writeInt(list.length);
    // Write each value in the list
    for (int idx = 0; idx < list.length; ++idx) {
      out.writeFloat(list[idx]);
    }
  }
}

/**
 * Attempts to parse a list of floats from the given data input stream.
 * This method expects that a byte flag exists, follwed by a positive
 * integer value for the length of the array, which is followed by at
 * least that number of floating point values.
 * 
 * @param in   The input stream, from which to read bytes
 * @returning  The float array pulled from the input stream (which
 *             can be null!)
 * @throws     IOException  if an error occurs with reading from the
 *             output stream
 */
private float[] loadFloatArray(DataInputStream in) throws IOException {
  // Read byte flag
  byte flag = in.readByte();
  
  if (flag == 0) {
    return null;
    
  } else {
    // Read the length of the list
    int len = in.readInt();
    float[] list = new float[len];
    // Read each value of the list
    for (int idx = 0; idx < list.length; ++idx) {
      list[idx] = in.readFloat();
    }
    
    return list;
  }
}

/**
 * Saves the 2D array of floats to the given data output stream. A flag byte is stored
 * first, ten the dimensions of array followed by each consecutive value in the array.
 * 
 * @param list  The array matrix of floats to save
 * @param out   The output stream, to which to save the float array matrix
 * @throws      IOException  if an error occurs with writing to the output stream
 */
private void saveFloatArray2D(float[][] list, DataOutputStream out) throws IOException {
  if (list == null) {
    // Write flag value
    out.writeByte(0);
    
  } else {
    // Write flag value
    out.writeByte(1);
    // Write the dimensions of the list
    out.writeInt(list.length);
    out.writeInt(list[0].length);
    // Write each value in the list
    for (int row = 0; row < list.length; ++row) {
      for (int col = 0; col < list[0].length; ++col) {
        out.writeFloat(list[row][col]);
      }
    }
  }
}

/**
 * Attempts to parse a 2D array of floats from the given data input stream.
 * This method expects that a byte flag exists, follwed by two positive
 * integer values for the dimensions of the array, which is followed by at
 * least that number of floating point values.
 * 
 * @param in   The data input stream, from which to read bytes
 * @returning  The float array matrix pulled from the input stream
 * @throws     IOException  if an error occurs with reading from the
 *             input stream
 */
private float[][] loadFloatArray2D(DataInputStream in) throws IOException {
  // Read byte flag
  byte flag = in.readByte();
  
  if (flag == 0) {
    return null;
    
  } else {
    // Read the length of the list
    int numOfRows = in.readInt(),
        numOfCols = in.readInt();
    float[][] list = new float[numOfRows][numOfCols];
    // Read each value of the list
    for (int row = 0; row < list.length; ++row) {
      for (int col = 0; col < list[0].length; ++col) {
        list[row][col] = in.readFloat();
      }
    }
    
    return list;
  }
}

/**
 * Writes anything stored in the ArrayList String buffers to tmp\test.out.
 */
private int writeBuffer() {
  try {
    PrintWriter out = new PrintWriter(sketchPath("tmp/test.out"));
    
    for (String line : buffer) {
      out.print(line);
    }
    
    println("Write to buffer successful.");
    out.close();
  } catch(Exception Ex) {
    Ex.printStackTrace();
    return 1;
  }
  
  buffer.clear();
  return 0;
}