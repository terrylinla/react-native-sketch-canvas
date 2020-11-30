#pragma once
#include "pch.h"
#include "winrt/Microsoft.ReactNative.h"
#include "NativeModules.h"
#include "RNSketchCanvas.h"
#include <system_error>


using namespace winrt::Microsoft::ReactNative;

namespace winrt::RNSketchCanvas
{
  REACT_MODULE(RNSketchCanvasModule, L"SketchCanvasModule");
  struct RNSketchCanvasModule
  {
    const std::string Name = "SketchCanvasModule";

    ReactContext reactContext = nullptr;

    REACT_INIT(RNSketchCanvasModule_Init);
    void RNSketchCanvasModule_Init(ReactContext const& context) noexcept
    {
      reactContext = context;
    }

    REACT_METHOD(transferToBase64);
    void transferToBase64(
      int tag,
      std::string type,
      bool transparent,
      bool includeImage,
      bool includeText,
      bool cropToImageSize,
      std::function<void(JSValue, JSValue)> callback
    ) noexcept
    {
      XamlUIService uiService = XamlUIService::FromContext(reactContext.Handle());
      auto sketchCanvasInstance = uiService.ElementFromReactTag(tag).as<winrt::RNSketchCanvas::implementation::RNSketchCanvasView>();
      IAsyncOperation<winrt::hstring> asyncOp = sketchCanvasInstance.get()->getBase64(type, transparent, includeImage, includeText, cropToImageSize);
      asyncOp.Completed([=](IAsyncOperation<winrt::hstring> const& sender, AsyncStatus const asyncStatus)
        {
          if (asyncStatus == AsyncStatus::Error)
          {
            std::string error = "HRESULT " + std::to_string(sender.ErrorCode()) + ": " + std::system_category().message(sender.ErrorCode());
            callback(error, nullptr);
          } else if (asyncStatus == AsyncStatus::Completed)
          {
            callback(nullptr, winrt::to_string(sender.GetResults()));
          }
        }
      );
    }

  };

}

