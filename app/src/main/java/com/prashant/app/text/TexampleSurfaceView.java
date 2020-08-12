// This is based on the OpenGL ES 1.0 sample application from the Android Developer website:
// http://developer.android.com/resources/tutorials/opengl/opengl-es10.html

package com.prashant.app.text;

import android.content.Context;
import android.opengl.GLSurfaceView;

class TexampleSurfaceView extends GLSurfaceView {

   public TexampleSurfaceView(Context context){
      super( context );
      
      // Set to use OpenGL ES 2.0
      setEGLContextClientVersion(2); 

      // Set the Renderer for drawing on the GLSurfaceView
      setRenderer( new Texample2Renderer( context ) );
   }
}
