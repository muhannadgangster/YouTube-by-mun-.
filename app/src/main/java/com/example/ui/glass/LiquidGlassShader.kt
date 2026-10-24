package com.example.ui.glass

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import com.mytube.ui.glass.LiquidGlassShader as MytubeLiquidGlassShader

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
object LiquidGlassShader {
    val source: String get() = MytubeLiquidGlassShader.source

    fun create(): RuntimeShader {
        return MytubeLiquidGlassShader.create()
    }
}
