package com.prashant.app;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.prashant.app.objects.Panel;
import com.prashant.app.objects.ImageButton;
import com.prashant.app.objects.Star;
import com.prashant.app.objects.YellowBackground;
import com.prashant.app.objects.TimeIndicator;
import com.prashant.app.programs.ColorShaderProgram;
import com.prashant.app.programs.TextureShaderProgram;
import com.prashant.app.text.TextObject;
import com.prashant.app.util.Geometry;
import com.prashant.app.util.Geometry.*;
import com.prashant.app.util.MatrixHelper;
import com.prashant.app.util.TextureHelper;

import java.util.Random;

import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.invertM;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.multiplyMV;
import static android.opengl.Matrix.rotateM;
import static android.opengl.Matrix.scaleM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.setLookAtM;
import static android.opengl.Matrix.translateM;

public class TriviaRenderer implements GLSurfaceView.Renderer {
    private final Context context;

    private final float[] projectionMatrix = new float[16];
    private float[] modelMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private final float[] viewProjectionMatrix = new float[16];
    private final float[] invertedViewProjectionMatrix = new float[16];
    private final float[] modelViewProjectionMatrix = new float[16];
    private final float[] rotationMatrix = new float[16];

    private TextureShaderProgram textureProgram;
    private ColorShaderProgram colorProgram;
    private YellowBackground yellowBackground;
    private Star star;
    private ImageButton heading;
    private ImageButton[] imageButton = new ImageButton[4];
    private Panel darkPanel, lightPanel, bottomBar;
    private TimeIndicator scaleBar;
    private TextObject theTextObj = new TextObject();;
    private boolean mustRebuildText = true;
    private int yBgTexture, starTexture, headingTexture, buttonTexture, wrongButtonTexture, correctButtonTexture;
    private GLSurfaceView glSurfaceView;
    private Point b0Pos,b1Pos,b2Pos,b3Pos;
    private float an = 0.0f, angle = 0f, lpanelPos = -1.9f, sFactor = 1.4f, sFactorSpeed = 0.001f, newFactor = 0.0f;
    private boolean prsd = false, scroll = false, left = true;
    private int buttonNo = -1;
    private Random random = new Random();
    private boolean correct = random.nextBoolean();

    public TriviaRenderer(Context context, GLSurfaceView glSurfaceView) {
        this.context = context;
        this.glSurfaceView = glSurfaceView;
    }

    public void handleTouchPress(float normalizedX, float normalizedY) {

        Ray ray = convertNormalized2DPointToRay(normalizedX, normalizedY);

        // Now test if this ray intersects with the mallet by creating a
        // bounding sphere that wraps the mallet.
        Sphere button0 = new Sphere(new Point(b0Pos.x/2, b0Pos.y+0.25f, b0Pos.z),0.25f);
        Sphere button1 = new Sphere(new Point(b1Pos.x+0.5f, b1Pos.y, b1Pos.z),0.25f);
        Sphere button2 = new Sphere(new Point(b2Pos.x/2, b2Pos.y+0.25f, b2Pos.z),0.25f);
        Sphere button3 = new Sphere(new Point(b3Pos.x+0.5f, b3Pos.y, b3Pos.z),0.25f);

        // If the ray intersects (if the user touched a part of the screen that
        // intersects the mallet's bounding sphere), then set malletPressed =
        // true.
        if(!prsd) {
            if(Geometry.intersects(button0, ray)) { prsd = true; buttonNo=0; return; }
            if(Geometry.intersects(button1, ray)) { prsd = true; buttonNo=1; return; }
            if(Geometry.intersects(button2, ray)) { prsd = true; buttonNo=2; return; }
            if(Geometry.intersects(button3, ray)) { prsd = true; buttonNo=3; }
            else prsd = false;
        }
    }
    private Ray convertNormalized2DPointToRay(
            float normalizedX, float normalizedY) {
        // We'll convert these normalized device coordinates into world-space
        // coordinates. We'll pick a point on the near and far planes, and draw a
        // line between them. To do this transform, we need to first multiply by
        // the inverse matrix, and then we need to undo the perspective divide.
        final float[] nearPointNdc = {normalizedX, normalizedY, -1, 1};
        final float[] farPointNdc =  {normalizedX, normalizedY,  1, 1};

        final float[] nearPointWorld = new float[4];
        final float[] farPointWorld = new float[4];

        multiplyMV(nearPointWorld, 0, invertedViewProjectionMatrix, 0, nearPointNdc, 0);
        multiplyMV(farPointWorld, 0, invertedViewProjectionMatrix, 0, farPointNdc, 0);

        // Why are we dividing by W? We multiplied our vector by an inverse
        // matrix, so the W value that we end up is actually the *inverse* of
        // what the projection matrix would create. By dividing all 3 components
        // by W, we effectively undo the hardware perspective divide.
        divideByW(nearPointWorld);
        divideByW(farPointWorld);

        // We don't care about the W value anymore, because our points are now
        // in world coordinates.
        Point nearPointRay = new Point(nearPointWorld[0], nearPointWorld[1], nearPointWorld[2]);
        Point farPointRay = new Point(farPointWorld[0], farPointWorld[1], farPointWorld[2]);
        return new Ray(nearPointRay, Geometry.vectorBetween(nearPointRay, farPointRay));
    }

