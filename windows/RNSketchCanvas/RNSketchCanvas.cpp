#include "pch.h"
#include "JSValueXaml.h"
#include "RNSketchCanvas.h"
#include "Utility.h"
#include "RNSketchCanvasView.g.cpp"
#include <system_error>

namespace winrt
{
  using namespace Microsoft::ReactNative;
  using namespace Windows::Data::Json;
  using namespace Windows::Foundation;
  using namespace Windows::Foundation::Numerics;
  using namespace Windows::Storage;
  using namespace Windows::UI;
  using namespace Windows::UI::Popups;
  using namespace Windows::UI::Xaml;
  using namespace Windows::UI::Xaml::Controls;
  using namespace Windows::UI::Xaml::Input;
  using namespace Windows::UI::Xaml::Media;
  using namespace Microsoft::Graphics::Canvas;
  using namespace Microsoft::Graphics::Canvas::Text;
  using namespace Microsoft::Graphics::Canvas::UI::Xaml;
} // namespace winrt

namespace winrt::RNSketchCanvas::implementation
{

  RNSketchCanvasView::RNSketchCanvasView(winrt::IReactContext const& reactContext) : m_reactContext(reactContext)
  {
    // Sets a Transparent background so that it receives mouse events in the JavaScript side.
    mCanvasControl = Microsoft::Graphics::Canvas::UI::Xaml::CanvasControl();
    this->Children().Append(mCanvasControl);
    mCanvasControl.Background(SolidColorBrush(Colors::Transparent()));
    
    mCanvasDrawRevoker = mCanvasControl.Draw(winrt::auto_revoke, { get_weak(), &RNSketchCanvasView::OnCanvasDraw });
    mCanvaSizeChangedRevoker = mCanvasControl.SizeChanged(winrt::auto_revoke, { get_weak(), &RNSketchCanvasView::OnCanvasSizeChanged });

  }

  void RNSketchCanvasView::openImageFile(std::string filename, std::string directory, std::string mode)
  {
    if (!filename.empty())
    {
      if (!directory.empty())
      {
        filename = directory + "/" + filename;
      }
      Uri uri(nullptr);
      bool useUri = false;
      try
      {
        uri = Uri(winrt::to_hstring(filename));
        std::string schemeName = winrt::to_string(uri.SchemeName());
        if (schemeName.rfind("ms-", 0) == 0)
        {
          useUri = true;
        }
      } catch (...)
      {
      }
      try
      {
        IAsyncOperation<CanvasBitmap> asyncBitmapOp;

        if (useUri)
        {
          // Valid URI for file or special ms- location URLs.
          asyncBitmapOp = CanvasBitmap::LoadAsync(CanvasDevice::GetSharedDevice(), uri);
        } else
        {
          asyncBitmapOp = CanvasBitmap::LoadAsync(CanvasDevice::GetSharedDevice(), winrt::to_hstring(filename));
        }
        asyncBitmapOp.Completed(
          [=](auto&& sender, AsyncStatus const args)
          {
            if (args == AsyncStatus::Completed)
            {
              CanvasBitmap bitmap = sender.GetResults();
              m_reactContext.UIDispatcher().Post([=]()
                {
                  mBackgroundImage = bitmap;
                  mOriginalWidth = mBackgroundImage.value().SizeInPixels().Width;
                  mOriginalHeight = mBackgroundImage.value().SizeInPixels().Height;
                  mContentMode = mode;
                  invalidateCanvas(true);
                }
              );
            }
          }
        );
      } catch (...)
      {
      }
    }
  }

  winrt::Windows::Foundation::Collections::
    IMapView<winrt::hstring, winrt::Microsoft::ReactNative::ViewManagerPropertyType>
    RNSketchCanvasView::NativeProps() noexcept
  {
    auto nativeProps = winrt::single_threaded_map<hstring, ViewManagerPropertyType>();
    nativeProps.Insert(L"localSourceImage", ViewManagerPropertyType::Map);
    nativeProps.Insert(L"text", ViewManagerPropertyType::Array);
    return nativeProps.GetView();
  }

