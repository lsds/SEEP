package uk.ac.imperial.lsds.streamsql.windows;

import uk.ac.imperial.lsds.seep.state.CustomState;

public class WindowState<K> implements CustomState {
	
	private K state;
	
	private IWindow window;
	
	public WindowState(IWindow window, K state) {
		this.window = window;
		this.state = state;
	}
	
	public IWindow getWindow() {
		return window;
	}

	public void setWindow(IWindow window) {
		this.window = window;
	}

	public K getState() {
		return state;
	}

	public void setState(K state) {
		this.state = state;
	}

}
