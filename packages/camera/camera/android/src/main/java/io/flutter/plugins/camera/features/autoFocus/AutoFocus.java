package io.flutter.plugins.camera.features.autofocus;

import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.util.Log;
import io.flutter.plugins.camera.CameraProperties;
import io.flutter.plugins.camera.features.CameraFeature;

public class AutoFocus implements CameraFeature<FocusMode> {
  private boolean isSupported;
  private FocusMode currentSetting = FocusMode.auto;

  // When we switch recording modes we re-create this feature with
  // the appropriate setting here.
  private final boolean recordingVideo;

  public AutoFocus(CameraProperties cameraProperties, boolean recordingVideo) {
    this.recordingVideo = recordingVideo;
    this.isSupported = checkIsSupported(cameraProperties);
  }

  @Override
  public String getDebugName() {
    return "AutoFocus";
  }

  @Override
  public FocusMode getValue() {
    return currentSetting;
  }

  @Override
  public void setValue(FocusMode value) {
    this.currentSetting = value;
  }

  @Override
  public boolean checkIsSupported(CameraProperties cameraProperties) {
    int[] modes = cameraProperties.getControlAutoFocusAvailableModes();
    Log.i("Camera", "checkAutoFocusSupported | modes:");
    for (int mode : modes) {
      Log.i("Camera", "checkAutoFocusSupported | ==> " + mode);
    }

    // Check if fixed focal length lens. If LENS_INFO_MINIMUM_FOCUS_DISTANCE=0, then this is fixed.
    // Can be null on some devices.
    final Float minFocus = cameraProperties.getLensInfoMinimumFocusDistance();
    // final Float maxFocus = cameraCharacteristics.get(CameraCharacteristics.LENS_INFO_HYPERFOCAL_DISTANCE);

    // Value can be null on some devices:
    // https://developer.android.com/reference/android/hardware/camera2/CameraCharacteristics#LENS_INFO_MINIMUM_FOCUS_DISTANCE
    boolean isFixedLength;
    if (minFocus == null) {
      isFixedLength = true;
    } else {
      isFixedLength = minFocus == 0;
    }
    Log.i("Camera", "checkAutoFocusSupported | minFocus " + minFocus);

    final boolean supported =
        !isFixedLength
            && !(modes == null
                || modes.length == 0
                || (modes.length == 1 && modes[0] == CameraCharacteristics.CONTROL_AF_MODE_OFF));
    return supported;
  }

  @Override
  public void updateBuilder(CaptureRequest.Builder requestBuilder) {
    if (!isSupported) {
      return;
    }

    Log.i("Camera", "updateFocusMode | currentSetting: " + currentSetting);

    switch (currentSetting) {
      case locked:
        requestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
        break;

      case auto:
        requestBuilder.set(
            CaptureRequest.CONTROL_AF_MODE,
            recordingVideo
                ? CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO
                : CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
      default:
        break;
    }
  }

  public boolean getIsSupported() {
    return this.isSupported;
  }
}