  void RNSketchCanvasView::UpdateProperties(winrt::Microsoft::ReactNative::IJSValueReader const& propertyMapReader) noexcept
  {
    const JSValueObject& propertyMap = JSValue::ReadObjectFrom(propertyMapReader);
    for (auto const& pair : propertyMap)
    {
      auto const& propertyName = pair.first;
      auto const& propertyValue = pair.second;
      if (propertyName == "localSourceImage")
      {
        if (propertyValue != nullptr)
        {
          auto const& localSourceImageMap = propertyValue.AsObject();
          std::string filename = "";
          std::string directory = "";
          std::string mode = "";
          
          auto value = localSourceImageMap.find("filename");
          if (value != localSourceImageMap.end())
          {
            filename = value->second.AsString();
          }
          value = localSourceImageMap.find("directory");
          if (value != localSourceImageMap.end())
          {
            directory = value->second.AsString();
          }
          value = localSourceImageMap.find("mode");
          if (value != localSourceImageMap.end())
          {
            mode = value->second.AsString();
          }

          this->openImageFile(filename, directory, mode);
        }
      } else if (propertyName == "text")
      {
        if (propertyValue != nullptr)
        {
          this->setCanvasText(propertyValue.AsArray());
        }
      }
    }
  }

  winrt::Microsoft::ReactNative::ConstantProviderDelegate RNSketchCanvasView::ExportedViewConstants() noexcept
  {
    return [](winrt::Microsoft::ReactNative::IJSValueWriter const& constantWriter)
    {
      WriteProperty(constantWriter, L"MainBundlePath", L"ms-appx:///");
      WriteProperty(constantWriter, L"NSCachesDirectory", ApplicationData::Current().LocalCacheFolder().Path());
      WriteProperty(constantWriter, L"TemporaryDirectory", L"ms-appdata:///temp");
      WriteProperty(constantWriter, L"RoamingDirectory", L"ms-appdata:///roaming");
      WriteProperty(constantWriter, L"LocalDirectory", L"ms-appdata:///local");
    };
  }

  winrt::Microsoft::ReactNative::ConstantProviderDelegate RNSketchCanvasView::ExportedCustomBubblingEventTypeConstants() noexcept
  {
    return nullptr;
  }

  winrt::Microsoft::ReactNative::ConstantProviderDelegate RNSketchCanvasView::ExportedCustomDirectEventTypeConstants() noexcept
  {
    return [](winrt::IJSValueWriter const& constantWriter)
    {
      // NOTE: A bubbling event might be more appropriate here?
      WriteCustomDirectEventTypeConstant(constantWriter, "onChange", "onChange");
    };
  }

  winrt::Windows::Foundation::Collections::IVectorView<winrt::hstring> RNSketchCanvasView::Commands() noexcept
  {
    auto commands = winrt::single_threaded_vector<hstring>();
    commands.Append(L"addPoint");
    commands.Append(L"newPath");
    commands.Append(L"clear");
    commands.Append(L"addPath");
    commands.Append(L"deletePath");
    commands.Append(L"save");
    commands.Append(L"endPath");
    return commands.GetView();
  }

  void RNSketchCanvasView::DispatchCommand(winrt::hstring const& commandId, winrt::Microsoft::ReactNative::IJSValueReader const& commandArgsReader) noexcept
  {
    auto commandArgs = JSValue::ReadArrayFrom(commandArgsReader);
    if (commandId == L"addPoint")
    {
      this->addPoint(commandArgs[0].AsSingle(), commandArgs[1].AsSingle());
    } else if (commandId == L"newPath")
    {
      this->newPath(commandArgs[0].AsInt32(), commandArgs[1].AsUInt32(), commandArgs[2].AsSingle());
    } else if (commandId == L"clear")
    {
      this->clear();
    } else if (commandId == L"addPath")
    {
      const auto& path = commandArgs[3].AsArray();
      std::vector<float2> pointPath;
      for (unsigned int i = 0; i < path.size(); i++)
      {
        std::string pointstring = path[i].AsString();
        int commaIndex = pointstring.find(",");
        pointPath.push_back(
          float2(
            std::stof(pointstring.substr(0, commaIndex)),
            std::stof(pointstring.substr(commaIndex + 1, std::string::npos))
          )
        );
      }
      this->addPath(commandArgs[0].AsInt32(), commandArgs[1].AsUInt32(), commandArgs[2].AsSingle(), pointPath);
    } else if (commandId == L"deletePath")
    {
      this->deletePath(commandArgs[0].AsInt32());
    } else if (commandId == L"save")
    {
      this->save(
        commandArgs[0].AsString(),
        commandArgs[1].AsString(),
        commandArgs[2].AsString(),
        commandArgs[3].AsBoolean(),
        commandArgs[4].AsBoolean(),
        commandArgs[5].AsBoolean(),
        commandArgs[6].AsBoolean()
      );
    } else if (commandId == L"endPath")
    {
      this->end();
    }
  }
  void RNSketchCanvasView::clear()
  {
    mPaths.clear();
    mCurrentPath = nullptr;
    mNeedsFullRedraw = true;
    invalidateCanvas(true);
  }
  void RNSketchCanvasView::newPath(int32_t id, uint32_t strokeColor, float strokeWidth)
  {
    Color color = Utility::uint32ToColor(strokeColor);
    mCurrentPath = std::make_shared<SketchData>(id, color, strokeWidth);
    mPaths.push_back(mCurrentPath);
    // On Android, hardware acceleration is disabled when erasing here.
    // Looks like it could be done through ForceSoftwareRenderer.
    // So far, I couldn't find a reason to do it on Windows.
    invalidateCanvas(true);
  }

