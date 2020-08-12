/***
 * Excerpted from "OpenGL ES for Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/kbogla for more book information.
***/
package com.prashant.app.objects;

import com.prashant.app.Constants;
import com.prashant.app.data.VertexArray;
import com.prashant.app.programs.ColorShaderProgram;

import java.util.List;

import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glDrawArrays;

public class Panel {

    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int COLOR_COMPONENT_COUNT = 4;
//    private static final int STRIDE = POSITION_COMPONENT_COUNT * Constants.BYTES_PER_FLOAT;
    private static final int STRIDE = (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * Constants.BYTES_PER_FLOAT;
    private final VertexArray vertexArray;

    private static float n1 = 0.3f, n2 = 0.8f;

    private static final float[] VERTEX_DATA = {
            // Order of coordinates: X, Y, R,G,B

//            0.2f, 0.2f, 0.7f, 0.7f, 0.7f,
//            0.3f, 0.3f, 0.7f, 0.7f, 0.7f,
//            0.2f, 0.3f, 0.7f, 0.7f, 0.7f,
//            0.2f, 0.2f, 0.7f, 0.7f, 0.7f,
//            0.3f, 0.3f, 0.7f, 0.7f, 0.7f,
//            0.3f, 0.2f, 0.7f, 0.7f, 0.7f,

            n1, -n1, 1.0f, 0.5f, 1.0f, 1.0f,
            n2, n2, 1.0f, 0.5f, 1.0f, 1.0f,
            n1, n2, 1.0f, 0.5f, 1.0f, 1.0f,
            n1, -n1, 1.0f, 0.5f, 1.0f, 1.0f,
            n2, n2, 1.0f, 0.5f, 1.0f, 1.0f,
            n2, -n1, 1.0f, 0.5f, 1.0f, 1.0f,

//            n1, -n1, 0.7f, 0.7f, 0.7f,
//            n2, n2, 0.7f, 0.7f, 0.7f,
//            n1, n2, 0.7f, 0.7f, 0.7f,
//            n1, -n1, 0.7f, 0.7f, 0.7f,
//            n2, n2, 0.7f, 0.7f, 0.7f,
//            n2, -n1, 0.7f, 0.7f, 0.7f,
    };

    public Panel() {
        vertexArray = new VertexArray(VERTEX_DATA);
    }

    public void bindData(ColorShaderProgram colorProgram) {
        vertexArray.setVertexAttribPointer(0,colorProgram.getPositionAttributeLocation(), POSITION_COMPONENT_COUNT, STRIDE);
    }

    public void draw() {
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 6);
    }
}