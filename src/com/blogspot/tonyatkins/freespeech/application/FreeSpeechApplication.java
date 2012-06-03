package com.blogspot.tonyatkins.freespeech.application;

import org.acra.annotation.ReportsCrashes;

import com.atlassian.jconnect.droid.Api;

import android.app.Application;

@ReportsCrashes(formKey = "")
public class FreeSpeechApplication extends Application {
  @Override
  public void onCreate() {
    Api.init(this);
    super.onCreate();
  }
}