  void RNSketchCanvasView::addPoint(float x, float y)
  {
    mCurrentPath->addPoint(Point(x, y));
    if (mCurrentPath->isTranslucent)
    {
      auto session = mTranslucentDrawingCanvas.value().CreateDrawingSession();
      session.Clear(Colors::Transparent());
      mCurrentPath->draw(session);
    } else
    {
      auto session = mDrawingCanvas.value().CreateDrawingSession();
      mCurrentPath->drawLastPoint(session);
    }
    mCanvasControl.Invalidate();
  }
  void RNSketchCanvasView::addPath(int32_t id, uint32_t strokeColor, float strokeWidth, std::vector<float2> points)
  {
    bool exist = false;
    for (std::shared_ptr<SketchData> & data : mPaths)
    {
      if (data->id == id)
      {
        return;
      }
    }
    if (!exist)
    {
      std::shared_ptr<SketchData> newPath = std::make_shared<SketchData>(id, Utility::uint32ToColor(strokeColor), strokeWidth, points);
      mPaths.push_back(newPath);
      {
        auto session = mDrawingCanvas.value().CreateDrawingSession();
        newPath->draw(session);
      }
      invalidateCanvas(true);
    }
  }
  void RNSketchCanvasView::deletePath(int32_t id)
  {
    int index = -1;
    for (unsigned int i = 0; i < mPaths.size(); i++)
    {
      if (mPaths[i]->id == id)
      {
        index = i;
        break;
      }
    }
    if (index > -1)
    {
      mPaths.erase(mPaths.begin() + index);
      mNeedsFullRedraw = true;
      invalidateCanvas(true);
    }
  }
  void RNSketchCanvasView::end()
  {
    if (mCurrentPath != nullptr)
    {
      if (mCurrentPath->isTranslucent)
      {
        {
          auto session = mDrawingCanvas.value().CreateDrawingSession();
          mCurrentPath->draw(session);
        }
        {
          auto session = mTranslucentDrawingCanvas.value().CreateDrawingSession();
          session.Clear(Colors::Transparent());
        }
      }
      mCurrentPath = nullptr;
    }
  }

  IAsyncOperation<winrt::hstring> RNSketchCanvasView::saveHelper(std::string format, std::string folder, std::string filename, bool transparent, bool includeImage, bool includeText, bool cropToImageSize)
  {
    // Saving in Pictures requires the application having the picturesLibrary capability.
    StorageFolder picsRoot = KnownFolders::PicturesLibrary();
    StorageFolder targetSaveFolder = picsRoot;
    bool try_to_create_folders = false;
    try
    {
      targetSaveFolder = co_await picsRoot.GetFolderAsync(winrt::to_hstring(folder));
    } catch (...)
    {
      // Try to create folders.
      try_to_create_folders = true;
    }
    if (try_to_create_folders)
    {
      targetSaveFolder = co_await picsRoot.CreateFolderAsync(winrt::to_hstring(folder));
    }
    StorageFile file = co_await targetSaveFolder.CreateFileAsync(winrt::to_hstring(filename + (format=="png"?".png":".jpg" )), CreationCollisionOption::ReplaceExisting);

    CanvasBitmap bitmap = createImage(format == "png" && transparent, includeImage, includeText, cropToImageSize);

    auto transactionStream = co_await file.OpenTransactedWriteAsync();

    co_await bitmap.SaveAsync(
      transactionStream.Stream(),
      format == "png" ? CanvasBitmapFileFormat::Png : CanvasBitmapFileFormat::Jpeg,
      format == "png" ? 1.0f : 0.9f
    );
    co_await transactionStream.CommitAsync();
    return file.Path();
  }

