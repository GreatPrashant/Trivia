package com.prashant.app.objects;
import com.prashant.app.data.VertexArray;
import com.prashant.app.programs.TextureShaderProgram;
import com.prashant.app.util.Geometry;

import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glDrawArrays;
import static com.prashant.app.Constants.BYTES_PER_FLOAT;

public class ImageButton {

    public Geometry.Point position;

    private static float n1 = 0.3f, n2 = 0.8f;

    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int TEXTURE_COORDINATES_COMPONENT_COUNT = 2;
    private static final int STRIDE = (POSITION_COMPONENT_COUNT + TEXTURE_COORDINATES_COMPONENT_COUNT) * BYTES_PER_FLOAT;

    private static final float[] VERTEX_DATA = {
            // Order of coordinates: X, Y, S, T
            n1, -n1, 1.0f, 0.0f,
            n2, n2,  0.0f, 1.0f,
            n1, n2,  0.0f, 0.0f,
            n1, -n1, 1.0f, 0.0f,
            n2, n2,  0.0f, 1.0f,
            n2, -n1, 1.0f, 1.0f
    };

    private final VertexArray vertexArray;

    public ImageButton() {
        vertexArray = new VertexArray(VERTEX_DATA);
    }

    public void bindData(TextureShaderProgram textureProgram) {
        vertexArray.setVertexAttribPointer(0, textureProgram.getPositionAttributeLocation(), POSITION_COMPONENT_COUNT, STRIDE);
        vertexArray.setVertexAttribPointer(POSITION_COMPONENT_COUNT, textureProgram.getTextureCoordinatesAttributeLocation(), TEXTURE_COORDINATES_COMPONENT_COUNT, STRIDE);
    }

    public void draw() {
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 6);
    }

    public void setPoint(Geometry.Point position) {
        this.position = position;
    }

    public Geometry.Point getPosition() {
        return position;
    }
}
