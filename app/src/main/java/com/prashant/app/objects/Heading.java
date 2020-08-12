/***
 * Excerpted from "OpenGL ES for Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/kbogla for more book information.
***/
package com.prashant.app.objects;

import com.prashant.app.data.VertexArray;
import com.prashant.app.programs.TextureShaderProgram;

import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.glDrawArrays;
import static com.prashant.app.Constants.BYTES_PER_FLOAT;

public class Heading {
    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int TEXTURE_COORDINATES_COMPONENT_COUNT = 2;
    private static final int STRIDE = (POSITION_COMPONENT_COUNT + TEXTURE_COORDINATES_COMPONENT_COUNT) * BYTES_PER_FLOAT;
    private static float n1 = 0.3f, n2 = 0.8f;

    private static final float[] VERTEX_DATA = {
        // Order of coordinates: X, Y, S, T

    n1, -n1, 0.5f, 0.5f,
    n2, n2, 0f, 0.9f,
    n1, n2, 1f, 0.9f,
    n1, -n1, 1f, 0.1f,
    n2, n2, 0f, 0.1f,
    n2, -n1, 0f, 0.9f };

    private final VertexArray vertexArray;

    public Heading() {
        vertexArray = new VertexArray(VERTEX_DATA);
    }
    
    public void bindData(TextureShaderProgram textureProgram) {
        vertexArray.setVertexAttribPointer(0, textureProgram.getPositionAttributeLocation(), POSITION_COMPONENT_COUNT, STRIDE);
        vertexArray.setVertexAttribPointer(POSITION_COMPONENT_COUNT, textureProgram.getTextureCoordinatesAttributeLocation(), TEXTURE_COORDINATES_COMPONENT_COUNT, STRIDE);
    }
        
    public void draw() {                                
        glDrawArrays(GL_TRIANGLE_FAN, 0, 6);
    }    
}
