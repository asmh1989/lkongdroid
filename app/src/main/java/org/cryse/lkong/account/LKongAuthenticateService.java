package org.cryse.lkong.account;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

@SuppressWarnings({ "ALL", "AlibabaClassMustHaveAuthor" }) public class LKongAuthenticateService extends Service {
    @Override
    public IBinder onBind(Intent intent) {

        LKongAuthenticator authenticator = new LKongAuthenticator(this);
        return authenticator.getIBinder();
    }
}
