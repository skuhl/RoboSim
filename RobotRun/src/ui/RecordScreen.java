package ui;

import java.util.Calendar;

import global.Fields;

/**
 * TODO general comments
 * 
 * @author James Walker
 */
public class RecordScreen implements Runnable {
	
	private boolean recording;
	
	public RecordScreen() {
		recording = false;
	}
	
	/**
	 * TODO comment this
	 * 
	 * @return
	 */
	public boolean isRecording() {
		return recording;
	}
	
	@Override
	public void run() {
		try{ 
			// create a timestamp and attach it to the filename
			Calendar calendar = Calendar.getInstance();
			java.util.Date now = calendar.getTime();
			java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(now.getTime());
			String filename = "output_" + currentTimestamp.toString() + ".flv"; 
			filename = filename.replace(' ', '_');
			filename = filename.replace(':', '_');   

			// record screen
			Fields.debug("run script to record screen...\n");
			Runtime rt = Runtime.getRuntime();
			/*Process proc = rt.exec("ffmpeg -f dshow -i " + 
					"video=\"screen-capture-recorder\":audio=\"Microphone" + 
					" (Conexant SmartAudio HD)\" " + filename );
			Process proc = rt.exec(script); */
			while(!recording) {
				Thread.sleep(4000);
			}
			rt.exec("taskkill /F /IM ffmpeg.exe"); // close ffmpeg
			Fields.debug("finish recording\n");

		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param state
	 */
	public void setRecording(boolean state) {
		recording = state;
	}
}
