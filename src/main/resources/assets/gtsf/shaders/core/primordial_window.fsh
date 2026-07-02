#version 150

#define M_PI 3.1415926535897932384626433832795

#moj_import <fog.glsl>

const int symbolcount = 11;
const float lightmix = 0.2f;

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;

uniform mat2 cosmicuvs[symbolcount];

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

uniform float time;
uniform float yaw;
uniform float pitch;
uniform float externalScale;
uniform float opacity;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in vec4 normal;
in vec3 fPos;

out vec4 fragColor;

mat4 rotationMatrix(vec3 axis, float angle) {
    axis = normalize(axis);
    float s = sin(angle);
    float c = cos(angle);
    float oc = 1.0 - c;

    return mat4(oc * axis.x * axis.x + c,           oc * axis.x * axis.y - axis.z * s,  oc * axis.z * axis.x + axis.y * s,  0.0,
                oc * axis.x * axis.y + axis.z * s,  oc * axis.y * axis.y + c,           oc * axis.y * axis.z - axis.x * s,  0.0,
                oc * axis.z * axis.x - axis.y * s,  oc * axis.y * axis.z + axis.x * s,  oc * axis.z * axis.z + c,           0.0,
                0.0,                                0.0,                                0.0,                                1.0);
}

float hash21(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453123);
}

vec2 hash22(vec2 p) {
    float x = hash21(p + vec2(17.0, 3.1));
    float y = hash21(p + vec2(11.7, 29.4));
    return vec2(x, y);
}

mat2 rot2(float angle) {
    float s = sin(angle);
    float c = cos(angle);
    return mat2(c, -s, s, c);
}

float starGlint(vec2 p, float flare) {
    float d = length(p);
    float core = 0.028 / max(d, 0.001);
    float cross = max(0.0, 1.0 - abs(p.x * p.y * 900.0)) * flare;
    vec2 rp = vec2(p.x + p.y, p.x - p.y);
    float diag = max(0.0, 1.0 - abs(rp.x * rp.y * 450.0)) * flare * 0.45;
    float falloff = smoothstep(0.36, 0.0, d);
    return (core + cross + diag) * falloff;
}

vec3 starLayer(vec2 uv, float density, float threshold, vec3 tint, float twinkleSpeed, float t) {
    vec2 gv = fract(uv * density) - 0.5;
    vec2 id = floor(uv * density);
    vec3 stars = vec3(0.0);

    for (int y = -1; y <= 1; y++) {
        for (int x = -1; x <= 1; x++) {
            vec2 offset = vec2(float(x), float(y));
            vec2 cell = id + offset;
            float seed = hash21(cell);
            if (seed < threshold) {
                continue;
            }

            vec2 pos = hash22(cell) - 0.5;
            vec2 delta = gv - offset - pos;
            float size = mix(0.045, 0.14, hash21(cell + 4.7));
            float flare = mix(0.18, 0.7, hash21(cell + 9.3));
            float twinkle = 0.72 + 0.28 * sin(t * twinkleSpeed + seed * 18.0);
            float star = starGlint(delta / size, flare) * twinkle;
            stars += tint * star * smoothstep(threshold, 1.0, seed);
        }
    }

    return stars;
}

vec3 sampleBackdrop(vec2 uv, vec2 drift, vec2 texel, float gamma) {
    vec2 sampleUv = fract(uv + drift);
    vec3 center = texture(Sampler1, sampleUv).rgb;
    vec3 blurA = texture(Sampler1, fract(sampleUv + vec2(texel.x, 0.0))).rgb;
    vec3 blurB = texture(Sampler1, fract(sampleUv - vec2(texel.x, 0.0))).rgb;
    vec3 blurC = texture(Sampler1, fract(sampleUv + vec2(0.0, texel.y))).rgb;
    vec3 blurD = texture(Sampler1, fract(sampleUv - vec2(0.0, texel.y))).rgb;
    return pow((center * 0.42 + blurA * 0.145 + blurB * 0.145 + blurC * 0.145 + blurD * 0.145), vec3(gamma));
}

vec3 saturateColor(vec3 color, float saturation) {
    float luminance = dot(color, vec3(0.2126, 0.7152, 0.0722));
    return mix(vec3(luminance), color, saturation);
}

