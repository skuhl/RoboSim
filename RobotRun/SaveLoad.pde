
import java.nio.charset.Charset;

/**
 * This method saves the program state.
 */
void saveState() {
  try{
    //create tmp directory if not present
    File f = new File(sketchPath("tmp/"));
    f.mkdirs();
    
    Path p1 = Paths.get(sketchPath("tmp/programs.ser")); 
    Path p2 = Paths.get(sketchPath("tmp/currentProgram.ser"));
    Path p3 = Paths.get(sketchPath("tmp/singleInstruction.ser"));
    Path p4 = Paths.get(sketchPath("tmp/frames"));
        
    println("Path: " + Paths.get(sketchPath("tmp/programs.ser")).toString());
    if (Files.exists(p1)) Files.delete(p1);
    if (Files.exists(p2)) Files.delete(p2);
    if (Files.exists(p3)) Files.delete(p3);
    if (Files.exists(p4)) Files.delete(p4);
    /*
    out = new FileOutputStream(sketchPath("tmp/currentProgram.ser"));
    if (currentProgram == null){
      String tmp = "null";
      out.write(tmp.getBytes(Charset.forName("UTF-8")));
    }else{
      out.write(currentProgram.toExport().getBytes(Charset.forName("UTF-8")));
    }
    out.close();
    */
    
    if(programs.size() > 0){
      out = new FileOutputStream(sketchPath("tmp/programs.ser"));
      if (programs.size() == 0){
        String tmp = "null";
        out.write(tmp.getBytes(Charset.forName("UTF-8")));
      }else{
         for(int i=0;i<programs.size();i++){
           out.write(programs.get(i).toExport().getBytes(Charset.forName("UTF-8")));
           String blank = "\n";
           out.write(blank.getBytes(Charset.forName("UTF-8")));
         }
      } 
      out.close();
    }

    /*
    out = new FileOutputStream(sketchPath("tmp/singleInstruction.ser"));
    if (singleInstruction == null ) {
       String tmp = "null";
       out.write(tmp.getBytes(Charset.forName("UTF-8")));
    }else{
       out.write(singleInstruction.toExport().getBytes(Charset.forName("UTF-8")));
    }
    out.close();
    */
    
    saveFrameBytes( new File(sketchPath("tmp/frames.ser")) );
    
  }catch(IOException e){
     e.printStackTrace();
     println("which class caused the exception? " + e.getClass().toString());
  }
}

public int saveFrames() {
  
  
  
  return 0;
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
  
  Path p1 = Paths.get(sketchPath("tmp/programs.ser")); 
  if (!Files.exists(p1)) return 1;
  if(loadPrograms(p1)==0) return 1;
  
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
  int ret = loadFrameBytes(frameFile);
  
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

/**
 * This method loads built-in programs and user-defined programs 
 *
 * @PARAM:path - where to find the file that stores program state
 * @return: 1 if success, otherwise 0.
 */
int loadPrograms(Path path){
  try{
    Scanner s = new Scanner(path);
    while (s.hasNext()){
      Program aProgram;
      String curr = s.next();
      if (curr.equals("null")){
        programs = new ArrayList<Program>();
        s.close();
        return 1;
      }
      else{
        String name = s.next();
        name = name.replace('_', ' ');
        aProgram = new Program(name);
        int nextRegister = s.nextInt();
        aProgram.loadNextRegister(nextRegister);
        for(int i = 0; i < aProgram.getRegistersLength(); i += 1){
          s.next(); // consume token: <Point>
          Point p = new Point(s.nextFloat(), s.nextFloat(), s.nextFloat(), 
                              s.nextFloat(), s.nextFloat(), s.nextFloat(), s.nextFloat(), 
                              s.nextFloat(), s.nextFloat(), s.nextFloat(), 
                              s.nextFloat(), s.nextFloat(), s.nextFloat());
          s.next(); // consume token: </Point> 
          aProgram.addRegister(p, i);
        }

        while(s.hasNext()){
          curr = s.next();
          if (curr.equals("<MotionInstruction>")){
            // load a motion instruction
            MotionInstruction instruction = new MotionInstruction(s.nextInt(), s.nextInt(), Boolean.valueOf(s.next()), s.nextFloat(), s.nextFloat(), s.nextInt(), s.nextInt()); //1.0
            aProgram.addInstruction(instruction);
            s.next(); // consume token: </MotionInstruction>
          }
          else if (curr.equals("<FrameInstruction>")){
            // load a Frame instruction
            FrameInstruction instruction = new FrameInstruction(s.nextInt(), s.nextInt());
            aProgram.addInstruction(instruction);
            s.next(); // consume token: </FrameInstruction>
          }
          else if(curr.equals("<ToolInstruction>")){
            // load a tool instruction
            ToolInstruction instruction = new ToolInstruction(s.next(), s.nextInt(), s.nextInt());
            aProgram.addInstruction(instruction);
            s.next(); // consume token: </ToolInstruction>
          }
          else{ // has scanned </Program>
            // that's the end of program
            addProgram(aProgram);
            break;     
          }
        } // end of while
      } // end of if      
    } // end of while
    s.close();
    return 1; 
  }
  catch(IOException e){
    e.printStackTrace();
    //return 0;
  }     
  return 1;
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
    FileOutputStream out = new FileOutputStream(dest.toString());
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
    // Could not find the given file
    System.out.printf("%s does not exist!\n", dest.getName());
    FNFEx.printStackTrace();
    return 1;
  } catch (IOException IOEx) {
    // Error with reading the values for the Frames
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
    FileInputStream in = new FileInputStream(src.toString());
    DataInputStream dataIn = new DataInputStream(in);
    
    // Load Tool Frames
    int size = dataIn.readInt();
    toolFrames = new Frame[size];
    int idx;
    
    for (idx = 0; idx < size; ++idx) {
      toolFrames[idx] = loadFrame(dataIn);
    }
    
    // Load User Frames
    size = dataIn.readInt();
    userFrames = new Frame[size];
    
    for (idx = 0; idx < size; ++idx) {
      userFrames[idx] = loadFrame(dataIn);
    }
    
    dataIn.close();
    in.close();
    return 0;
  } catch (FileNotFoundException FNFEx) {
    // Could not find the given file
    System.out.printf("%s does not exist!\n", src.getName());
    FNFEx.printStackTrace();
    return 1;
  } catch (EOFException EOFEx) {
    // End of file reached
    System.out.printf("Reached end of %s!\n", src.getName());
    EOFEx.printStackTrace();
    return 3;
  } catch (IOException IOEx) {
    // Error with reading the values for the Frames
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
public void saveFrame(Frame f, DataOutputStream out) throws IOException {
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
public Frame loadFrame(DataInputStream in) throws IOException {
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