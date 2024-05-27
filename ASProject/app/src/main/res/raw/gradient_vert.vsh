attribute vec4 position;
attribute vec4 color;

uniform mat4 projectionMatrix;
uniform mat4 transformMatrix;

varying vec4 vColor;

void main()
{	
	vColor = color;
    gl_Position = projectionMatrix * transformMatrix * position;
}