  void RNSketchCanvasView::save(std::string format, std::string folder, std::string filename, bool transparent, bool includeImage, bool includeText, bool cropToImageSize)
  {
    IAsyncOperation<winrt::hstring> asyncSave = saveHelper(format, folder, filename, transparent, includeImage, includeText, cropToImageSize);

    asyncSave.Completed([=](IAsyncOperation<winrt::hstring> const& sender, AsyncStatus const asyncStatus)
      {
        if (asyncStatus == AsyncStatus::Error)
        {
          //std::string error = "HRESULT " + std::to_string(sender.ErrorCode()) + ": " + std::system_category().message(sender.ErrorCode());
          onSaved(false, "");
        } else if (asyncStatus == AsyncStatus::Completed)
        {
          onSaved(true, winrt::to_string(sender.GetResults()));
        }
      }
    );

  }

  IAsyncOperation<winrt::hstring> RNSketchCanvasView::getBase64(std::string format, bool transparent, bool includeImage, bool includeText, bool cropToImageSize)
  {
    CanvasBitmap bitmap = createImage(format == "png" && transparent, includeImage, includeText, cropToImageSize);

    Streams::InMemoryRandomAccessStream stream;

    co_await bitmap.SaveAsync(
      stream,
      format == "png" ? CanvasBitmapFileFormat::Png : CanvasBitmapFileFormat::Jpeg,
      format == "png" ? 1.0f : 0.9f
    );
    
    Streams::Buffer buffer(stream.Size());
    Streams::IBuffer readbuffer = co_await stream.ReadAsync(buffer, stream.Size(), Streams::InputStreamOptions::None);

    return Windows::Security::Cryptography::CryptographicBuffer::EncodeToBase64String(readbuffer);
  }
  void RNSketchCanvasView::OnCanvasDraw(CanvasControl const& canvas, CanvasDrawEventArgs const& args)
  {
    if (mNeedsFullRedraw && mDrawingCanvas.has_value())
    {
      auto session = mDrawingCanvas.value().CreateDrawingSession();
      session.Clear(Colors::Transparent());
      for (std::shared_ptr<SketchData> & path : mPaths)
      {
        path->draw(session);
      }
      mNeedsFullRedraw = false;
    }

    if (mBackgroundImage.has_value())
    {
      args.DrawingSession().DrawImage(
        mBackgroundImage.value(),
        Utility::fillImage(
          mBackgroundImage.value().SizeInPixels().Width,
          mBackgroundImage.value().SizeInPixels().Height,
          canvas.ActualWidth(),
          canvas.ActualHeight(),
          mContentMode
        )
      );
    }

    for (std::shared_ptr<CanvasText> & text : mArrSketchOnText)
    {
      args.DrawingSession().DrawText(
        winrt::to_hstring(text->text),
        text->drawPosition.x + text->lineOffset.x,
        text->drawPosition.y + text->lineOffset.y,
        text->color,
        text->paint
      );
    }

    if (mDrawingCanvas.has_value())
    {
      args.DrawingSession().DrawImage(mDrawingCanvas.value());
    }
    if (mTranslucentDrawingCanvas.has_value() && mCurrentPath != nullptr && mCurrentPath->isTranslucent)
    {
      args.DrawingSession().DrawImage(mTranslucentDrawingCanvas.value());
    }
    
    for (std::shared_ptr<CanvasText> & text : mArrTextOnSketch)
    {
      args.DrawingSession().DrawText(
        winrt::to_hstring(text->text),
        text->drawPosition.x + text->lineOffset.x,
        text->drawPosition.y + text->lineOffset.y,
        text->color,
        text->paint
      );
    }

  }
  void RNSketchCanvasView::OnCanvasSizeChanged(const IInspectable canvas, Windows::UI::Xaml::SizeChangedEventArgs const& args)
  {
    Size newSize = args.NewSize();
    if (newSize.Width >= 0 && newSize.Height >= 0)
    {
      mDrawingCanvas = CanvasRenderTarget(CanvasDevice::GetSharedDevice(), newSize.Width, newSize.Height, mCanvasControl.Dpi());
      {
        auto session = mDrawingCanvas.value().CreateDrawingSession();
        session.Clear(Colors::Transparent());
      }
      mTranslucentDrawingCanvas = CanvasRenderTarget(CanvasDevice::GetSharedDevice(), newSize.Width, newSize.Height, mCanvasControl.Dpi());
      {
        auto session = mTranslucentDrawingCanvas.value().CreateDrawingSession();
        session.Clear(Colors::Transparent());
      }

      for (std::shared_ptr<CanvasText> & text : mArrCanvasText)
      {
        float2 position = float2(text->position.x, text->position.y);
        if (!text->isAbsoluteCoordinate)
        {
          position.x *= newSize.Width;
          position.y *= newSize.Height;
        }
        position.x -= text->textBounds.X;
        position.y -= text->textBounds.Y;
        position.x -= (text->textBounds.Width * text->anchor.x);
        position.y -= (text->height * text->anchor.y);
        text->drawPosition = position;
      }

      mNeedsFullRedraw = true;
      mCanvasControl.Invalidate();
    }
  }

