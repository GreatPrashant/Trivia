package com.prashant.app.objects;

import com.prashant.app.data.VertexArray;
import com.prashant.app.programs.TextureShaderProgram;

import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.glDrawArrays;
import static com.prashant.app.Constants.BYTES_PER_FLOAT;

public class YellowBackground {
    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int TEXTURE_COORDINATES_COMPONENT_COUNT = 2;
    private static final int STRIDE = (POSITION_COMPONENT_COUNT + TEXTURE_COORDINATES_COMPONENT_COUNT) * BYTES_PER_FLOAT;
    
    private static final float[] VERTEX_DATA = {
        // Order of coordinates: X, Y, S, T
             0.0f,  0.0f, 0.5f, 0.5f,
            -1.5f, -1.5f, 0.0f, 1.0f,
             1.5f, -1.5f, 1.0f, 1.0f,
             1.5f,  1.5f, 1.0f, 0.0f,
            -1.5f,  1.5f, 0.0f, 0.0f,
            -1.5f, -1.5f, 0.0f, 1.0f };

    private final VertexArray vertexArray;
    
    public YellowBackground() {
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
