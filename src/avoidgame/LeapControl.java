package avoidgame;

/******************************************************************************\
 * Copyright (C) 2012-2013 Leap Motion, Inc. All rights reserved.               *
 * Leap Motion proprietary and confidential. Not for distribution.              *
 * Use subject to the terms of the Leap Motion SDK Agreement available at       *
 * https://developer.leapmotion.com/sdk_agreement, or another agreement         *
 * between Leap Motion and you, your company or other organization.             *
 \******************************************************************************/

import java.io.IOException;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.SlickException;

import com.leapmotion.leap.CircleGesture;
import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Finger;
import com.leapmotion.leap.FingerList;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Gesture;
import com.leapmotion.leap.Gesture.State;
import com.leapmotion.leap.GestureList;
import com.leapmotion.leap.Hand;
import com.leapmotion.leap.KeyTapGesture;
import com.leapmotion.leap.Listener;
import com.leapmotion.leap.ScreenTapGesture;
import com.leapmotion.leap.SwipeGesture;
import com.leapmotion.leap.Vector;

class SampleListener extends Listener {
	Avoid myGame;
	AppGameContainer app;

	public SampleListener(Avoid a) throws SlickException {
		myGame = a;
		a.mouse = false;
		app = new AppGameContainer(myGame);
	}

	public void onInit(Controller controller) {
		System.out.println("Initialized");
	}

	public void onConnect(Controller controller) {
		controller.enableGesture(Gesture.Type.TYPE_SCREEN_TAP);
		System.out.println("Connected");

	}

	public void onDisconnect(Controller controller) {
		System.out.println("Disconnected");
	}

	public void onExit(Controller controller) {
		System.out.println("Exited");
	}

	public void onFrame(Controller controller) {
		// Get the most recent frame and report some basic information
		Frame frame = controller.frame();

		if (!frame.hands().empty()) {
			// Get the first hand
			Hand hand = frame.hands().get(0);

			// Check if the hand has any fingers
			FingerList fingers = hand.fingers();
			if (!fingers.empty()) {
				// Calculate the hand's average finger tip position
				Vector avgPos = Vector.zero();
				for (Finger finger : fingers) {
					avgPos = avgPos.plus(finger.tipPosition());
				}

				avgPos = avgPos.divide(fingers.count());

				float x = ((float) avgPos.getX() + 100) * 6;
				float y = 1000 - (3f * (float) avgPos.getY());
				float z = (float) avgPos.getZ();

				if (!myGame.gameOver) {
					myGame.leapUpdatePlayer(x, y);
					if (z <= -120) {
						myGame.leapLaser();
					} else {
						myGame.stopShootLaser();
					}
				}

			}
		}

	}
}

class LeapControl {
	public static void main(String[] args) throws SlickException {

		// Create a sample listener and controller
		SampleListener listener = new SampleListener(new Avoid("Avoid Game"));
		Controller controller = new Controller();
		// Have the sample listener receive events from the controller
		controller.addListener(listener);
		listener.app.setDisplayMode(1280, 768, false);
		listener.app.setShowFPS(false);
		listener.app.start();
		// Keep this process running until Enter is pressed
		System.out.println("Press Enter to quit...");
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Remove the sample listener when done
		controller.removeListener(listener);
	}
}
