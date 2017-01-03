package regs;

import global.Fields;
import robot.EEType;

public class RegisterFile {
	public static final int REG_SIZE = 100;
	public static final int IO_REG_SIZE = 5;
	// Position Registers
	private static Register[] GPOS_REG = new PositionRegister[REG_SIZE];
	// Data Registers
	private static Register[] DAT_REG = new DataRegister[REG_SIZE];
	// IO Registers
	private static Register[] IO_REG = new IORegister[IO_REG_SIZE];
	
	public static DataRegister getDReg(int idx) { return (DataRegister)DAT_REG[idx]; }
	
	public static Register[] getDRegFile() { return DAT_REG; }
	public static IORegister getIOReg(int idx) { return (IORegister)IO_REG[idx]; }
	public static Register[] getIORegFile() { return IO_REG; }
	
	public static PositionRegister getPReg(int idx) { return (PositionRegister)GPOS_REG[idx]; }
	public static Register[] getPRegFile() { return GPOS_REG; }
	public static void initRegisterFile() {
		for(int i = 0; i < REG_SIZE; i += 1) {
			GPOS_REG[i] = new PositionRegister(i);
			DAT_REG[i] = new DataRegister(i);
		}

		// Associated each End Effector with an I/O Register
		int idx = 0;
		IO_REG[idx++] = new IORegister(idx, (EEType.SUCTION).name(), Fields.OFF);
		IO_REG[idx++] = new IORegister(idx, (EEType.CLAW).name(), Fields.OFF);
		IO_REG[idx++] = new IORegister(idx, (EEType.POINTER).name(), Fields.OFF);
		IO_REG[idx++] = new IORegister(idx, (EEType.GLUE_GUN).name(), Fields.OFF);
		IO_REG[idx++] = new IORegister(idx, (EEType.WIELDER).name(), Fields.OFF);
	}
	
	public static DataRegister setDReg(int idx, DataRegister r) { return (DataRegister)(DAT_REG[idx] = r); }
	public static PositionRegister setPReg(int idx, PositionRegister r) { return (PositionRegister)(GPOS_REG[idx] = r); }
	public static int toggleIOReg(int idx) {
		if(((IORegister)IO_REG[idx]).state == Fields.OFF) {
			((IORegister)IO_REG[idx]).state = Fields.ON;
			return Fields.ON;
		}
		else {
			((IORegister)IO_REG[idx]).state = Fields.OFF;
			return Fields.OFF;
		}
	}
}