    private void divideByW(float[] vector) {
        vector[0] /= vector[3];
        vector[1] /= vector[3];
        vector[2] /= vector[3];
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        theTextObj.init();

        yellowBackground = new YellowBackground();
        star = new Star();
        heading = new ImageButton();
        bottomBar = new Panel();
        scaleBar = new TimeIndicator();
        b0Pos = new Point(-0.6f, 0.25f, 0.7f);
        b1Pos = new Point(-0.18f, 0.25f, 0.7f);
        b2Pos = new Point(-0.6f, -0.65f, 0.7f);
        b3Pos = new Point(-0.18f, -0.65f, 0.7f);
        for(int i = 0; i< imageButton.length; i++)
            imageButton[i] = new ImageButton();
        darkPanel = new Panel();
        lightPanel = new Panel();
        textureProgram = new TextureShaderProgram(context);
        colorProgram = new ColorShaderProgram(context);
        yBgTexture = TextureHelper.loadTexture(context, R.drawable.trivia_bg);
        starTexture = TextureHelper.loadTexture(context, R.drawable.star);
        headingTexture = TextureHelper.loadTexture(context, R.drawable.heading);
        buttonTexture = TextureHelper.loadTexture(context, R.drawable.btn_bg);
        wrongButtonTexture = TextureHelper.loadTexture(context, R.drawable.btn_wrong_bg);
        correctButtonTexture = TextureHelper.loadTexture(context, R.drawable.btn_correct_bg);

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        glViewport(0, 0, width, height);
        MatrixHelper.perspectiveM(projectionMatrix, 45, (float) width/ (float) height, 1f, 10f);
        setLookAtM(viewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        theTextObj.updateCamera(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        // Update the viewProjection matrix, and create an inverted matrix for
        // touch picking.
        multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        invertM(invertedViewProjectionMatrix, 0, viewProjectionMatrix, 0);

        positionYellowBackgroundInScene();
        textureProgram.useProgram();
        textureProgram.setUniforms(modelViewProjectionMatrix, yBgTexture);
        yellowBackground.bindData(textureProgram);
        yellowBackground.draw();

        positionHeadingInScene(-1.85f, -0.25f, 0.7f);
        textureProgram.useProgram();
        textureProgram.setUniforms(modelViewProjectionMatrix, headingTexture);
        heading.bindData(textureProgram);
        heading.draw();

        positionBottomObjectInScene(0.4f, -0.3f, 0.7f, false);
        colorProgram.useProgram();
        colorProgram.setUniforms(modelViewProjectionMatrix, 0.0f,0.0f,0.0f);
        bottomBar.bindData(colorProgram);
        bottomBar.draw();

        positionBottomObjectInScene(0.4f, -0.3f, 0.7f, true);
        colorProgram.useProgram();
        colorProgram.setUniforms(modelViewProjectionMatrix, 0.0f,0.0f,1.0f);
        scaleBar.bindData(colorProgram);
        scaleBar.draw();

        if(an<364.0f) {
            positionObjectInScene(b0Pos.x, b0Pos.y, b0Pos.z, buttonNo==0);
            textureProgram.useProgram();
            if(an>=180 && buttonNo==0) {
                if (correct) textureProgram.setUniforms(modelViewProjectionMatrix, correctButtonTexture);
                else textureProgram.setUniforms(modelViewProjectionMatrix, wrongButtonTexture);
            }
            else  textureProgram.setUniforms(modelViewProjectionMatrix, buttonTexture);
            imageButton[0].bindData(textureProgram);
            imageButton[0].draw();

            positionObjectInScene(b1Pos.x, b1Pos.y, b1Pos.z, buttonNo==1);
            textureProgram.useProgram();
            if(an>=180 && buttonNo==1) {
                if (correct) textureProgram.setUniforms(modelViewProjectionMatrix, correctButtonTexture);
                else textureProgram.setUniforms(modelViewProjectionMatrix, wrongButtonTexture);
            }
            else textureProgram.setUniforms(modelViewProjectionMatrix, buttonTexture);
            imageButton[1].bindData(textureProgram);
            imageButton[1].draw();

            positionObjectInScene(b2Pos.x, b2Pos.y, b2Pos.z, buttonNo==2);
            textureProgram.useProgram();
            if(an>=180 && buttonNo==2) {
                if (correct) textureProgram.setUniforms(modelViewProjectionMatrix, correctButtonTexture);
                else textureProgram.setUniforms(modelViewProjectionMatrix, wrongButtonTexture);
            }
            else textureProgram.setUniforms(modelViewProjectionMatrix, buttonTexture);
            imageButton[2].bindData(textureProgram);
            imageButton[2].draw();

            positionObjectInScene(b3Pos.x, b3Pos.y, b3Pos.z, buttonNo==3);
            textureProgram.useProgram();
            if(an>=180 && buttonNo==3) {
                if (correct) textureProgram.setUniforms(modelViewProjectionMatrix, correctButtonTexture);
                else textureProgram.setUniforms(modelViewProjectionMatrix, wrongButtonTexture);
            }
            else textureProgram.setUniforms(modelViewProjectionMatrix, buttonTexture);
            imageButton[3].bindData(textureProgram);
            imageButton[3].draw();
        }

        else{
            if(angle<360f) {
            positionStarInScene();
            textureProgram.useProgram();
            textureProgram.setUniforms(modelViewProjectionMatrix, starTexture);
            star.bindData(textureProgram);
            star.draw();
            }
        }

        if (lpanelPos >= -2.9f && scroll) {
            positionLightPanelObjectInScene();
            colorProgram.useProgram();
            colorProgram.setUniforms(modelViewProjectionMatrix, 0.2f, 0.2f, 0.2f);
            lightPanel.bindData(colorProgram);
            lightPanel.draw();
        }

        positionPanelObjectInScene(0.3f, -1.9f, 0.7f);
        colorProgram.useProgram();
        colorProgram.setUniforms(modelViewProjectionMatrix, 0.0f, 0.0f, 0.0f);
        darkPanel.bindData(colorProgram);
        darkPanel.draw();

        glSurfaceView.requestRender();
//
//        theTextObj.render();
//        theTextObj.setText("A high text quality!");
//        theTextObj.setPosition(1.0f, 4.5f, -1.0f);
//
//        if (mustRebuildText) {
//            theTextObj.update();
//            mustRebuildText = false;
//        }
//        checkGLError("onDrawFrame");
    }

    private void positionYellowBackgroundInScene() {
        setIdentityM(modelMatrix, 0);
        multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix,0, modelMatrix, 0);
    }

    private void positionHeadingInScene(float x, float y, float z) {
        // The yellowBackground is defined in terms of X & Y coordinates, so we rotate it
        // 90 degrees to lie flat on the XZ plane.
        setIdentityM(modelMatrix, 0);
        scaleM(modelMatrix, 0, 0.6f, 1.3f, 1.0f);
        translateM(modelMatrix, 0, x, y, z);
        multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix,0, modelMatrix, 0);
    }

