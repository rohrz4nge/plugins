package io.flutter.plugins.camera.features.resolution;

import android.hardware.camera2.CaptureRequest;
import android.media.CamcorderProfile;
import android.util.Log;
import android.util.Size;
import io.flutter.plugins.camera.CameraProperties;
import io.flutter.plugins.camera.features.CameraFeature;

public class Resolution implements CameraFeature<ResolutionPreset> {
  private final Size captureSize;
  private final Size previewSize;
  private final CamcorderProfile recordingProfile;
  //    private final boolean recordingVideo;
  private boolean isSupported;
  private ResolutionPreset currentSetting;

  public Resolution(
      CameraProperties cameraProperties, ResolutionPreset initialSetting, String cameraName) {
    setValue(initialSetting);

    // Resolution configuration
    recordingProfile =
        getBestAvailableCamcorderProfileForResolutionPreset(cameraName, initialSetting);
    captureSize = new Size(recordingProfile.videoFrameWidth, recordingProfile.videoFrameHeight);
    Log.i("Camera", "captureSize: " + captureSize);

    previewSize = computeBestPreviewSize(cameraName, initialSetting);
    this.isSupported = checkIsSupported(cameraProperties);
  }

  static CamcorderProfile getBestAvailableCamcorderProfileForResolutionPreset(
      String cameraName, ResolutionPreset preset) {
    int cameraId = Integer.parseInt(cameraName);
    switch (preset) {
        // All of these cases deliberately fall through to get the best available profile.
      case max:
        if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_HIGH)) {
          return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_HIGH);
        }
      case ultraHigh:
        if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_2160P)) {
          return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_2160P);
        }
      case veryHigh:
        if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_1080P)) {
          return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_1080P);
        }
      case high:
        if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_720P)) {
          return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_720P);
        }
      case medium:
        if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_480P)) {
          return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_480P);
        }
      case low:
        if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_QVGA)) {
          return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_QVGA);
        }
      default:
        if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_LOW)) {
          return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_LOW);
        } else {
          throw new IllegalArgumentException(
              "No capture session available for current capture session.");
        }
    }
  }

  static Size computeBestPreviewSize(String cameraName, ResolutionPreset preset) {
    if (preset.ordinal() > ResolutionPreset.high.ordinal()) {
      preset = ResolutionPreset.high;
    }

    CamcorderProfile profile =
        getBestAvailableCamcorderProfileForResolutionPreset(cameraName, preset);
    return new Size(profile.videoFrameWidth, profile.videoFrameHeight);
  }

  @Override
  public String getDebugName() {
    return "Resolution";
  }

  @Override
  public ResolutionPreset getValue() {
    return currentSetting;
  }

  @Override
  public void setValue(ResolutionPreset value) {
    this.currentSetting = value;
  }

  // Always supported
  @Override
  public boolean checkIsSupported(CameraProperties cameraProperties) {
    return true;
  }

  @Override
  public void updateBuilder(CaptureRequest.Builder requestBuilder) {
    if (!isSupported) {
      return;
    }
  }

  public CamcorderProfile getRecordingProfile() {
    return this.recordingProfile;
  }

  public Size getPreviewSize() {
    return this.previewSize;
  }

  public Size getCaptureSize() {
    return this.captureSize;
  }

  public boolean getIsSupported() {
    return this.isSupported;
  }
}