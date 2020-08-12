uniform mat4 u_Matrix;

attribute vec4 a_Position;  
attribute vec4 a_Color;

void main()                    
{
    //v_Color = a_Color;
    gl_Position = u_Matrix * a_Position;
}          