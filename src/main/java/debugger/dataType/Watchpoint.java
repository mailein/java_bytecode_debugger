package debugger.dataType;

public class Watchpoint extends Breakpoint{
	
	public Watchpoint(String fileSourcepath, int lineNumber) {
		super(fileSourcepath, lineNumber);
	}
	//TODO if contains . eg. C.ccc then it can be evaluated if ccc is static in class C
	//but no need to test for static, just get the refType and the field

	@Override
	public void add() {
		
	}

	@Override
	public void remove() {
		
	}

	@Override
	public void disable() {
		
	}

	@Override
	public boolean isLineBreakpoint() {
		return false;
	}

	@Override
	public boolean isWatchpoint() {
		return true;
	}
	
	
	
}
