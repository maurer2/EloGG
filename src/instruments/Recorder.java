package instruments;

import processing.core.PApplet;
import SimpleOpenNI.SimpleOpenNI;

public class Recorder extends PApplet {
	public SimpleOpenNI context;
	public boolean recordFlag = false;

	public void setup() {
		context = new SimpleOpenNI(this);

		if (recordFlag == false) {
			// playing, this works without the camera
			if (context.openFileRecording("guitar_short.oni") == false) {
				println("can't find recording !!!!");
				exit();
			}

			// it's possible to run the sceneAnalyzer over the recorded data
			// strea
			if (context.enableScene() == false) {
				println("can't setup scene!!!!");
				exit();
				return;
			}

			println("This file has " + context.framesPlayer() + " frames.");
		} else {
			// recording
			// enable depthMap generation
			if (context.enableDepth() == false) {
				println("Can't open the depthMap, maybe the camera is not connected!");
				exit();
				return;
			}

			// enable ir generation
			if (context.enableRGB() == false) {
				println("Can't open the rgbMap, maybe the camera is not connected or there is no rgbSensor!");
				exit();
				return;
			}

			// setup the recording
			context.enableRecorder(SimpleOpenNI.RECORD_MEDIUM_FILE,
					"guitar.oni");

			// select the recording channels
			context.addNodeToRecording(SimpleOpenNI.NODE_DEPTH,
					SimpleOpenNI.CODEC_16Z_EMB_TABLES);
			context.addNodeToRecording(SimpleOpenNI.NODE_IMAGE,
					SimpleOpenNI.CODEC_JPEG);
		}

		// set window size
		if ((context.nodes() & SimpleOpenNI.NODE_DEPTH) != 0) {
			if ((context.nodes() & SimpleOpenNI.NODE_IMAGE) != 0)
				// depth + rgb
				size(context.depthWidth() + 10 + context.rgbWidth(),
						context.depthHeight() > context.rgbHeight() ? context
								.depthHeight() : context.rgbHeight());
			else
				// only depth
				size(context.depthWidth(), context.depthHeight());
		} else
			exit();
	}

	public void draw() {
		// update
		context.update();

		background(200, 0, 0);

		// draw the cam data
		if ((context.nodes() & SimpleOpenNI.NODE_DEPTH) != 0) {
			if ((context.nodes() & SimpleOpenNI.NODE_IMAGE) != 0) {
				image(context.depthImage(), 0, 0);
				image(context.rgbImage(), context.depthWidth() + 10, 0);
			} else
				image(context.depthImage(), 0, 0);
		}

		if ((context.nodes() & SimpleOpenNI.NODE_SCENE) != 0)
			image(context.sceneImage(), 0, 0, context.sceneWidth() * .4f,
					context.sceneHeight() * .4f);

		// draw timeline
		if (recordFlag == false)
			drawTimeline();

	}

	public void drawTimeline() {
		pushStyle();

		stroke(255, 255, 0);
		line(10, height - 20, width - 10, height - 20);

		stroke(0);
		rectMode(CENTER);
		fill(255, 255, 0);

		int pos = (int) ((width - 2 * 10) * (float) context.curFramePlayer() / (float) context
				.framesPlayer());
		rect(pos, height - 20, 7, 17);

		popStyle();
	}

	public void keyPressed() {
		switch (key) {
		case CODED:
			switch (keyCode) {
			case LEFT:
				// jump back
				context.seekPlayer(-3, SimpleOpenNI.PLAYER_SEEK_CUR);
				break;
			case RIGHT:
				// jump forward
				context.seekPlayer(3, SimpleOpenNI.PLAYER_SEEK_CUR);
				break;
			case UP:
				// slow down
				context.setPlaybackSpeedPlayer(context.playbackSpeedPlayer() * 2.0f);
				println("playbackSpeedPlayer: " + context.playbackSpeedPlayer());
				break;
			case DOWN:
				// speed up
				context.setPlaybackSpeedPlayer(context.playbackSpeedPlayer() * 0.5f);
				println("playbackSpeedPlayer: " + context.playbackSpeedPlayer());
				break;
			}
			break;
		case ' ':
			// toggle pause
			context.setRepeatPlayer(!context.repeatPlayer());
			println("RepeatMode: " + context.repeatPlayer());
			break;
		case BACKSPACE:
			// restart
			context.seekPlayer(0, SimpleOpenNI.PLAYER_SEEK_SET);
			break;
		}
	}

}
