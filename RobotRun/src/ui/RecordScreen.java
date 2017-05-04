package ui;

import java.util.Calendar;

import global.Fields;
import robot.RobotRun;

public class RecordScreen implements Runnable {
	
	public RecordScreen() {
		System.out.format("Record screen...\n");
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
			System.out.format("run script to record screen...\n");
			Runtime rt = Runtime.getRuntime();
			/*Process proc = rt.exec("ffmpeg -f dshow -i " + 
					"video=\"screen-capture-recorder\":audio=\"Microphone" + 
					" (Conexant SmartAudio HD)\" " + filename );
			Process proc = rt.exec(script); */
			while(RobotRun.getInstance().getRecord() == Fields.ON) {
				Thread.sleep(4000);
			}
			rt.exec("taskkill /F /IM ffmpeg.exe"); // close ffmpeg
			System.out.format("finish recording\n");

		}catch (Throwable t) {
			t.printStackTrace();
		}

	}
}
