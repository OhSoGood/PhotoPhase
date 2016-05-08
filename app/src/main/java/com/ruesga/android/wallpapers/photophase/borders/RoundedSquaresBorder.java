/*
 * Copyright (C) 2016 Jorge Ruesga
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ruesga.android.wallpapers.photophase.borders;

import android.media.effect.EffectContext;
import android.opengl.GLES20;
import android.util.Log;

import com.ruesga.android.wallpapers.photophase.utils.GLESUtil;

/**
 * This rounded squares without border.<br/>
 * <table>
 * <tr><td>Parameter name</td><td>Meaning</td><td>Valid values</td></tr>
 * </table>
 */
public class RoundedSquaresBorder extends Border {

    private static final String TAG = "SimpleBorder";

    public static final String STRENGTH_PARAMETER = "strength";
    public static final String COLOR_PARAMETER = "color";

    private static final String FRAGMENT_SHADER =
            "precision mediump float;\n" +
            "uniform sampler2D tex_sampler;\n" +
            "uniform float w;\n" +
            "uniform float h;\n" +
            "uniform vec4 color;\n" +
            "uniform float strength;\n" +
            "varying vec2 v_texcoord;\n" +
            "bool is_rounded_border(vec2 p, vec2 c, float r) {\n" +
            "  float dx = (c.x - p.x);\n" +
            "  float dy = (c.y - p.y);\n" +
            "  dx *= dx;\n" +
            "  dy *= dy;\n" +
            "  return (dx + dy) > (r * r) &&\n" +
            "      (\n" +
            "        (c.x < 0.5 && c.y > 0.5 && c.x > p.x && c.y < p.y) ||\n" +
            "        (c.x > 0.5 && c.y > 0.5 && c.x < p.x && c.y < p.y) ||\n" +
            "        (c.x < 0.5 && c.y < 0.5 && c.x > p.x && c.y > p.y) ||\n" +
            "        (c.x > 0.5 && c.y < 0.5 && c.x < p.x && c.y > p.y)\n" +
            "      );\n" +
            "}\n" +
            "void main(void)\n" +
            "{\n" +
            "  float r = min(strength / w, strength / h);\n" +
            "  vec2 clt = vec2(0.0 + r, 1.0 - r);\n" +
            "  vec2 crt = vec2(1.0 - r, 1.0 - r);\n" +
            "  vec2 clb = vec2(0.0 + r, 0.0 + r);\n" +
            "  vec2 crb = vec2(1.0 - r, 0.0 + r);\n" +
            "  if (is_rounded_border(v_texcoord, clt, r)\n" +
            "    || is_rounded_border(v_texcoord, crt, r)\n" +
            "    || is_rounded_border(v_texcoord, clb, r)\n" +
            "    || is_rounded_border(v_texcoord, crb, r)) {\n" +
            "    vec4 tex = texture2D (tex_sampler, v_texcoord);\n" +
            "    float r = tex.r + (color.r - tex.r) * color.a;\n" +
            "    float g = tex.g + (color.g - tex.g) * color.a;\n" +
            "    float b = tex.b + (color.b - tex.b) * color.a;\n" +
            "    gl_FragColor = vec4(r, g, b, tex.a);\n" +
            "  } else {\n" +
            "    gl_FragColor = texture2D(tex_sampler, v_texcoord);\n" +
            "  }\n" +
            "}";

    private float mStrength = 50;

    private int mColorHandle;
    private int mWidthHandle;
    private int mHeightHandle;
    private int mStrengthHandle;

    /**
     * Constructor of <code>RoundedSquaresBorder</code>.
     *
     * @param ctx The effect context
     * @param name The effect name
     */
    public RoundedSquaresBorder(EffectContext ctx, String name) {
        super(ctx, RoundedSquaresBorder.class.getName());
        init(VERTEX_SHADER, FRAGMENT_SHADER);

        // Parameters
        mWidthHandle = GLES20.glGetUniformLocation(mProgram[0], "w");
        GLESUtil.glesCheckError("glGetUniformLocation");
        mHeightHandle = GLES20.glGetUniformLocation(mProgram[0], "h");
        GLESUtil.glesCheckError("glGetUniformLocation");
        mStrengthHandle = GLES20.glGetUniformLocation(mProgram[0], STRENGTH_PARAMETER);
        GLESUtil.glesCheckError("glGetUniformLocation");
        mColorHandle = GLES20.glGetUniformLocation(mProgram[0], COLOR_PARAMETER);
        GLESUtil.glesCheckError("glGetUniformLocation");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void applyParameters(int width, int height) {
        // Set parameters
        GLES20.glUniform1f(mWidthHandle, (float) width);
        GLESUtil.glesCheckError("glUniform1f");
        GLES20.glUniform1f(mHeightHandle, (float) height);
        GLESUtil.glesCheckError("glUniform1f");
        GLES20.glUniform1f(mStrengthHandle, mStrength);
        GLESUtil.glesCheckError("glUniform1f");
        GLES20.glUniform4fv(mColorHandle, 1, mColor.asVec4(), 0);
        GLESUtil.glesCheckError("glUniform4fv");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setParameter(String parameterKey, Object value) {
        if (parameterKey.compareTo(STRENGTH_PARAMETER) == 0) {
            try {
                float strength = Float.parseFloat(value.toString());
                if (strength < 0) {
                    Log.w(TAG, "strength parameter must be > 0");
                    return;
                }
                mStrength = strength;
            } catch (NumberFormatException ex) {
                // Ignore
            }
        }
    }
}
