#include "pch.h"
#include "SketchData.h"
#include "Utility.h"

namespace winrt
{
  using namespace Microsoft::Graphics::Canvas;
  using namespace Microsoft::Graphics::Canvas::Brushes;
  using namespace Microsoft::Graphics::Canvas::Geometry;
  using namespace Windows::Foundation;
  using namespace Windows::Foundation::Numerics;
  using namespace Windows::UI;
  using namespace Windows::UI::Xaml;
  /*using namespace Windows::UI::Xaml::Shapes;
  using namespace Windows::UI::Xaml::Controls;
  using namespace Windows::UI::Xaml::Media;*/
}

namespace winrt::RNSketchCanvas::implementation
{

  std::optional<CanvasStrokeStyle> SketchData::mStrokeStyle = std::nullopt;

  float2 SketchData::midPoint(const float2& p1, const float2& p2)
  {
    return float2((p1.x + p2.x) * .5f, (p1.y + p2.y) * .5f);
  }

  SketchData::SketchData(int id, Color strokeColor, float strokeWidth)
  {
    this->id = id;
    this->strokeColor = strokeColor;
    this->strokeWidth = strokeWidth;
    this->isTranslucent = strokeColor.A != 255 && !Utility::isColorTransparent(strokeColor);
    mPath.reset(); // Geometry has to be created with something.
  }

  SketchData::SketchData(int id, Color strokeColor, float strokeWidth, std::vector<float2> points)
  {
    this->id = id;
    this->strokeColor = strokeColor;
    this->strokeWidth = strokeWidth;
    this->points.insert(std::end(this->points), std::begin(points), std::end(points));
    this->isTranslucent = strokeColor.A != 255 && !Utility::isColorTransparent(strokeColor);
    mPath = this->isTranslucent ? evaluatePath() : nullptr;
  }

  void SketchData::addPoint(const float2& p)
  {
    points.push_back(p);
    int pointsCount = points.size();
    if (isTranslucent)
    {
      if (pointsCount >= 3)
      {
        addPointToPath(
          points[pointsCount - 3],
          points[pointsCount - 2],
          p);
      } else if (pointsCount >= 2)
      {
        addPointToPath(points[0], points[0], p);
      } else
      {
        addPointToPath(p, p, p);
      }
    } else
    {
      if (pointsCount >= 3)
      {
        float2 a = points[pointsCount - 3];
        float2 b = points[pointsCount - 2];
        float2 c = p;
        float2 prevMid = midPoint(a, b);
        float2 currentMid = midPoint(b, c);

      } else if (pointsCount >= 2)
      {
        float2 a = points[pointsCount - 2];
        float2 b = p;
        float2 mid = midPoint(a, b);
      }
    }
  }

  void SketchData::drawLastPoint(const CanvasDrawingSession& canvasDS)
  {
    int pointsCount = points.size();
    if (pointsCount < 1)
    {
      return;
    }
    draw(canvasDS, pointsCount - 1);
  }

  void SketchData::draw(const CanvasDrawingSession& canvasDS)
  {
    canvasDS.Blend(CanvasBlend::SourceOver);
    bool isErase = Utility::isColorTransparent(strokeColor);
    canvasDS.Blend(isErase ? CanvasBlend::Copy : CanvasBlend::SourceOver);

    if (this->isTranslucent)
    {
      canvasDS.DrawGeometry(mPath.value(), strokeColor, strokeWidth, getStrokeStyle());
    } else
    {
      int pointsCount = points.size();
      for (int i = 0; i < pointsCount; i++)
      {
        draw(canvasDS, i);
      }
    }
  }

