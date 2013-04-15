package avoidgame;

import java.util.prefs.Preferences;

import org.lwjgl.opengl.Display;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.KeyListener;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.ColorEffect;

public class Avoid extends BasicGame implements KeyListener {

	// Window Size============================================
	public static final int SCREEN_W = 1280;
	public static final int SCREEN_H = 768;

	// Images=================================================
	private Image playerImage, enemyImage, spacebackground;

	// Game Object============================================
	private Game game;

	// Player=================================================
	private Plane player;
	private float playerX;
	private float playerY;
	private int playerWidth;
	private int playerHeight;

	// Enemy==================================================
	private int enemyWidth;
	private int enemyHeight;

	// Level==================================================
	private int level = 1, threshold = 200;

	// Game States============================================
	boolean gameOver = true, menu = true, inGame, help, justResumed;

	// Contains bounding boxes if true=========================
	boolean diagnostics = false;

	// Mouse Control (for testing)=============================
	boolean mouse = true;

	// Lasers=================================================
	Laser laser = new Laser();
	private long curLaserTime;
	public static final int LASER_W = 100;
	boolean laserFiring;

	// Pausing================================================
	private long curPauseTime;
	long startPauseTime;
	boolean paused, pauseCountdown;

	// High Score=============================================
	private Preferences prefs;
	private String highScoreKey = "highscore";

	// Strings=================================================
	// Font to use
	UnicodeFont font;

	// Static Text
	private final static String title = "Avoid Game";
	private final static String pausedMessage = "<Space> to Resume. <Enter> for Menu.";
	private final static String startFromMenu = "<Enter> to Start";
	private int titleWidth, pausedMessageWidth, startFromMenuWidth;

	// Dynamic Text
	private String inGameOptions, countdownTime, gameOverMessage,
			highScoreMessage;
	private int inGameOptionsWidth, countdownTimeWidth, gameOverMessageWidth,
			highScoreMessageWidth;

	public Avoid(String title) {
		super(title);
		// Initialize preferences
		prefs = Preferences.userRoot().node(this.getClass().getName());
	}

	@Override
	public void render(GameContainer gc, Graphics g) throws SlickException {
		// Draw paused screen
		if (paused) {
			renderPause(gc, g);
		}
		// Draw the game
		else if (inGame) {
			renderGame(gc, g);
		}
		// Draw the menu
		else if (menu) {
			renderMenu(gc, g);
		}
		// Limit to 60 fps
		Display.sync(60);

	}

	private void renderPause(GameContainer gc, Graphics g)
			throws SlickException {
		spacebackground.draw(0, 0);
		g.setColor(Color.white);
		font.drawString(gc.getWidth() / 2 - pausedMessageWidth / 2,
				gc.getHeight() / 2, pausedMessage);
	}

	private void renderMenu(GameContainer gc, Graphics g) throws SlickException {

		spacebackground.draw(0, 0);
		g.setColor(Color.white);
		font.drawString(SCREEN_W / 2 - titleWidth / 2, SCREEN_H / 2 - 100,
				title);
		font.drawString(SCREEN_W / 2 - startFromMenuWidth / 2, SCREEN_H / 2,
				startFromMenu);

		font.drawString(SCREEN_W / 2 - highScoreMessageWidth / 2, 480,
				highScoreMessage);

	}

