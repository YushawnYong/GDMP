package org.godotengine.gdmp;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.util.Size;

import androidx.annotation.Nullable;

import com.google.mediapipe.components.CameraHelper.OnCameraStartedListener;
import com.google.mediapipe.components.CameraXPreviewHelper;
import com.google.mediapipe.components.ExternalTextureConverter;
import com.google.mediapipe.components.TextureFrameConsumer;
import com.google.mediapipe.framework.TextureFrame;
import com.google.mediapipe.glutil.EglManager;

public class GDMPCameraHelper extends CameraXPreviewHelper implements OnCameraStartedListener, TextureFrameConsumer {
    private final long nativeCallerPtr;
    private final Activity activity;
    private final EglManager eglManager;
    private ExternalTextureConverter converter;

    @Override
    public void onCameraStarted(@Nullable SurfaceTexture surfaceTexture) {
        Size frameSize = getFrameSize();
        converter = new ExternalTextureConverter(eglManager.getContext());
        converter.setConsumer(this);
        converter.setFlipY(true);
        converter.setRotation(activity.getWindowManager().getDefaultDisplay().getRotation());
        converter.setSurfaceTextureAndAttachToGLContext(
                surfaceTexture,
                isCameraRotated() ? frameSize.getHeight() : frameSize.getWidth(),
                isCameraRotated() ? frameSize.getWidth() : frameSize.getHeight()
        );
    }

    @Override
    public void onNewFrame(TextureFrame frame) {
        nativeOnNewFrame(nativeCallerPtr, frame, frame.getTextureName(), frame.getWidth(), frame.getHeight());
    }

    public GDMPCameraHelper(long nativeCallerPtr, Activity activity) {
        this.nativeCallerPtr = nativeCallerPtr;
        this.activity = activity;
        // Get graph GL context by getting current context.
        this.eglManager = new EglManager(EGL14.eglGetCurrentContext());
        setOnCameraStartedListener(this);
    }

    public void startCamera(int index, int width, int height) {
        CameraFacing cameraFacing = index == 0 ? CameraFacing.FRONT : CameraFacing.BACK;
        super.startCamera(activity, cameraFacing, null, new Size(width, height));
    }

    public void closeCamera() {
        super.closeCamera(activity);
        if (converter != null) {
            converter.close();
        }
    }

    public native void nativeOnNewFrame(long nativeCallerPtr, TextureFrame frame, int name, int width, int height);
}
