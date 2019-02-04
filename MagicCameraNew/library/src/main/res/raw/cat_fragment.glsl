 precision mediump float;

 varying mediump vec2 textureCoordinate;
 varying mediump vec2 textureCoordinate2;

 uniform sampler2D inputImageTexture;
 uniform sampler2D inputImageTexture2;  //edgeBurn
 uniform sampler2D inputImageTexture3;  //hefeMap
 uniform sampler2D inputImageTexture4;  //hefeGradientMap
 uniform sampler2D inputImageTexture5;  //hefeSoftLight
 uniform sampler2D inputImageTexture6;  //hefeMetal

 uniform float strength;

 void main()
{
    /*vec4 originColor = texture2D(inputImageTexture, textureCoordinate);
    vec3 texel = texture2D(inputImageTexture, textureCoordinate).rgb;
    vec3 edge = texture2D(inputImageTexture2, textureCoordinate).rgb;
    texel = texel * edge;
    gl_FragColor = vec4(texel, 1.0);*/
    vec4 originColor1 = texture2D(inputImageTexture, textureCoordinate);
    vec4 originColor2 = texture2D(inputImageTexture2, textureCoordinate2);
    //gl_FragColor = originColor1 + originColor2;
    gl_FragColor = originColor2;
}
