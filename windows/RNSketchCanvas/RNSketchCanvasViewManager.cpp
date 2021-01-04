#include "pch.h"
#include "NativeModules.h"
#include "JSValueXaml.h"
#include "RNSketchCanvasViewManager.h"
#include "RNSketchCanvas.h"

namespace winrt
{
  using namespace Microsoft::ReactNative;
  using namespace Windows::Foundation;
  using namespace Windows::Foundation::Collections;
  using namespace Windows::UI;
  using namespace Windows::UI::Xaml;
  using namespace Windows::UI::Xaml::Controls;
}

namespace winrt::RNSketchCanvas::implementation
{
  // IViewManager
  winrt::hstring RNSketchCanvasViewManager::Name() noexcept
  {
    return L"RNSketchCanvas";
  }

  winrt::FrameworkElement RNSketchCanvasViewManager::CreateView() noexcept
  {
    return winrt::RNSketchCanvas::RNSketchCanvasView(m_reactContext);
  }

  // IViewManagerWithReactContext
  winrt::IReactContext RNSketchCanvasViewManager::ReactContext() noexcept
  {
    return m_reactContext;
  }

  void RNSketchCanvasViewManager::ReactContext(IReactContext reactContext) noexcept
  {
    m_reactContext = reactContext;
  }

  // IViewManagerWithNativeProperties
  IMapView<hstring, ViewManagerPropertyType> RNSketchCanvasViewManager::NativeProps() noexcept
  {
    return winrt::RNSketchCanvas::implementation::RNSketchCanvasView::NativeProps();
  }

  void RNSketchCanvasViewManager::UpdateProperties(
    FrameworkElement const& view,
    IJSValueReader const& propertyMapReader) noexcept
  {
    if (auto module = view.try_as<winrt::RNSketchCanvas::RNSketchCanvasView>())
    {
      module.UpdateProperties(propertyMapReader);
    }
  }
  winrt::Microsoft::ReactNative::ConstantProviderDelegate RNSketchCanvasViewManager::ExportedViewConstants() noexcept
  {
    return winrt::RNSketchCanvas::implementation::RNSketchCanvasView::ExportedViewConstants();
  }
  // IViewManagerWithExportedEventTypeConstants
  ConstantProviderDelegate RNSketchCanvasViewManager::ExportedCustomBubblingEventTypeConstants() noexcept
  {
    return winrt::RNSketchCanvas::implementation::RNSketchCanvasView::ExportedCustomBubblingEventTypeConstants();
  }

  ConstantProviderDelegate RNSketchCanvasViewManager::ExportedCustomDirectEventTypeConstants() noexcept
  {
    return winrt::RNSketchCanvas::implementation::RNSketchCanvasView::ExportedCustomDirectEventTypeConstants();
  }

  // IViewManagerWithCommands
  IVectorView<hstring> RNSketchCanvasViewManager::Commands() noexcept
  {
    return winrt::RNSketchCanvas::implementation::RNSketchCanvasView::Commands();
  }

  void RNSketchCanvasViewManager::DispatchCommand(
    FrameworkElement const& view,
    winrt::hstring const& commandId,
    winrt::IJSValueReader const& commandArgsReader) noexcept
  {
    if (auto module = view.try_as<winrt::RNSketchCanvas::RNSketchCanvasView>())
    {
      module.DispatchCommand(commandId, commandArgsReader);
    }
  }
}
