
import java.nio.charset.Charset;

/**
 * This method saves the program state.
 */
void saveState() {
  try{
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
           //String blank = "\n";
           //out.write(blank.getBytes(Charset.forName("UTF-8")));
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
    
    saveFrames(sketchPath("tmp/frames.ser"));
    
  }catch(IOException e){
     e.printStackTrace();
     println("which class caused the exception? " + e.getClass().toString());
  }
}

public int saveFrames(String path) {
  try {
    FileOutputStream out = new FileOutputStream(path);
    
    // Save the Tool and User Frames to the path /tmp/frames.ser
    if (toolFrames != null) {
      
      // Save Tool Frames
      out.write( ("<FrameSet> ").getBytes( Charset.forName("UTF-8") ) );
      String size = toolFrames.length + " ";
      out.write(size.getBytes("UTF-8"));
        
      for (int idx = 0; idx < toolFrames.length; ++idx) {
        if(toolFrames[idx] != null){
          out.write( toolFrames[idx].toExport().getBytes( Charset.forName("UTF-8") ) );
          out.write( (" ").getBytes( Charset.forName("UTF-8") ) );
        }
      }
    }
    
    if (userFrames != null) {
      // Save User Frames
      out.write( ("</FrameSet> <FrameSet> ").getBytes( Charset.forName("UTF-8") ) );
      String size = userFrames.length + " ";
      out.write(size.getBytes("UTF-8"));
      
      for (int idx = 0; idx < userFrames.length; ++idx) {
        if(userFrames[idx] != null){
          out.write( userFrames[idx].toExport().getBytes( Charset.forName("UTF-8") ) );
          out.write( (" ").getBytes( Charset.forName("UTF-8") ) );
        }
      }
      
      out.write( ("</FrameSet>").getBytes( Charset.forName("UTF-8") ) );
    }
    
    out.close();
    
    return 1;
  } catch (Exception Ex) {
    Ex.printStackTrace();
    return 0;
  }
}

// this will automatically called when program starts
/**
 * Load the program state that is previously stored. 
 * @return: 1 if sucess, otherwise return 0;
 */
int loadState() {
  Path p1 = Paths.get(sketchPath("tmp/programs.ser")); 
  if (!Files.exists(p1)) return 0;
  if(loadPrograms(p1)==0) return 0;
  
  // If loading fails that create all new Frames
  Path p2 = Paths.get(sketchPath("tmp/frames.ser"));
  
  if (!Files.exists(p2) || loadFrames(p2) == 0) {
    
    toolFrames = new Frame[10];
    userFrames = new Frame[10];
    
    for (int n = 0; n < toolFrames.length; ++n) {
      toolFrames[n] = new Frame();
      userFrames[n] = new Frame();
    }
    
    saveFrames(sketchPath("tmp/frames.ser"));
    
    return 0;
  }
  
  return 1;
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
        for(int i=0;i<aProgram.getRegistersLength();i++){
          s.next(); // consume token: <Point>
          Point p = new Point(s.nextFloat(), s.nextFloat(), s.nextFloat(), s.nextFloat(), s.nextFloat(), s.nextFloat(),
          s.nextFloat(), s.nextFloat(), s.nextFloat(), s.nextFloat(), s.nextFloat(), s.nextFloat());
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
 * This method loads all saved Tool and User Frames form /tmp/frames.ser
 * 
 * @param path  the path from which to load the Frames from
 * @return      1 if loading was successful, 0 otherwise
 */
public int loadFrames(Path path) {
  
  try {
    Scanner reader = new Scanner(path);
    // Consume "<FrameSet>"
    reader.next();
    // Read Tool Frame Set length
    toolFrames = new Frame[reader.nextInt()];
   
    String token;
    // Read each Tool Frame one Vector at a time
    for (int idx = 0; idx < toolFrames.length; ++idx) {
      // Consume "<Frame>"
      reader.next();
      
      token = reader.next();
      float x = Float.parseFloat(token);
      token = reader.next();
      float y = Float.parseFloat(token);
      token = reader.next();
      float z = Float.parseFloat(token);
      // Create origin point
      PVector o = new PVector(x, y ,z);
      
      token = reader.next();
      x = Float.parseFloat(token);
      token = reader.next();
      y = Float.parseFloat(token);
      token = reader.next();
      z = Float.parseFloat(token);
      // Create w, p, and r
      PVector wpr = new PVector(x, y ,z);
      
      float[][] axes = new float[3][3];
      // Create axes points
      
      for (int col = 0; col < 3; ++col) {
        for (int row = 0; row < 3; ++row) {
          axes[row][col] = Float.parseFloat(reader.next());;
        }  
      }
      
      toolFrames[idx] = new Frame(o, wpr, axes);
      
      reader.next();
    }
    
    // Consume "</FrameSet>"
    reader.next();
    
    // Consume "<FrameSet>"
    reader.next();
    // Read User Frame Set length
    userFrames = new Frame[reader.nextInt()];
    
    // Read each User Frame one Vector at a time
    for (int idx = 0; idx < toolFrames.length; ++idx) {
      // Consume "<Frame>"
      reader.next();
      
      token = reader.next();
      float x = Float.parseFloat(token);
      token = reader.next();
      float y = Float.parseFloat(token);
      token = reader.next();
      float z = Float.parseFloat(token);
      // Create origin point
      PVector o = new PVector(x, y ,z);
      
      token = reader.next();
      x = Float.parseFloat(token);
      token = reader.next();
      y = Float.parseFloat(token);
      token = reader.next();
      z = Float.parseFloat(token);
      // Create w, p, and r
      PVector wpr = new PVector(x, y ,z);
      
      float[][] axes = new float[3][3];
      // Create axes points
      
      for (int col = 0; col < 3; ++col) {
        for (int row = 0; row < 3; ++row) {
          axes[row][col] = Float.parseFloat(reader.next());;
        }  
      }
      
      userFrames[idx] = new Frame(o, wpr, axes);
      
      reader.next();
    }
    
    // Consume "</FrameSet>"
    reader.next();
    
    reader.close();
    
  } catch (Exception e) {
    e.printStackTrace();
    return 0;
  }
  
  return 1;
}