	private void renderGame(GameContainer gc, Graphics g) throws SlickException {
		spacebackground.draw(0, 0);

		font.drawString(0, 0, "Level : " + level);

		g.setColor(Color.blue);
		playerImage.draw(playerX, playerY);

		g.setColor(Color.green);
		if (diagnostics) {
			// Draw bounding boxes
			g.drawRect(player.body.left, player.body.bottom, player.body.width,
					player.body.height);
			g.drawRect(player.wings.left, player.wings.bottom,
					player.wings.width, player.wings.height);
		}
		if (laserFiring) {
			g.fillRect(playerX, 0, LASER_W, SCREEN_H - (SCREEN_H - playerY));

		}
		for (Plane plane : game.planes()) {
			enemyImage.draw(plane.x, plane.y);
			if (diagnostics) {
				// Draw bounding boxes
				g.drawRect(plane.body.left, plane.body.bottom,
						plane.body.width, plane.body.height);
				g.drawRect(plane.wings.left, plane.wings.bottom,
						plane.wings.width, plane.wings.height);
			}
		}
		if ((justResumed || gameOver) && pauseCountdown) {
			font.drawString(SCREEN_W / 2 - countdownTimeWidth / 2,
					SCREEN_H / 3, countdownTime);
		}
		if (laser.getEnergy() > 20) {
			g.setColor(Color.green);
		} else {
			g.setColor(Color.red);
		}
		g.fillRect(0, SCREEN_H - 50, laser.getEnergy() * 3, 50);
		if (gameOver && gameOverMessage != null) {
			font.drawString(SCREEN_W / 2 - gameOverMessageWidth / 2,
					SCREEN_H / 2, gameOverMessage);
		} else {
			font.drawString(SCREEN_W / 2 - inGameOptionsWidth / 2, 0,
					inGameOptions);
		}
	}

	@Override
	public void init(GameContainer gc) throws SlickException {
		// Load font
		font = new UnicodeFont("fonts/font.ttf", 44, false, false);
		font.addAsciiGlyphs();
		font.getEffects().add(new ColorEffect());
		font.loadGlyphs();
		// Load the images
		playerImage = new Image("res/jet.png");
		spacebackground = new Image("res/spacebackground.png");
		enemyImage = new Image("res/enemy.png");

		// Set the dimensions
		enemyWidth = enemyImage.getWidth();
		enemyHeight = enemyImage.getHeight();
		playerWidth = playerImage.getWidth();
		playerHeight = playerImage.getHeight();

		// Player
		playerX = SCREEN_W / 2 - playerWidth / 2;
		playerY = SCREEN_H - 1.5f * playerHeight;
		player = new Plane(playerX, playerY, playerWidth, playerHeight, -1,
				false);

		// Game object for enemy planes
		game = new Game(level, enemyWidth, enemyHeight);

		// Update text
		highScoreMessage = "High Score: " + prefs.getInt(highScoreKey, 0);
		inGameOptions = " Score: 0 <Space> for Pause";
		countdownTime = "3";

		// Update text width
		highScoreMessageWidth = font.getWidth(highScoreMessage);
		inGameOptionsWidth = font.getWidth(inGameOptions);
		pausedMessageWidth = font.getWidth(pausedMessage);
		startFromMenuWidth = font.getWidth(startFromMenu);
		countdownTimeWidth = font.getWidth(countdownTime);
		titleWidth = font.getWidth(title);

	}

	public void leapUpdatePlayer(float x, float y) {
		if (!paused && !justResumed) {
			if (x < 0) {
				x = 0;
			}
			if (x > SCREEN_W - playerWidth) {
				x = SCREEN_W - playerWidth;
			}
			if (y < 0) {
				y = 0;
			}
			if (y > SCREEN_H - playerHeight) {
				y = SCREEN_H - playerHeight;
			}

			playerX = x;
			playerY = y;
			player.updatePlayer(playerX, playerY);
		}
	}

	public void leapLaser() {
		if (laser.getEnergy() > 20 && !laserFiring) {
			laserFiring = true;
			startLaserCountdown();
		}
	}

	public void stopShootLaser() {
		laserFiring = false;
	}

	public void levelUp() {
		level++;
		threshold += level * 150;
		if (level < 9) {
			game.addPlane();
		}
	}

	public void startCountdown() {
		curPauseTime = System.currentTimeMillis() / 1000;
		pauseCountdown = true;
	}

	public void startLaserCountdown() {
		curLaserTime = System.currentTimeMillis();
	}

	public void countDown() {
		long timeRemaining = 3 - (System.currentTimeMillis() / 1000 - curPauseTime);
		countdownTime = "" + timeRemaining;
		countdownTimeWidth = font.getWidth(countdownTime);
		if (timeRemaining < 1) {
			if (!justResumed) {
				restart();
			} else {
				justResumed = false;
				gameOver = false;
			}
			pauseCountdown = false;
		}
	}