  void RNSketchCanvasView::setCanvasText(JSValueArray const& aText)
  {
    mArrCanvasText.clear();
    mArrSketchOnText.clear();
    mArrTextOnSketch.clear();

    for (int i = 0; i < aText.size(); i++)
    {
      JSValueObject const& property = aText[i].AsObject();
      if (property.find("text") != property.end())
      {
        std::string alignment = property.find("alignment") != property.end() ? property["alignment"].AsString() : "Left";
        int lineOffset = 0, maxTextWidth = 0;
        std::vector<std::string> lines = Utility::splitLines(property["text"].AsString());
        std::vector<std::shared_ptr<CanvasText>> textSet;
        for (auto const& line : lines)
        {
          std::vector<std::shared_ptr<CanvasText>> & arr = property.find("overlay") != property.end() && property["overlay"].AsString() == "TextOnSketch" ? mArrTextOnSketch : mArrSketchOnText;
          std::shared_ptr<CanvasText> text = std::make_shared<CanvasText>();
          text->paint.HorizontalAlignment(CanvasHorizontalAlignment::Left);
          text->text = line;
          if (property.find("font") != property.end())
          {
            text->paint.FontFamily(winrt::to_hstring(property["font"].AsString()));
          }
          text->paint.FontSize(property.find("fontSize") != property.end() ? property["fontSize"].AsSingle() : 12.0f);
          text->paint.WordWrapping(CanvasWordWrapping::NoWrap);
          text->paint.LastLineWrapping(false);
          text->color = property.find("fontColor") != property.end() ? Utility::uint32ToColor(property["fontColor"].AsInt32()) : Colors::Black();
          text->anchor = property.find("anchor") != property.end() ? float2(property["anchor"].AsObject()["x"].AsSingle(), property["anchor"].AsObject()["y"].AsSingle()) : float2(.0f, .0f);
          text->position = property.find("position") != property.end() ? float2(property["position"].AsObject()["x"].AsSingle(), property["position"].AsObject()["y"].AsSingle()) : float2(.0f, .0f);
          text->isAbsoluteCoordinate = !(property.find("coordinate") != property.end() && property["coordinate"].AsString() == "Ratio");
          CanvasTextLayout boundsLayout = CanvasTextLayout(CanvasDevice::GetSharedDevice(), winrt::to_hstring(line), text->paint, 0, 0);
          text->textBounds = Rect(boundsLayout.LayoutBounds().X, boundsLayout.LayoutBounds().Y, boundsLayout.LayoutBounds().Width, boundsLayout.LayoutBounds().Height);
          text->lineOffset = float2(0, lineOffset);
          lineOffset += text->textBounds.Height * (property.find("lineHeightMultiple") != property.end() ? property["lineHeightMultiple"].AsSingle() : 1.f);
          maxTextWidth = max(maxTextWidth, text->textBounds.Width);
          arr.push_back(text);
          mArrCanvasText.push_back(text);
          textSet.push_back(text);
        }
        for (auto& text : textSet)
        {
          text->height = lineOffset;
          if (text->textBounds.Width < maxTextWidth)
          {
            float diff = maxTextWidth - text->textBounds.Width;
            text->textBounds.X += diff * text->anchor.x;
          }
        }
        if (mCanvasControl.ActualWidth() > 0 && mCanvasControl.ActualHeight() > 0)
        {
          for (std::shared_ptr<CanvasText> & text : textSet)
          {
            text->height = lineOffset;
            float2 position = float2(text->position.x, text->position.y);
            if (!text->isAbsoluteCoordinate)
            {
              position.x *= mCanvasControl.ActualWidth();
              position.y *= mCanvasControl.ActualHeight();
            }
            position.x -= text->textBounds.X;
            position.y -= text->textBounds.Y;
            position.x -= (text->textBounds.Width * text->anchor.x);
            position.y -= (text->height * text->anchor.y);
            text->drawPosition = position;
          }
        }
        if (lines.size() > 1)
        {
          for (std::shared_ptr<CanvasText> & text : textSet)
          {
            if (alignment == "Right")
            {
              text->lineOffset.x = (maxTextWidth - text->textBounds.Width);
            } else if (alignment == "Center")
            {
              text->lineOffset.x = (maxTextWidth - text->textBounds.Width) / 2;
            }
          }
        }
      }
    }
    invalidateCanvas(false);
  }

