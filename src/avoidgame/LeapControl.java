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
import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Finger;
import com.leapmotion.leap.FingerList;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Hand;
import com.leapmotion.leap.Listener;
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
					// add the position vectors up for each finger
					avgPos = avgPos.plus(finger.tipPosition());
				}
				// divide the position vectors by the number of fingers
				avgPos = avgPos.divide(fingers.count());
				// scale the x position to the game resolution
				float x = ((float) avgPos.getX() + 100) * 6;
				// scale the y position to the game resolution
				float y = 1000 - (3f * (float) avgPos.getY());
				// z-axis value for the laser
				float z = (float) avgPos.getZ();

				if (!myGame.gameOver) {
					// move the player to the position
					myGame.movePlayer(x, y);
					// if you lean your hand forward enough,
					// this will shoot the laser
					if (z <= -120) {
						myGame.shootLaser();
					} else { // stop shooting the laser if the hand does
							 // not goin far enough
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
		SampleListener listener = new SampleListener(new Avoid("Avoid Game",
				true, false));
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