vec4 sampleSymbolSprite(int symbol, vec2 uv) {
    vec2 clampedUv = clamp(uv, 0.0, 1.0);
    float umin = cosmicuvs[symbol][0][0];
    float umax = cosmicuvs[symbol][1][0];
    float vmin = cosmicuvs[symbol][0][1];
    float vmax = cosmicuvs[symbol][1][1];
    vec2 cosmictex = vec2(
        umin * (1.0 - clampedUv.x) + umax * clampedUv.x,
        vmin * (1.0 - clampedUv.y) + vmax * clampedUv.y
    );
    return texture(Sampler0, cosmictex);
}

float getSymbolBias(int symbol) {
    if (symbol == 0 || symbol == 4) {
        return 0.10;
    }
    if (symbol == 2) {
        return 0.18;
    }
    if (symbol == 1 || symbol == 3 || symbol == 5 || symbol == 8) {
        return 1.32;
    }
    if (symbol == 6) {
        return 1.18;
    }
    if (symbol == 7) {
        return 0.42;
    }
    if (symbol == 10) {
        return 1.10;
    }
    return 1.58;
}

vec3 getSymbolTint(int symbol, float coolMix) {
    if (symbol == 1 || symbol == 5 || symbol == 8) {
        return mix(vec3(0.42, 0.82, 1.0), vec3(0.96, 0.98, 1.0), coolMix);
    }
    if (symbol == 3 || symbol == 6 || symbol == 9) {
        return mix(vec3(0.26, 0.62, 1.0), vec3(0.88, 0.96, 1.0), coolMix);
    }
    if (symbol == 7) {
        return mix(vec3(0.44, 1.0, 0.84), vec3(0.90, 0.98, 1.0), coolMix);
    }
    if (symbol == 10) {
        return mix(vec3(0.10, 0.04, 0.30), vec3(0.55, 0.28, 0.85), coolMix);
    }
    return mix(vec3(0.54, 0.70, 1.0), vec3(0.96, 0.98, 1.0), coolMix);
}

vec3 symbolLayer(vec2 uv, float density, float sizeMin, float sizeMax, float t, float offsetSeed) {
    vec2 grid = uv * density;
    vec2 id = floor(grid);
    vec2 gv = fract(grid) - 0.5;
    vec3 symbols = vec3(0.0);

    for (int y = -1; y <= 1; y++) {
        for (int x = -1; x <= 1; x++) {
            vec2 offset = vec2(float(x), float(y));
            vec2 cell = id + offset + offsetSeed;
            float seed = hash21(cell);
            if (seed < 0.04) {
                continue;
            }

            int symbol = int(floor(hash21(cell + 4.7) * float(symbolcount)));
            float symbolBias = getSymbolBias(symbol);
            if (symbolBias <= 0.12) {
                continue;
            }

            if (symbol == 10 && hash21(cell + 31.5) > 0.20) {
                continue;
            }

            vec2 jitter = (hash22(cell + 8.3) - 0.5) * 0.32;
            float size = (symbol == 10)
                ? mix(0.26, 0.44, hash21(cell + 2.3))
                : mix(sizeMin, sizeMax, hash21(cell + 2.3));
            float angle = hash21(cell + 12.7) * (M_PI * 2.0);
            vec2 local = (gv - offset - jitter) / size;
            local = rot2(angle) * local;

            if (max(abs(local.x), abs(local.y)) > 0.5) {
                continue;
            }

            vec2 spriteUv = local + 0.5;
            vec4 sprite = sampleSymbolSprite(symbol, spriteUv);
            float spriteStrength = smoothstep(0.06, 0.60, sprite.a);
            if (spriteStrength <= 0.001) {
                continue;
            }

            float twinkle = 0.82 + 0.18 * sin(t * (0.22 + hash21(cell + 16.0) * 0.45) + seed * 19.0);
            vec3 tint = getSymbolTint(symbol, fract(seed * 7.13));
            symbols += mix(tint, vec3(1.0), spriteStrength * 0.26) * pow(spriteStrength, 1.08) * symbolBias * twinkle;
        }
    }

    return symbols;
}

