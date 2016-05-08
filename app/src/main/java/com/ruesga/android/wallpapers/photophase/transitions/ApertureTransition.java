/*
 * Copyright (C) 2015 Jorge Ruesga
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

package com.ruesga.android.wallpapers.photophase.transitions;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLException;
import android.os.SystemClock;

import com.ruesga.android.wallpapers.photophase.PhotoFrame;
import com.ruesga.android.wallpapers.photophase.R;
import com.ruesga.android.wallpapers.photophase.textures.TextureManager;
import com.ruesga.android.wallpapers.photophase.transitions.Transitions.TRANSITIONS;
import com.ruesga.android.wallpapers.photophase.utils.GLESUtil;

import java.nio.FloatBuffer;

/**
 * A transition that applies a aperture transition to the picture.
 */
public class ApertureTransition extends Transition {

    private static final float TRANSITION_TIME = 1800.0f;

    private static final int[] VERTEX_SHADER = {R.raw.default_vertex_shader};
    private static final int[] FRAGMENT_SHADER = {R.raw.aperture_fragment_shader};

    private boolean mRunning;
    private long mTime;

    protected int mTargetTextureHandler;
    private int mRadiusHandler;

    /**
     * Constructor of <code>ApertureTransition</code>
     *
     * @param ctx The current context
     * @param tm The texture manager
     */
    public ApertureTransition(Context ctx, TextureManager tm) {
        super(ctx, tm, VERTEX_SHADER, FRAGMENT_SHADER);

        mTargetTextureHandler = GLES20.glGetUniformLocation(mProgramHandlers[0], "sTexture2");
        GLESUtil.glesCheckError("glGetUniformLocation");
        mRadiusHandler = GLES20.glGetUniformLocation(mProgramHandlers[0], "radius");
        GLESUtil.glesCheckError("glGetUniformLocation");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TRANSITIONS getType() {
        return TRANSITIONS.APERTURE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasTransitionTarget() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRunning() {
        return mRunning;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSelectable(PhotoFrame frame) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        mTime = -1;
        mRunning = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void select(PhotoFrame target) {
        super.select(target);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void apply(float[] matrix) throws GLException {
        // Check internal vars
        if (mTarget == null ||
            mTarget.getPositionBuffer() == null ||
            mTarget.getTextureBuffer() == null) {
            return;
        }
        if (mTransitionTarget == null ||
            mTransitionTarget.getPositionBuffer() == null ||
            mTransitionTarget.getTextureBuffer() == null) {
            return;
        }

        // Set the time the first time
        if (mTime == -1) {
            mTime = SystemClock.uptimeMillis();
        }

        final float delta = Math.min(SystemClock.uptimeMillis() - mTime, TRANSITION_TIME) / TRANSITION_TIME;
        draw(matrix, delta);

        // Transition ended
        if (delta == 1) {
            mRunning = false;
        }
    }

    /**
     * Method that draws the picture texture
     *
     * @param matrix The model-view-projection matrix
     */
    protected void draw(float[] matrix, float radius) {
        // Bind default FBO
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLESUtil.glesCheckError("glBindFramebuffer");

        // Use our shader program
        useProgram(0);

        // Disable blending
        GLES20.glDisable(GLES20.GL_BLEND);
        GLESUtil.glesCheckError("glDisable");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandlers[0], 1, false, matrix, 0);
        GLESUtil.glesCheckError("glUniformMatrix4fv");

        // Delta
        GLES20.glUniform1f(mRadiusHandler, radius);
        GLESUtil.glesCheckError("glUniform1f");

        // Textures
        FloatBuffer textureBuffer = mTarget.getTextureBuffer();
        textureBuffer.position(0);
        GLES20.glVertexAttribPointer(mTextureCoordHandlers[0], 2, GLES20.GL_FLOAT, false, 0, textureBuffer);
        GLESUtil.glesCheckError("glVertexAttribPointer");
        GLES20.glEnableVertexAttribArray(mTextureCoordHandlers[0]);
        GLESUtil.glesCheckError("glEnableVertexAttribArray");

        // Position
        FloatBuffer positionBuffer = mTarget.getPositionBuffer();
        positionBuffer.position(0);
        GLES20.glVertexAttribPointer(mPositionHandlers[0], 2, GLES20.GL_FLOAT, false, 0, positionBuffer);
        GLESUtil.glesCheckError("glVertexAttribPointer");
        GLES20.glEnableVertexAttribArray(mPositionHandlers[0]);
        GLESUtil.glesCheckError("glEnableVertexAttribArray");

        // Set the input textures
        // Texture 1
        int texture = mTarget.getTextureHandle();
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLESUtil.glesCheckError("glActiveTexture");
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
        GLESUtil.glesCheckError("glBindTexture");
        GLES20.glUniform1i(mTextureHandlers[0], 0);
        GLESUtil.glesCheckError("glUniform1i");
//        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
//        GLESUtil.glesCheckError("glDrawElements");

        // Texture 2
        int targetTexture = mTransitionTarget.getTextureHandle();
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLESUtil.glesCheckError("glActiveTexture");
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, targetTexture);
        GLESUtil.glesCheckError("glBindTexture");
        GLES20.glUniform1i(mTargetTextureHandler, 1);
        GLESUtil.glesCheckError("glUniform1i");

        // Draw
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLESUtil.glesCheckError("glDrawElements");

        // Disable attributes
        GLES20.glDisableVertexAttribArray(mPositionHandlers[0]);
        GLESUtil.glesCheckError("glDisableVertexAttribArray");
        GLES20.glDisableVertexAttribArray(mTextureCoordHandlers[0]);
        GLESUtil.glesCheckError("glDisableVertexAttribArray");
    }

    @Override
    public void recycle() {
        super.recycle();
        mTargetTextureHandler = -1;
        mRadiusHandler = -1;
    }
}