  void RNSketchCanvasView::onSaved(bool success, std::string path)
  {
    auto control = this->get_strong().try_as<winrt::FrameworkElement>();
    bool local_success = success;
    std::string local_path = path;
    m_reactContext.DispatchEvent(
      control,
      L"onChange",
      [&](winrt::Microsoft::ReactNative::IJSValueWriter const& eventDataWriter) noexcept
      {
        eventDataWriter.WriteObjectBegin();
        WriteProperty(eventDataWriter, L"success", local_success);
        if (local_path.empty())
        {
          WriteProperty(eventDataWriter, L"path", nullptr);
        } else
        {
          WriteProperty(eventDataWriter, L"path", local_path);
        }
        eventDataWriter.WriteObjectEnd();
      }
    );
  }

  void RNSketchCanvasView::invalidateCanvas(bool shouldDispatchEvent)
  {
    if (shouldDispatchEvent)
    {
      auto control = this->get_strong().try_as<winrt::FrameworkElement>();
      auto pathsSize = mPaths.size();
      m_reactContext.DispatchEvent(
        control,
        L"onChange",
        [&](winrt::Microsoft::ReactNative::IJSValueWriter const& eventDataWriter) noexcept
        {
          eventDataWriter.WriteObjectBegin();
          WriteProperty(eventDataWriter, L"pathsUpdate", pathsSize);
          eventDataWriter.WriteObjectEnd();
        }
      );
    }
    mCanvasControl.Invalidate();
  }

  Microsoft::Graphics::Canvas::CanvasBitmap RNSketchCanvasView::createImage(bool transparent, bool includeImage, bool includeText, bool cropToImageSize)
  {
    CanvasRenderTarget canvas = CanvasRenderTarget(
      CanvasDevice::GetSharedDevice(),
      mBackgroundImage.has_value() && cropToImageSize ? mOriginalWidth : mCanvasControl.ActualWidth(),
      mBackgroundImage.has_value() && cropToImageSize ? mOriginalHeight : mCanvasControl.ActualHeight(),
      mCanvasControl.Dpi()
    );
    {
      auto session = canvas.CreateDrawingSession();
      session.Clear(transparent ? Colors::Transparent() : Colors::White());

      if (mBackgroundImage.has_value() && includeImage)
      {
        session.DrawImage(
          mBackgroundImage.value(),
          Utility::fillImage(
            mBackgroundImage.value().SizeInPixels().Width,
            mBackgroundImage.value().SizeInPixels().Height,
            canvas.SizeInPixels().Width,
            canvas.SizeInPixels().Height,
            "AspectFit"
          )
        );
      }

      if (includeText)
      {
        for (std::shared_ptr<CanvasText> & text : mArrSketchOnText)
        {
          session.DrawText(
            winrt::to_hstring(text->text),
            text->drawPosition.x + text->lineOffset.x,
            text->drawPosition.y + text->lineOffset.y,
            text->color,
            text->paint
          );
        }
      }

      if (mBackgroundImage.has_value() && cropToImageSize)
      {
        session.DrawImage(
          mDrawingCanvas.value(),
          Utility::fillImage(
            mDrawingCanvas.value().SizeInPixels().Width,
            mDrawingCanvas.value().SizeInPixels().Height,
            canvas.SizeInPixels().Width,
            canvas.SizeInPixels().Height,
            "AspectFill"
          )
        );
      } else
      {
        session.DrawImage(mDrawingCanvas.value());
      }

      if (includeText)
      {
        for (std::shared_ptr<CanvasText> & text : mArrTextOnSketch)
        {
          session.DrawText(
            winrt::to_hstring(text->text),
            text->drawPosition.x + text->lineOffset.x,
            text->drawPosition.y + text->lineOffset.y,
            text->color,
            text->paint
          );
        }
      }

    }
    
    return canvas;
  }

}