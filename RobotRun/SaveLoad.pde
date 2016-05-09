import java.nio.charset.Charset;

/**
 * This method saves the program state.
 */
void saveState() {
  try{
       Path p1 = Paths.get(sketchPath("tmp/programs.ser")); 
       Path p2 = Paths.get(sketchPath("tmp/currentProgram.ser"));
       Path p3 = Paths.get(sketchPath("tmp/singleInstruction.ser"));
       println("Path: " + Paths.get(sketchPath("tmp/programs.ser")).toString());
       if (Files.exists(p1)) Files.delete(p1);
       if (Files.exists(p2)) Files.delete(p2);
       if (Files.exists(p3)) Files.delete(p3);
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
  }catch(IOException e){
     e.printStackTrace();
     println("which class caused the exception? " + e.getClass().toString());
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
            }else{
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
                  }else if (curr.equals("<FrameInstruction>")){
                     // load a Frame instruction
                     FrameInstruction instruction = new FrameInstruction(s.nextInt(), s.nextInt());
                     aProgram.addInstruction(instruction);
                     s.next(); // consume token: </FrameInstruction>
                  }else if(curr.equals("<ToolInstruction>")){
                     // load a tool instruction
                     ToolInstruction instruction = new ToolInstruction(s.next(), s.nextInt(), s.nextInt());
                     aProgram.addInstruction(instruction);
                     s.next(); // consume token: </ToolInstruction>
                  }else{ // has scanned </Program>
                     // that's the end of program
                     
                     if (programs.size() < 1) {
                       programs.add(aProgram);
                     } else {
                       int idx = 0;
                       while (idx < programs.size() && programs.get(idx).name.compareTo(aProgram.name) < 0) { ++idx; }
                       programs.add(idx, aProgram);
                     }
                     
                     break;
                     
                  }
               } // end of while
            } // end of if
            
         } // end of while
        s.close();
        return 1; 
   }catch(IOException e){
        e.printStackTrace();
        //return 0;
   }     
   return 1;
}