void main() {
    vec4 mask = texture(Sampler0, texCoord0.xy);
    float maskStrength = mask.a * mix(0.52, 1.0, smoothstep(0.24, 0.92, mask.r));
    float oneOverExternalScale = 1.0 / externalScale;

    vec4 col = vec4(0.02, 0.03, 0.08, 1.0);

    bool isGui = externalScale > 50.0;
    vec2 backgroundUv;
    vec3 dir3 = vec3(0.0, 0.0, 1.0);

    if (isGui) {
        backgroundUv = texCoord0.xy;
    } else {
        vec4 dir = normalize(vec4(-fPos, 0.0));

        float sb = sin(pitch);
        float cb = cos(pitch);
        dir = normalize(vec4(dir.x, dir.y * cb - dir.z * sb, dir.y * sb + dir.z * cb, 0.0));

        float sa = sin(-yaw);
        float ca = cos(-yaw);
        dir = normalize(vec4(dir.z * sa + dir.x * ca, dir.y, dir.z * ca - dir.x * sa, 0.0));

        dir3 = normalize(dir.xyz);
        backgroundUv = vec2(
            atan(dir3.z, dir3.x) / (2.0 * M_PI) + 0.5,
            asin(clamp(dir3.y, -1.0, 1.0)) / M_PI + 0.5
        );
    }
    vec2 backgroundUvFar = backgroundUv * vec2(2.35, 2.05);
    vec2 backgroundUvNear = backgroundUv * vec2(3.10, 2.85);
    vec2 texel = vec2(1.0 / 889.0, 1.0 / 500.0);

    vec3 bgFar = sampleBackdrop(backgroundUvFar, vec2(0.18, 0.14), texel * 2.2, 0.84);
    vec3 bgNear = sampleBackdrop(backgroundUvNear,
        vec2(0.31, 0.27) + vec2(time * 0.000010 * oneOverExternalScale, 0.0), texel * 1.4, 0.88);
    vec3 bgMix = mix(bgFar * 1.06, bgNear * 0.82, 0.48);
    bgMix = max(bgMix - vec3(0.012, 0.008, 0.0), 0.0);
    bgMix = saturateColor(pow(bgMix, vec3(0.78)), 1.70);

    float magentaMask = smoothstep(0.24, 0.86, bgMix.r + bgMix.b * 0.55);
    float tealMask = smoothstep(0.20, 0.90, bgMix.g + (1.0 - abs(dir3.y)) * 0.26);
    vec3 cosmicColor = mix(vec3(0.05, 0.22, 0.88), vec3(0.22, 0.56, 0.98), magentaMask);
    cosmicColor = mix(cosmicColor, vec3(0.04, 0.92, 0.74), tealMask * 0.44);

    col.rgb = vec3(0.016, 0.020, 0.060) + bgMix * cosmicColor * 1.92;

    float veil = pow(1.0 - smoothstep(0.08, 0.86, abs(dir3.y)), 1.8);
    col.rgb += vec3(0.022, 0.090, 0.24) * veil * 0.30;

    float t = time * 0.02;
    vec2 symbolUv = backgroundUv + vec2(time * 0.000012 * oneOverExternalScale, 0.0);
    col.rgb += symbolLayer(symbolUv, 36.0, 0.18, 0.31, t, 0.0) * 0.64;
    col.rgb += symbolLayer(symbolUv + vec2(0.19, 0.11), 56.0, 0.12, 0.23, t * 1.14, 9.7) * 0.42;
    col.rgb += symbolLayer(symbolUv + vec2(0.41, 0.27), 76.0, 0.08, 0.17, t * 0.88, 23.1) * 0.28;

    vec3 shade = vertexColor.rgb * 0.04 + vec3(0.96);
    col.rgb *= shade;
    col.rgb += starLayer(backgroundUv * vec2(14.0, 9.0), 1.0, 0.982, vec3(0.44, 0.60, 0.98), 0.9, t) * 0.18;
    col.rgb += starLayer(backgroundUv * vec2(22.0, 15.0), 1.0, 0.989, vec3(0.62, 0.82, 1.0), 1.3, t) * 0.24;
    col.rgb += starLayer(backgroundUv * vec2(34.0, 24.0), 1.0, 0.995, vec3(0.95, 0.98, 1.0), 1.8, t) * 0.28;
    col.rgb = saturateColor(col.rgb, 1.18);
    col.rgb = max((col.rgb - 0.08) * 1.10 + 0.08, 0.0);
    col.rgb *= 1.02;

    float brightness = clamp(max(max(col.r, col.g), col.b), 0.0, 1.0);
    col.a *= maskStrength * opacity * (1.04 + brightness * 0.18);
    col = clamp(col, 0.0, 1.0);

    fragColor = linear_fog(col * ColorModulator, vertexDistance, FogStart, FogEnd, FogColor);
}
