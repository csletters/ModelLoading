package com.example.modelloading;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import vuforia.LoadingDialogHandler;
import vuforia.SampleApplicationSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import com.qualcomm.vuforia.Matrix44F;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Tool;
import com.qualcomm.vuforia.Trackable;
import com.qualcomm.vuforia.TrackableResult;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.Matrix;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MyRenderer implements Renderer {

	private final Context mActivityContext;
	float[] projection = new float[16];
	float[] view = new float[16];
	float[] model = new float[16];
	float[] mv = new float[16];
	float[] MVP = new float[16];
	float[] rotMatrix = new float[16];
	float[] transMatrix = new float[16];
	int horseColor = 0;
	SampleApplicationSession vuforiaAppSession;
	int widthView, heightView;
	int vertexShaderHandle, fragmentShaderHandle, mProgram, positionHandle,
			mTextureCoordinateHandle, mTextureDataHandle;
	int projectionHandle, viewmatHandle, modelHandle, mTextureUniformHandle,
			mhorsecolorHandler, modelviewHandle;
	Ground groundModel;
	com.qualcomm.vuforia.Renderer vuforiaRenderer;
	int frame = 0;
	LoadingDialogHandler loadDialog;
	ArrayList<ObjLoader> horseFrames;
	ArrayList<FloatBuffer> vertexBuffers;
	private FloatBuffer texBuffer;
	TextView jockey, trainer, position, horse;
	ImageView portraitImage;
	String currentlyTracking = "", jockeyName = "", horseName= "", trainerName="",currentPosition="";
	JSONArray articles;
	Bitmap jockeyImage;

	public MyRenderer(final Context activityContext,SampleApplicationSession session,LoadingDialogHandler loadingDialogHandler, TextView jockeyText,TextView horseText, TextView trainerText, TextView positionText,ImageView image) {
		mActivityContext = activityContext;
		vuforiaAppSession = session;
		loadDialog = loadingDialogHandler;
		jockey = jockeyText;
		trainer = trainerText;
		position = positionText;
		horse = horseText;
		portraitImage = image;
		retrieveData();
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		// TODO Auto-generated method stub
		GLES20.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		State state = vuforiaRenderer.begin();
		vuforiaRenderer.drawVideoBackground();

		// draw horse
		long time = SystemClock.uptimeMillis() % 4000L;
		float angle = 0.090f * ((int) time);
		GLES20.glUseProgram(mProgram);
		if (frame > 13)
			frame = 0;
		if (state.getNumTrackableResults() > 0) {
			TrackableResult result = state.getTrackableResult(0);
			Trackable trackable = result.getTrackable();
			Matrix44F modelViewMatrix_Vuforia = Tool
					.convertPose2GLMatrix(result.getPose());
			mv = modelViewMatrix_Vuforia.getData();
			Matrix.scaleM(mv, 0, 3, 3, 3);
			Matrix.rotateM(mv, 0, 90.0f, 1.0f, 0, 0);

			// check if image needs to be updated
			if (currentlyTracking.equals(trackable.getName())) {
				// do nothing
			} else {
				currentlyTracking = trackable.getName();
				try {
					updateData(trackable.getName());
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			displayData(trackable.getName());
			projection = vuforiaAppSession.getProjectionMatrix().getData();
			groundModel.draw(vertexBuffers.get(frame), texBuffer,
					mTextureDataHandle, model, view, projection, mv,
					modelviewHandle, mhorsecolorHandler, positionHandle,
					mTextureCoordinateHandle, projectionHandle, viewmatHandle,
					modelHandle, mTextureUniformHandle, horseFrames.get(frame)
							.getVertices().length, horseColor);
			frame++;
		}
		/*
		 * Matrix.translateM(model, 0, 0, -10, 0); Matrix.scaleM(model, 0, 0.7f,
		 * 0.7f, 0.7f); Matrix.rotateM(model, 0, angle, 0.0f, 1.0f, 0.0f);
		 */

		// Matrix.setIdentityM(model, 0);

		vuforiaRenderer.end();
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		// TODO Auto-generated method stub
		GLES20.glViewport(0, 0, width, height);
		float ratio = (float) width / height;
		Matrix.perspectiveM(projection, 0, 90, ratio, 1, 1000);
		Matrix.setLookAtM(view, 0, 0, 10, 50, 0, 0, 0, 0, 1, 0);
		Matrix.setIdentityM(model, 0);
		Matrix.multiplyMM(mv, 0, view, 0, model, 0);
		Matrix.multiplyMM(MVP, 0, projection, 0, mv, 0);
		Matrix.setIdentityM(transMatrix, 0);
		widthView = width;
		heightView = height;
		vuforiaAppSession.onSurfaceChanged(width, height);

	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		// TODO Auto-generated method stub
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		GLES20.glClearDepthf(1.0f);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		GLES20.glDepthFunc(GLES20.GL_LESS);
		GLES20.glDepthMask(true);
		groundModel = new Ground();
		vuforiaRenderer = com.qualcomm.vuforia.Renderer.getInstance();
		vuforiaAppSession.onSurfaceCreated();
		// load horse textures and vertices
		loadVerticesTextures();

		// load horse shader program
		loadHorseShader();
		loadDialog.sendEmptyMessage(LoadingDialogHandler.HIDE_LOADING_DIALOG);
	}

	public void updateTranslation(float xcord, float ycord) {
		Matrix.translateM(transMatrix, 0, xcord, 0, ycord);
	}

	public void loadVerticesTextures() {
		horseFrames = new ArrayList<ObjLoader>();
		vertexBuffers = new ArrayList<FloatBuffer>();
		//load vertices of each frame
		for (int x = 1; x <= 14; x++) {
			ObjLoader load = new ObjLoader("horse" + x + ".obj",mActivityContext);
			horseFrames.add(load);
		}
		//create and store a vertexbuffer for each frame
		for (int x = 0; x < 14; x++) {
			// buffer for vertices
			ByteBuffer buffer = ByteBuffer.allocateDirect(4 * horseFrames
					.get(x).getVertices().length);
			buffer.order(ByteOrder.nativeOrder());

			FloatBuffer vertexBuffer;
			vertexBuffer = buffer.asFloatBuffer();
			vertexBuffer.put(horseFrames.get(x).getVertices());
			vertexBuffer.position(0);
			vertexBuffers.add(vertexBuffer);
		}

		//load texture cords buffer
		ByteBuffer tBuffer = ByteBuffer.allocateDirect(4 * horseFrames.get(0)
				.getTexCords().length);
		tBuffer.order(ByteOrder.nativeOrder());

		texBuffer = tBuffer.asFloatBuffer();
		texBuffer.put(horseFrames.get(0).getTexCords());
		texBuffer.position(0);
	}

	public void loadHorseShader() {
		// load shaders
		String vertexShaderCode = RawResourceReader
				.readTextFileFromRawResource(mActivityContext, R.raw.vertex);
		String fragmentShaderCode = RawResourceReader
				.readTextFileFromRawResource(mActivityContext, R.raw.fragment);

		vertexShaderHandle = ShaderHelper.compileShader(
				GLES20.GL_VERTEX_SHADER, vertexShaderCode);
		fragmentShaderHandle = ShaderHelper.compileShader(
				GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

		mProgram = ShaderHelper.createAndLinkProgram(vertexShaderHandle,
				fragmentShaderHandle);

		// load texture
		mTextureDataHandle = TextureHelper.loadTexture(mActivityContext,
				R.drawable.texture4);

		// attributes
		positionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
		mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgram,
				"aTexCord");

		// uniforms
		projectionHandle = GLES20.glGetUniformLocation(mProgram, "projection");
		viewmatHandle = GLES20.glGetUniformLocation(mProgram, "view");
		modelHandle = GLES20.glGetUniformLocation(mProgram, "model");
		mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram,
				"uTexture");
		mhorsecolorHandler = GLES20.glGetUniformLocation(mProgram,
				"uhorsecolor");
		modelviewHandle = GLES20.glGetUniformLocation(mProgram, "mv");
	}

	private void updateData(String trackerName) throws JSONException {
		// TODO Auto-generated method stub
		if (trackerName.equals("chips")) {
			horseColor = 0;
			jockeyName = articles.getJSONObject(0).getString("title");
			horseName = "This horse";
			trainerName = "This trainer";
			currentPosition = "1";
			jockeyImage = DownloadImage("http://static2.wikia.nocookie.net/__cb20100723234957/starcraft/images/thumb/2/20/SCV_SC2_Head1.jpg/157px-SCV_SC2_Head1.jpg");
		}
		if(trackerName.equals("stones")) {
			horseColor = 1;
			jockeyName = "That jockey";
			horseName = "That horse";
			trainerName = "That trainer";
			currentPosition = "2";
			jockeyImage = DownloadImage("http://static3.wikia.nocookie.net/__cb20100721020260/starcraft/images/thumb/c/c7/SiegeTank_SC2_Head1.jpg/157px-SiegeTank_SC2_Head1.jpg");
		}
	}

	public void displayData(String trackerName) {

		portraitImage.post(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				portraitImage.setImageBitmap(jockeyImage);
			}

		});
		jockey.post(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				jockey.setText(jockeyName);
			}

		});
		horse.post(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				horse.setText(horseName);
			}

		});
		trainer.post(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				trainer.setText(trainerName);
			}

		});
		position.post(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				position.setText(currentPosition);
			}

		});

	}

	private void retrieveData() {
		// TODO Auto-generated method stub
		new HttpAsyncTask()
				.execute("http://hmkcode.appspot.com/rest/controller/get.json");
	}

	private static String convertInputStreamToString(InputStream inputStream)
			throws IOException {
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(inputStream));
		String line = "";
		String result = "";
		while ((line = bufferedReader.readLine()) != null)
			result += line;

		inputStream.close();
		return result;

	}

	public static String GET(String url) {
		InputStream inputStream = null;
		String result = "";
		try {

			// create HttpClient
			HttpClient httpclient = new DefaultHttpClient();

			// make GET request to the given URL
			HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

			// receive response as inputStream
			inputStream = httpResponse.getEntity().getContent();

			// convert inputstream to string
			if (inputStream != null)
				result = convertInputStreamToString(inputStream);
			else
				result = "Did not work!";

		} catch (Exception e) {
			Log.d("InputStream", e.getLocalizedMessage());
		}

		return result;
	}

	private class HttpAsyncTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {

			return GET(urls[0]);
		}

		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(String result) {
			try {
				JSONObject json = new JSONObject(result);
				String str = "";

				articles = json.getJSONArray("articleList");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private Bitmap DownloadImage(String URL) {
        Bitmap bitmap = null;
        InputStream in = null;
        try {
            in = OpenHttpConnection(URL);
            bitmap = BitmapFactory.decodeStream(in);
            in.close();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        return bitmap;
    }
	
	 private InputStream OpenHttpConnection(String urlString) throws IOException {
	        InputStream in = null;
	        int response = -1;

	        URL url = new URL(urlString);
	        URLConnection conn = url.openConnection();

	        if (!(conn instanceof HttpURLConnection))
	            throw new IOException("Not an HTTP connection");

	        try {
	            HttpURLConnection httpConn = (HttpURLConnection) conn;
	            httpConn.setAllowUserInteraction(false);
	            httpConn.setInstanceFollowRedirects(true);
	            httpConn.setRequestMethod("GET");
	            httpConn.connect();
	            response = httpConn.getResponseCode();
	            if (response == HttpURLConnection.HTTP_OK) {
	                in = httpConn.getInputStream();
	            }
	        } catch (Exception ex) {
	            throw new IOException("Error connecting");
	        }
	        return in;
	    }

}