  void SketchData::draw(const CanvasDrawingSession& canvasDS, int pointIndex)
  {
    int pointsCount = points.size();
    if (pointIndex >= pointsCount)
    {
      return;
    }

    bool isErase = Utility::isColorTransparent(strokeColor);
    canvasDS.Blend(isErase ? CanvasBlend::Copy : CanvasBlend::SourceOver);

    if (pointsCount >= 3 && pointIndex >= 2)
    {
      float2 a = points[pointIndex - 2];
      float2 b = points[pointIndex - 1];
      float2 c = points[pointIndex];
      float2 prevMid = midPoint(a, b);
      float2 currMid = midPoint(b, c);

      // Draw a curve
      CanvasPathBuilder path = CanvasPathBuilder(canvasDS.Device());
      path.BeginFigure(prevMid);
      path.AddQuadraticBezier(b, currMid);
      path.EndFigure(CanvasFigureLoop::Open);
      canvasDS.DrawGeometry(CanvasGeometry::CreatePath(path), strokeColor, strokeWidth, getStrokeStyle());
    } else if (pointsCount >= 2 && pointIndex >= 1)
    {
      float2 a = points[pointIndex - 1];
      float2 b = points[pointIndex];
      float2 mid = midPoint(a, b);

      // Draw a line to the middle of points a and b
      // This is so the next draw which uses a curve looks correct and continues from there
      canvasDS.DrawLine(a, mid, strokeColor, strokeWidth, getStrokeStyle());
    } else if (pointsCount >= 1)
    {
      float2 a = points[pointIndex];

      // Draw a single point
      canvasDS.DrawLine(a, a, strokeColor, strokeWidth, getStrokeStyle());
    }
  }

  CanvasStrokeStyle SketchData::getStrokeStyle()
  {
    if (!SketchData::mStrokeStyle.has_value())
    {

      CanvasStrokeStyle style;
      style.DashStyle(CanvasDashStyle::Solid);
      style.StartCap(CanvasCapStyle::Round);
      style.EndCap(CanvasCapStyle::Round);
      style.DashCap(CanvasCapStyle::Round);
      SketchData::mStrokeStyle = style;
    }
    return SketchData::mStrokeStyle.value();
  }

  CanvasGeometry SketchData::evaluatePath()
  {
    int pointsCount = points.size();
    CanvasPathBuilder path(CanvasDevice::GetSharedDevice());
    for (int pointIndex = 0; pointIndex < pointsCount; pointIndex++)
    {
      if (pointsCount >= 3 && pointIndex >= 2)
      {
        float2 a = points[pointIndex - 2];
        float2 b = points[pointIndex - 1];
        float2 c = points[pointIndex];
        float2 prevMid = midPoint(a, b);
        float2 currMid = midPoint(b, c);

        // Draw a curve
        path.BeginFigure(prevMid);
        path.AddQuadraticBezier(b, currMid);
        path.EndFigure(CanvasFigureLoop::Open);
      } else if (pointsCount >= 2 && pointIndex >= 1)
      {
        float2 a = points[pointIndex - 1];
        float2 b = points[pointIndex];
        float2 mid = midPoint(a, b);

        // Draw a line to the middle of points a and b
        // This is so the next draw which uses a curve looks correct and continues from there
        path.BeginFigure(a);
        path.AddLine(mid);
        path.EndFigure(CanvasFigureLoop::Open);
      } else if (pointsCount >= 1)
      {
        float2 a = points[pointIndex];

        // Draw a single point
        path.BeginFigure(a);
        path.AddLine(a);
        path.EndFigure(CanvasFigureLoop::Open);
      }
    }

    return CanvasGeometry::CreatePath(path);
  }

  void SketchData::addPointToPath(
    const float2& tPoint,
    const float2& pPoint,
    const float2& point
  )
  {
    CanvasPathBuilder path = CanvasPathBuilder(CanvasDevice::GetSharedDevice());
    float2 mid1 = midPoint(pPoint, tPoint);
    float2 mid2 = midPoint(point, pPoint);
    path.BeginFigure(mid1);
    path.AddQuadraticBezier(pPoint, mid2);
    path.EndFigure(CanvasFigureLoop::Open);
    CanvasGeometry geometry = CanvasGeometry::CreatePath(path);
    if (mPath.has_value())
    {
      mPath = CanvasGeometry::CreateGroup(CanvasDevice::GetSharedDevice(), { mPath.value(), geometry });
    } else
    {
      mPath = geometry;
    }
  }

}
