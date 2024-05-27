#ifdef GL_ES
precision mediump float;
#endif
uniform sampler2D imgTexture;

uniform lowp int inkEffect;
uniform lowp vec4 blendColor;

varying vec2 texCoordinate;

void main()
{
	lowp vec4 color = texture2D(imgTexture, texCoordinate) * blendColor;

	if(inkEffect == 2)			//INVERT
		color.rgb = vec3(1,1,1)-color.rgb;
	else if(inkEffect == 10)	//MONO
	{
		lowp float mono = 0.3125*color.r + 0.5625*color.g + 0.125*color.b;
		color.rgb = vec3(mono,mono,mono);
	}
	
	gl_FragColor = color;
}
