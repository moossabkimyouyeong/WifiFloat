package com.chizhu.wififloat;
import android.accessibilityservice.*;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;
public class TouchService extends AccessibilityService {
 public static TouchService active;
 @Override protected void onServiceConnected(){active=this;}
 @Override public void onAccessibilityEvent(AccessibilityEvent e){}
 @Override public void onInterrupt(){}
 @Override public void onDestroy(){active=null;super.onDestroy();}
 public void tap(int x,int y){Path p=new Path();p.moveTo(x,y);GestureDescription.Builder b=new GestureDescription.Builder();b.addStroke(new GestureDescription.StrokeDescription(p,0,60));dispatchGesture(b.build(),null,null);}
}