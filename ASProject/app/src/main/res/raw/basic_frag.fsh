#ifdef GL_ES
 precision mediump float;
#endif
varying vec2 texCoordinate;
uniform sampler2D imgTexture;

void main()
{
	lowp vec4 color = texture2D(imgTexture, texCoordinate);
	gl_FragColor = color;
}
