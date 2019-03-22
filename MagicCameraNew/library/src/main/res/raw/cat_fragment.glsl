 precision mediump float;

 varying mediump vec2 textureCoordinate;
 varying mediump vec2 textureCoordinate2;
 varying mediump vec2 textureCoordinate3;
 varying mediump vec2 textureCoordinate4;
 varying mediump vec2 textureCoordinate5;

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
    vec4 originColor3 = texture2D(inputImageTexture3, textureCoordinate3);
    vec4 originColor4 = texture2D(inputImageTexture4, textureCoordinate4);
    vec4 originColor5 = texture2D(inputImageTexture5, textureCoordinate5);
    vec4 combinedColor;
    if (originColor2.a != 0.0) {
        combinedColor = vec4(originColor2.rgb, 0.5);
    } else if (originColor3.a != 0.0) {
        combinedColor = vec4(originColor3.rgb, 0.5);
    } else if (originColor4.a != 0.0) {
        combinedColor = vec4(originColor4.rgb, 0.5);
    } else if (originColor5.a != 0.0) {
        combinedColor = vec4(originColor5.rgb, 0.5);
    } else {
        combinedColor = originColor1;
    }
    /*if (originColor2.a == 0.0) {
        combinedColor = originColor1;
    } else {
        //originColor2.a = 0.5;
        combinedColor = vec4(originColor2.rgb, 0.5);
    }*/
    gl_FragColor = combinedColor;
}
