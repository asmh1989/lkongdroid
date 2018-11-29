package org.cryse.lkong.utils;

import rx.Subscription;

@SuppressWarnings({ "ALL", "AlibabaClassMustHaveAuthor" }) public class SubscriptionUtils {
    public static void checkAndUnsubscribe(Subscription subscription){
        if(subscription != null && !subscription.isUnsubscribed()) {
          subscription.unsubscribe();
        }
    }
}
