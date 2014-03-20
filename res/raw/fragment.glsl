precision mediump float;
uniform sampler2D uTexture;
varying vec2 vTexCord;
uniform int uhorsecolor;

void main() {
	/*
	vec3 lightVector = normalize(vPosition - vlight);
	vec3 viewVector = normalize(vPosition - viewpoint);
	float diffuse =3.0*dot(normalize(vNormal), lightVector);
	float specular = pow(dot(viewVector,-lightVector),4.0);*/
	vec2 flipped_texcoord = vec2(vTexCord.x, 1.0 - vTexCord.y);
	vec3 horseColor = texture2D(uTexture, flipped_texcoord).rgb;
	if(uhorsecolor == 1)
	{		
		if((horseColor.r > horseColor.g+horseColor.g/1.1) && (horseColor.r > horseColor.b+horseColor.b/1.1))
		{
			gl_FragColor = vec4(horseColor.g,horseColor.r,horseColor.b,1.0)+0.1;
		}
		else
			gl_FragColor = texture2D(uTexture, flipped_texcoord)+0.1;
	}
	else
		gl_FragColor = texture2D(uTexture, flipped_texcoord)+0.1;
}