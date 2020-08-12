package com.prashant.app;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.prashant.app.objects.Button;
import com.prashant.app.objects.Heading;
import com.prashant.app.objects.Star;
import com.prashant.app.objects.Table;
import com.prashant.app.programs.ColorShaderProgram;
import com.prashant.app.programs.TextureShaderProgram;
import com.prashant.app.util.Geometry;
import com.prashant.app.util.Geometry.*;
import com.prashant.app.util.MatrixHelper;
import com.prashant.app.util.TextureHelper;

import androidx.lifecycle.ViewModel;

import static android.opengl.GLES20.glClearColor;
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
    private final float[] modelMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private final float[] viewProjectionMatrix = new float[16];
    private final float[] invertedViewProjectionMatrix = new float[16];
    private final float[] modelViewProjectionMatrix = new float[16];

    private TextureShaderProgram textureProgram;
    private ColorShaderProgram colorProgram;
    private Table table;
    private Star star;
    private Heading heading, bottomBar;
    private Button[] button = new Button[4];
    private Button darkPanel, lightPanel;
    private int texture, starTexture, headingTexture;
    private GLSurfaceView glSurfaceView;
    private Point buttonPosition;
    private float an = 0.0f, angle = 0f, lpanelPos = -1.9f;
    private boolean prsd = false, scroll = false, left = true;

    public TriviaRenderer(Context context, GLSurfaceView glSurfaceView) {
        this.context = context;
        this.glSurfaceView = glSurfaceView;
    }

    public void handleTouchPress(float normalizedX, float normalizedY) {

        Ray ray = convertNormalized2DPointToRay(normalizedX, normalizedY);

        // Now test if this ray intersects with the mallet by creating a
        // bounding sphere that wraps the mallet.
        Sphere malletBoundingSphere = new Sphere(new Point(buttonPosition.x/2, buttonPosition.y, buttonPosition.z),0.25f);

        // If the ray intersects (if the user touched a part of the screen that
        // intersects the mallet's bounding sphere), then set malletPressed =
        // true.
        if(!prsd)
            prsd = Geometry.intersects(malletBoundingSphere, ray);
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
        table = new Table();
        star = new Star();
        heading = new Heading();
        bottomBar = new Heading();
        buttonPosition = new Point(-0.85f, 0.3f, 0.7f);
        for(int i=0; i<button.length; i++)
            button[i] = new Button();
        darkPanel = new Button();
        lightPanel = new Button();
        textureProgram = new TextureShaderProgram(context);
        colorProgram = new ColorShaderProgram(context);
        texture = TextureHelper.loadTexture(context, R.drawable.trivia_bg);
        starTexture = TextureHelper.loadTexture(context, R.drawable.star);
        headingTexture = TextureHelper.loadTexture(context, R.drawable.heading);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        glViewport(0, 0, width, height);
        MatrixHelper.perspectiveM(projectionMatrix, 45, (float) width/ (float) height, 1f, 10f);
        setLookAtM(viewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        invertM(invertedViewProjectionMatrix, 0, viewProjectionMatrix, 0);

        positionTableInScene();
        textureProgram.useProgram();
        textureProgram.setUniforms(modelViewProjectionMatrix, texture);
        table.bindData(textureProgram);
        table.draw();

        positionHeadingInScene(-1.4f, -0.3f, 0.7f, false);
        textureProgram.useProgram();
        textureProgram.setUniforms(modelViewProjectionMatrix, headingTexture);
        heading.bindData(textureProgram);
        heading.draw();

        positionHeadingInScene(0.4f, -0.3f, 0.7f, true);

        textureProgram.useProgram();
        textureProgram.setUniforms(modelViewProjectionMatrix, headingTexture);
        bottomBar.bindData(textureProgram);
        bottomBar.draw();

        if(an<364.0f) {
            positionObjectInScene(buttonPosition.x, buttonPosition.y, buttonPosition.z);
            colorProgram.useProgram();
            colorProgram.setUniforms(modelViewProjectionMatrix, 0.7f, 0.7f, 0.7f);
            button[0].bindData(colorProgram);
            button[0].draw();

            positionObjectInScene(-0.3f, 0.3f, 0.7f);
            colorProgram.useProgram();
            colorProgram.setUniforms(modelViewProjectionMatrix, 0.7f, 0.7f, 0.7f);
            button[1].bindData(colorProgram);
            button[1].draw();

            positionObjectInScene(-0.85f, -0.85f, 0.7f);
            colorProgram.useProgram();
            colorProgram.setUniforms(modelViewProjectionMatrix, 0.7f, 0.7f, 0.7f);
            button[2].bindData(colorProgram);
            button[2].draw();

            positionObjectInScene(-0.3f, -0.85f, 0.7f);
            colorProgram.useProgram();
            colorProgram.setUniforms(modelViewProjectionMatrix, 0.7f, 0.7f, 0.7f);
            button[3].bindData(colorProgram);
            button[3].draw();
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
    }


    private void positionTableInScene() {
        // The table is defined in terms of X & Y coordinates, so we rotate it
        // 90 degrees to lie flat on the XZ plane.
        setIdentityM(modelMatrix, 0);
        multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix,0, modelMatrix, 0);
    }

    private void positionHeadingInScene(float x, float y, float z, boolean bottom) {
        // The table is defined in terms of X & Y coordinates, so we rotate it
        // 90 degrees to lie flat on the XZ plane.
        setIdentityM(modelMatrix, 0);
        translateM(modelMatrix, 0, x, y, z);
        if(bottom)
            scaleM(modelMatrix,0, 1.0f, 1.4f, 1.0f);
        multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix,0, modelMatrix, 0);
    }

    private void positionStarInScene() {
        setIdentityM(modelMatrix, 0);
        angle+=2.5f;
        if(angle>300) scroll = true;
        rotateM(modelMatrix, 0, angle, 1f, 0f, 0f);
        multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix,0, modelMatrix, 0);
    }

    private void positionObjectInScene(float x, float y, float z) {
        setIdentityM(modelMatrix, 0);
        translateM(modelMatrix, 0, x, y, z);
        if(prsd) {
            an+=0.4f;
            rotateM(modelMatrix, 0,  an, 0.0f, 1.0f, 0.0f);
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
        prsd = false;
        scroll = false;
        lpanelPos = -1.9f;
        left = true;
        an = 0.0f;
        angle = 0.0f;
    }
}
