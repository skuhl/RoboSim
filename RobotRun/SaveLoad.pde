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
  if (!f.exists()) { f.mkdirs(); }
  
  /* Load all saved Programs */
  
  File progFile = new File( sketchPath("tmp/programs.bin") );
  
  if (progFile.exists()) {
    ret = loadProgramBytes(progFile);
    
    if (ret == 0) {
      println("Successfully loaded programs!");
    } else {
      println("Failed to load programs ...");
      error = 1;
    }
  }
  
  /* Load and Initialize the Tool and User Frames */
  
  ret = 1;
  
  File frameFile = new File( sketchPath("tmp/frames.bin") );
  
  if (frameFile.exists()) {
    // Load both the User and Tool Frames
    ret = loadFrameBytes(frameFile);
    
    if (ret == 0) {
      println("Successfully loaded frames!");
    } else {
       println("Failed to load frames ..."); 
       
       if (error == 0) {
          error = 2;
        } else {
          error = 3;
        }
    }
  }
  
  // Create new frames if they could not be loaded
  if (ret != 0) {
    
    toolFrames = new Frame[10];
    userFrames = new Frame[10];
    
    for (int n = 0; n < toolFrames.length; ++n) {
      toolFrames[n] = new Frame();
      userFrames[n] = new Frame();
    }
  }
  
    
  /* Load and Initialize the Position Register and Registers */
  
  File regFile = new File(sketchPath("tmp/registers.bin"));
  
  if (regFile.exists()) {
      ret = loadRegisterBytes(regFile);
      
      if (ret == 0) {
        println("Successfully loaded registers!");
      } else {
        println("Failed to load registers ...");
        
        if (error == 0) {
          error = 4;
        } else if (error == 1) {
          error = 5;
        } else if (error == 2) {
          error = 6;
        } else if (error == 3) {
          error = 7;
        }
      }
  }
  
  // Initialize uninitialized registers and position registers to with null fields
  for (int reg = 0; reg < REG.length; ++reg) {
    
    if (REG[reg] == null) {
      REG[reg] = new Register(null, null);
    }
    
    if (POS_REG[reg] == null) {  
      POS_REG[reg] = new PositionRegister(null, null);
    }
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
    if (!dest.exists()) {      
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
    
    for (Program prog : programs) {
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
    
    while (size-- > 0) {
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
  
  out.writeUTF(p.name);
  out.writeInt(p.nextRegister);
  out.writeInt(p.instructions.size());
  // Save each instruction
  for (Instruction inst : p.instructions) {
    saveInstruction(inst, out);
    // Save only the Points associated with a MotionInstruction
    if (inst instanceof MotionInstruction) {
      savePoint(p.p[ ((MotionInstruction)inst).register ], out);
    }
  }
}

/**
 * Creates a program from data in the given input stream. A maximum of
 * 500 instructions will be read for a single program
 * 
 * @param in            The input stream to read from
 * @return              A program created from data in the input stream
 * @throws IOException  If an error occurs with reading from the input
 *                      stream
 */
private Program loadProgram(DataInputStream in) throws IOException {
  // Read program name
  String name = in.readUTF();
  Program prog = new Program(name);
  // Read the next register value
  int nReg = in.readInt();
  prog.loadNextRegister(nReg);
  // Read the number of instructions stored for this porgram
  int numOfInst = max(0, min(in.readInt(), 500));
  
  while (numOfInst-- > 0) {
    // Read each instruction
    Instruction inst = loadInstruction(in);
    prog.addInstruction(inst);
    // Read the points stored after each MotionIntruction
    if (inst instanceof MotionInstruction) {
      Point pt = loadPoint(in);
      prog.addRegister(pt, ((MotionInstruction)inst).register);
    }
  }
  
  return prog;
}

/**
 * Saves the data associated with the given Point object to the file opened
 * by the given output stream.
 * 
 * @param   p            The Point of which to save the data
 * @param   out          The output stream used to save the Point
 * @throws  IOException  If an error occurs with writing the data of the Point
 */
private void savePoint(Point p, DataOutputStream out) throws IOException {
  // Write position of the point
  out.writeFloat(p.pos.x);
  out.writeFloat(p.pos.y);
  out.writeFloat(p.pos.z);
  
  // Write point's orientation
  for (float o : p.ori) {
    out.writeFloat(o);
  }
  
  // Write the joint angles for the point's position
  for (float j : p.joints) {
    out.writeFloat(j);
  }
}

/**
 * Loads the data of a Point from the file opened by the given
 * input stream.
 *
 * @param  in           The input stream used to read the data of
 *                      a Point
 * @return              The Point stored at the current position
 *                      of the input stream
 * @throws IOException  If an error occurs with reading the data
 *                      of the Point
 */
private Point loadPoint(DataInputStream in) throws IOException {
        // Read the point's position
  float pos_x = in.readFloat(),
        pos_y = in.readFloat(),
        pos_z = in.readFloat(),
        // Read the point's orientation
        orien_r = in.readFloat(),
        orien_i = in.readFloat(),
        orien_j = in.readFloat(),
        orien_k = in.readFloat(),
        // Read the joint angles for the joint's position
        joint_1 = in.readFloat(),
        joint_2 = in.readFloat(),
        joint_3 = in.readFloat(),
        joint_4 = in.readFloat(),
        joint_5 = in.readFloat(),
        joint_6 = in.readFloat();
  
  return new Point(pos_x, pos_y, pos_z,
                   orien_r, orien_i, orien_j, orien_k,
                   joint_1, joint_2, joint_3, joint_4, joint_5, joint_6);
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
    out.writeByte(0);
    // Write data associated with the MotionIntruction object
    out.writeInt(m_inst.motionType);
    out.writeInt(m_inst.register);
    out.writeBoolean(m_inst.globalRegister);
    out.writeFloat(m_inst.speed);
    out.writeFloat(m_inst.termination);
    out.writeInt(m_inst.userFrame);
    out.writeInt(m_inst.toolFrame);
  } else if (inst instanceof FrameInstruction) {
    
    FrameInstruction f_inst = (FrameInstruction)inst;
    // Flag byte denoting this instruction as a FrameInstruction
    out.writeByte(1);
    // Write data associated with the FrameInstruction object
    out.writeInt(f_inst.frameType);
    out.writeInt(f_inst.idx);
  } else if (inst instanceof ToolInstruction) {
    
    ToolInstruction t_inst = (ToolInstruction)inst;
    // Flag byte denoting this instruction as a ToolInstruction
    out.writeByte(2);
    // Write data associated with the ToolInstruction object
    out.writeUTF(t_inst.type);
    out.writeInt(t_inst.bracket);
    out.writeInt(t_inst.setToolStatus);
  } else {/* TODO add other instructions! */}
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
  // Determine what type of instruction is stored in the succeding bytes
  byte instType = in.readByte();
  
  if (instType == 0) {
    
    // Read data for a MotionInstruction object
    int mType = in.readInt();
    int reg = in.readInt();
    boolean isGlobal = in.readBoolean();
    float spd = in.readFloat();
    float term = in.readFloat();
    int uFrame = in.readInt();
    int tFrame = in.readInt();
    
    inst = new MotionInstruction(mType, reg, isGlobal, spd, term, uFrame, tFrame);
  } else if (instType == 1) {
    
    // Read data for a FrameInstruction object
    inst = new FrameInstruction( in.readInt(), in.readInt() );
  } else if (instType == 2) {
    
    // Read data for a ToolInstruction object
    String type = in.readUTF();
    int bracket = in.readInt();
    int setting = in.readInt();
    
    inst = new ToolInstruction(type, bracket, setting);
  } else {/* TODO add other instructions! */}
  
  return inst;
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
    if (!dest.exists()) {
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
    for (Frame frame : toolFrames) {
      saveFrame(frame, dataOut);
    }
    
    // Save User Frames
    dataOut.writeInt(userFrames.length);
    for (Frame frame : userFrames) {
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
  
  try {
    FileInputStream in = new FileInputStream(src);
    DataInputStream dataIn = new DataInputStream(in);
    
    // Load Tool Frames
    int size = max(0, min(dataIn.readInt(), 10));
    toolFrames = new Frame[size];
    int idx;
    
    for (idx = 0; idx < size; ++idx) {
      toolFrames[idx] = loadFrame(dataIn);
    }
    
    // Load User Frames
    size = max(0, min(dataIn.readInt(), 10));
    userFrames = new Frame[size];
    
    for (idx = 0; idx < size; ++idx) {
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
  // Write frame origin
  PVector v = f.getOrigin();
  out.writeFloat(v.x);
  out.writeFloat(v.y);
  out.writeFloat(v.z);
  // Write frame orientation
  v = f.getWpr();
  out.writeFloat(v.x);
  out.writeFloat(v.y);
  out.writeFloat(v.z);
  // Write frame axes
  for (int row = 0; row < 3; ++row) {
    for (int col = 0; col < 3; ++col) {
      out.writeFloat(f.axes[row][col]);
    }
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
  // Read origin values
  PVector origin = new PVector();
  origin.x = in.readFloat();
  origin.y = in.readFloat();
  origin.z = in.readFloat();
  // Read orientation values
  PVector wpr = new PVector();
  wpr.x = in.readFloat();
  wpr.y = in.readFloat();
  wpr.z = in.readFloat();
  
  float[][] axesVectors = new float[3][3];
  // Read axes vector values
  for (int row = 0; row < 3; ++row) {
    for (int col = 0; col < 3; ++col) {
      axesVectors[row][col] = in.readFloat();
    }
  }
  
  return new Frame(origin, wpr, axesVectors);
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
    if (!dest.exists()) {
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
    for (int idx = 0; idx < REG.length; ++idx) {
      if (REG[idx].value != null || REG[idx].comment != null) {
        initializedR.add(idx);
        ++numOfREntries;
      }
      
      if (POS_REG[idx].point != null || POS_REG[idx].comment != null) {
        initializedPR.add(idx);
        ++numOfPREntries;
      }
    }
    
    dataOut.writeInt(numOfREntries);
    // Save the Register entries
    for (Integer idx : initializedR) {
      dataOut.writeInt(idx);
      
      if (REG[idx].value == null) {
        // save for null Float value
        dataOut.writeFloat(Float.NaN);
      } else {
        dataOut.writeFloat(REG[idx].value);
      }
      
      if (REG[idx].comment == null) {
        dataOut.writeUTF("");
      } else {
        dataOut.writeUTF(REG[idx].comment);
      }
    }
    
    dataOut.writeInt(numOfPREntries);
    // Save the Position Register entries
    for (Integer idx : initializedPR) {
      dataOut.writeInt(idx);
      
      if (POS_REG[idx].point == null) {
        // Save for null Point value
        savePoint( new Point(Float.NaN, Float.NaN, Float.NaN,
                             Float.NaN, Float.NaN, Float.NaN, Float.NaN), dataOut );
      } else {
        savePoint(POS_REG[idx].point, dataOut);
      }
      
      if (POS_REG[idx].comment == null) {
        dataOut.writeUTF("");
      } else {
        dataOut.writeUTF(POS_REG[idx].comment);
      }
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
    
    int size = max(0, min(dataIn.readInt(), REG.length));
    
    // Load the Register entries
    while (size-- > 0) {
      // Each entry is saved after its respective index in REG
      int reg = dataIn.readInt();
      
      Float v = dataIn.readFloat();
      // Null values are saved as NaN
      if (Float.isNaN(v)) { v = null; }
      
      String c = dataIn.readUTF();
      // Null comments are saved as ""
      if (c.equals("")) { c = null; }
      
      REG[reg] = new Register(c, v);
    }
    
    size = max(0, min(dataIn.readInt(), POS_REG.length));
    
    // Load the Position Register entries
    while (size-- > 0) {
      // Each entry is saved after its respective index in POS_REG
      int idx = dataIn.readInt();
      
      Point p = loadPoint(dataIn);
      // Null points are stored with pos Vectors filled with NaNs
      if (Float.isNaN(p.pos.x)) { p = null; }
      
      String c = dataIn.readUTF();
      // Null comments are stored as ""
      if (c == "") { c = null; }
      
      POS_REG[idx] = new PositionRegister(c, p);
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