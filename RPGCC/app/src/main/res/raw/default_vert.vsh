attribute vec4 position;
attribute vec2 texCoord;

uniform mat4 projectionMatrix;
uniform mat4 transformMatrix;

varying vec2 texCoordinate;

void main()
{	
	texCoordinate = texCoord;
    gl_Position = projectionMatrix * transformMatrix * position;
}