	public void laserCooldown() {
		// the number of seconds since you last fired the laser
		long laserElapsedTime = System.currentTimeMillis() - curLaserTime;
		if (laser.isDepleted()) {
			laserFiring = false;
		} else if (laserFiring && laserElapsedTime > 10) {
			curLaserTime = System.currentTimeMillis();
			laser.deplete();
		}
	}

	public void reset() {
		level = 1;
		game = new Game(level, enemyWidth, enemyHeight);
		threshold = 200;
		laserFiring = false;
		laser.resetEnergy();
	}

	public void restart() {
		reset();
		gameOver = false;

	}

	@Override
	public void update(GameContainer gc, int delta) throws SlickException {
		if (inGame && (gameOver || justResumed) && pauseCountdown) {
			countDown();

		}
		if (!gameOver && inGame && !paused && !justResumed) {
			if (laserFiring) {
				laserCooldown();
			} else {
				laser.regen();
			}
			if (mouse) {
				Input g = gc.getInput();
				int x = g.getMouseX();
				int y = g.getMouseY();
				if (g.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)) {
					if (laser.getEnergy() > 20) {
						laserFiring = true;
						startLaserCountdown();
					}
				} else {
					laserFiring = false;
				}
				if (x < 0) {
					x = 0;
				}
				if (x > SCREEN_W - playerWidth) {
					x = SCREEN_W - playerWidth;
				}
				if (y < 0) {
					y = 0;
				}
				if (y > SCREEN_H - playerHeight) {
					y = SCREEN_H - playerHeight;
				}

				playerX = x;
				playerY = y;

				player.updatePlayer(playerX, playerY);
			}
			// ============================================================

			// Move enemy planes
			game.movePlanes(laserFiring, playerX, LASER_W, SCREEN_H
					- (SCREEN_H - playerY));

			// Update the score text
			inGameOptions = "Score: " + game.getScore() + "<Space> for Pause";
			inGameOptionsWidth = font.getWidth(inGameOptions);

			// Check for loss
			if (game.checkLose(player)) {
				gameOver = true;
				int curHighScore = prefs.getInt(highScoreKey, 0);
				int curGameScore = game.getScore();
				if (curGameScore > curHighScore) {
					prefs.putInt(highScoreKey, curGameScore);
					highScoreMessage = "High Score: "
							+ prefs.getInt(highScoreKey, 0);
					highScoreMessageWidth = font.getWidth(highScoreMessage);
				}
				gameOverMessage = "Game Over. Score: " + game.getScore()
						+ ".<Space> to restart and <Enter> to menu";
				gameOverMessageWidth = font.getWidth(gameOverMessage);
				return;
			}

			// Check for level up
			if (game.getScore() > threshold) {
				levelUp();
			}
		}

	}

	@Override
	public void keyPressed(int key, char c) {
		switch (key) {
		case Input.KEY_ENTER:
			// menu to game
			if (menu) {
				reset();
				startCountdown();
				menu = false;
				inGame = true;
			}// leave from pause to menu
			else if (paused || gameOver && !pauseCountdown) {
				reset();
				inGameOptions = "Score: 0 <Space> for Pause";
				inGameOptionsWidth = font.getWidth(inGameOptions);
				inGame = false;
				gameOver = true;
				menu = true;
				paused = false;
			}

			break;
		case Input.KEY_SPACE:
			// restart after game over
			if (inGame && gameOver && !pauseCountdown) {
				startCountdown();
			}
			// pause
			else if (inGame) {
				paused = true;
				inGame = false;
				startPauseTime = System.currentTimeMillis() / 1000;

			}
			// unpause
			else if (paused) {
				menu = false;
				inGame = true;
				paused = false;
				justResumed = true;
				startCountdown();
			}

			break;

		}

	}

	public static void main(String[] args) throws SlickException {
		AppGameContainer app = new AppGameContainer(new Avoid("Avoid Game"));
		app.setDisplayMode(1280, 768, false);
		app.setShowFPS(false);
		app.start();
	}
}
