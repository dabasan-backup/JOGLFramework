package com.github.dabasan.joglf.gl;

public class TestMain {
	public static void main(String[] args) {
		new TestMain();
	}
	public TestMain() {
		var window = new Viewer();
		window.SetExitProcessWhenDestroyed();
	}
}
