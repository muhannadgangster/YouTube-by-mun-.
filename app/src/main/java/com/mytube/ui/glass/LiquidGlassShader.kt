package com.mytube.ui.glass

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * Android Graphics Shading Language (AGSL) Real-Time Optical Liquid Glass Shader.
 * 
 * Rebuilt according to exact physics specification:
 * - Rounded-rectangle signed distance field (SDF)
 * - Thin displacement band near the edge: bevel = min(radius * 0.9, height / 2, 24dp)
 * - Quadratic falloff: s = (1 - distanceFromEdge / bevel)^2
 * - Inward edge normal displacement scaled by Refraction (0-60)
 * - Undistorted, crystal-clear center
 * - Order: Displace -> Blur -> Saturate (1.5)
 * - Fill: Tint color alpha = 0.5 * (1 - Transparency / 100) + 0.03 + Darkness overlay = (Darkness / 100) * 0.6
 * - Edge & bevel: 1dp border (Edge * 0.55), inner top highlight (Edge * 0.7), inner bottom highlight (Edge * 0.25), soft inner glow
 * - Reflection: 115° diagonal sheen sweep, alpha = Reflection * 0.6, period = 7s / (Animation speed / 100)
 * - Quality modes: Low (blur only), Medium (blur + refraction + static sheen), High (fully animated)
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
object LiquidGlassShader {

    val source = """
        uniform shader background;
        uniform float2 resolution;
        uniform float cornerRadius;
        uniform float refraction;
        uniform float time;
        uniform float animSpeed;
        uniform float edgeHighlight;
        uniform float reflection;
        uniform float transparency;
        uniform float tintDarkness;
        uniform float4 tintColor;
        uniform float qualityMode;
        uniform float density;

        half4 main(float2 p) {
            float2 size = max(resolution, float2(1.0, 1.0));
            float halfW = size.x * 0.5;
            float halfH = size.y * 0.5;
            float2 center = float2(halfW, halfH);
            float dScale = max(1.0, density);

            // Clamped corner radius
            float r = clamp(cornerRadius, 0.0, min(halfW, halfH));

            // Rounded rectangle signed distance field
            float2 q = abs(p - center) - float2(halfW - r, halfH - r);
            float sdf = min(max(q.x, q.y), 0.0) + length(max(q, float2(0.0, 0.0))) - r;

            // Anti-aliased outer boundary mask
            float inside = 1.0 - smoothstep(-0.8, 0.5, sdf);
            half4 bgOrig = background.eval(p);
            if (inside <= 0.001) {
                return bgOrig;
            }

            // Inward distance from the glass border
            float distFromEdge = max(0.0, -sdf);

            // Inward normal calculation
            float2 d = abs(p - center) - float2(halfW - r, halfH - r);
            float2 outwardN;
            if (d.x > 0.0 || d.y > 0.0) {
                outwardN = normalize(max(d, float2(0.0001, 0.0001))) * sign(p - center);
            } else {
                outwardN = (d.x > d.y) ? float2(sign(p.x - center.x), 0.0) : float2(0.0, sign(p.y - center.y));
            }
            float2 inwardN = -outwardN;

            // Bevel band width: min(radius * 0.9, height / 2, 24dp)
            float maxBevelPx = 24.0 * dScale;
            float bevel = min(r * 0.9, min(halfH, maxBevelPx));
            bevel = max(bevel, 1.0);

            // 1. REFRACTION DISPLACEMENT:
            // Quadratic falloff: s = (1 - distanceFromEdge / bevel)^2
            // Applied only near edge; center stays completely undistorted!
            float2 shift = float2(0.0, 0.0);
            if (qualityMode >= 0.5 && distFromEdge < bevel) {
                float ratio = distFromEdge / bevel;
                float s = (1.0 - ratio) * (1.0 - ratio);
                // Pixel shift along inward edge normal, scaled by Refraction (0-60)
                float shiftMagnitude = refraction * s * (dScale * 0.45);
                shift = inwardN * shiftMagnitude;
            }

            // Displace first: Sample background at shifted coordinate
            float2 uvDisplaced = clamp(p + shift, float2(0.5, 0.5), size - float2(0.5, 0.5));
            half4 sampledBg = background.eval(uvDisplaced);
            half3 col = sampledBg.rgb;

            // 2. SATURATE (about 1.5): Enhances vibrancy through crystal glass
            float luma = dot(col, half3(0.299, 0.587, 0.114));
            col = mix(half3(luma), col, 1.5);

            // 3. FILL:
            // Tint color with alpha = 0.5 * (1 - Transparency / 100) + 0.03
            // Black overlay with alpha = (Darkness / 100) * 0.6
            float tintAlpha = clamp(0.5 * (1.0 - transparency / 100.0) + 0.03, 0.0, 1.0);
            float darkAlpha = clamp((tintDarkness / 100.0) * 0.6, 0.0, 1.0);
            col = mix(col, tintColor.rgb, tintAlpha);
            col = mix(col, half3(0.0, 0.0, 0.0), darkAlpha);

            // 4. EDGE AND BEVEL:
            float edgeFactor = clamp(edgeHighlight / 100.0, 0.0, 1.0);
            
            // 1dp border with alpha = Edge * 0.55
            float border1dp = 1.0 * dScale;
            float borderAlpha = smoothstep(border1dp, 0.0, distFromEdge) * (edgeFactor * 0.55);
            col = mix(col, half3(1.0, 1.0, 1.0), borderAlpha);

            // Inner top highlight: alpha = Edge * 0.70
            float topFactor = max(0.0, inwardN.y);
            float topHighlight = exp(-distFromEdge / (3.2 * dScale)) * (edgeFactor * 0.70) * topFactor;
            col += half3(topHighlight);

            // Inner bottom highlight: alpha = Edge * 0.25
            float bottomFactor = max(0.0, -inwardN.y);
            float bottomHighlight = exp(-distFromEdge / (3.2 * dScale)) * (edgeFactor * 0.25) * bottomFactor;
            col += half3(bottomHighlight);

            // Soft inner glow
            float innerGlow = exp(-distFromEdge / (8.0 * dScale)) * (edgeFactor * 0.15);
            col += half3(innerGlow);

            // 5. REFLECTION:
            // Diagonal (115 degrees) soft white sheen band moving across surface
            // alpha = Reflection * 0.6, one pass every 7s / (animSpeed / 100)
            if (qualityMode >= 0.5 && reflection > 1.0) {
                float angleRad = 115.0 * 3.14159265 / 180.0;
                float2 sheenDir = float2(sin(angleRad), -cos(angleRad));
                float proj = dot(p - center, sheenDir);
                float totalSpan = length(size) + 120.0 * dScale;
                
                float sheenPhase = 0.35; // Static for Medium quality
                if (qualityMode > 1.5) { // High quality animated
                    float speedFactor = max(0.2, animSpeed / 100.0);
                    float period = 7.0 / speedFactor;
                    sheenPhase = fract(time / period);
                }

                float sheenPos = (sheenPhase - 0.5) * totalSpan;
                float bandWidth = 26.0 * dScale;
                float distToSheen = abs(proj - sheenPos);
                float sheen = smoothstep(bandWidth, 0.0, distToSheen);
                float sheenAlpha = pow(sheen, 2.0) * ((reflection / 100.0) * 0.6);
                col += half3(sheenAlpha);
            }

            // Smooth outer anti-aliasing composite
            half4 result = half4(col, bgOrig.a);
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
