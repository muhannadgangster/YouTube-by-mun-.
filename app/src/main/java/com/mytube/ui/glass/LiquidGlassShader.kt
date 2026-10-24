package com.mytube.ui.glass

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * Android Graphics Shading Language (AGSL) Real-Time Optical Liquid Glass Shader.
 * 
 * Physically grounded optical liquid glass:
 * - Truly fluid and transparent (lets live background, videos, and UI shine through crisp and clear)
 * - True optical lens displacement and curvature ("ہلکی سی ٹلٹی" - smooth tilted refraction normal)
 * - Prismatic chromatic dispersion (RGB channel separation on curved edges)
 * - Elegant Fresnel specular rim highlight and meniscus curvature
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
object LiquidGlassShader {

    val source = """
        uniform shader background;
        uniform float2 resolution;
        uniform float cornerRadius;
        uniform float strength;
        uniform float time;
        uniform float tiltStrength;
        uniform float lightAngle;

        half4 main(float2 p) {
            float2 size = max(resolution, float2(1.0, 1.0));
            float2 center = size * 0.5;
            float2 halfSize = size * 0.5;
            float r = clamp(
                cornerRadius,
                0.0,
                min(halfSize.x, halfSize.y)
            );

            // SDF calculation for smooth rounded rectangle
            float2 q = abs(p - center);
            float2 d = q - halfSize + float2(r, r);
            float sdf =
                length(max(d, float2(0.0, 0.0))) +
                min(max(d.x, d.y), 0.0) - r;

            // Anti-aliased boundary mask
            float inside = 1.0 - smoothstep(-0.8, 0.8, sdf);
            half4 bgOrig = background.eval(p);
            if (inside <= 0.001) {
                return bgOrig;
            }

            // Normalized coordinates across the liquid dome (-1.0 to 1.0)
            float2 normCoord = (p - center) / max(halfSize, float2(1.0, 1.0));
            float distSq = clamp(dot(normCoord, normCoord), 0.0, 1.0);
            
            // Continuous lens dome curvature: z = sqrt(1.0 - distSq)
            float z = sqrt(1.0 - distSq * 0.72);
            
            // Fluid surface normal vector
            float2 normal = normCoord * (1.15 - z * 0.35);
            
            // Border proximity: 0.0 in interior, rising sharply to 1.0 at outer curved edges
            float borderDist = clamp(-sdf, 0.0, 36.0);
            float borderProximity = 1.0 - smoothstep(1.0, 22.0, borderDist);
            
            // Optical tilt & refraction displacement:
            // Pronounced tilt near borders, gentle smooth tilt in center
            float effTilt = clamp(tiltStrength, 0.0, 3.0);
            float borderTilt = pow(borderProximity, 1.20) * 2.4;
            float centerTilt = distSq * 0.32;
            float tiltAmount = (centerTilt + borderTilt) * effTilt * 18.0;
            float2 offset = normal * tiltAmount;

            // Chromatic dispersion (prismatic color separation on curved glass edges)
            float disp = 0.022 * effTilt * (borderProximity * 1.35 + distSq * 0.28);
            float2 uvR = clamp(p - offset * (1.0 + disp), float2(0.0, 0.0), size - float2(1.0, 1.0));
            float2 uvG = clamp(p - offset, float2(0.0, 0.0), size - float2(1.0, 1.0));
            float2 uvB = clamp(p - offset * (1.0 - disp), float2(0.0, 0.0), size - float2(1.0, 1.0));

            half red = background.eval(uvR).r;
            half green = background.eval(uvG).g;
            half blue = background.eval(uvB).b;
            half3 glassCol = half3(red, green, blue);

            // Crystal-clear fluid transparency without dark fills
            glassCol *= half3(1.01, 1.02, 1.03);

            // Specular Fresnel rim light: Directional highlight along perimeter
            float2 lightDir = float2(cos(lightAngle), sin(lightAngle));
            float specular = max(0.0, dot(normal, -lightDir));
            float rimFactor = exp(-abs(sdf) / 12.0);
            float rimLight = pow(specular, 4.0) * rimFactor * 0.32 * (effTilt * 0.5 + 0.5);

            // Inner ambient liquid sheen along border contour
            float innerSheen = exp(-abs(sdf + 2.5) / 3.5) * 0.08 * effTilt;

            glassCol += half3(rimLight + innerSheen, rimLight + innerSheen * 1.06, rimLight + innerSheen * 1.14);

            // Smoothly composite with background preserving complete transparency
            half4 result = half4(glassCol, bgOrig.a);
            return mix(bgOrig, result, inside);
        }
    """.trimIndent()

    @Volatile
    private var cachedShader: RuntimeShader? = null

    fun obtain(): RuntimeShader {
        check(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        var s = cachedShader
        if (s == null) {
            synchronized(this) {
                s = cachedShader
                if (s == null) {
                    s = RuntimeShader(source)
                    cachedShader = s
                }
            }
        }
        return s!!
    }

    fun create(): RuntimeShader {
        return obtain()
    }
}
