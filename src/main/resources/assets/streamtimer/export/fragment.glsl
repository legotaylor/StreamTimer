#version 120

uniform sampler2D uTexture;
varying vec2 vTexCoord;
uniform float uTime;
uniform float uRainbow;
uniform float uBackground;
uniform vec4 uBackgroundColor;
uniform float uActive;
uniform float uFinished;
uniform float uShouldDim;

vec3 hsv2rgb(vec3 c) {
    vec3 rgb = clamp(abs(mod(c.x * 6.0 + vec3(0.0,4.0,2.0), 6.0) - 3.0) - 1.0, 0.0, 1.0);
    rgb = rgb * rgb * (3.0 - 2.0 * rgb);
    return c.z * mix(vec3(1.0), rgb, c.y);
}

vec3 rainbow(vec3 c) {
    return uRainbow != 1.0F ? hsv2rgb(c) : vec3(1.0F);
}

void main() {
    vec4 colorIn = texture2D(uTexture, vTexCoord);
    if (uFinished == 1.0) colorIn.rgb *= 0.25;
    else if (uActive == 0.0 && uShouldDim == 1.0) colorIn.rgb *= 0.5;
    vec3 textColor = rainbow(vec3(mod(uTime * 0.2 + vTexCoord.x + vTexCoord.y, 1.0), 1.0, 1.0)) * colorIn.rgb;
    float textAlpha = colorIn.a;
    vec3 bgColor = uBackgroundColor.rgb;
    float bgAlpha = uBackgroundColor.a;
    vec3 finalColor;
    float finalAlpha;
    if (uBackground == 1.0) {
        finalColor = mix(bgColor, textColor, textAlpha);
        finalAlpha = textAlpha + bgAlpha * (1.0 - textAlpha);
    } else {
        finalColor = textColor;
        finalAlpha = textAlpha;
    }

    gl_FragColor = vec4(finalColor, finalAlpha);
}
