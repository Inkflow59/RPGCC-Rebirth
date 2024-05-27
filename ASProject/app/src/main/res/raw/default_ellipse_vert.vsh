attribute vec4 position;
attribute vec2 texCoord;

varying vec2 texCoordinate;
varying vec2 pPos;

uniform mat4 projectionMatrix;
uniform mat4 transformMatrix;

void main()
{	
	texCoordinate = texCoord;
	pPos = position.xy;
	gl_Position = projectionMatrix * transformMatrix * position;
}