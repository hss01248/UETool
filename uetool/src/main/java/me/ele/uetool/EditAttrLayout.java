package me.ele.uetool;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import me.ele.uetool.base.Element;

import static me.ele.uetool.EditAttrLayout.Mode.MOVE;
import static me.ele.uetool.EditAttrLayout.Mode.SHOW;
import static me.ele.uetool.base.DimenUtil.dip2px;
import static me.ele.uetool.base.DimenUtil.px2dip;

public class EditAttrLayout extends CollectViewsLayout {

  private final int moveUnit = dip2px(1);
  private final int lineBorderDistance = dip2px(5);

  private Paint areaPaint = new Paint() {
    {
      setAntiAlias(true);
      setColor(0x30000000);
    }
  };

  private @Mode int mode = SHOW;
  private Element element;
  private AttrsDialog dialog;
  private OnDragListener onDragListener;

  public EditAttrLayout(Context context) {
    super(context);
  }

  public EditAttrLayout(Context context,
      @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public EditAttrLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    if (element != null) {
      Rect rect = element.getRect();
      canvas.drawRect(rect, areaPaint);
      if (mode == SHOW) {
        drawLineWithText(canvas, rect.left, rect.top - lineBorderDistance, rect.right,
            rect.top - lineBorderDistance);
        drawLineWithText(canvas, rect.right + lineBorderDistance, rect.top,
            rect.right + lineBorderDistance, rect.bottom);
      } else if (mode == MOVE) {
        Rect originRect = element.getOriginRect();
        canvas.drawRect(originRect, dashLinePaint);
        Element parentElement = element.getParentElement();
        if (parentElement != null) {
          Rect parentRect = parentElement.getRect();
          int x = rect.left + rect.width() / 2;
          int y = rect.top + rect.height() / 2;
          drawLineWithText(canvas, rect.left, y, parentRect.left, y, dip2px(2));
          drawLineWithText(canvas, x, rect.top, x, parentRect.top, dip2px(2));
          drawLineWithText(canvas, rect.right, y, parentRect.right, y, dip2px(2));
          drawLineWithText(canvas, x, rect.bottom, x, parentRect.bottom, dip2px(2));
        }
        if (onDragListener != null) {
          onDragListener.showOffset(
              "Offset:\n"
                  + "x -> "
                  + px2dip(rect.left - originRect.left, true)
                  + " y -> "
                  + px2dip(rect.top - originRect.top, true));
        }
      }
    }
  }

  private float lastX, lastY;

  @Override public boolean onTouchEvent(MotionEvent event) {
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        lastX = event.getX();
        lastY = event.getY();
        break;
      case MotionEvent.ACTION_UP:

        if (mode == SHOW) {
          final Element element = getTargetElement(event.getX(), event.getY());
          if (element != null) {
            this.element = element;
            invalidate();
            if (dialog == null) {
              dialog = new AttrsDialog(getContext());
              dialog.setAttrDialogCallback(new AttrsDialog.AttrDialogCallback() {
                @Override public void enableMove() {
                  mode = MOVE;
                  dialog.dismiss();
                }
              });
              dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override public void onDismiss(DialogInterface dialog) {
                  element.reset();
                  invalidate();
                }
              });
            }
            dialog.show(element);
          }
        }

        break;
      case MotionEvent.ACTION_MOVE:

        if (mode == MOVE && element != null) {
          boolean changed = false;
          View view = element.getView();
          float diffX = event.getX() - lastX;
          if (Math.abs(diffX) >= moveUnit) {
            view.setTranslationX(view.getTranslationX() + diffX);
            lastX = event.getX();
            changed = true;
          }
          float diffY = event.getY() - lastY;
          if (Math.abs(diffY) >= moveUnit) {
            view.setTranslationY(view.getTranslationY() + diffY);
            lastY = event.getY();
            changed = true;
          }
          if (changed) {
            element.reset();
            invalidate();
          }
        }

        break;
    }
    return true;
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    element = null;
    if (dialog != null) {
      dialog.dismiss();
    }
  }

  public void setOnDragListener(OnDragListener onDragListener) {
    this.onDragListener = onDragListener;
  }

  @IntDef({
      SHOW,
      MOVE,
  })
  @Retention(RetentionPolicy.SOURCE) public @interface Mode {
    int SHOW = 1;
    int MOVE = 2;
  }

  public interface OnDragListener {
    void showOffset(String offsetContent);
  }
}