package me.ele.uetool.sample;

import android.app.Application;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.squareup.leakcanary.LeakCanary;
import me.ele.uetool.UETool;

public class AppContext extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        LeakCanary.install(this);
        Fresco.initialize(this);

        UETool.putFilterClass(FilterOutView.class);
        UETool.putAttrsProviderClass(CustomAttribution.class);
        UETool.init(this);

    }
}
