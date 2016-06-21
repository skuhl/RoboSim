
import java.nio.charset.Charset;

/**
 * This method saves the program state.
 */
void saveState() {
  saveProgramBytes( new File(sketchPath("tmp/programs.bin")) );
  saveFrameBytes( new File(sketchPath("tmp/frames.bin")) );
}

/**
 * Load program and frames from their respective save files.
 *
 * @return  0 if successful,
 *          1 if the program loading failed,
 *          2 if the frame loading failed.
 */
public int loadState() {
  // If loading fails that create all new Frames
  File f = new File(sketchPath("tmp/"));
  if (!f.exists()) { f.mkdirs(); }
  
  File progFile = new File( sketchPath("tmp/programs.bin") );
  
  if (!progFile.exists()) {
    try {
      // Create 'programs.bin' if it does not already exist
      progFile.createNewFile();
      System.out.printf("Successfully created %s.\n", progFile.getName());
    } catch (IOException IOEx) {
      // Error with the creation of 'programs.bin'
      System.out.printf("Could not create %s ...\n", progFile.getName());
      IOEx.printStackTrace();
      return 1;
    }
  }
  
  int ret = loadProgramBytes(progFile);
  
  if (ret == 0) {
    println("Successfully loaded programs.\n");
  }
  
  // Find the file 'frames.bin' in the 'tmp/' folder
  File frameFile = new File( sketchPath("tmp/frames.bin") );
  
  if (!frameFile.exists()) {
    try {
      // Create 'frames.bin' if it does not already exist
      frameFile.createNewFile();
      System.out.printf("Successfully created %s.\n", frameFile.getName());
    } catch (IOException IOEx) {
      // Error with the creation of 'frames.bin'
      System.out.printf("Could not create %s ...\n", frameFile.getName());
      IOEx.printStackTrace();
      return 2;
    }
  }
  
  // Load both the User and Tool Frames
  ret = loadFrameBytes(frameFile);
  
  if (ret == 0) {
    println("Successfully loaded Frames.");
  } else {
    // Create new frames if they could not be loaded
    toolFrames = new Frame[10];
    userFrames = new Frame[10];
    
    for (int n = 0; n < toolFrames.length; ++n) {
      toolFrames[n] = new Frame();
      userFrames[n] = new Frame();
    }
  }
  
  return 0;
}

public int saveProgramBytes(File dest) {
  
  try {
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

public int loadProgramBytes(File src) {
  
  try {
    FileInputStream in = new FileInputStream(src);
    DataInputStream dataIn = new DataInputStream(in);
    // Read the number of programs stored in src
    int size = dataIn.readInt();
    
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

private Program loadProgram(DataInputStream in) throws IOException {
  // Read program name
  String name = in.readUTF();
  Program prog = new Program(name);
  // Read the next register value
  int nReg = in.readInt();
  prog.loadNextRegister(nReg);
  // Read the number of insturctions stored for this porgram
  int numOfInst = min(200, in.readInt());
  
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
  }
  
  return inst;
}

/**
 * Given a valid file path, both the Tool Frame and then the User
 * Frame sets are saved to the file. First the length of a list
 * is saved and then its respective elements.
 *
 * @param dest  the file to which the frame sets will be saved
 * @return      0 if successful,
 *              1 if an error occurs with accessing the give file
 *              2 if an error occurs with writing to the file
 */
public int saveFrameBytes(File dest) {
  
  try {
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
    int size = min(10, dataIn.readInt());
    toolFrames = new Frame[size];
    int idx;
    
    for (idx = 0; idx < size; ++idx) {
      System.out.printf("T: %d\n", idx);
      toolFrames[idx] = loadFrame(dataIn);
    }
    
    // Load User Frames
    size = min(10, dataIn.readInt());
    userFrames = new Frame[size];
    
    for (idx = 0; idx < size; ++idx) {
      System.out.printf("U: %d\n", idx);
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