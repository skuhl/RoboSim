/**
 * This method saves all programs, frames, and initialized registers,
 * each to separate files
 */
public void saveState() {
  saveProgramBytes( new File(sketchPath("tmp/programs.bin")) );
  saveFrameBytes( new File(sketchPath("tmp/frames.bin")) );
  saveRegisterBytes( new File(sketchPath("tmp/registers.bin")) );
}

/**
 * Load program, frames, and registers from their respective
 * binary files.
 *
 * @return  0 if all loads were successful,
 *          1 if only the program loading failed,
 *          2 if only the frame loading failed,
 *          3 if only program and frame loading failed,
 *          4 if only register loading failed,
 *          5 if only register and program loading failed,
 *          6 if only register and frame loading failed,
 *          7 if all loads failed
 */
public int loadState() {
  int ret = 0,
  error = 0;
  
  File f = new File(sketchPath("tmp/"));
  if(!f.exists()) { f.mkdirs(); }
  
  /* Load all saved Programs */
  
  File progFile = new File( sketchPath("tmp/programs.bin") );
  
  if(progFile.exists()) {
    ret = loadProgramBytes(progFile);
    
    if(ret == 0) {
      println("Successfully loaded programs!");
    } else {
      println("Failed to load programs ...");
      error = 1;
    }
  }
  
  /* Load and Initialize the Tool and User Frames */
  
  ret = 1;
  
  File frameFile = new File( sketchPath("tmp/frames.bin") );
  
  if(frameFile.exists()) {
    // Load both the User and Tool Frames
    ret = loadFrameBytes(frameFile);
    
    if(ret == 0) {
      println("Successfully loaded frames!");
    } else {
      println("Failed to load frames ..."); 
      
      if(error == 0) {
        error = 2;
      } else {
        error = 3;
      }
    }
  }
  
  // Create new frames if they could not be loaded
  if(ret != 0) {
    
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
    ret = loadRegisterBytes(regFile);
    
    if(ret == 0) {
      println("Successfully loaded registers!");
    } else {
      println("Failed to load registers ...");
      
      if(error == 0) {
        error = 4;
      } else if(error == 1) {
        error = 5;
      } else if(error == 2) {
        error = 6;
      } else if(error == 3) {
        error = 7;
      }
    }
  }
  
  // Initialize uninitialized registers and position registers to with null fields
  for(int reg = 0; reg < DREG.length; reg += 1) {
    
    if(DREG[reg] == null) {
      DREG[reg] = new DataRegister();
      DREG[reg].setIdx(reg);
    }
    
    if(GPOS_REG[reg] == null) {
      GPOS_REG[reg] = new PositionRegister();
      GPOS_REG[reg].setIdx(reg);
    }
  }
  
  int idx = 0;
  // Associated each End Effector with an I/O Register
  IO_REG[idx++] = new IORegister(EndEffector.SUCTION);
  IO_REG[idx++] = new IORegister(EndEffector.CLAW);
  IO_REG[idx++] = new IORegister(EndEffector.POINTER);
  
  for (; idx < IO_REG.length; ++idx) {
    // Unassociated registers
    IO_REG[idx] = new IORegister(null);
    IO_REG[idx].setIdx(idx);
  }
  
  
  
  return error;
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
public int saveProgramBytes(File dest) {
  
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
public int loadProgramBytes(File src) {
  
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
    out.writeInt(p.nextRegister);
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
    prog.setNextRegister(nReg);
    // Read the number of instructions stored for this porgram
    int numOfInst = max(0, min(in.readInt(), 500));
    
    while(numOfInst-- > 0) {
      // Read each instruction
      Instruction inst = loadInstruction(in);
      prog.addInstruction(inst);
      // Read the points stored after each MotionIntruction
      if(inst instanceof MotionInstruction) {
        Point pt = loadPoint(in);
        prog.addPosition(pt, ((MotionInstruction)inst).positionNum);
      }
    }
    
    return prog;
  }
}

/**
 * Saves the data associated with the given Point object to the file opened
 * by the given output stream. Null Points are saved a single zero byte.
 * 
 * @param   p            The Point of which to save the data //<>//
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
    saveFloatArray(p.orientation, out);
    // Write the joint angles for the point's position
    saveFloatArray(p.angles, out);
    
    if (p.angles == null) {
      println("null angles!"); //<>//
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
    float[] orientation = loadFloatArray(in);
    // Read the joint angles for the joint's position
    float[] angles = loadFloatArray(in);
    
    if (angles == null) {
      println("null angles!"); //<>//
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
    
  } /* Add other instructions here! */
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
    
  } /* Add other instructions here! */
    else if (instType == 7) {
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
public int saveFrameBytes(File dest) {
  
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
public int loadFrameBytes(File src) {
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
  
  // Write frame origin
  savePVector(f.getOrigin(), out);
  // Write frame axes
  saveFloatArray(f.axes, out);
  
  // Write frame orientation points
  for (Point pt : f.axesTeachPoints) {
    savePoint(pt, out);
  }
  
  // Write frame manual entry origin value
  savePVector(f.DEOrigin, out);
  // Write frame manual entry origin value
  saveFloatArray(f.DEAxesOffsets, out);
  
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
    println(type);
    throw new IOException("Invalid Frame type!");
  }
  
  // Read origin values
  f.setOrigin( loadPVector(in) );
  // Read axes quaternion values
  f.setAxes( loadFloatArray(in) );
  
  // Read origin values
  f.axesTeachPoints = new Point[3];
  // Read in orientation points
  for (int idx = 0; idx < 3; ++idx) {
    f.axesTeachPoints[idx] = loadPoint(in);
  }
  
  // Read manual entry origin values
  f.DEOrigin = loadPVector(in);
  f.DEAxesOffsets = loadFloatArray(in);
  
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
public int saveRegisterBytes(File dest) {
  
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
    for(int idx = 0; idx < DREG.length; ++idx) {
      if(DREG[idx].value != null || DREG[idx].comment != null) {
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
      
      if(DREG[idx].value == null) {
        // save for null Float value
        dataOut.writeFloat(Float.NaN);
      } else {
        dataOut.writeFloat(DREG[idx].value);
      }
      
      if(DREG[idx].comment == null) {
        dataOut.writeUTF("");
      } else {
        dataOut.writeUTF(DREG[idx].comment);
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
public int loadRegisterBytes(File src) {
  
  try {
    FileInputStream in = new FileInputStream(src);
    DataInputStream dataIn = new DataInputStream(in);
    
    int size = max(0, min(dataIn.readInt(), DREG.length));
    
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
      
      DREG[reg] = new DataRegister(c, v);
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
      
      GPOS_REG[idx] = new PositionRegister(c, p, isCartesian);
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
public int saveScenarioBytes(File dest) {
  
  try {
    FileOutputStream out = new FileOutputStream(dest);
    DataOutputStream dataOut = new DataOutputStream(out);
    
    int numOfScenarios = SCENARIOS.size();
    // Save the number of scenarios
    dataOut.writeInt(numOfScenarios);
    // Save the active scenario
    dataOut.writeInt(activeScenarioIdx);
    // Save all the scenarios
    for (Scenario s : SCENARIOS) {
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
public int loadScenarios(File src) {
  
  try {
    FileInputStream in = new FileInputStream(src);
    DataInputStream dataIn = new DataInputStream(in);
    
    int numOfScenarios = dataIn.readInt();
    activeScenarioIdx = dataIn.readInt();
    
    // Load all scenarios saved
    while (numOfScenarios-- > 0) {
      SCENARIOS.add( loadScenario(dataIn) );
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

/**
 * Saveds all the data associated with a scenario to the given output stream.
 * First a single flag byte is saved to the stream followed by the number of
 * objects in the scenario and then exxactly that number of world objects.
 * 
 * @param s    The scenario to save
 * @param out  The output stream to which to save the scenario
 * @throws     IOException if an erro occurs with writing to the output stream
 */
public void saveScenario(Scenario s, DataOutputStream out) throws IOException {
  
  if (s == null) {
    // Indicate the value saved is null
    out.writeByte(0);
    
  } else {
    // Indicate the value saved is non-null
    out.writeByte(1);
    
    int size = s.size();
    // Save the number of world objects in the scenario
    out.writeInt(size);
    
    for (int idx = 0; idx < size; ++idx) {
      // Save all the world objects associated with the scenario
      saveWorldObject(s.getWorldObject(idx), out);  
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
public Scenario loadScenario(DataInputStream in) throws IOException, NullPointerException {
  // Read flag byte
  byte flag = in.readByte();
  
  if (flag == 0) {
    return null;
    
  } else {
    // Read the name of the scenario
    String name = in.readUTF();
    Scenario s = new Scenario(name);
    // Read the number of objects in the scenario
    int size = in.readInt();
    // Read all the world objects contained in the scenario
    while (size-- > 0) {
      s.addWorldObject( loadWorldObject(in) );
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
public void saveWorldObject(WorldObject wldObj, DataOutputStream out) throws IOException {
  
  if (wldObj == null) {
    // Indicate that the value saved is null
    out.writeByte(0);
    
  } else {
    // Save the name and form of the object
    out.writeUTF(wldObj.getName());
    saveShape(wldObj.getForm(), out);
    
    if (wldObj instanceof Part) {
      Part part = (Part)wldObj;
      // Indicate that the value saved is a Part
      out.writeByte(1);
      // Save the bounding-box and fixture reference of the part
      saveOBB(part.getOBB(), out);
      saveWorldObject(part.getFixtureRef(), out);
      
    } else if (wldObj instanceof Fixture) {
      Fixture fixture = (Fixture)wldObj;
      // Indicate that the value saved is a Fixture
      out.writeByte(2);
      // Save the local orientation of the fixture
      savePVector(fixture.getCenter(), out);
      saveFloatArray2D(fixture.getOrientationAxes(), out);
      
    }
  }
}

/**
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
public WorldObject loadWorldObject(DataInputStream in) throws IOException, NullPointerException {
  // Load the flag byte
  byte flag = in.readByte();
  WorldObject wldObj = null;
  
  if (flag != 0) {
    // Load the name and shape of the object
    String name = in.readUTF();
    Shape form = loadShape(in);
    
    if (flag == 1) {
      // Load the part's bounding-box and fixture reference
      BoundingBox OBB = loadOBB(in);
      Fixture reference = (Fixture)loadWorldObject(in);
      
      wldObj = new Part(name, form, OBB, reference);
      
    } else if (flag == 2) {
      // Load the fixture's local orientation
      PVector center = loadPVector(in);
      float[][] orientationAxes = loadFloatArray2D(in);
      CoordinateSystem localOrientation = new CoordinateSystem();
      localOrientation.setOrigin(center);
      localOrientation.setAxes(orientationAxes);
      
      wldObj = new Fixture(name, form, localOrientation);
      
    } 
  }
  
  return wldObj;
}

/**
 * Saves all the fields associated with the given Bounding-Box to the given data
 * output stream. A single flag byte is written first, followed by the length,
 * height, and width of box, then the center position PVector, and finally the
 * box's orientation in the form of a 3x3 float array matrix.
 * 
 * @param OBB  The Bounding-Box object to save
 * @param out  The output stream to which to save the Bounding-Box
 * @throws     IOException  if an error occurs with writing to the output stream.
 */
public void saveOBB(BoundingBox OBB, DataOutputStream out) throws IOException {
  
  if (OBB == null) {
    // Indicate the saved value is null
    out.writeByte(0);
    
  } else {
    // Indicate the saved value is non-null
    out.writeByte(1);
    // Save the bounding-boxe's dimensions
    out.writeFloat( OBB.getDim(0) );
    out.writeFloat( OBB.getDim(1) );
    out.writeFloat( OBB.getDim(2) );
    // Save the local orientation of the bounding-box
    savePVector(OBB.getCenter(), out);
    saveFloatArray2D(OBB.getOrientationAxes(), out);
  }
}

/**
 * Attempts to load the data of a Bounding Box object from the given
 * data input stream. It is expected that the input stream contains a
 * single byte (for the flag byte) followed three float values, a
 * PVector, and finally a 3x3 float array matrix.
 * 
 * @param in   The data stream, from which to read bytes
 * @returning  The Bounding-Box pulled from the input stream (which
 *             can be null!)
 * @throws     IOException if an error occurs with reading from the
 *             input stream
 */
public BoundingBox loadOBB(DataInputStream in) throws IOException {
  
  byte flag = in.readByte();
  
  if (flag == 0) {
    return null;
    
  } else {
    // Read the dimensions of the box
    float len = in.readFloat(),
          hgt = in.readFloat(),
          wid = in.readFloat();
    // Read the local orientation of the box
    PVector center = loadPVector(in);
    float[][] axes = loadFloatArray2D(in);
    
    BoundingBox OBB = new BoundingBox(len, hgt, wid);
    // Set the local orientation of the box
    OBB.setCenter(center);
    OBB.setOrientationAxes(axes);
    return OBB;
  }
}

/**
 * Saves all the fields associated with the given Coordinate System to the given data output
 * stream. First a single byte is wrote to the output stream. Then, the origin vector and
 * finally the axes vectors are written to the output stream.
 * 
 * @param cs   The Coordinate System to save
 * @param out  The output stream to which to save the Coordinate System
 * @throws     IOException  if an error occurs in with writing to the output stream
 */
public void saveCoordSystem(CoordinateSystem cs, DataOutputStream out) throws IOException {
  if (cs == null) {
    // Indicate the saved value is null
    out.writeByte(0);
    
  } else {
    // Indicate the saved value is non-null
    out.writeByte(1);
    // Save the origin value of the coodinate system
    savePVector(cs.getOrigin(), out);
    // Save the axes vectors of the coordinate system
    saveFloatArray2D(cs.getAxes(), out);
  }
}

/**
 * Attempt to load the data of a Coordinate System object from the given data input
 * stream. It is expected that the input stream contains a single byte (for the byte
 * flag) followed by a PVector object and then finally a 3x3 float array matrix.
 * 
 * @param in   The input stream, from which to read bytes
 * @returning  The Coordinate System pulled from the input stream (which can be null!)
 * @throws     IOException  if an error occurs with reading from the input stream
 */
public CoordinateSystem loadCoordSystem(DataInputStream in) throws IOException {
  // Read the flag byte
  byte flag = in.readByte();
  
  if (flag == 0) {
    return null;
    
  } else {
    // Read the origin PVector and axes vectors
    PVector origin = loadPVector(in);
    float[][] axes = loadFloatArray2D(in);
    
    CoordinateSystem cs = new CoordinateSystem();
    cs.setOrigin(origin);
    cs.setAxes(axes);
    
    return cs;
  }
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
public void saveShape(Shape shape, DataOutputStream out) throws IOException {
  if (shape == null) {
    // Indicate the saved value is null
    out.writeByte(0);
  } else {
    
    if (shape instanceof Box) {
      Box b = (Box)shape;
      // Indicate the saved value is a box
      out.writeByte(1);
      // Save length, height, and width of the box
      savePVector(b.getDimensions(), out);
      
    } else if (shape instanceof Cylinder) {
      Cylinder c = (Cylinder)shape;
      // Indicate the value saved is a cylinder
      out.writeByte(2);
      // Save the radius and height of the cylinder
      out.writeFloat(c.getRadius());
      out.writeFloat(c.getHeight());
      
    } else if (shape instanceof ModelShape) {
      ModelShape m = (ModelShape)shape;
      // Indicate the value saved is a complex shape
      out.writeByte(2);
      // Save the source path of the complex shape
      out.writeUTF(m.getSourcePath());
      
    }
    // Write the shape's color fields
    out.writeBoolean( shape.isFilled() );
    out.writeInt( shape.getFillColor() );
    out.writeInt( shape.getOutlineColor() );
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
public Shape loadShape(DataInputStream in) throws IOException, NullPointerException {
  // Read flag byte
  byte flag = in.readByte();
  Shape shape = null;
  
  if (flag == 1) {
    PVector dimensions = loadPVector(in);
    boolean isFilled = in.readBoolean();
    int fill = in.readInt(),
        outline = in.readInt();
    // Create a box
    shape = new Box(fill, outline, dimensions.x, dimensions.y, dimensions.z);
    shape.setFillFlag(isFilled);
    
  } else if (flag == 2) {
    float radius = in.readFloat(),
          hgt = in.readFloat();
    boolean isFilled = in.readBoolean();
    int fill = in.readInt(),
        outline = in.readInt();
    // Create a cylinder
    shape = new Cylinder(fill, outline, radius, hgt);
    shape.setFillFlag(isFilled);
    
  } else if (flag == 3) {
    String srcPath = in.readUTF();
    boolean isFilled = in.readBoolean();
    int fill = in.readInt(),
        outline = in.readInt();
    // Creates a complex shape from the srcPath located in RobotRun/data/
    shape = new ModelShape(srcPath, fill, outline);
    shape.setFillFlag(isFilled);
  }
  
  return shape;
}

/**
 * Saves the x, y, z fields associated with the given PVector Object to the
 * given output stream. Null values for p are accepted.
 */
public void savePVector(PVector p, DataOutputStream out) throws IOException {
  
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
public PVector loadPVector(DataInputStream in) throws IOException {
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
 * Saves the list of floats to the given data output stream. A flag byte is stored
 * first, ten the length of list followed by each consecutive value in the list.
 * 
 * @param list  The array of floats to save
 * @param out   The output stream, to which to save the float array
 * @throws      IOException  if an error occurs with writing to the output stream
 */
public void saveFloatArray(float[] list, DataOutputStream out) throws IOException {
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
public float[] loadFloatArray(DataInputStream in) throws IOException {
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
public void saveFloatArray2D(float[][] list, DataOutputStream out) throws IOException {
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
public float[][] loadFloatArray2D(DataInputStream in) throws IOException {
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
public int writeBuffer() {
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