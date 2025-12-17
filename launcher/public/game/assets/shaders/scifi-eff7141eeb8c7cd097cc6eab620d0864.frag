#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif

varying LOWP vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;

// Controls the intensity of the vignette (0.0 = none, 1.0 = strong)
uniform float u_vignetteIntensity; 
// Controls the offset for RGB separation (e.g. 0.005)
uniform float u_chromaticAberrationIntensity;

void main() {
    vec2 texCoords = v_texCoords;
    vec2 center = vec2(0.5, 0.5);
    vec2 toCenter = center - texCoords;
    float dist = length(toCenter);

    // --- Chromatic Aberration ---
    // Separate RGB channels based on distance from center
    // Red and Blue channels are shifted in opposite directions along the vector to center
    vec2 aberrationOffset = toCenter * u_chromaticAberrationIntensity * dist * 2.0;

    float r = texture2D(u_texture, texCoords - aberrationOffset).r;
    float g = texture2D(u_texture, texCoords).g;
    float b = texture2D(u_texture, texCoords + aberrationOffset).b;

    // Combine channels
    vec4 texColor = vec4(r, g, b, 1.0);

    // --- Vignette ---
    // Smoothstep creates a soft edge.
    // 0.75 is the radius where darkness is fully applied (outer edge)
    // The second parameter controls where the fade starts.
    // Increasing Intensity reduces the inner radius, making more of the screen dark.
    float vignette = smoothstep(0.8, 0.8 - (0.5 + u_vignetteIntensity * 0.5), dist);

    // Apply vignette
    texColor.rgb *= vignette;

    // Final color with vertex color (alpha)
    gl_FragColor = texColor * v_color;
}