    private void positionStarInScene() {
        setIdentityM(modelMatrix, 0);
        angle+=2.5f;
        if(angle>300) scroll = true;
        rotateM(modelMatrix, 0, angle, 1f, 0f, 0f);
        multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix,0, modelMatrix, 0);
    }

    private void positionObjectInScene(float x, float y, float z, boolean pressedButton) {
        setIdentityM(modelMatrix, 0);
        translateM(modelMatrix, 0, x, y, z);
        scaleM(modelMatrix,0,0.8f, 0.8f, 1.0f);
        if(prsd && pressedButton) {
            an+=2.4f;
            rotateM(modelMatrix, 0, an, 0.0f, 1.0f, 0.0f);
        }
        multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix,0, modelMatrix, 0);
    }

    private void positionBottomObjectInScene(float x, float y, float z, boolean sBar) {
        setIdentityM(modelMatrix, 0);

        if(sBar) {
            sFactor -= sFactorSpeed;
            newFactor += sFactorSpeed;
            if(sFactor<0.0f) sFactor = 0.0f;
            //Log.d("000000000",""+(y+(1.4f)-sFactor));
            translateM(modelMatrix, 0, x, (y+(1.4f)-sFactor)-newFactor/5, z);
//            translateM(modelMatrix, 0, x, y-sFactor+0.004f, z);
            scaleM(modelMatrix, 0, 1.0f, sFactor, 1.0f);
        }
        else {
            translateM(modelMatrix, 0, x, y, z);
            scaleM(modelMatrix, 0, 1.0f, 1.4f, 1.0f);
        }
        multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix,0, modelMatrix, 0);
    }

    private void positionPanelObjectInScene(float x, float y, float z) {
        setIdentityM(modelMatrix, 0);
        translateM(modelMatrix, 0, x, y, z);
        rotateM(modelMatrix, 0, 90.0f, 0f, 0.0f, 1.0f);
        scaleM(modelMatrix, 0, 1f, 1.4f,1f);
        multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix,0, modelMatrix, 0);
    }

    private void positionLightPanelObjectInScene() {
        setIdentityM(modelMatrix, 0);
        if(lpanelPos>-1.42f) left = false;
        if(left)
            lpanelPos += 0.0104f;
        else
            lpanelPos -= 0.0104f;
        if(lpanelPos<-1.9f) resetAll();
        translateM(modelMatrix, 0, 0.3f, lpanelPos, 0.7f);
        rotateM(modelMatrix, 0, 90.0f, 0f, 0.0f, 1.0f);
        scaleM(modelMatrix, 0, 1f, 1.4f,1f);
        multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix,0, modelMatrix, 0);
    }

    private void resetAll() {
        correct = random.nextBoolean();
        prsd = false;
        scroll = false;
        lpanelPos = -1.9f;
        left = true;
        an = 0.0f;
        angle = 0.0f;
        sFactor = 1.4f;
        newFactor = 0.0f;
        buttonNo = -1;
    }

    static public void checkGLError(final String aDesc) {
        int errorCode = GLES20.GL_NO_ERROR;
        do {
            errorCode =  GLES20.glGetError();
            if (errorCode != GLES20.GL_NO_ERROR)
                android.util.Log.i("ERROR", "GL error: " + aDesc + " errorCode:" + errorCode);
        } while (errorCode != GLES20.GL_NO_ERROR);
    }
}
